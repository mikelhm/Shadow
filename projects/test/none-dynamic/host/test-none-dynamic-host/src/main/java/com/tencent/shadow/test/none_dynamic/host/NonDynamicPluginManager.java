package com.tencent.shadow.test.none_dynamic.host;

import android.content.Context;
import android.os.Build;

import com.tencent.shadow.core.manager.BasePluginManager;
import com.tencent.shadow.core.manager.installplugin.InstallPluginException;

import java.io.File;
import java.util.ArrayList;

public class NonDynamicPluginManager extends BasePluginManager {
    @Override
    protected String getName() {
        return "Non-dynamic-Plugin-Manager";
    }

    public NonDynamicPluginManager(Context context) {
        super(context);
    }

    public String getAbi(File apkFile) throws InstallPluginException {
       return getPluginPreferredAbi(getPluginSupportedAbis(), apkFile);
    }

    /**
     * 获取可用的ABI列表。
     * 和Build.SUPPORTED_ABIS的区别是，这是宿主已经决定了当前进程用32位so还是64位so了，
     * 所以可用的ABI只能是其中一部分。
     */
    private String[] getPluginSupportedAbis() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String nativeLibraryDir = mHostContext.getApplicationInfo().nativeLibraryDir;
            int nextIndexOfLastSlash = nativeLibraryDir.lastIndexOf('/') + 1;
            String instructionSet = nativeLibraryDir.substring(nextIndexOfLastSlash);
            if (!isKnownInstructionSet(instructionSet)) {
                throw new IllegalStateException("不认识的instructionSet==" + instructionSet);
            }
            boolean is64Bit = is64BitInstructionSet(instructionSet);
            return is64Bit ? Build.SUPPORTED_64_BIT_ABIS : Build.SUPPORTED_32_BIT_ABIS;
        } else {
            String cpuAbi = Build.CPU_ABI;
            String cpuAbi2 = Build.CPU_ABI2;
            ArrayList<String> list = new ArrayList<>(2);
            if (cpuAbi != null && !cpuAbi.isEmpty()) {
                list.add(cpuAbi);
            }
            if (cpuAbi2 != null && !cpuAbi2.isEmpty()) {
                list.add(cpuAbi2);
            }
            return list.toArray(new String[0]);
        }
    }

    /**
     * 根据VMRuntime.ABI_TO_INSTRUCTION_SET_MAP
     */
    private static boolean isKnownInstructionSet(String instructionSet) {
        return "arm".equals(instructionSet) ||
                "mips".equals(instructionSet) ||
                "mips64".equals(instructionSet) ||
                "x86".equals(instructionSet) ||
                "x86_64".equals(instructionSet) ||
                "arm64".equals(instructionSet);
    }

    /**
     * Returns whether the given {@code instructionSet} is 64 bits.
     *
     * @param instructionSet a string representing an instruction set.
     * @return true if given {@code instructionSet} is 64 bits, false otherwise.
     * <p>
     * copy from VMRuntime.java
     */
    private static boolean is64BitInstructionSet(String instructionSet) {
        return "arm64".equals(instructionSet) ||
                "x86_64".equals(instructionSet) ||
                "mips64".equals(instructionSet);
    }

}
