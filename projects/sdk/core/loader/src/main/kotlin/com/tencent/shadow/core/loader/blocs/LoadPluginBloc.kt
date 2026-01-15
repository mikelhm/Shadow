/*
 * Tencent is pleased to support the open source community by making Tencent Shadow available.
 * Copyright (C) 2019 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *     https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.tencent.shadow.core.loader.blocs

import android.content.Context
import android.util.Log
import com.tencent.shadow.core.common.InstalledApk
import com.tencent.shadow.core.load_parameters.LoadParameters
import com.tencent.shadow.core.loader.exceptions.LoadPluginException
import com.tencent.shadow.core.loader.infos.PluginParts
import com.tencent.shadow.core.loader.managers.ComponentManager
import com.tencent.shadow.core.loader.managers.PluginPackageManagerImpl
import com.tencent.shadow.core.runtime.PluginPartInfo
import com.tencent.shadow.core.runtime.PluginPartInfoManager
import com.tencent.shadow.core.runtime.ShadowAppComponentFactory
import java.io.File
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

object LoadPluginBloc {
    @Throws(LoadPluginException::class)
    fun loadPlugin(
        executorService: ExecutorService,
        componentManager: ComponentManager,
        lock: ReentrantLock,
        pluginPartsMap: MutableMap<String, PluginParts>,
        hostAppContext: Context,
        installedApk: InstalledApk,
        loadParameters: LoadParameters
    ): Future<*> {
        Log.d("ShadowPlugin", "开始加载插件，插件路径: ${installedApk.apkFilePath}, partKey: ${loadParameters.partKey}")

        if (installedApk.apkFilePath == null) {
            Log.e("ShadowPlugin", "apkFilePath==null，无法加载插件")
            throw LoadPluginException("apkFilePath==null")
        } else {
            Log.d("ShadowPlugin", "步骤1: 开始构建ClassLoader")
            val buildClassLoader = executorService.submit(Callable {
                Log.d("ShadowPlugin", "开始执行ClassLoader构建任务")
                lock.withLock {
                    try {
                        val result = LoadApkBloc.loadPlugin(installedApk, loadParameters, pluginPartsMap)
                        Log.d("ShadowPlugin", "ClassLoader构建成功")
                        result
                    } catch (e: Exception) {
                        Log.e("ShadowPlugin", "ClassLoader构建失败", e)
                        throw e
                    }
                }
            })
            Log.d("ShadowPlugin", "已提交ClassLoader构建任务，future = $buildClassLoader")

            Log.d("ShadowPlugin", "步骤2: 开始构建PluginManifest")
            val buildPluginManifest = executorService.submit(Callable {
                Log.d("ShadowPlugin", "开始获取PluginManifest")
                try {
                    val pluginClassLoader = buildClassLoader.get()
                    Log.d("ShadowPlugin", "获取到ClassLoader，开始加载PluginManifest")
                    val pluginManifest = pluginClassLoader.loadPluginManifest()
                    Log.d("ShadowPlugin", "PluginManifest加载成功，包名: ${pluginManifest.applicationPackageName}")

                    Log.d("ShadowPlugin", "开始检查包名")
                    CheckPackageNameBloc.check(pluginManifest, hostAppContext)
                    Log.d("ShadowPlugin", "包名检查通过")
                    pluginManifest
                } catch (e: Exception) {
                    Log.e("ShadowPlugin", "PluginManifest构建失败", e)
                    throw e
                }
            })

            Log.d("ShadowPlugin", "步骤3: 开始构建PluginApplicationInfo")
            val buildPluginApplicationInfo = executorService.submit(Callable {
                Log.d("ShadowPlugin", "开始构建PluginApplicationInfo")
                try {
                    val pluginManifest = buildPluginManifest.get()
                    Log.d("ShadowPlugin", "获取到PluginManifest，开始创建ApplicationInfo")
                    val pluginApplicationInfo = CreatePluginApplicationInfoBloc.create(
                        installedApk,
                        loadParameters,
                        pluginManifest,
                        hostAppContext
                    )
                    Log.d("ShadowPlugin", "PluginApplicationInfo创建成功")
                    pluginApplicationInfo
                } catch (e: Exception) {
                    Log.e("ShadowPlugin", "PluginApplicationInfo构建失败", e)
                    throw e
                }
            })

            Log.d("ShadowPlugin", "步骤4: 开始构建PackageManager")
            val buildPackageManager = executorService.submit(Callable {
                Log.d("ShadowPlugin", "开始构建PluginPackageManager")
                try {
                    val pluginApplicationInfo = buildPluginApplicationInfo.get()
                    Log.d("ShadowPlugin", "获取到PluginApplicationInfo，开始创建PackageManager")
                    val hostPackageManager = hostAppContext.packageManager
                    val packageManager = PluginPackageManagerImpl(
                        pluginApplicationInfo,
                        installedApk.apkFilePath,
                        componentManager,
                        hostPackageManager,
                    )
                    Log.d("ShadowPlugin", "PluginPackageManager创建成功")
                    packageManager
                } catch (e: Exception) {
                    Log.e("ShadowPlugin", "PackageManager构建失败", e)
                    throw e
                }
            })

            Log.d("ShadowPlugin", "步骤5: 开始构建Resources")
            val buildResources = executorService.submit(Callable {
                Log.d("ShadowPlugin", "开始创建Resources")
                try {
                    val resources = CreateResourceBloc.create(installedApk.apkFilePath, hostAppContext)
                    Log.d("ShadowPlugin", "Resources创建成功")
                    resources
                } catch (e: Exception) {
                    Log.e("ShadowPlugin", "Resources构建失败", e)
                    throw e
                }
            })

            Log.d("ShadowPlugin", "步骤6: 开始构建AppComponentFactory")
            val buildAppComponentFactory = executorService.submit(Callable {
                Log.d("ShadowPlugin", "开始构建AppComponentFactory")
                try {
                    val pluginClassLoader = buildClassLoader.get()
                    val pluginManifest = buildPluginManifest.get()
                    Log.d("ShadowPlugin", "获取到PluginManifest，appComponentFactory: ${pluginManifest.appComponentFactory}")

                    val appComponentFactory = pluginManifest.appComponentFactory
                    if (appComponentFactory != null) {
                        Log.d("ShadowPlugin", "加载自定义AppComponentFactory: $appComponentFactory")
                        val clazz = pluginClassLoader.loadClass(appComponentFactory)
                        val factory = ShadowAppComponentFactory::class.java.cast(clazz.newInstance())
                        Log.d("ShadowPlugin", "自定义AppComponentFactory实例化成功")
                        factory
                    } else {
                        Log.d("ShadowPlugin", "使用默认AppComponentFactory")
                        ShadowAppComponentFactory()
                    }
                } catch (e: Exception) {
                    Log.e("ShadowPlugin", "AppComponentFactory构建失败", e)
                    throw e
                }
            })

            Log.d("ShadowPlugin", "步骤7: 开始构建Application")
            val buildApplication = executorService.submit(Callable {
                Log.d("ShadowPlugin", "开始创建ShadowApplication")
                try {
                    val pluginClassLoader = buildClassLoader.get()
                    val resources = buildResources.get()
                    val appComponentFactory = buildAppComponentFactory.get()
                    val pluginManifest = buildPluginManifest.get()
                    val pluginApplicationInfo = buildPluginApplicationInfo.get()

                    Log.d("ShadowPlugin", "所有依赖组件已就绪，开始创建Application")
                    val application = CreateApplicationBloc.createShadowApplication(
                        pluginClassLoader,
                        loadParameters,
                        pluginManifest,
                        resources,
                        hostAppContext,
                        componentManager,
                        pluginApplicationInfo,
                        appComponentFactory
                    )
                    Log.d("ShadowPlugin", "ShadowApplication创建成功")
                    application
                } catch (e: Exception) {
                    Log.e("ShadowPlugin", "Application构建失败", e)
                    throw e
                }
            })

            Log.d("ShadowPlugin", "步骤8: 开始构建RunningPlugin")
            val buildRunningPlugin = executorService.submit {
                Log.d("ShadowPlugin", "开始构建RunningPlugin")
                try {
                    val pluginFile = File(installedApk.apkFilePath)
                    Log.d("ShadowPlugin", "检查插件文件是否存在: ${pluginFile.absolutePath}")
                    if (pluginFile.exists().not()) {
                        Log.e("ShadowPlugin", "插件文件不存在: ${installedApk.apkFilePath}")
                        throw LoadPluginException("插件文件不存在.pluginFile==" + installedApk.apkFilePath)
                    }
                    Log.d("ShadowPlugin", "插件文件存在，继续后续流程")

                    val pluginPackageManager = buildPackageManager.get()
                    Log.d("ShadowPlugin", "获取到PackageManager")

                    val pluginClassLoader = buildClassLoader.get()
                    Log.d("ShadowPlugin", "获取到ClassLoader")

                    val resources = buildResources.get()
                    Log.d("ShadowPlugin", "获取到Resources")

                    val shadowApplication = buildApplication.get()
                    Log.d("ShadowPlugin", "获取到ShadowApplication")

                    val appComponentFactory = buildAppComponentFactory.get()
                    Log.d("ShadowPlugin", "获取到AppComponentFactory")

                    val pluginManifest = buildPluginManifest.get()
                    Log.d("ShadowPlugin", "获取到PluginManifest")

                    lock.withLock {
                        Log.d("ShadowPlugin", "获取锁，开始添加插件信息到组件管理器")
                        componentManager.addPluginApkInfo(
                            pluginManifest,
                            loadParameters,
                            installedApk.apkFilePath,
                        )
                        Log.d("ShadowPlugin", "已添加插件APK信息到ComponentManager")

                        val pluginParts = PluginParts(
                            appComponentFactory,
                            shadowApplication,
                            pluginClassLoader,
                            resources,
                            pluginPackageManager
                        )
                        pluginPartsMap[loadParameters.partKey] = pluginParts
                        Log.d("ShadowPlugin", "已添加PluginParts到map，partKey: ${loadParameters.partKey}")

                        val pluginPartInfo = PluginPartInfo(
                            shadowApplication, resources,
                            pluginClassLoader, pluginPackageManager
                        )
                        PluginPartInfoManager.addPluginInfo(pluginClassLoader, pluginPartInfo)
                        Log.d("ShadowPlugin", "已添加插件信息到PluginPartInfoManager")

                        Log.i("ShadowPlugin", "插件加载完成: ${pluginManifest.applicationPackageName}, partKey: ${loadParameters.partKey}")
                    }
                } catch (e: Exception) {
                    Log.e("ShadowPlugin", "RunningPlugin构建失败", e)
                    throw e
                }
            }

            Log.d("ShadowPlugin", "所有任务已提交，等待插件加载完成")
            return buildRunningPlugin
        }
    }
}