package com.luolai.droidrender;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;

public abstract class BaseActivity extends Activity {
    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        MainActivity.overwriteConfigurationLocale(this);
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        MainActivity.overwriteConfigurationLocale(this);
    }
}
