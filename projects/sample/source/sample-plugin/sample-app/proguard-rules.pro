# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/cubershi/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

#这是Shadow在编译期将AndroidManifest.xml中所需信息生成的Java类，没有被代码自然引用，所以需要手工keep住。
-keep class com.tencent.shadow.core.manifest_parser.PluginManifest{*;}

# 忽略 Shadow 运行时类缺失警告
-dontwarn com.tencent.shadow.core.runtime.container.**

# 保留关键类（防止 R8 移除引用）
-keep class com.tencent.shadow.core.runtime.container.GeneratedHostActivityDelegator { *; }
-keep class com.tencent.shadow.core.runtime.container.HostActivityDelegator { *; }
-keep class com.tencent.shadow.core.runtime.container.PluginContainerActivity { *; }
