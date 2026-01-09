package com.tencent.shadow.sample.plugin.app.lib.usecases.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.tencent.shadow.sample.host.lib.HostResourceHelper;

public class NotificationHelper {

    private static final String CHANNEL_ID = "custom_notification_channel";
    private static final String CHANNEL_NAME = "自定义通知";
    private static final int NOTIFICATION_ID = 20001;

    public static void showCustomNotification(Context context) {
        // 创建通知渠道（Android 8.0+需要）
        createNotificationChannel(context);

        // 创建自定义布局
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), HostResourceHelper.getCustomNotificationLayout());

        // 设置布局中的文本内容
        remoteViews.setTextViewText(HostResourceHelper.getTextViewTitleId(), "自定义通知Demo");
        remoteViews.setTextViewText(HostResourceHelper.getTextViewContentId(), "这是一个带按钮的自定义通知");

        // 构建通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(HostResourceHelper.getNotificationSmallIcon())  // 小图标
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), HostResourceHelper.getNotificationLargeIcon()))  // 大图标
                .setCustomContentView(remoteViews)  // 设置自定义布局
                .setAutoCancel(true)  // 点击后自动取消
                .setPriority(NotificationCompat.PRIORITY_HIGH);  // 设置优先级

        Notification notification = builder.build();

        // 显示通知
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("用于显示自定义通知");
            channel.enableLights(true);  // 开启指示灯
            channel.setLightColor(0xFF0000);  // 设置指示灯颜色
            channel.enableVibration(true);  // 开启振动

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public static void cancelNotification(Context context) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_ID);
        }
    }
}
