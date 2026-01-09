package com.tencent.shadow.sample.host.lib;

public class HostResourceHelper {
    // 获取宿主中的资源ID
    public static int getNotificationSmallIcon() {
        return R.drawable.ic_launcher;
    }

    public static int getNotificationLargeIcon() {
        return R.drawable.ic_launcher_round;
    }

    public static int getCustomNotificationLayout() {
        return R.layout.custom_notification;
    }

    public static int getTextViewTitleId() {
        return R.id.tv_title;
    }

    public static int getTextViewContentId() {
        return R.id.tv_content;
    }
}

