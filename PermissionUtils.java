package com.luolai.droidrender;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Build.VERSION;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionUtils {
    private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 100;
    private static final String[] PERMISSIONS = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};

    public static boolean checkPermission(Activity activity) {
        if (VERSION.SDK_INT < 23) {
            return true;
        }
        String str = "android.permission.READ_EXTERNAL_STORAGE";
        String str2 = "android.permission.WRITE_EXTERNAL_STORAGE";
        if (ContextCompat.checkSelfPermission(activity, str) == 0 && ContextCompat.checkSelfPermission(activity, str2) == 0) {
            return true;
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, str) || ActivityCompat.shouldShowRequestPermissionRationale(activity, str2)) {
            new Builder(activity).setMessage(C0354R.string.permission_request_content).setPositiveButton(17039370, new OnClickListener(activity) {
                private final /* synthetic */ Activity f$0;

                {
                    this.f$0 = r1;
                }

                public final void onClick(DialogInterface dialogInterface, int i) {
                    ActivityCompat.requestPermissions(this.f$0, PermissionUtils.PERMISSIONS, 100);
                }
            }).show();
        } else {
            ActivityCompat.requestPermissions(activity, PERMISSIONS, 100);
        }
        return false;
    }

    public static boolean onRequestPermissionsResult(Activity activity, int i, String[] strArr, int[] iArr) {
        if (i == 100) {
            if (iArr.length > 0) {
                for (int i2 : iArr) {
                    if (i2 != 0) {
                        showRequestFailDialog(activity);
                        return false;
                    }
                }
                return true;
            }
            showRequestFailDialog(activity);
        }
        return false;
    }

    private static void showRequestFailDialog(Activity activity) {
        if (VERSION.SDK_INT >= 23) {
            new Builder(activity).setMessage(C0354R.string.permission_request_fail_message).setPositiveButton(17039370, new OnClickListener(activity) {
                private final /* synthetic */ Activity f$0;

                {
                    this.f$0 = r1;
                }

                public final void onClick(DialogInterface dialogInterface, int i) {
                    this.f$0.finish();
                }
            }).setOnDismissListener(new OnDismissListener(activity) {
                private final /* synthetic */ Activity f$0;

                {
                    this.f$0 = r1;
                }

                public final void onDismiss(DialogInterface dialogInterface) {
                    this.f$0.finish();
                }
            }).show();
        }
    }
}
