/* -*- Mode: Java; c-basic-offset: 4; tab-width: 20; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus;

import android.os.StrictMode;
import android.support.v7.preference.PreferenceManager;

import org.mozilla.focus.locale.LocaleAwareApplication;
import org.mozilla.focus.search.SearchEngineManager;
import org.mozilla.focus.session.VisibilityLifeCycleCallback;
import org.mozilla.focus.telemetry.AppStartupTimeMeasurement;
import org.mozilla.focus.telemetry.DataUploadPreference;
import org.mozilla.focus.telemetry.TelemetryWrapper;
import org.mozilla.focus.utils.AppConstants;

public class FocusApplication extends LocaleAwareApplication {
    private VisibilityLifeCycleCallback visibilityLifeCycleCallback;

    @Override
    public void onCreate() {
        AppStartupTimeMeasurement.applicationOnCreate();
        super.onCreate();

        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        enableStrictMode();

        SearchEngineManager.getInstance().init(this);

        TelemetryWrapper.init(this);

        DataUploadPreference.init(this.getApplicationContext());

        registerActivityLifecycleCallbacks(visibilityLifeCycleCallback = new VisibilityLifeCycleCallback(this));
    }

    public VisibilityLifeCycleCallback getVisibilityLifeCycleCallback() {
        return visibilityLifeCycleCallback;
    }

    private void enableStrictMode() {
        // Android/WebView sometimes commit strict mode violations, see e.g.
        // https://github.com/mozilla-mobile/focus-android/issues/660
        if (AppConstants.isReleaseBuild()) {
            return;
        }

        final StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new StrictMode.ThreadPolicy.Builder().detectAll();
        final StrictMode.VmPolicy.Builder vmPolicyBuilder = new StrictMode.VmPolicy.Builder().detectAll();

        threadPolicyBuilder.penaltyDialog();
        vmPolicyBuilder.penaltyLog();

        StrictMode.setThreadPolicy(threadPolicyBuilder.build());
        StrictMode.setVmPolicy(vmPolicyBuilder.build());
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
//        OkHttpWrapper.onLowMemory(); // Disabled because it throws NPE.
        // If you need to dump more memory, you may be able to clear the Picasso cache.
    }
}
