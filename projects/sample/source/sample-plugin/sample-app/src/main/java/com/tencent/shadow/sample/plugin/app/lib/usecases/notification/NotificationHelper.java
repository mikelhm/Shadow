package com.tencent.shadow.sample.plugin.app.lib.usecases.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.tencent.shadow.sample.host.lib.HostResourceHelper;
import com.tencent.shadow.sample.plugin.app.lib.usecases.activity.TestActivityOnCreate;
import com.tencent.shadow.sample.plugin.app.lib.usecases.dialog.TestDialogActivity;

public class NotificationHelper {

    private static final String CHANNEL_ID = "custom_notification_channel";
    private static final String CHANNEL_NAME = "自定义通知";
    private static final int NOTIFICATION_ID = 20001;
    private static final String NOTIFICATION_ACTION = "plugin.intent.action.NotificationAction";

    public static void showCustomNotification(Context context) {
        // 创建通知渠道（Android 8.0+需要）
        createNotificationChannel(context);

        // 创建自定义布局
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), HostResourceHelper.getCustomNotificationLayout());

        // 设置布局中的文本内容
        remoteViews.setTextViewText(HostResourceHelper.getTextViewTitleId(), "自定义通知Demo");
        remoteViews.setTextViewText(HostResourceHelper.getTextViewContentId(), "这是一个带按钮的自定义通知");

        remoteViews.setOnClickPendingIntent(HostResourceHelper.getBtnClickId(), getPendingIntent(context));

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

    private static PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(context, HostResourceHelper.getPendingIntentActivity(TestActivityOnCreate.class));
        intent.setAction(NOTIFICATION_ACTION);
        return PendingIntent.getActivity(context, 0, intent, addMutabilityFlags(PendingIntent.FLAG_UPDATE_CURRENT));
    }

    private static int addMutabilityFlags(int flags) {
        boolean isMutable = (flags & PendingIntent.FLAG_UPDATE_CURRENT) != 0;
        if (isMutable) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                return flags | PendingIntent.FLAG_MUTABLE;
            } else {
                return flags;
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return flags | PendingIntent.FLAG_IMMUTABLE;
            } else {
                return flags;
            }
        }
    }

    public static void showSystemNotification(Context context) {
        // 创建通知渠道
        createNotificationChannel(context);
        // 创建点击意图
        Intent intent = new Intent(context, HostResourceHelper.getPendingIntentActivity(TestActivityOnCreate.class));
        intent.setAction("system.notification.click");
        intent.putExtra("source", "system_notification");
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                addMutabilityFlags(PendingIntent.FLAG_UPDATE_CURRENT)
        );
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(HostResourceHelper.getNotificationSmallIcon())
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), HostResourceHelper.getNotificationLargeIcon()))
                .setContentTitle("系统通知")
                .setContentText("任务正在处理中...")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setProgress(100, 80, false)
                .setOngoing(true);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            // 显示初始通知
            notificationManager.notify(NOTIFICATION_ID + 1, builder.build());
        }
    }
}
