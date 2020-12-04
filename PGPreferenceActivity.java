package com.luolai.droidrender;

import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import com.luolai.base.GAHelper;

public class PGPreferenceActivity extends PreferenceActivity {
    private static final String TAG = PGPreferenceActivity.class.getSimpleName();

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        MainActivity.overwriteConfigurationLocale(this);
        addPreferencesFromResource(C0354R.xml.pref);
        GAHelper.recordScreen(this, TAG);
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        MainActivity.overwriteConfigurationLocale(this);
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        if (getIntent() != null && getIntent().getBooleanExtra(Constants.EXTRA_FROM_MAINACTIVITY, false)) {
            MainActivity.gotoMainActivity(this);
        }
        super.onDestroy();
    }
}
