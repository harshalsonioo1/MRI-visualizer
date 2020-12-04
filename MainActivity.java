package com.luolai.droidrender;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.analytics.FirebaseAnalytics.Param;
import com.luolai.base.Entity.BitmapPack;
import com.luolai.base.Entity.PatientPreview;
import com.luolai.base.Entity.PreviewSlice;
import com.luolai.base.GAHelper;
import com.luolai.base.Log;
import com.luolai.base.Log.Logger;
import com.luolai.droidrender.DialogHelper.FileSaverCallback;
import com.luolai.droidrender.iab.util.IabHelper;
import com.luolai.droidrender.iab.util.IabHelper.OnIabSetupFinishedListener;
import com.luolai.droidrender.iab.util.IabHelper.QueryInventoryFinishedListener;
import com.luolai.droidrender.iab.util.IabResult;
import com.luolai.droidrender.iab.util.Inventory;
import com.luolai.droidrender.iab.util.Purchase;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Locale;

public class MainActivity extends Activity {
    private static String KEY_DEFAULT_2 = "key_default_2";
    public static final int REQUEST_BCD = 1;
    public static final int REQUEST_BCD_SAVE = 5;
    public static final int REQUEST_CLOUD = 6;
    public static final int REQUEST_CLOUD_TREE = 7;
    public static final int REQUEST_EDIT_LAYOUT = 4;
    public static final int REQUEST_VRCM = 2;
    public static final int REQUEST_VRCM_JNI = 3;
    private static final String TAG = "MainActivity";
    public static float m3DResample = 2.0f;
    private static LinkedHashMap<CharSequence, Uri> mVideoTutoralSet = new LinkedHashMap<>();
    boolean goJNI = true;
    public int m3DQuality = 1;
    int mActionBarHeight = 0;
    private ImageView mConfirm;
    boolean mEnteredFullscreen = false;
    QueryInventoryFinishedListener mGotInventoryListener = new QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult iabResult, Inventory inventory) {
            String str = MainActivity.TAG;
            Log.m47d(str, "Query inventory finished.");
            if (MainActivity.this.mHelper != null) {
                boolean z = true;
                if (iabResult.isFailure()) {
                    Log.m48e(str, MainActivity.this.getString(C0354R.string.support_query_fail, new Object[]{iabResult}));
                    return;
                }
                Log.m47d(str, "Query inventory was successful.");
                Editor edit = MainActivity.this.getSharedPreferences(Constants.PREF_NAME_SETTINGS, 0).edit();
                Purchase purchase = inventory.getPurchase(SupportActivity.SKU_PREMIUM);
                boolean z2 = purchase != null && SupportActivity.verifyDeveloperPayload(purchase);
                edit.putBoolean(Constants.PREF_IS_VIP, z2);
                StringBuilder sb = new StringBuilder();
                String str2 = "User is ";
                sb.append(str2);
                sb.append(z2 ? "PREMIUM" : "NOT PREMIUM");
                Log.m47d(str, sb.toString());
                Purchase purchase2 = inventory.getPurchase(SupportActivity.SKU_NOADS);
                if (purchase2 == null || !SupportActivity.verifyDeveloperPayload(purchase2)) {
                    z = false;
                }
                edit.putBoolean(Constants.PREF_IS_NOADS, z);
                StringBuilder sb2 = new StringBuilder();
                sb2.append(str2);
                sb2.append(z ? "NoADS" : "NOT NoADS");
                Log.m47d(str, sb2.toString());
                GAHelper.reportPremiumLevel(MainActivity.this, z2, z);
                edit.apply();
                MainActivity.this.updateADS();
                Log.m47d(str, "Initial inventory query finished; enabling main UI.");
            }
        }
    };
    IabHelper mHelper;
    private boolean mIsEnglishOnly;
    private boolean mIsFullScreen = false;
    /* access modifiers changed from: private */
    public boolean mIsLayoutReady = false;
    boolean mIsMenuShowing = false;
    long mLastBackTime = 0;
    private ImageView mLastPatient;
    private ImageView mNextPatient;
    public float mOneDP;
    /* access modifiers changed from: private */
    public int[] mPendingImageList = null;
    PhysiJNIView mPhysiJNIView;
    public Handler mRanderingHandler;
    public HandlerThread mRanderingThread;
    /* access modifiers changed from: private */
    public Point mRootSize = new Point(0, 0);
    int mStatusBarHeight = 0;
    private TextView mTitle;
    /* access modifiers changed from: private */
    public Point mViewSize;

    /* renamed from: com.luolai.droidrender.MainActivity$22 */
    static /* synthetic */ class C032422 {
        static final /* synthetic */ int[] $SwitchMap$com$luolai$droidrender$Constants$FileType = new int[FileType.values().length];

        /* JADX WARNING: Can't wrap try/catch for region: R(10:0|1|2|3|4|5|6|7|8|10) */
        /* JADX WARNING: Can't wrap try/catch for region: R(8:0|1|2|3|4|5|6|(3:7|8|10)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0014 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001f */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x002a */
        static {
            /*
                com.luolai.droidrender.Constants$FileType[] r0 = com.luolai.droidrender.Constants.FileType.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$luolai$droidrender$Constants$FileType = r0
                int[] r0 = $SwitchMap$com$luolai$droidrender$Constants$FileType     // Catch:{ NoSuchFieldError -> 0x0014 }
                com.luolai.droidrender.Constants$FileType r1 = com.luolai.droidrender.Constants.FileType.bcd     // Catch:{ NoSuchFieldError -> 0x0014 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0014 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0014 }
            L_0x0014:
                int[] r0 = $SwitchMap$com$luolai$droidrender$Constants$FileType     // Catch:{ NoSuchFieldError -> 0x001f }
                com.luolai.droidrender.Constants$FileType r1 = com.luolai.droidrender.Constants.FileType.vrcm     // Catch:{ NoSuchFieldError -> 0x001f }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001f }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001f }
            L_0x001f:
                int[] r0 = $SwitchMap$com$luolai$droidrender$Constants$FileType     // Catch:{ NoSuchFieldError -> 0x002a }
                com.luolai.droidrender.Constants$FileType r1 = com.luolai.droidrender.Constants.FileType.stl     // Catch:{ NoSuchFieldError -> 0x002a }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x002a }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x002a }
            L_0x002a:
                int[] r0 = $SwitchMap$com$luolai$droidrender$Constants$FileType     // Catch:{ NoSuchFieldError -> 0x0035 }
                com.luolai.droidrender.Constants$FileType r1 = com.luolai.droidrender.Constants.FileType.snapshot     // Catch:{ NoSuchFieldError -> 0x0035 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0035 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0035 }
            L_0x0035:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.luolai.droidrender.MainActivity.C032422.<clinit>():void");
        }
    }

    public static class MyDialog extends AlertDialog {
        public MyDialog(Context context) {
            super(context);
        }

        public void onCreate(Bundle bundle) {
            super.onCreate(bundle);
            LayoutParams attributes = getWindow().getAttributes();
            attributes.width = -1;
            getWindow().setAttributes(attributes);
        }
    }

    public static native void deletePatient(int[] iArr);

    public static native BitmapPack draw(int i, boolean z);

    public static native boolean exportFile(String str, int i);

    public static native void function(long j, int i);

    public static native void functions(long j, String str);

    public static native int[] getMenu(int i);

    public static native PatientPreview[] getPatientList(int i);

    public static native PreviewSlice getPreviewSlice(boolean z, int i, int i2);

    public static native int getValueInt(int i);

    public static native int getViewHashFromNumber(int i);

    public static native boolean hasPatient(boolean z);

    public static native void iconFunction(long j, int i);

    public static native void initJNI(PhysiJNIView physiJNIView, float f, Context context);

    public static native boolean isViewFocused(int i);

    public static native void loadFor(int i);

    public static native int loadPixelAsync(int i, int i2);

    public static native void loadPixelDataAndDoShift(int i);

    public static native void loadToJNI(String[] strArr);

    public static native void menuFunction(long j);

    public static native void postInitCDibNative(int[] iArr);

    public static native void readVRCM(String str);

    public static native void setString(byte[] bArr);

    public static native void setUI(int i, int i2, int i3, int i4, int i5, int i6);

    public static native void setViewSize(int i, int i2);

    public static native void startDeletePatient(boolean z, PhysiJNIView physiJNIView);

    public static native byte touch(int i, int i2, int i3, int i4, int i5, int i6, long j, int i7);

    /* access modifiers changed from: private */
    public void updateADS() {
    }

    public void setupActionBarDropdown(String[] strArr, int i) {
    }

    public static void postInitCDib(Context context, int[] iArr) {
        postInitCDibNative(iArr);
        deleteUnusedCloudCacheFiles(context);
    }

    private static void deleteUnusedCloudCacheFiles(Context context) {
        File[] listFiles;
        if (context != null) {
            File file = new File(Constants.getFolderPath(context, FileType.cloud));
            if (file.exists() && file.isDirectory()) {
                File[] listFiles2 = file.listFiles();
                if (listFiles2 != null && listFiles2.length > 0) {
                    for (File file2 : file.listFiles()) {
                        if (!file2.getName().startsWith("v")) {
                            file2.delete();
                        }
                    }
                }
            }
        }
    }

    public static void whatsNew(final Context context) {
        if (!context.getSharedPreferences(Constants.PREF_NAME_SETTINGS, 0).getBoolean(Constants.PREF_KEY_WHATS_NEW, false)) {
            createDefaultVRCM(context);
            Builder builder = new Builder(context);
            builder.setTitle(C0354R.string.dlg_whats_new_title);
            StringBuilder sb = new StringBuilder();
            for (int i = 31; i > 0; i--) {
                Resources resources = context.getResources();
                StringBuilder sb2 = new StringBuilder();
                sb2.append("dlg_whats_new_content_");
                sb2.append(i);
                int identifier = resources.getIdentifier(sb2.toString(), "string", BuildConfig.APPLICATION_ID);
                if (identifier > 0) {
                    sb.append(context.getString(identifier));
                    sb.append("\n\n\n");
                }
            }
            if (sb.length() > 0) {
                builder.setMessage(sb.toString());
                builder.setPositiveButton(C0354R.string.whats_new_show_tutorial, new OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Editor edit = context.getSharedPreferences(Constants.PREF_NAME_SETTINGS, 0).edit();
                        edit.putBoolean(Constants.PREF_KEY_WHATS_NEW, true);
                        edit.apply();
                        MainActivity.goToVideo(context, "https://youtu.be/ClgSyoijeWk");
                        MainActivity.disclaimer(context);
                    }
                });
                builder.setNegativeButton(17039370, new OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Editor edit = context.getSharedPreferences(Constants.PREF_NAME_SETTINGS, 0).edit();
                        edit.putBoolean(Constants.PREF_KEY_WHATS_NEW, true);
                        edit.apply();
                        MainActivity.disclaimer(context);
                    }
                });
                builder.setCancelable(false);
                AlertDialog create = builder.create();
                create.setCanceledOnTouchOutside(false);
                create.show();
                return;
            }
            return;
        }
        disclaimer(context);
    }

    /* access modifiers changed from: private */
    public static void disclaimer(final Context context) {
        if (!context.getSharedPreferences(Constants.PREF_NAME_SETTINGS, 0).getBoolean(Constants.PREF_KEY_USER_AGREE, false)) {
            Builder builder = new Builder(context);
            builder.setTitle(C0354R.string.dialog_disclaimer_title);
            builder.setMessage(C0354R.string.dialog_disclaimer_message);
            builder.setPositiveButton(C0354R.string.agree, new OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    Editor edit = context.getSharedPreferences(Constants.PREF_NAME_SETTINGS, 0).edit();
                    edit.putBoolean(Constants.PREF_KEY_USER_AGREE, true);
                    edit.apply();
                }
            });
            builder.setNegativeButton(C0354R.string.disagree, new OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    ((Activity) context).finish();
                }
            });
            builder.setCancelable(false);
            AlertDialog create = builder.create();
            create.setCanceledOnTouchOutside(false);
            create.show();
        }
    }

    public static boolean supportExport(Context context) {
        if (Constants.sDebug) {
            return true;
        }
        boolean z = false;
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREF_NAME_SETTINGS, 0);
        boolean z2 = sharedPreferences.getBoolean(Constants.PREF_IS_VIP, false);
        boolean z3 = sharedPreferences.getBoolean(Constants.PREF_IS_NOADS, false);
        if (Build.BRAND.equalsIgnoreCase("htc")) {
            return true;
        }
        if (z2 || z3) {
            z = true;
        }
        return z;
    }

    public static void gotoMainActivity(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, MainActivity.class);
        context.startActivity(intent);
    }

    private static void createVideoTutoralSet(Context context) {
        mVideoTutoralSet.put(context.getString(C0354R.string.menu_video_tutorial_basic), Uri.parse("https://youtu.be/96BAT2dmH5s"));
        mVideoTutoralSet.put(context.getString(C0354R.string.menu_video_tutorial_2d), Uri.parse("https://youtu.be/HOUgpfKilJI"));
        mVideoTutoralSet.put(context.getString(C0354R.string.menu_video_tutorial_2dfree), Uri.parse("https://youtu.be/A0id_7nlLlY"));
        mVideoTutoralSet.put(context.getString(C0354R.string.menu_video_tutorial_3d), Uri.parse("https://youtu.be/OCU0zeC03KU"));
        mVideoTutoralSet.put(context.getString(C0354R.string.menu_video_tutorial_3d_render), Uri.parse("https://youtu.be/SWpTDmhzKlg"));
        mVideoTutoralSet.put(context.getString(C0354R.string.menu_video_tutorial_measure), Uri.parse("https://youtu.be/r-IwWRgSIlA"));
        mVideoTutoralSet.put(context.getString(C0354R.string.menu_video_tutorial_layouts), Uri.parse("https://youtu.be/W9ISr-cs4X8"));
        mVideoTutoralSet.put(context.getString(C0354R.string.menu_video_tutorial_segmentation), Uri.parse("https://youtu.be/H8otSJKZHjA"));
        mVideoTutoralSet.put(context.getString(C0354R.string.menu_video_tutorial_step_grow), Uri.parse("https://youtu.be/dQuoqnTJmgM"));
        mVideoTutoralSet.put(context.getString(C0354R.string.menu_video_tutorial_mesh), Uri.parse("https://youtu.be/TicmdE1qaeg"));
    }

    /* access modifiers changed from: private */
    public static void startTutoral(Context context, CharSequence charSequence) {
        if (mVideoTutoralSet.size() <= 0) {
            createVideoTutoralSet(context);
        }
        Uri uri = (Uri) mVideoTutoralSet.get(charSequence);
        if (uri != null) {
            goToVideo(context, uri.toString());
        }
    }

    /* access modifiers changed from: private */
    public static void goToVideo(Context context, String str) {
        if (!TextUtils.isEmpty(str)) {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            intent.setData(Uri.parse(str));
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void createVideoTutoralDlg(final Context context) {
        if (mVideoTutoralSet.size() <= 0) {
            createVideoTutoralSet(context);
        }
        Builder builder = new Builder(context);
        final CharSequence[] charSequenceArr = (CharSequence[]) mVideoTutoralSet.keySet().toArray(new CharSequence[mVideoTutoralSet.size()]);
        builder.setItems(charSequenceArr, new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.startTutoral(context, charSequenceArr[i]);
                GAHelper.sendTracker(context, MainActivity.TAG, "Show video tutoral", "", (long) i);
            }
        });
        builder.create().show();
    }

    public static Intent getFilePickerIntent(Context context, int i, boolean z, String str, String str2) {
        String str3;
        Intent intent = new Intent();
        intent.setClass(context, FilePicker.class);
        if (z) {
            intent.putExtra(FilePicker.ONLY_DIRS, true);
        }
        intent.putExtra(FilePicker.FIRST_DLG, true);
        if (!TextUtils.isEmpty(str)) {
            intent.putExtra(FilePicker.WITH_SUB_NAME, str);
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREF_NAME_SETTINGS, 0);
        if (i != 1) {
            str3 = sharedPreferences.getString(Constants.PREF_KEY_LAST_DIR_VRCM, null);
        } else if (TextUtils.isEmpty(str2)) {
            str3 = sharedPreferences.getString(Constants.PREF_KEY_LAST_DIR_IMAGE, null);
        } else {
            str3 = sharedPreferences.getString(Constants.PREF_KEY_LAST_DIR_IMAGE_EXTERNAL, null);
            intent.putExtra(FilePicker.IS_SD, true);
        }
        if (str3 != null) {
            intent.putExtra(FilePicker.START_DIR, str3);
        }
        if (!TextUtils.isEmpty(str2)) {
            intent.putExtra(FilePicker.WITH_URI, str2);
        }
        intent.putExtra(Constants.EXTRA_REQUEST_CODE, i);
        return intent;
    }

    public static void goToLandingPageIfNoPatient(Context context) {
        if (!hasPatient(false)) {
            goToLandingPage(context);
        }
    }

    public static void goToLandingPage(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, LandingActivity.class);
        intent.putExtra(Constants.EXTRA_FROM_MAINACTIVITY, true);
        context.startActivity(intent);
    }

    public static Locale getLocaleFromPref(Activity activity) {
        if ("1".equals(activity.getSharedPreferences(Constants.PREF_NAME_SETTINGS, 0).getString(Constants.PREF_KEY_ENGLISH_ONLY, "0"))) {
            return Locale.ENGLISH;
        }
        return Locale.getDefault();
    }

    public static void overwriteConfigurationLocale(Activity activity) {
        Locale localeFromPref = getLocaleFromPref(activity);
        Configuration configuration = activity.getResources().getConfiguration();
        if (localeFromPref != configuration.locale) {
            Locale.setDefault(localeFromPref);
            configuration.locale = localeFromPref;
            activity.getResources().updateConfiguration(configuration, activity.getResources().getDisplayMetrics());
        }
    }

    private static void createDefaultVRCM(Context context) {
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.getAppDataPath(context));
        sb.append("/VRCM/");
        String sb2 = sb.toString();
        File file = new File(sb2);
        if (!file.exists()) {
            file.mkdirs();
        }
        insertDefaultVRCM(context, sb2, "def.vrcm", "Default_1.vrcm", null);
        String str = "Default_2.vrcm";
        insertDefaultVRCM(context, sb2, str, str, KEY_DEFAULT_2);
        String str2 = "Default_3.vrcm";
        insertDefaultVRCM(context, sb2, str2, str2, null);
        String str3 = "Surface_Skin.vrcm";
        insertDefaultVRCM(context, sb2, str3, str3, null);
    }

    private static void insertDefaultVRCM(Context context, String str, String str2, String str3, String str4) {
        BufferedOutputStream bufferedOutputStream;
        boolean isEmpty = TextUtils.isEmpty(str4);
        String str5 = Constants.PREF_NAME_SETTINGS;
        boolean z = !isEmpty ? context.getSharedPreferences(str5, 0).getBoolean(str4, false) : true;
        StringBuilder sb = new StringBuilder();
        sb.append(str);
        sb.append(str3);
        File file = new File(sb.toString());
        if (!file.exists() || !z) {
            AssetManager assets = context.getAssets();
            try {
                file.createNewFile();
                InputStream open = assets.open(str2);
                bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
                byte[] bArr = new byte[4096];
                while (true) {
                    int read = open.read(bArr, 0, 4096);
                    if (read <= 0) {
                        break;
                    }
                    bufferedOutputStream.write(bArr, 0, read);
                }
                if (!TextUtils.isEmpty(str4)) {
                    context.getSharedPreferences(str5, 0).edit().putBoolean(str4, true).apply();
                }
                bufferedOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Throwable th) {
                bufferedOutputStream.close();
                throw th;
            }
        }
    }

    public static String createSampleFile(Context context) {
        BufferedOutputStream bufferedOutputStream;
        BufferedOutputStream bufferedOutputStream2;
        BufferedOutputStream bufferedOutputStream3;
        BufferedOutputStream bufferedOutputStream4;
        String samplePath = Constants.getSamplePath(context);
        File file = new File(samplePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        StringBuilder sb = new StringBuilder();
        sb.append(samplePath);
        sb.append("DroidRender_sample_2.bcd");
        File file2 = new File(sb.toString());
        if (!file2.exists()) {
            AssetManager assets = context.getAssets();
            try {
                file2.createNewFile();
                InputStream open = assets.open("test1.bcd");
                bufferedOutputStream4 = new BufferedOutputStream(new FileOutputStream(file2));
                byte[] bArr = new byte[4096];
                while (true) {
                    int read = open.read(bArr, 0, 4096);
                    if (read <= 0) {
                        break;
                    }
                    bufferedOutputStream4.write(bArr, 0, read);
                }
                bufferedOutputStream4.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Throwable th) {
                bufferedOutputStream4.close();
                throw th;
            }
        }
        StringBuilder sb2 = new StringBuilder();
        sb2.append(Constants.getSamplePath(context));
        sb2.append("BRAINIX01.dcm");
        if (!new File(sb2.toString()).exists()) {
            AssetManager assets2 = context.getAssets();
            for (int i = 1; i < 22; i++) {
                try {
                    File file3 = new File(String.format("%sBRAINIX%02d.dcm", new Object[]{Constants.getSamplePath(context), Integer.valueOf(i)}));
                    file3.createNewFile();
                    InputStream open2 = assets2.open(String.format("IM-0001-00%02d.dcm", new Object[]{Integer.valueOf(i)}));
                    bufferedOutputStream3 = new BufferedOutputStream(new FileOutputStream(file3));
                    byte[] bArr2 = new byte[4096];
                    while (true) {
                        int read2 = open2.read(bArr2, 0, 4096);
                        if (read2 <= 0) {
                            break;
                        }
                        bufferedOutputStream3.write(bArr2, 0, read2);
                    }
                    bufferedOutputStream3.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                } catch (Throwable th2) {
                    bufferedOutputStream3.close();
                    throw th2;
                }
            }
        }
        StringBuilder sb3 = new StringBuilder();
        sb3.append(Constants.getSamplePath(context));
        sb3.append("IDEFIX.dcm");
        File file4 = new File(sb3.toString());
        if (!file4.exists()) {
            AssetManager assets3 = context.getAssets();
            try {
                file4.createNewFile();
                InputStream open3 = assets3.open("IM-0001-40016.dcm");
                bufferedOutputStream2 = new BufferedOutputStream(new FileOutputStream(file4));
                byte[] bArr3 = new byte[4096];
                while (true) {
                    int read3 = open3.read(bArr3, 0, 4096);
                    if (read3 <= 0) {
                        break;
                    }
                    bufferedOutputStream2.write(bArr3, 0, read3);
                }
                bufferedOutputStream2.close();
            } catch (IOException e3) {
                e3.printStackTrace();
            } catch (Throwable th3) {
                bufferedOutputStream2.close();
                throw th3;
            }
        }
        StringBuilder sb4 = new StringBuilder();
        sb4.append(Constants.getSamplePath(context));
        String str = "TRAGICOMIX.dcm";
        sb4.append(str);
        String sb5 = sb4.toString();
        File file5 = new File(sb5);
        if (!file5.exists()) {
            AssetManager assets4 = context.getAssets();
            try {
                file5.createNewFile();
                InputStream open4 = assets4.open(str);
                bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file5));
                byte[] bArr4 = new byte[4096];
                while (true) {
                    int read4 = open4.read(bArr4, 0, 4096);
                    if (read4 <= 0) {
                        break;
                    }
                    bufferedOutputStream.write(bArr4, 0, read4);
                }
                bufferedOutputStream.close();
            } catch (IOException e4) {
                e4.printStackTrace();
            } catch (Throwable th4) {
                bufferedOutputStream.close();
                throw th4;
            }
        }
        return sb5;
    }

    private void setSystemUISize() {
        TypedValue typedValue = new TypedValue();
        if (getTheme().resolveAttribute(16843499, typedValue, true)) {
            this.mActionBarHeight = TypedValue.complexToDimensionPixelSize(typedValue.data, getResources().getDisplayMetrics());
        }
        Resources resources = getResources();
        int identifier = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (identifier > 0) {
            this.mStatusBarHeight = resources.getDimensionPixelSize(identifier);
        }
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setLocale();
        showActionBar();
        if (this.goJNI) {
            setContentView(C0354R.layout.activity_main_jni);
            this.mPhysiJNIView = (PhysiJNIView) findViewById(C0354R.C0356id.physiview);
            registerForContextMenu(this.mPhysiJNIView);
            this.mPhysiJNIView.setMainActivity(this);
            this.mRanderingThread = new HandlerThread("Randing_thread");
            this.mRanderingThread.setPriority(9);
            this.mRanderingThread.start();
            this.mRanderingHandler = new Handler(this.mRanderingThread.getLooper());
            this.mOneDP = getResources().getDimension(C0354R.dimen.onedp);
            final View decorView = getWindow().getDecorView();
            decorView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    if (MainActivity.this.mRootSize.x != decorView.getWidth() || MainActivity.this.mRootSize.y != decorView.getHeight()) {
                        MainActivity.this.mRootSize.x = decorView.getWidth();
                        MainActivity.this.mRootSize.y = decorView.getHeight();
                        MainActivity.this.onSystemUIChange(true);
                    }
                }
            });
            initJNI(this.mPhysiJNIView, this.mOneDP, this);
            if (Constants.sDebug) {
                setLogger();
            }
            if (!getSharedPreferences(Constants.PREF_NAME_SETTINGS, 0).getBoolean(Constants.PREF_CONTROL_TREE_STATUS, true)) {
                function(82, 0);
            }
            updateADS();
            this.mPhysiJNIView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    if (MainActivity.this.mViewSize == null || MainActivity.this.mViewSize.x != MainActivity.this.mPhysiJNIView.getWidth() || MainActivity.this.mViewSize.y != MainActivity.this.mPhysiJNIView.getHeight()) {
                        if (MainActivity.this.mViewSize == null) {
                            MainActivity.this.mViewSize = new Point();
                        }
                        MainActivity.this.mViewSize.x = MainActivity.this.mPhysiJNIView.getWidth();
                        MainActivity.this.mViewSize.y = MainActivity.this.mPhysiJNIView.getHeight();
                        MainActivity.setViewSize(MainActivity.this.mPhysiJNIView.getWidth(), MainActivity.this.mPhysiJNIView.getHeight());
                        MainActivity.overwriteConfigurationLocale(MainActivity.this);
                        MainActivity.this.mIsLayoutReady = true;
                        MainActivity mainActivity = MainActivity.this;
                        mainActivity.postInitCDibWhenLayoutReady(mainActivity.mPendingImageList);
                        MainActivity.this.mPendingImageList = null;
                    }
                }
            });
        }
        if (PermissionUtils.checkPermission(this)) {
            onNewIntent(getIntent());
        }
        this.mPhysiJNIView.setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {
            public void onSystemUiVisibilityChange(int i) {
                MainActivity.this.onSystemUIChange(false);
            }
        });
        whatsNew(this);
        this.mHelper = new IabHelper(this, SupportActivity.base64EncodedPublicKey);
        this.mHelper.enableDebugLogging(true);
        String str = TAG;
        Log.m47d(str, "Starting setup.");
        this.mHelper.startSetup(new OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult iabResult) {
                String str = MainActivity.TAG;
                Log.m47d(str, "Setup finished.");
                if (!iabResult.isSuccess()) {
                    Log.m48e(str, MainActivity.this.getString(C0354R.string.support_setup_fail, new Object[]{iabResult.toString()}));
                } else if (MainActivity.this.mHelper != null) {
                    Log.m47d(str, "Setup successful. Querying inventory.");
                    MainActivity.this.mHelper.queryInventoryAsync(MainActivity.this.mGotInventoryListener);
                }
            }
        });
        GAHelper.recordScreen(this, str);
    }

    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        if (PermissionUtils.onRequestPermissionsResult(this, i, strArr, iArr)) {
            onNewIntent(getIntent());
        }
    }

    /* access modifiers changed from: private */
    public void onSystemUIChange(boolean z) {
        LinearLayout.LayoutParams layoutParams;
        if ((!this.mIsMenuShowing || z) && this.mEnteredFullscreen) {
            if ((this.mPhysiJNIView.getSystemUiVisibility() & 4) == 0) {
                int dimension = (int) getTheme().obtainStyledAttributes(new int[]{16843499}).getDimension(0, 0.0f);
                Rect rect = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
                layoutParams = new LinearLayout.LayoutParams(rect.width(), rect.height() - dimension);
                layoutParams.setMargins(rect.left, rect.top + dimension, 0, 0);
            } else {
                layoutParams = new LinearLayout.LayoutParams(-1, -1);
            }
            this.mPhysiJNIView.setLayoutParams(layoutParams);
        }
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        updateADS();
        checkQualityPref();
        checkLanguage();
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREF_NAME_SETTINGS, 0);
        function(83, Integer.parseInt(sharedPreferences.getString(getString(C0354R.string.pref_key_3d_default_type), getString(C0354R.string.pref_key_3d_default_type_default_value))));
        function(89, Integer.parseInt(sharedPreferences.getString(getString(C0354R.string.pref_key_shading_level), getString(C0354R.string.pref_key_shading_level_default_value))));
        String str = "0";
        function(91, Integer.parseInt(sharedPreferences.getString(getString(C0354R.string.pref_key_2d_touch), str)));
        function(92, Integer.parseInt(sharedPreferences.getString(getString(C0354R.string.pref_key_3d_touch), str)));
        function(93, Integer.parseInt(sharedPreferences.getString(getString(C0354R.string.pref_key_sync_touch), "1")));
    }

    private void checkLanguage() {
        boolean equals = "1".equals(getSharedPreferences(Constants.PREF_NAME_SETTINGS, 0).getString(Constants.PREF_KEY_ENGLISH_ONLY, "0"));
        if (this.mIsEnglishOnly != equals) {
            this.mIsEnglishOnly = equals;
            restartActivity();
        }
    }

    private void checkQualityPref() {
        set3DQuality(Integer.parseInt(getSharedPreferences(Constants.PREF_NAME_SETTINGS, 0).getString(Constants.PREF_3D_QUALITY, "1")));
    }

    private void set3DQuality(int i) {
        if (this.m3DQuality != i) {
            this.m3DQuality = i;
            int i2 = this.m3DQuality;
            if (i2 == 0) {
                m3DResample = 3.0f;
            } else if (i2 == 1) {
                m3DResample = 2.0f;
            } else {
                m3DResample = 1.0f;
            }
            function(81, this.m3DQuality);
            this.mPhysiJNIView.setUI(0, true);
        }
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        IabHelper iabHelper = this.mHelper;
        if (iabHelper != null) {
            iabHelper.dispose();
            this.mHelper = null;
        }
        System.exit(0);
    }

    public void onExportFile(final FileType fileType) {
        final int i;
        StringBuilder sb = new StringBuilder();
        sb.append(Environment.getExternalStorageDirectory().getAbsolutePath());
        sb.append("/DroidRender/");
        String sb2 = sb.toString();
        int i2 = C032422.$SwitchMap$com$luolai$droidrender$Constants$FileType[fileType.ordinal()];
        final int i3 = 0;
        String str = "";
        if (i2 == 1) {
            StringBuilder sb3 = new StringBuilder();
            sb3.append(sb2);
            sb3.append("BCD");
            sb2 = sb3.toString();
            i3 = C0354R.string.export_bcd_success;
            i = C0354R.string.export_bcd_fail;
            str = ".bcd";
        } else if (i2 == 2) {
            StringBuilder sb4 = new StringBuilder();
            sb4.append(sb2);
            sb4.append("VRCM");
            sb2 = sb4.toString();
            i3 = C0354R.string.export_vrcm_success;
            i = C0354R.string.export_vrcm_fail;
            str = ".vrcm";
        } else if (i2 == 3) {
            StringBuilder sb5 = new StringBuilder();
            sb5.append(sb2);
            sb5.append("STL");
            sb2 = sb5.toString();
            i3 = C0354R.string.export_stl_success;
            i = C0354R.string.export_stl_fail;
            str = ".stl";
        } else if (i2 != 4) {
            i = 0;
        } else {
            i3 = C0354R.string.export_snapshot_success;
            i = C0354R.string.export_snapshot_fail;
        }
        C030911 r2 = new FileSaverCallback() {
            public void onFileSaved(final String str) {
                new AsyncTask<Void, Void, Boolean>() {
                    /* access modifiers changed from: protected */
                    public void onPreExecute() {
                        PhysiJNIView.showRingDialog((Context) MainActivity.this, true, (int) C0354R.string.save_file_progress_title, 0);
                    }

                    /* access modifiers changed from: protected */
                    public Boolean doInBackground(Void... voidArr) {
                        return Boolean.valueOf(MainActivity.exportFile(str, fileType.ordinal()));
                    }

                    /* access modifiers changed from: protected */
                    public void onPostExecute(Boolean bool) {
                        int i = i3;
                        if (!bool.booleanValue()) {
                            i = i;
                        }
                        MainActivity.this.mPhysiJNIView.showToast(MainActivity.this.getString(i, new Object[]{str}));
                        PhysiJNIView.showRingDialog((Context) MainActivity.this, false, 0, 0);
                    }
                }.execute(new Void[0]);
            }
        };
        if (fileType == FileType.snapshot) {
            StringBuilder sb6 = new StringBuilder();
            sb6.append(getFilesDir().getAbsolutePath());
            sb6.append("/snapshot.bcd");
            r2.onFileSaved(sb6.toString());
            return;
        }
        DialogHelper.CreateFileSaveDialog(this, null, str, sb2, r2);
    }

    /* access modifiers changed from: protected */
    public void onNewIntent(Intent intent) {
        String str = ".zip";
        if (intent != null) {
            if (!"android.intent.action.VIEW".equals(intent.getAction())) {
                if (!Constants.ACTION_LOAD_FILE.equals(intent.getAction())) {
                    if (!Constants.ACTION_EDIT_LAYOUT.equals(intent.getAction())) {
                        if (Constants.ACTION_LOAD_IMAGE.equals(intent.getAction())) {
                            Bundle extras = intent.getExtras();
                            if (extras != null) {
                                String str2 = Constants.EXTRA_IMAGE_LIST;
                                if (extras.containsKey(str2)) {
                                    postInitCDibWhenLayoutReady(extras.getIntArray(str2));
                                }
                            }
                        } else {
                            if (Constants.ACTION_SELECT_PATIENT.equals(intent.getAction())) {
                                Bundle extras2 = intent.getExtras();
                                if (extras2 != null) {
                                    String str3 = Constants.EXTRA_PATIENT_ORDER;
                                    if (extras2.containsKey(str3)) {
                                        function(80, extras2.getInt(str3));
                                    }
                                }
                            }
                        }
                    } else if (getValueInt(3) != 0 && this.mPhysiJNIView.mUiType >= 4) {
                        this.mPhysiJNIView.setUI(0, true);
                    }
                } else if (intent.getExtras() != null) {
                    onActivityResult(intent.getExtras().getInt(Constants.EXTRA_REQUEST_CODE, 0), -1, intent);
                }
            } else if (intent.getData() == null) {
                finish();
                return;
            } else {
                if ("file".equals(intent.getData().getScheme())) {
                    String path = intent.getData().getPath();
                    if (!TextUtils.isEmpty(path)) {
                        FilePicker.returnDir(this, path, 1, false);
                    }
                } else {
                    if (Param.CONTENT.equals(intent.getData().getScheme())) {
                        try {
                            InputStream openInputStream = getContentResolver().openInputStream(intent.getData());
                            File file = new File(Constants.getFolderPath(this, FileType.content));
                            if (!file.exists() || file.isDirectory()) {
                                file.mkdirs();
                            }
                            StringBuilder sb = new StringBuilder();
                            sb.append(file.getAbsolutePath());
                            sb.append(File.separator);
                            sb.append(intent.getData().getLastPathSegment());
                            String sb2 = sb.toString();
                            if (intent.getType() != null && intent.getType().contains("zip") && !sb2.endsWith(str)) {
                                StringBuilder sb3 = new StringBuilder();
                                sb3.append(sb2);
                                sb3.append(str);
                                sb2 = sb3.toString();
                            }
                            File file2 = new File(sb2);
                            FileOutputStream fileOutputStream = new FileOutputStream(file2);
                            byte[] bArr = new byte[1024];
                            while (true) {
                                int read = openInputStream.read(bArr);
                                if (read < 0) {
                                    break;
                                }
                                fileOutputStream.write(bArr, 0, read);
                            }
                            openInputStream.close();
                            fileOutputStream.close();
                            FilePicker.returnDir(this, file2.getAbsolutePath(), 1, false);
                        } catch (IOException e) {
                            StringBuilder sb4 = new StringBuilder();
                            sb4.append("Can't read from uri ");
                            sb4.append(intent.getData());
                            Log.m51w(TAG, sb4.toString(), e);
                        }
                    }
                }
            }
        }
        super.onNewIntent(intent);
    }

    /* access modifiers changed from: private */
    public void postInitCDibWhenLayoutReady(int[] iArr) {
        if (iArr == null) {
            return;
        }
        if (this.mIsLayoutReady) {
            postInitCDib(this, iArr);
        } else {
            this.mPendingImageList = iArr;
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(C0354R.menu.activity_main, menu);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean z = false;
        boolean z2 = !this.goJNI || getValueInt(3) != 0;
        menu.findItem(C0354R.C0356id.menu_ui_set).setVisible(z2);
        menu.findItem(C0354R.C0356id.menu_tools).setVisible(getValueInt(7) != 0);
        MenuItem findItem = menu.findItem(C0354R.C0356id.menu_tree);
        findItem.setVisible(z2);
        findItem.setTitle(getValueInt(6) != 0 ? C0354R.string.menu_hide_tree : C0354R.string.menu_show_tree);
        MenuItem findItem2 = menu.findItem(C0354R.C0356id.menu_fullscreen);
        if (VERSION.SDK_INT >= 19) {
            z = true;
        }
        findItem2.setVisible(z);
        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        boolean z = false;
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREF_NAME_SETTINGS, 0);
        int itemId = menuItem.getItemId();
        String str = Constants.EXTRA_FROM_MAINACTIVITY;
        switch (itemId) {
            case C0354R.C0356id.menu_edit_layout /*2131165250*/:
                Intent intent = new Intent();
                intent.setClass(this, LayoutEditorActivity.class);
                if (this.mPhysiJNIView.mUiType >= 4) {
                    intent.putExtra(LayoutEditorActivity.KEY_INITIAL_LAYOUT, this.mPhysiJNIView.mUiType - 4);
                }
                intent.putExtra(str, true);
                startActivity(intent);
                break;
            case C0354R.C0356id.menu_fullscreen /*2131165251*/:
                hideSystemUI();
                break;
            case C0354R.C0356id.menu_load_bcd /*2131165252*/:
                goToLandingPage(this);
                return true;
            case C0354R.C0356id.menu_settings /*2131165255*/:
                Intent intent2 = new Intent();
                intent2.setClass(this, PGPreferenceActivity.class);
                intent2.putExtra(str, true);
                startActivity(intent2);
                break;
            case C0354R.C0356id.menu_tools /*2131165256*/:
                function(86, 0);
                return true;
            case C0354R.C0356id.menu_tree /*2131165257*/:
                int i = getValueInt(6) != 0 ? 0 : 1;
                function(82, i);
                Editor edit = sharedPreferences.edit();
                if (i == 1) {
                    z = true;
                }
                edit.putBoolean(Constants.PREF_CONTROL_TREE_STATUS, z).apply();
                GAHelper.sendTracker(this, TAG, "Show tree", "", (long) i);
                break;
            case C0354R.C0356id.menu_ui_set /*2131165258*/:
                Builder builder = new Builder(this);
                builder.setSingleChoiceItems(C0354R.array.ui_sets, this.mPhysiJNIView.mUiType, new OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MainActivity.this.mPhysiJNIView.setUI(i, false);
                        dialogInterface.dismiss();
                        GAHelper.sendTracker(MainActivity.this, MainActivity.TAG, "Set layout", String.format("%d", new Object[]{Integer.valueOf(i)}), (long) i);
                    }
                });
                builder.create().show();
                return true;
            case C0354R.C0356id.menu_video_tutorial /*2131165259*/:
                createVideoTutoralDlg(this);
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void setVRThreshold() {
        if (!this.goJNI || getValueInt(3) != 0) {
            final MyDialog myDialog = new MyDialog(this);
            View inflate = ((LayoutInflater) getSystemService("layout_inflater")).inflate(C0354R.layout.layout_vr_adjust, (ViewGroup) findViewById(C0354R.C0356id.root_view));
            final TextView textView = (TextView) inflate.findViewById(C0354R.C0356id.text);
            final SeekBar seekBar = (SeekBar) inflate.findViewById(C0354R.C0356id.seekbar);
            final int valueInt = getValueInt(1);
            int valueInt2 = getValueInt(2);
            int valueInt3 = getValueInt(4);
            seekBar.setMax(valueInt2 - valueInt);
            C031213 r5 = new OnSeekBarChangeListener() {
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                public void onStopTrackingTouch(SeekBar seekBar) {
                }

                public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                    textView.setText(String.format("%d", new Object[]{Integer.valueOf(i + valueInt)}));
                }
            };
            myDialog.setButton(-1, "Apply change", new OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (MainActivity.this.goJNI) {
                        MainActivity.function(71, seekBar.getProgress() + valueInt);
                    }
                    myDialog.dismiss();
                }
            });
            myDialog.setButton(-2, "Cancel", new OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            seekBar.setOnSeekBarChangeListener(r5);
            seekBar.setProgress(valueInt3 - valueInt);
            myDialog.setTitle(C0354R.string.vr_threshold);
            myDialog.setView(inflate);
            myDialog.show();
        }
    }

    private void setVRType() {
        Builder builder = new Builder(this);
        builder.setTitle(C0354R.string.vr_type);
        builder.setItems(new CharSequence[]{"Basic", "Volume shading", "Volume rendering"}, new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                int i2 = 2;
                if (i != 1) {
                    i2 = i != 2 ? 0 : 3;
                }
                if (MainActivity.this.goJNI) {
                    MainActivity.function(70, i2);
                }
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    private void setLogger() {
        Log.setLogger(new Logger() {
            public void log(int i, String str, String str2) {
                if (i == 2) {
                    android.util.Log.i(str, str2);
                } else if (i == 3) {
                    android.util.Log.w(str, str2);
                } else if (i != 4) {
                    android.util.Log.d(str, str2);
                } else {
                    android.util.Log.e(str, str2);
                }
            }

            public void log(int i, String str, String str2, Throwable th) {
                if (i == 2) {
                    android.util.Log.i(str, str2, th);
                } else if (i == 3) {
                    android.util.Log.w(str, str2, th);
                } else if (i != 4) {
                    android.util.Log.d(str, str2, th);
                } else {
                    android.util.Log.e(str, str2, th);
                }
            }
        });
    }

    private void loadFileFromDir(File file, ArrayList<String> arrayList) {
        if (file != null && file.isDirectory()) {
            File[] listFiles = file.listFiles();
            if (listFiles != null && listFiles.length > 0) {
                for (int i = 0; i < listFiles.length; i++) {
                    if (listFiles[i] != null && listFiles[i].canRead() && listFiles[i].isFile()) {
                        arrayList.add(listFiles[i].getAbsolutePath());
                    } else if (listFiles[i] != null && listFiles[i].canRead() && listFiles[i].isDirectory()) {
                        loadFileFromDir(listFiles[i], arrayList);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int i, int i2, Intent intent) {
        if (i2 == -1 && intent != null) {
            String str = FilePicker.CHOSEN_DIRECTORY;
            if (i == 2) {
                String stringExtra = intent.getStringExtra(str);
                if (this.goJNI) {
                    readVRCM(stringExtra);
                }
            } else if (i != 3) {
                if (i == 5) {
                    exportFile(intent.getStringExtra(str), FileType.bcd.ordinal());
                }
                super.onActivityResult(i, i2, intent);
            } else {
                String stringExtra2 = intent.getStringExtra(str);
                File file = new File(stringExtra2);
                String absolutePath = file.getAbsolutePath();
                if (!file.isDirectory() && file.getParent() != null) {
                    absolutePath = file.getParent();
                }
                getSharedPreferences(Constants.PREF_NAME_SETTINGS, 0).edit().putString(Constants.PREF_KEY_LAST_DIR_VRCM, absolutePath).apply();
                functions(32968, stringExtra2);
            }
        }
    }

    public void setLocale() {
        overwriteConfigurationLocale(this);
        this.mIsEnglishOnly = "1".equals(getSharedPreferences(Constants.PREF_NAME_SETTINGS, 0).getString(Constants.PREF_KEY_ENGLISH_ONLY, "0"));
    }

    private void restartActivity() {
        Toast.makeText(this, C0354R.string.toast_language_change, 0).show();
    }

    public void openSampleFile() {
        FilePicker.returnDir(this, createSampleFile(this), 1, false);
    }

    private void showActionBar() {
        View inflate = ((LayoutInflater) getSystemService("layout_inflater")).inflate(C0354R.layout.action_bar_with_confirm, null);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setCustomView(inflate, new ActionBar.LayoutParams(-1, -1));
        this.mConfirm = (ImageView) inflate.findViewById(C0354R.C0356id.confirm);
        this.mConfirm.setVisibility(8);
        this.mNextPatient = (ImageView) inflate.findViewById(C0354R.C0356id.back);
        this.mNextPatient.setVisibility(8);
        this.mNextPatient = (ImageView) inflate.findViewById(C0354R.C0356id.next);
        this.mNextPatient.setVisibility(8);
        this.mNextPatient.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                MainActivity.function(90, 1);
            }
        });
        this.mLastPatient = (ImageView) inflate.findViewById(C0354R.C0356id.last);
        this.mLastPatient.setVisibility(8);
        this.mLastPatient.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                MainActivity.function(90, 0);
            }
        });
        this.mTitle = (TextView) inflate.findViewById(C0354R.C0356id.path);
        this.mTitle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, ManagePatientActivity.class);
                intent.putExtra(Constants.EXTRA_FROM_MAINACTIVITY, true);
                intent.putExtra(ManagePatientActivity.EXTRA_USE_FOR, 1);
                MainActivity.this.startActivity(intent);
            }
        });
    }

    public void onPatientChanged(String str, int i, boolean z, boolean z2) {
        int i2 = 8;
        this.mLastPatient.setVisibility(z ? 0 : 8);
        ImageView imageView = this.mNextPatient;
        if (z2) {
            i2 = 0;
        }
        imageView.setVisibility(i2);
        SpannableString spannableString = new SpannableString(getString(C0354R.string.name_symble, new Object[]{str}));
        spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(), 0);
        this.mTitle.setText(spannableString);
        DialogHelper.createHintDialog(this, HintType.main, new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                if (MainActivity.getValueInt(8) != 0) {
                    DialogHelper.createHintDialog(MainActivity.this, HintType.working_area, new OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            MainActivity.menuFunction(32979);
                        }
                    }, new OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                }
            }
        });
    }

    public void onWindowFocusChanged(boolean z) {
        super.onWindowFocusChanged(z);
        if (z && this.mIsFullScreen) {
            hideSystemUI();
        }
        if (z) {
            this.mIsMenuShowing = false;
        }
    }

    private void hideSystemUI() {
        if (VERSION.SDK_INT >= 19) {
            this.mEnteredFullscreen = true;
            this.mPhysiJNIView.setSystemUiVisibility(5894);
            if (!this.mIsFullScreen) {
                Toast.makeText(this, C0354R.string.toast_leave_fullscreen, 0).show();
                this.mIsFullScreen = true;
            }
        }
    }

    private void showSystemUI() {
        if (VERSION.SDK_INT >= 19) {
            this.mPhysiJNIView.setSystemUiVisibility(1792);
        }
        this.mIsFullScreen = false;
    }

    public void onBackPressed() {
        if (this.mIsFullScreen) {
            showSystemUI();
        } else if (Calendar.getInstance().getTimeInMillis() - this.mLastBackTime > 5000) {
            Toast.makeText(this, C0354R.string.toast_leave_app, 0).show();
            this.mLastBackTime = Calendar.getInstance().getTimeInMillis();
        } else {
            super.onBackPressed();
        }
    }
}
