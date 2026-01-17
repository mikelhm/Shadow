package com.sample.ads;


import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bytedance.sdk.openadsdk.api.init.PAGConfig;
import com.bytedance.sdk.openadsdk.api.init.PAGSdk;
import com.vungle.ads.InitializationListener;
import com.vungle.ads.VungleAds;
import com.vungle.ads.VungleError;

public class AdsManager {
    public static void initAds(Context context) {
        VungleAds.init(context, "12345", new InitializationListener() {
            @Override
            public void onSuccess() {
                Log.d("AdsManager", "Vungle SDK init onSuccess()");
            }
            @Override
            public void onError(@NonNull VungleError vungleError) {
                Log.d("AdsManager", "vungle sdk onError():" + vungleError.getErrorMessage());
            }
        });


        PAGSdk.init(context.getApplicationContext(), new  PAGConfig.Builder()
                .appId("5122011")
                .supportMultiProcess(false)
                .debugLog(true)
                .build(), new PAGSdk.PAGInitCallback() {
            @Override
            public void success() {
                Log.d("AdsManager", "Pangle sdk init success");
            }

            @Override
            public void fail(int i, String s) {
                Log.d("AdsManager", "Pangle sdk init fail");
            }
        });
    }
}
