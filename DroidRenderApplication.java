package com.luolai.droidrender;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.luolai.base.AnalysisUtils;
import com.luolai.base.Entity;
import java.io.File;

public class DroidRenderApplication extends Application {
    private static AnalysisUtils sTracker;

    static {
        System.loadLibrary("PGDicomReader");
    }

    public static synchronized AnalysisUtils getDefaultTracker(Context context) {
        AnalysisUtils analysisUtils;
        synchronized (DroidRenderApplication.class) {
            if (sTracker == null) {
                sTracker = new AnalysisUtils(context);
            }
            analysisUtils = sTracker;
        }
        return analysisUtils;
    }

    public void onCreate() {
        super.onCreate();
        Constants.setSystemString();
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.getAppDataPath(this));
        sb.append(Constants.RECENT_FILE_FOLDER_PATH);
        File file = new File(sb.toString());
        if (!file.exists()) {
            file.mkdirs();
        }
        MainActivity.function(83, Integer.parseInt(getSharedPreferences(Constants.PREF_NAME_SETTINGS, 0).getString(getString(C0354R.string.pref_key_3d_default_type), getString(C0354R.string.pref_key_3d_default_type_default_value))));
        MainActivity.initJNI(null, getResources().getDimension(C0354R.dimen.onedp), this);
        MainActivity.functions(84, Constants.getAppDataPath(this));
        MainActivity.functions(85, Constants.getFolderPath(this, FileType.cloud));
        createDeafultLayout();
    }

    private void createDeafultLayout() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREF_NAME_LAYOUTS, 0);
        String str = Constants.PREF_NAME_SET_DEFAULT_LAYOUTS;
        if (!sharedPreferences.getBoolean(str, false)) {
            Editor edit = sharedPreferences.edit();
            int i = 0;
            while (true) {
                int i2 = 1;
                if (i < 4) {
                    int layoutPrefValue = Constants.getLayoutPrefValue(sharedPreferences, PrefKeyOrder.num, i, 0);
                    if (layoutPrefValue == 0) {
                        edit.putInt(Constants.getLayoutPrefKey(PrefKeyOrder.num, i, 0), 1);
                        edit.putInt(Constants.getLayoutPrefKey(PrefKeyOrder.orientation, i, 0), 0);
                        edit.putInt(Constants.getLayoutPrefKey(PrefKeyOrder.left, i, layoutPrefValue), 0);
                        edit.putInt(Constants.getLayoutPrefKey(PrefKeyOrder.top, i, layoutPrefValue), 0);
                        edit.putInt(Constants.getLayoutPrefKey(PrefKeyOrder.width, i, layoutPrefValue), 100);
                        edit.putInt(Constants.getLayoutPrefKey(PrefKeyOrder.height, i, layoutPrefValue), 100);
                        if (i == 0) {
                            edit.putInt(Constants.getLayoutPrefKey(PrefKeyOrder.type, i, layoutPrefValue), 1);
                        } else {
                            int viewTypeByOrder = Entity.getViewTypeByOrder(i + 1);
                            if (viewTypeByOrder >= 0) {
                                i2 = viewTypeByOrder;
                            }
                            edit.putInt(Constants.getLayoutPrefKey(PrefKeyOrder.type, i, layoutPrefValue), i2);
                        }
                    }
                    i++;
                } else {
                    edit.putBoolean(str, true);
                    edit.apply();
                    return;
                }
            }
        }
    }
}
