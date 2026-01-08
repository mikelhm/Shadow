package com.tencent.shadow.sample.plugin.app.lib.usecases.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

public class NotificationPermissionUtil {
    private static final String TAG = "NotificationPermission";
    private static final int REQUEST_CODE_NOTIFICATION_PERMISSION = 1001;

    /**
     * 检查是否已经授予通知权限
     */
    public static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 需要检查通知权限
            return ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED;
        } else {
            // Android 13以下版本自动有通知权限
            return true;
        }
    }

    /**
     * 请求通知权限
     * @param activity 当前Activity
     * @param showRationaleDialog 是否显示解释对话框（如果用户之前拒绝了）
     * @return true表示已授权或不需要授权，false表示需要请求权限
     */
    public static boolean requestNotificationPermission(Activity activity,
                                                        boolean showRationaleDialog) {
        // Android 13以下不需要权限
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Log.d(TAG, "SDK < 33, 不需要通知权限");
            return true;
        }

        // 检查是否已经授权
        if (hasNotificationPermission(activity)) {
            Log.d(TAG, "通知权限已授予");
            return true;
        }

        // 检查是否需要显示解释对话框
        if (showRationaleDialog &&
                ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        android.Manifest.permission.POST_NOTIFICATIONS
                )) {
            // 显示解释为什么需要这个权限
            showPermissionRationaleDialog(activity);
            return false;
        }

        // 直接请求权限
        ActivityCompat.requestPermissions(
                activity,
                new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                REQUEST_CODE_NOTIFICATION_PERMISSION
        );

        return false;
    }

    /**
     * 显示权限解释对话框
     */
    private static void showPermissionRationaleDialog(Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle("需要通知权限")
                .setMessage("此功能需要通知权限来显示服务运行状态。\n请授权通知权限以正常使用。")
                .setPositiveButton("去授权", (dialog, which) -> {
                    // 再次请求权限
                    ActivityCompat.requestPermissions(
                            activity,
                            new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                            REQUEST_CODE_NOTIFICATION_PERMISSION
                    );
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 处理权限请求结果
     */
    public static void onRequestPermissionsResult(Activity activity,
                                                  int requestCode,
                                                  @NonNull String[] permissions,
                                                  @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "用户授予了通知权限");
                // 权限已授予，可以启动服务
                if (onPermissionGrantedListener != null) {
                    onPermissionGrantedListener.onGranted();
                }
            } else {
                Log.d(TAG, "用户拒绝了通知权限");
                // 显示引导用户去设置页面的对话框
                showGoToSettingsDialog(activity);
            }
        }
    }

    /**
     * 显示跳转到设置页面的对话框
     */
    private static void showGoToSettingsDialog(Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle("通知权限被拒绝")
                .setMessage("您拒绝了通知权限，这将影响前台服务的显示。\n是否前往设置页面手动开启权限？")
                .setPositiveButton("去设置", (dialog, which) -> {
                    openAppSettings(activity);
                })
                .setNegativeButton("稍后再说", null)
                .show();
    }

    /**
     * 打开应用设置页面
     */
    public static void openAppSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);

        if (intent.resolveActivity(context.getPackageManager()) != null) {
            if (context instanceof Activity) {
                context.startActivity(intent);
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }
    }

    /**
     * 权限授予后的回调接口
     */
    public interface OnPermissionGrantedListener {
        void onGranted();
    }

    private static OnPermissionGrantedListener onPermissionGrantedListener;

    public static void setOnPermissionGrantedListener(OnPermissionGrantedListener listener) {
        onPermissionGrantedListener = listener;
    }
}
