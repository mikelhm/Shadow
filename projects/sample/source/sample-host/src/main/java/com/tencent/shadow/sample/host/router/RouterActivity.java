package com.tencent.shadow.sample.host.router;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.tencent.shadow.dynamic.host.EnterCallback;
import com.tencent.shadow.sample.constant.Constant;
import com.tencent.shadow.sample.host.HostApplication;
import com.tencent.shadow.sample.host.PluginHelper;

public class RouterActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
        finish();
    }

    private void handleIntent(Intent intent) {
        Log.d("ShadowPlugin", "Router activity handle intent = " + intent);
        if(intent == null) {
            return;
        }
        String action = intent.getAction();
        if(("plugin.intent.action.NotificationAction").equals(action)) {
           startPluginActivity(intent, "com.tencent.shadow.sample.plugin.app.lib.usecases.activity.TestActivityOnCreate");
        }
    }

    private void startPluginActivity(Intent intent, String targetClass) {
        HostApplication.getApp().loadPluginManager(PluginHelper.getInstance().pluginManagerFile);
        Bundle bundle = new Bundle();
        bundle.putString(Constant.KEY_PLUGIN_ZIP_PATH, PluginHelper.getInstance().pluginZipFile.getAbsolutePath());
        bundle.putString(Constant.KEY_PLUGIN_PART_KEY, getIntent().getStringExtra(Constant.KEY_PLUGIN_PART_KEY));
        bundle.putString(Constant.KEY_ACTIVITY_CLASSNAME, targetClass);
        bundle.putBundle(Constant.KEY_EXTRAS, intent.getExtras());
        HostApplication.getApp().getPluginManager().enter(this,
                Constant.FROM_ID_START_ACTIVITY, bundle, new EnterCallback() {
                    @Override
                    public void onShowLoadingView(View view) {

                    }

                    @Override
                    public void onCloseLoadingView() {

                    }

                    @Override
                    public void onEnterComplete() {

                    }
                });
    }
}
