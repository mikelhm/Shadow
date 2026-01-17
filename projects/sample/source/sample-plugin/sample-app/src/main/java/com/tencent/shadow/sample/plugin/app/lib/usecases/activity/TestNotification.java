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

package com.tencent.shadow.sample.plugin.app.lib.usecases.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.tencent.shadow.sample.plugin.app.lib.R;
import com.tencent.shadow.sample.plugin.app.lib.gallery.cases.entity.UseCase;
import com.tencent.shadow.sample.plugin.app.lib.usecases.notification.NotificationHelper;
import com.tencent.shadow.sample.plugin.app.lib.usecases.utils.NotificationPermissionUtil;

public class TestNotification extends Activity {

    public static class Case extends UseCase {
        @Override
        public String getName() {
            return "通知测试";
        }

        @Override
        public String getSummary() {
            return "测试通知";
        }

        @Override
        public Class getPageClass() {
            return TestNotification.class;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_test_notification);
        NotificationPermissionUtil.requestNotificationPermission(this,true);
        findViewById(R.id.btn_test_custom_notification).setOnClickListener(view -> {
            NotificationHelper.showCustomNotification(this);
        });
        findViewById(R.id.btn_test_system_notification).setOnClickListener(view -> {
            NotificationHelper.showSystemNotification(this);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
