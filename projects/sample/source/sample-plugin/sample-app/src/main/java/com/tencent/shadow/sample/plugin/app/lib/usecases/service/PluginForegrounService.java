package com.tencent.shadow.sample.plugin.app.lib.usecases.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class PluginForegrounService extends Service {

    private static final String TAG = "PluginForegrounService";
    private static final String CHANNEL_ID = "plugin_foreground_channel";
    private static final String CHANNEL_NAME = "插件前台服务";
    private static final int NOTIFICATION_ID = 10001;

    @Override
    public void onCreate() {
        Log.d(TAG, "PluginForegrounService onCreate ");
        createNotificationChannel();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "PluginForegrounService onDestroy ");
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "PluginForegrounService onStartCommand ");
        startForeground(NOTIFICATION_ID, createNotification());
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 创建通知channel
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("插件前台服务运行通知");

            NotificationManager manager = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * 创建通知
     */
    private Notification createNotification() {
        NotificationCompat.Builder builder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder(this);
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        }

        return builder
                .setContentTitle("插件服务运行中")
                .setContentText("插件前台服务正在运行...")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setOngoing(true)
                .setAutoCancel(false)
                .setWhen(System.currentTimeMillis())
                .build();
    }

    /**
     * 启动前台服务的静态方法（供外部调用）
     */
    public static void start(Context context) {
        Intent intent = new Intent(context.getApplicationContext(), PluginForegrounService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8.0+ 必须使用 startForegroundService
            context.getApplicationContext().startForegroundService(intent);
        } else {
            // Android 8.0以下使用普通startService
            context.getApplicationContext().startService(intent);
        }
    }

    /**
     * 停止服务的静态方法
     */
    public static void stop(Context context) {
        Intent intent = new Intent(context.getApplicationContext(), PluginForegrounService.class);
        context.getApplicationContext().stopService(intent);
    }
}
