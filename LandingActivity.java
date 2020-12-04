package com.luolai.droidrender;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.documentfile.provider.DocumentFile;
import com.google.firebase.analytics.FirebaseAnalytics.Param;
import com.luolai.base.GAHelper;
import com.luolai.base.StorageUtil;
import java.io.File;
import java.util.ArrayList;

public class LandingActivity extends BaseActivity {
    /* access modifiers changed from: private */
    public static final String TAG = LandingActivity.class.getSimpleName();
    private ItemAdapter mItemAdapter;
    private BroadcastReceiver mMediaBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.MEDIA_MOUNTED".equals(action) || "android.hardware.usb.action.USB_DEVICE_ATTACHED".equals(action) || "android.hardware.usb.action.USB_ACCESSORY_ATTACHED".equals(action)) {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        LandingActivity.this.setUpListView();
                    }
                }, 5000);
            } else if ("android.intent.action.MEDIA_UNMOUNTED".equals(action) || "android.hardware.usb.action.USB_DEVICE_DETACHED".equals(action) || "android.hardware.usb.action.USB_ACCESSORY_DETACHED".equals(action)) {
                LandingActivity.this.setUpListView();
            }
        }
    };

    private static class ItemAdapter extends BaseAdapter implements OnItemClickListener {
        /* access modifiers changed from: private */
        public Context mContext;
        private ArrayList<ItemPack> mItems = new ArrayList<>(3);

        public long getItemId(int i) {
            return (long) i;
        }

        public ItemAdapter(Context context) {
            this.mContext = context;
            createItem(context);
        }

        private void createItem(final Context context) {
            this.mItems.add(new ItemPack(C0354R.string.landing_load_internal, C0354R.C0355drawable.ic_internal, new Runnable() {
                public void run() {
                    GAHelper.sendTracker(context, LandingActivity.TAG, "Click", "Internal storage", 0);
                    ((Activity) ItemAdapter.this.mContext).startActivityForResult(MainActivity.getFilePickerIntent(ItemAdapter.this.mContext, 1, false, null, null), 1);
                }
            }));
            final String sDCardPath = StorageUtil.getSDCardPath(this.mContext);
            if (!TextUtils.isEmpty(sDCardPath)) {
                File file = new File(sDCardPath);
                if (file.exists() && file.isDirectory() && file.canRead()) {
                    this.mItems.add(new ItemPack(C0354R.string.landing_load_sd, C0354R.C0355drawable.ic_sd, new Runnable() {
                        public void run() {
                            GAHelper.sendTracker(context, LandingActivity.TAG, "Click", "External", 0);
                            ((Activity) ItemAdapter.this.mContext).startActivityForResult(MainActivity.getFilePickerIntent(ItemAdapter.this.mContext, 1, false, null, sDCardPath), 1);
                        }
                    }));
                }
            }
            this.mItems.add(new ItemPack(C0354R.string.landing_load_sample, C0354R.C0355drawable.ic_sample, new Runnable() {
                public void run() {
                    GAHelper.sendTracker(context, LandingActivity.TAG, "Click", "Load sample", 0);
                    Intent intent = new Intent();
                    intent.setClass(ItemAdapter.this.mContext, ManagePatientActivity.class);
                    intent.putExtra(ManagePatientActivity.EXTRA_USE_FOR, 4);
                    ((Activity) ItemAdapter.this.mContext).startActivityForResult(intent, 1);
                }
            }));
            StringBuilder sb = new StringBuilder();
            sb.append(context.getFilesDir().getAbsolutePath());
            sb.append("/snapshot.bcd");
            final File file2 = new File(sb.toString());
            if (file2.exists() && file2.isFile() && file2.canRead()) {
                this.mItems.add(new ItemPack(C0354R.string.import_snapshot, C0354R.C0355drawable.ic_recent, new Runnable() {
                    public void run() {
                        GAHelper.sendTracker(context, LandingActivity.TAG, "Click", "Load snapshot", 0);
                        Intent intent = new Intent();
                        intent.setClass(ItemAdapter.this.mContext, ManagePatientActivity.class);
                        intent.putExtra(FilePicker.CHOSEN_DIRECTORY, file2.getAbsolutePath());
                        intent.putExtra(ManagePatientActivity.EXTRA_USE_FOR, 0);
                        ((Activity) ItemAdapter.this.mContext).startActivityForResult(intent, 1);
                    }
                }));
            }
            StringBuilder sb2 = new StringBuilder();
            sb2.append(Constants.getAppDataPath(this.mContext));
            sb2.append(Constants.RECENT_FILE_FOLDER_PATH);
            if (new File(sb2.toString()).listFiles().length > 0) {
                this.mItems.add(new ItemPack(C0354R.string.landing_load_recent, C0354R.C0355drawable.ic_recent, new Runnable() {
                    public void run() {
                        GAHelper.sendTracker(context, LandingActivity.TAG, "Click", "Recent image", 0);
                        Intent intent = new Intent();
                        intent.setClass(ItemAdapter.this.mContext, ManagePatientActivity.class);
                        intent.putExtra(ManagePatientActivity.EXTRA_USE_FOR, 3);
                        ((Activity) ItemAdapter.this.mContext).startActivityForResult(intent, 1);
                    }
                }));
            }
            if (context.getPackageManager().queryIntentActivities(getSAFIntent(), 0).size() > 0 && VERSION.SDK_INT >= 19) {
                this.mItems.add(new ItemPack(C0354R.string.landing_load_cloud, C0354R.C0355drawable.ic_cloud, new Runnable() {
                    public void run() {
                        GAHelper.sendTracker(context, LandingActivity.TAG, "Click", "Load from cloud", 0);
                        DialogHelper.createHintDialog(ItemAdapter.this.mContext, HintType.saf, new OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ((Activity) ItemAdapter.this.mContext).startActivityForResult(ItemAdapter.this.getSAFIntent(), 6);
                            }
                        });
                    }
                }));
            }
            this.mItems.add(new ItemPack(C0354R.string.menu_settings, C0354R.C0355drawable.ic_setting, new Runnable() {
                public void run() {
                    GAHelper.sendTracker(context, LandingActivity.TAG, "Click", "Settings", 0);
                    Intent intent = new Intent();
                    intent.setClass(ItemAdapter.this.mContext, PGPreferenceActivity.class);
                    ItemAdapter.this.mContext.startActivity(intent);
                }
            }));
            this.mItems.add(new ItemPack(C0354R.string.menu_video_tutorial, C0354R.C0355drawable.ic_video, new Runnable() {
                public void run() {
                    MainActivity.createVideoTutoralDlg(ItemAdapter.this.mContext);
                }
            }));
        }

        /* access modifiers changed from: private */
        public Intent getSAFIntent() {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.GET_CONTENT");
            intent.addCategory("android.intent.category.OPENABLE");
            if (VERSION.SDK_INT >= 18) {
                intent.putExtra("android.intent.extra.ALLOW_MULTIPLE", true);
            }
            intent.setType("*/*");
            return intent;
        }

        public int getCount() {
            return this.mItems.size();
        }

        public Object getItem(int i) {
            if (i < 0 || i >= getCount()) {
                return null;
            }
            return this.mItems.get(i);
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = View.inflate(this.mContext, C0354R.layout.list_item_text_icon, null);
            }
            ItemPack itemPack = (ItemPack) getItem(i);
            if (itemPack != null) {
                ((TextView) view.findViewById(C0354R.C0356id.text)).setText(itemPack.mStringResID);
                ((ImageView) view.findViewById(C0354R.C0356id.image)).setImageResource(itemPack.mIconResID);
            }
            return view;
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
            ItemPack itemPack = (ItemPack) getItem(i);
            if (itemPack != null) {
                Context context = this.mContext;
                if (context instanceof LandingActivity) {
                    ((LandingActivity) context).runOnUiThread(itemPack.mAction);
                }
            }
        }
    }

    private static class ItemPack {
        public Runnable mAction;
        public int mIconResID;
        public int mStringResID;

        public ItemPack(int i, int i2, Runnable runnable) {
            this.mStringResID = i;
            this.mIconResID = i2;
            this.mAction = runnable;
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    public static long folderSize(File file) {
        File[] listFiles;
        long j;
        long j2 = 0;
        for (File file2 : file.listFiles()) {
            if (file2.isFile()) {
                j = file2.length();
            } else {
                j = folderSize(file2);
            }
            j2 += j;
        }
        return j2;
    }

    public static void clearFolder(File file) {
        File[] listFiles;
        for (File file2 : file.listFiles()) {
            if (file2.isFile()) {
                file2.delete();
            } else {
                clearFolder(file2);
                file2.delete();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        PermissionUtils.checkPermission(this);
        MainActivity.createSampleFile(this);
        setContentView(C0354R.layout.activity_landing);
        setTitle(C0354R.string.landing_title);
        GAHelper.recordScreen(this, TAG);
        Intent intent = getIntent();
        int i = getSharedPreferences(Constants.PREF_NAME_SETTINGS, 0).getInt(Constants.PREF_KEY_CLEAR_CACHE_THRESHOLD, 30);
        if (intent != null) {
            if ("android.intent.action.MAIN".equals(intent.getAction()) && getCachedSize() > ((float) i)) {
                createClearCacheDialog(true);
                registerStandardMediaChanged();
            }
        }
        MainActivity.whatsNew(this);
        registerStandardMediaChanged();
    }

    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        PermissionUtils.onRequestPermissionsResult(this, i, strArr, iArr);
    }

    /* access modifiers changed from: private */
    public void setUpListView() {
        ListView listView = (ListView) findViewById(C0354R.C0356id.list);
        this.mItemAdapter = new ItemAdapter(this);
        listView.setAdapter(this.mItemAdapter);
        listView.setOnItemClickListener(this.mItemAdapter);
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        MainActivity.function(88, Integer.parseInt(getSharedPreferences(Constants.PREF_NAME_SETTINGS, 0).getString(getString(C0354R.string.pref_key_dicom_library), getString(C0354R.string.pref_key_dicom_library_default_value))));
        setUpListView();
        super.onResume();
    }

    private String getFileName(Uri uri) {
        String scheme = uri.getScheme();
        String str = null;
        if (scheme.equals("file")) {
            return uri.getLastPathSegment();
        }
        if (!scheme.equals(Param.CONTENT)) {
            return null;
        }
        String str2 = "_display_name";
        Cursor query = getContentResolver().query(uri, new String[]{str2}, null, null, null);
        if (!(query == null || query.getCount() == 0)) {
            int columnIndexOrThrow = query.getColumnIndexOrThrow(str2);
            query.moveToFirst();
            str = query.getString(columnIndexOrThrow);
        }
        if (query == null) {
            return str;
        }
        query.close();
        return str;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0076 A[SYNTHETIC, Splitter:B:27:0x0076] */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0081 A[SYNTHETIC, Splitter:B:34:0x0081] */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x008e A[SYNTHETIC, Splitter:B:42:0x008e] */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:24:0x0071=Splitter:B:24:0x0071, B:31:0x007c=Splitter:B:31:0x007c} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String saveSingleUri(android.net.Uri r6, long r7) {
        /*
            r5 = this;
            java.lang.String r0 = r6.getScheme()
            java.lang.String r1 = "file"
            boolean r0 = r1.equals(r0)
            if (r0 == 0) goto L_0x0011
            java.lang.String r6 = r6.getPath()
            return r6
        L_0x0011:
            com.luolai.droidrender.Constants$FileType r0 = com.luolai.droidrender.Constants.FileType.cloud
            java.lang.String r0 = com.luolai.droidrender.Constants.getFolderPath(r5, r0)
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r0)
            r1.append(r7)
            java.lang.String r7 = "-"
            r1.append(r7)
            java.lang.String r7 = r5.getFileName(r6)
            r1.append(r7)
            java.lang.String r7 = r1.toString()
            java.io.File r8 = new java.io.File
            r8.<init>(r7)
            r7 = 0
            android.content.ContentResolver r0 = r5.getContentResolver()     // Catch:{ FileNotFoundException -> 0x007a, IOException -> 0x006f, all -> 0x006d }
            java.io.InputStream r6 = r0.openInputStream(r6)     // Catch:{ FileNotFoundException -> 0x007a, IOException -> 0x006f, all -> 0x006d }
            r8.createNewFile()     // Catch:{ FileNotFoundException -> 0x007a, IOException -> 0x006f, all -> 0x006d }
            java.io.BufferedOutputStream r0 = new java.io.BufferedOutputStream     // Catch:{ FileNotFoundException -> 0x007a, IOException -> 0x006f, all -> 0x006d }
            java.io.FileOutputStream r1 = new java.io.FileOutputStream     // Catch:{ FileNotFoundException -> 0x007a, IOException -> 0x006f, all -> 0x006d }
            r1.<init>(r8)     // Catch:{ FileNotFoundException -> 0x007a, IOException -> 0x006f, all -> 0x006d }
            r0.<init>(r1)     // Catch:{ FileNotFoundException -> 0x007a, IOException -> 0x006f, all -> 0x006d }
            r1 = 4096(0x1000, float:5.74E-42)
            byte[] r2 = new byte[r1]     // Catch:{ FileNotFoundException -> 0x006b, IOException -> 0x0069 }
        L_0x0051:
            r3 = 0
            int r4 = r6.read(r2, r3, r1)     // Catch:{ FileNotFoundException -> 0x006b, IOException -> 0x0069 }
            if (r4 <= 0) goto L_0x005c
            r0.write(r2, r3, r4)     // Catch:{ FileNotFoundException -> 0x006b, IOException -> 0x0069 }
            goto L_0x0051
        L_0x005c:
            java.lang.String r6 = r8.getAbsolutePath()     // Catch:{ FileNotFoundException -> 0x006b, IOException -> 0x0069 }
            r0.close()     // Catch:{ IOException -> 0x0064 }
            goto L_0x0068
        L_0x0064:
            r7 = move-exception
            r7.printStackTrace()
        L_0x0068:
            return r6
        L_0x0069:
            r6 = move-exception
            goto L_0x0071
        L_0x006b:
            r6 = move-exception
            goto L_0x007c
        L_0x006d:
            r6 = move-exception
            goto L_0x008c
        L_0x006f:
            r6 = move-exception
            r0 = r7
        L_0x0071:
            r6.printStackTrace()     // Catch:{ all -> 0x008a }
            if (r0 == 0) goto L_0x0089
            r0.close()     // Catch:{ IOException -> 0x0085 }
            goto L_0x0089
        L_0x007a:
            r6 = move-exception
            r0 = r7
        L_0x007c:
            r6.printStackTrace()     // Catch:{ all -> 0x008a }
            if (r0 == 0) goto L_0x0089
            r0.close()     // Catch:{ IOException -> 0x0085 }
            goto L_0x0089
        L_0x0085:
            r6 = move-exception
            r6.printStackTrace()
        L_0x0089:
            return r7
        L_0x008a:
            r6 = move-exception
            r7 = r0
        L_0x008c:
            if (r7 == 0) goto L_0x0096
            r7.close()     // Catch:{ IOException -> 0x0092 }
            goto L_0x0096
        L_0x0092:
            r7 = move-exception
            r7.printStackTrace()
        L_0x0096:
            goto L_0x0098
        L_0x0097:
            throw r6
        L_0x0098:
            goto L_0x0097
        */
        throw new UnsupportedOperationException("Method not decompiled: com.luolai.droidrender.LandingActivity.saveSingleUri(android.net.Uri, long):java.lang.String");
    }

    private void asyncSaveFiles(final Uri[] uriArr) {
        if (uriArr != null && uriArr.length > 0) {
            File file = new File(Constants.getFolderPath(this, FileType.cloud));
            if (!file.exists()) {
                file.mkdirs();
            }
            final long currentTimeMillis = System.currentTimeMillis();
            new AsyncTask<Void, Void, String[]>() {
                /* access modifiers changed from: protected */
                public void onPreExecute() {
                    PhysiJNIView.showRingDialog((Context) LandingActivity.this, true, (int) C0354R.string.message_load_from_other, 0);
                }

                /* access modifiers changed from: protected */
                public String[] doInBackground(Void... voidArr) {
                    ArrayList arrayList = new ArrayList();
                    for (Uri access$100 : uriArr) {
                        String access$1002 = LandingActivity.this.saveSingleUri(access$100, currentTimeMillis);
                        if (!TextUtils.isEmpty(access$1002)) {
                            arrayList.add(access$1002);
                        }
                    }
                    return (String[]) arrayList.toArray(new String[arrayList.size()]);
                }

                /* access modifiers changed from: protected */
                public void onPostExecute(String[] strArr) {
                    PhysiJNIView.showRingDialog((Context) LandingActivity.this, false, 0, 0);
                    if (strArr != null && strArr.length > 0) {
                        Intent intent = new Intent();
                        intent.putExtra(FilePicker.CHOSEN_DIRECTORIES, strArr);
                        intent.setClass(LandingActivity.this, ManagePatientActivity.class);
                        intent.setAction(Constants.ACTION_LOAD_FILE);
                        intent.putExtra(ManagePatientActivity.EXTRA_FROM_CLOUD, true);
                        LandingActivity.this.startActivityForResult(intent, 1);
                    }
                }
            }.execute(new Void[0]);
        }
    }

    /* access modifiers changed from: private */
    public float getCachedSize() {
        File file = new File(Constants.getFolderPath(this, FileType.cloud));
        if (file.exists()) {
            return ((float) folderSize(file)) / 1048576.0f;
        }
        return 0.0f;
    }

    private void createClearCacheDialog(final boolean z) {
        Builder builder = new Builder(this);
        builder.setTitle(C0354R.string.title_clear_cache);
        builder.setMessage(getString(C0354R.string.message_clear_cache, new Object[]{Float.valueOf(getCachedSize())}));
        builder.setNegativeButton(C0354R.string.f13no, new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                if (z) {
                    MainActivity.whatsNew(LandingActivity.this);
                    SharedPreferences sharedPreferences = LandingActivity.this.getSharedPreferences(Constants.PREF_NAME_SETTINGS, 0);
                    String str = Constants.PREF_KEY_CLEAR_CACHE_THRESHOLD;
                    int i2 = sharedPreferences.getInt(str, 30);
                    while (i2 <= ((int) LandingActivity.this.getCachedSize())) {
                        i2 += 20;
                    }
                    sharedPreferences.edit().putInt(str, i2).apply();
                }
            }
        });
        builder.setPositiveButton(17039370, new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                LandingActivity.this.clearCache();
                if (z) {
                    MainActivity.whatsNew(LandingActivity.this);
                    LandingActivity.this.getSharedPreferences(Constants.PREF_NAME_SETTINGS, 0).edit().putInt(Constants.PREF_KEY_CLEAR_CACHE_THRESHOLD, 30).apply();
                }
            }
        });
        AlertDialog create = builder.create();
        create.setCanceledOnTouchOutside(false);
        create.show();
    }

    /* access modifiers changed from: private */
    public void clearCache() {
        clearFolder(new File(Constants.getFolderPath(this, FileType.cloud)));
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int i, int i2, Intent intent) {
        if (i2 == -1) {
            if (i == 1 && intent.getExtras() != null) {
                Bundle extras = intent.getExtras();
                String str = Constants.EXTRA_IMAGE_LIST;
                if (extras.containsKey(str)) {
                    Intent intent2 = new Intent();
                    intent2.setClass(this, MainActivity.class);
                    intent2.setFlags(67108864);
                    intent2.setAction(Constants.ACTION_LOAD_IMAGE);
                    intent2.putExtra(str, intent.getExtras().getIntArray(str));
                    startActivity(intent2);
                    finish();
                    super.onActivityResult(i, i2, intent);
                }
            }
            if (i == 6) {
                ArrayList arrayList = new ArrayList();
                if (VERSION.SDK_INT < 18 || intent.getData() != null) {
                    arrayList.add(intent.getData());
                } else {
                    ClipData clipData = intent.getClipData();
                    if (clipData != null && clipData.getItemCount() > 0) {
                        for (int i3 = 0; i3 < clipData.getItemCount(); i3++) {
                            Uri uri = clipData.getItemAt(i3).getUri();
                            if (uri != null) {
                                arrayList.add(uri);
                            }
                        }
                    }
                }
                if (arrayList.size() > 0) {
                    asyncSaveFiles(expendAllFoldersFromUri((Uri[]) arrayList.toArray(new Uri[arrayList.size()])));
                }
            } else if (i == 7) {
                DocumentFile fromTreeUri = DocumentFile.fromTreeUri(this, intent.getData());
                ArrayList arrayList2 = new ArrayList();
                loadFromDocFile(fromTreeUri, arrayList2);
                if (arrayList2.size() > 0) {
                    asyncSaveFiles((Uri[]) arrayList2.toArray(new Uri[arrayList2.size()]));
                }
            }
            super.onActivityResult(i, i2, intent);
        }
    }

    private Uri[] expendAllFoldersFromUri(Uri[] uriArr) {
        ArrayList arrayList = new ArrayList();
        for (Uri uri : uriArr) {
            String scheme = uri.getScheme();
            if ("file".equals(scheme)) {
                arrayList.add(uri);
            } else if (Param.CONTENT.equals(scheme)) {
                DocumentFile fromSingleUri = DocumentFile.fromSingleUri(this, uri);
                if (fromSingleUri != null && fromSingleUri.canRead() && !fromSingleUri.isDirectory()) {
                    arrayList.add(uri);
                }
            }
        }
        if (arrayList.size() > 0) {
            return (Uri[]) arrayList.toArray(new Uri[arrayList.size()]);
        }
        return null;
    }

    private void loadFromDocFile(DocumentFile documentFile, ArrayList<Uri> arrayList) {
        DocumentFile[] listFiles;
        for (DocumentFile documentFile2 : documentFile.listFiles()) {
            if (documentFile2.canRead()) {
                if (documentFile2.isDirectory()) {
                    loadFromDocFile(documentFile2, arrayList);
                } else {
                    Uri uri = documentFile2.getUri();
                    if (uri != null) {
                        arrayList.add(uri);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        if (getIntent() != null && getIntent().getBooleanExtra(Constants.EXTRA_FROM_MAINACTIVITY, false)) {
            MainActivity.gotoMainActivity(this);
        }
        BroadcastReceiver broadcastReceiver = this.mMediaBroadcastReceiver;
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
        super.onDestroy();
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == C0354R.C0356id.menu_clear_cache) {
            createClearCacheDialog(false);
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void requestOpenDirectory(int i) {
        startActivityForResult(new Intent("android.intent.action.OPEN_DOCUMENT_TREE"), i);
    }

    public void registerStandardMediaChanged() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.MEDIA_MOUNTED");
        intentFilter.addAction("android.intent.action.MEDIA_UNMOUNTED");
        intentFilter.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED");
        intentFilter.addAction("android.hardware.usb.action.USB_DEVICE_DETACHED");
        intentFilter.addAction("android.hardware.usb.action.USB_ACCESSORY_ATTACHED");
        intentFilter.addAction("android.hardware.usb.action.USB_ACCESSORY_DETACHED");
        registerReceiver(this.mMediaBroadcastReceiver, intentFilter);
    }
}
