package com.luolai.droidrender;

import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TouchDelegate;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.luolai.base.GAHelper;
import com.luolai.base.StorageUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class FilePicker extends BaseActivity {
    public static final String CHOSEN_DIRECTORIES = "chosenDirs";
    public static final String CHOSEN_DIRECTORY = "chosenDir";
    public static final String FIRST_DLG = "firstdlg";
    public static final String IS_SD = "is_sd";
    public static final String ONLY_DIRS = "onlyDirs";
    public static final int PICK_DIRECTORY = 1;
    public static final String SAMPLE_PATH = "https://www.dropbox.com/sh/gtg8dd4cknlkuoq/AADyAftu-32fUJemMnyNUm4ga?dl=0";
    public static final String SHOW_HIDDEN = "showHidden";
    public static final String START_DIR = "startDir";
    private static final String TAG = FilePicker.class.getSimpleName();
    public static final String WITH_SUB_NAME = "with_sub";
    public static final String WITH_URI = "with_uri";
    /* access modifiers changed from: private */
    public File dir;
    private ImageView mBack;
    /* access modifiers changed from: private */
    public CheckBoxAdapter mCheckBoxAdapter;
    private ImageView mConfirm;
    private boolean mFirstDlg = false;
    boolean mIsSD = false;
    private ListView mListView;
    /* access modifiers changed from: private */
    public int mRequestCode;
    private TextView mTitle;
    private Toast mToast = null;
    /* access modifiers changed from: private */
    public String mWithSub = "";
    /* access modifiers changed from: private */
    public boolean onlyDirs = false;
    /* access modifiers changed from: private */
    public boolean showHidden = false;

    private static class CheckBoxAdapter extends BaseAdapter implements OnItemClickListener {
        private FilePicker mFilePicker;
        private ArrayList<SingleItem> mItems = new ArrayList<>();
        private int mSamllIconsize = 0;

        public long getItemId(int i) {
            return 0;
        }

        public CheckBoxAdapter(FilePicker filePicker, File file) {
            this.mFilePicker = filePicker;
            ArrayList filter = FilePicker.filter(file.listFiles(), filePicker.onlyDirs, filePicker.showHidden, filePicker.onlyDirs, filePicker.mWithSub);
            if (filter.size() > 0) {
                Iterator it = filter.iterator();
                while (it.hasNext()) {
                    File file2 = (File) it.next();
                    if (file2.exists() && file2.canRead() && file2.isDirectory()) {
                        this.mItems.add(new SingleItem(file2));
                    }
                }
                Iterator it2 = filter.iterator();
                while (it2.hasNext()) {
                    File file3 = (File) it2.next();
                    if (file3.exists() && file3.canRead() && !file3.isDirectory()) {
                        this.mItems.add(new SingleItem(file3));
                    }
                }
            }
            this.mSamllIconsize = filePicker.getResources().getDimensionPixelSize(C0354R.dimen.small_icon_size);
        }

        public void selectAll() {
            if (this.mItems.size() > 0) {
                Iterator it = this.mItems.iterator();
                while (it.hasNext()) {
                    ((SingleItem) it.next()).mIsChecked = true;
                }
                onSelectionChanged();
                notifyDataSetChanged();
            }
        }

        public void deselectAll() {
            if (this.mItems.size() > 0) {
                Iterator it = this.mItems.iterator();
                while (it.hasNext()) {
                    ((SingleItem) it.next()).mIsChecked = false;
                }
                onSelectionChanged();
                notifyDataSetChanged();
            }
        }

        public String[] getSelectedFilePathes() {
            if (this.mItems.size() > 0) {
                ArrayList arrayList = new ArrayList();
                Iterator it = this.mItems.iterator();
                while (it.hasNext()) {
                    SingleItem singleItem = (SingleItem) it.next();
                    if (singleItem.mIsChecked) {
                        arrayList.add(singleItem.mFile.getAbsolutePath());
                    }
                }
                if (arrayList.size() > 0) {
                    return (String[]) arrayList.toArray(new String[arrayList.size()]);
                }
            }
            return null;
        }

        /* access modifiers changed from: private */
        public void onSelectionChanged() {
            if (this.mItems.size() > 0) {
                int i = 0;
                Iterator it = this.mItems.iterator();
                while (it.hasNext()) {
                    if (((SingleItem) it.next()).mIsChecked) {
                        i++;
                    }
                }
                this.mFilePicker.OnSelectChange(i);
            }
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
                view = View.inflate(this.mFilePicker, C0354R.layout.list_item_text_checkbox, null);
            }
            final SingleItem singleItem = (SingleItem) getItem(i);
            if (singleItem != null) {
                final CheckBox checkBox = (CheckBox) view.findViewById(C0354R.C0356id.checkbox);
                checkBox.setChecked(singleItem.mIsChecked);
                checkBox.setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        SingleItem singleItem = singleItem;
                        singleItem.mIsChecked = !singleItem.mIsChecked;
                        checkBox.setChecked(singleItem.mIsChecked);
                        CheckBoxAdapter.this.onSelectionChanged();
                    }
                });
                ((ImageView) view.findViewById(C0354R.C0356id.image)).setImageResource(singleItem.mFile.isDirectory() ? C0354R.C0355drawable.ic_folder : C0354R.C0355drawable.ic_file);
                ((TextView) view.findViewById(C0354R.C0356id.text)).setText(singleItem.mFile.getName());
                int i2 = this.mSamllIconsize;
                double d = (double) i2;
                Double.isNaN(d);
                view.setTouchDelegate(new TouchDelegate(new Rect(0, 0, (int) (d * 1.2d), i2 * 3), checkBox));
                ((TextView) view.findViewById(C0354R.C0356id.second)).setVisibility(8);
            }
            return view;
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
            SingleItem singleItem = (SingleItem) getItem(i);
            if (singleItem == null) {
                return;
            }
            if (singleItem.mFile.isDirectory()) {
                this.mFilePicker.goToDir(singleItem.mFile.getAbsolutePath());
            } else if (view != null) {
                ((CheckBox) view.findViewById(C0354R.C0356id.checkbox)).performClick();
            }
        }
    }

    private static class SingleItem {
        public File mFile;
        public boolean mIsChecked = false;

        SingleItem(File file) {
            this.mFile = file;
        }
    }

    public static void returnDir(Context context, String str, int i, boolean z) {
        Intent intent = new Intent();
        intent.putExtra(CHOSEN_DIRECTORY, str);
        if (z) {
            intent.putExtra(IS_SD, true);
        }
        goToNextActivity(context, intent, i);
    }

    public static void returnDirs(Context context, String[] strArr, String str, int i, boolean z) {
        Intent intent = new Intent();
        intent.putExtra(CHOSEN_DIRECTORY, str);
        intent.putExtra(CHOSEN_DIRECTORIES, strArr);
        if (z) {
            intent.putExtra(IS_SD, true);
        }
        goToNextActivity(context, intent, i);
    }

    private static void goToNextActivity(Context context, Intent intent, int i) {
        boolean z = context instanceof MainActivity;
        String str = Constants.ACTION_LOAD_FILE;
        if (z) {
            intent.setClass(context, ManagePatientActivity.class);
            intent.setAction(str);
            intent.putExtra(Constants.EXTRA_IS_LOAD_BY_OUTER_FILE, true);
            context.startActivity(intent);
        } else if (i == 1) {
            intent.setClass(context, ManagePatientActivity.class);
            intent.setAction(str);
            ((Activity) context).startActivityForResult(intent, 1);
        } else {
            intent.setClass(context, MainActivity.class);
            intent.setFlags(67108864);
            intent.putExtra(Constants.EXTRA_REQUEST_CODE, i);
            intent.setAction(str);
            context.startActivity(intent);
            if (context instanceof FilePicker) {
                ((FilePicker) context).finish();
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x004d, code lost:
        if (r4.regionMatches(true, r4.length() - r14.length(), r14, 0, r14.length()) != false) goto L_0x0057;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.util.ArrayList<java.io.File> filter(java.io.File[] r10, boolean r11, boolean r12, boolean r13, java.lang.String r14) {
        /*
            java.util.ArrayList r13 = new java.util.ArrayList
            r13.<init>()
            if (r10 == 0) goto L_0x005d
            int r0 = r10.length
            if (r0 <= 0) goto L_0x005d
            int r0 = r10.length
            r1 = 0
        L_0x000c:
            if (r1 >= r0) goto L_0x005d
            r2 = r10[r1]
            if (r11 == 0) goto L_0x0019
            boolean r3 = r2.isDirectory()
            if (r3 != 0) goto L_0x0019
            goto L_0x005a
        L_0x0019:
            if (r12 != 0) goto L_0x0022
            boolean r3 = r2.isHidden()
            if (r3 == 0) goto L_0x0022
            goto L_0x005a
        L_0x0022:
            boolean r3 = android.text.TextUtils.isEmpty(r14)
            if (r3 != 0) goto L_0x0057
            java.lang.String r4 = r2.getName()
            int r3 = r4.length()
            int r5 = r14.length()
            int r5 = r5 + 1
            if (r3 <= r5) goto L_0x0050
            r5 = 1
            int r3 = r4.length()
            int r6 = r14.length()
            int r6 = r3 - r6
            r8 = 0
            int r9 = r14.length()
            r7 = r14
            boolean r3 = r4.regionMatches(r5, r6, r7, r8, r9)
            if (r3 == 0) goto L_0x0050
            goto L_0x0057
        L_0x0050:
            boolean r3 = r2.isDirectory()
            if (r3 != 0) goto L_0x0057
            goto L_0x005a
        L_0x0057:
            r13.add(r2)
        L_0x005a:
            int r1 = r1 + 1
            goto L_0x000c
        L_0x005d:
            java.util.Collections.sort(r13)
            return r13
        */
        throw new UnsupportedOperationException("Method not decompiled: com.luolai.droidrender.FilePicker.filter(java.io.File[], boolean, boolean, boolean, java.lang.String):java.util.ArrayList");
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Bundle extras = getIntent().getExtras();
        this.dir = Environment.getExternalStorageDirectory();
        if (extras != null) {
            String string = extras.getString(START_DIR);
            this.showHidden = extras.getBoolean(SHOW_HIDDEN, false);
            this.onlyDirs = extras.getBoolean(ONLY_DIRS, false);
            this.mWithSub = extras.getString(WITH_SUB_NAME, "");
            this.mRequestCode = extras.getInt(Constants.EXTRA_REQUEST_CODE, 0);
            this.mIsSD = extras.getBoolean(IS_SD, false);
            if (string != null) {
                File file = new File(string);
                if (file.exists() && file.canRead() && file.isDirectory()) {
                    this.dir = file;
                }
            } else {
                String str = WITH_URI;
                if (extras.containsKey(str)) {
                    File file2 = new File(extras.getString(str));
                    if (file2.exists() && file2.canRead() && file2.isDirectory()) {
                        this.dir = file2;
                    }
                }
            }
        }
        showActionBar();
        setContentView(C0354R.layout.chooser_list);
        this.mListView = (ListView) findViewById(16908298);
        goToDir(this.dir.getAbsolutePath());
        if (!this.dir.canRead()) {
            finish();
        } else {
            DialogHelper.createHintDialog(this, HintType.file_picker, null);
        }
        GAHelper.recordScreen(this, TAG);
    }

    private void showActionBar() {
        View inflate = ((LayoutInflater) getSystemService("layout_inflater")).inflate(C0354R.layout.action_bar_with_confirm, null);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setCustomView(inflate, new LayoutParams(-1, -1));
        this.mConfirm = (ImageView) inflate.findViewById(C0354R.C0356id.confirm);
        this.mBack = (ImageView) inflate.findViewById(C0354R.C0356id.back);
        this.mTitle = (TextView) inflate.findViewById(C0354R.C0356id.path);
        this.mConfirm.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (FilePicker.this.mCheckBoxAdapter == null || FilePicker.this.dir == null) {
                    FilePicker.this.finish();
                    return;
                }
                FilePicker filePicker = FilePicker.this;
                FilePicker.returnDirs(filePicker, filePicker.mCheckBoxAdapter.getSelectedFilePathes(), FilePicker.this.dir.getAbsolutePath(), FilePicker.this.mRequestCode, FilePicker.this.mIsSD);
            }
        });
    }

    /* access modifiers changed from: private */
    public void goToDir(String str) {
        File file = new File(str);
        if (!file.canRead()) {
            showToast(C0354R.string.file_picker_error_cant_read);
            return;
        }
        CheckBoxAdapter checkBoxAdapter = this.mCheckBoxAdapter;
        if (checkBoxAdapter != null) {
            String[] selectedFilePathes = checkBoxAdapter.getSelectedFilePathes();
            if (selectedFilePathes != null && selectedFilePathes.length > 0) {
                showCancelSelectionAlert();
            }
        }
        this.dir = file;
        this.mTitle.setText(this.dir.getAbsolutePath());
        int i = 8;
        this.mConfirm.setVisibility(8);
        final String parent = this.dir.getParent();
        ImageView imageView = this.mBack;
        if (!TextUtils.isEmpty(parent) && new File(parent).canRead()) {
            i = 0;
        }
        imageView.setVisibility(i);
        if (!TextUtils.isEmpty(parent)) {
            this.mBack.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    FilePicker.this.goToDir(parent);
                }
            });
        }
        this.mCheckBoxAdapter = new CheckBoxAdapter(this, this.dir);
        this.mListView.setAdapter(this.mCheckBoxAdapter);
        this.mListView.setOnItemClickListener(this.mCheckBoxAdapter);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(C0354R.menu.file_picker, menu);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(C0354R.C0356id.menu_select_all).setVisible(TextUtils.isEmpty(this.mWithSub));
        menu.findItem(C0354R.C0356id.menu_deselect_all).setVisible(TextUtils.isEmpty(this.mWithSub));
        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId == C0354R.C0356id.menu_deselect_all) {
            CheckBoxAdapter checkBoxAdapter = this.mCheckBoxAdapter;
            if (checkBoxAdapter != null) {
                checkBoxAdapter.deselectAll();
            }
        } else if (itemId == C0354R.C0356id.menu_select_all) {
            CheckBoxAdapter checkBoxAdapter2 = this.mCheckBoxAdapter;
            if (checkBoxAdapter2 != null) {
                checkBoxAdapter2.selectAll();
            }
        }
        return super.onOptionsItemSelected(menuItem);
    }

    /* access modifiers changed from: private */
    public void chooseForSave(String str) {
        final EditText editText = new EditText(this);
        if (!TextUtils.isEmpty(str)) {
            editText.setText(str);
        }
        Builder builder = new Builder(this);
        builder.setView(editText);
        builder.setTitle(C0354R.string.file_picker_title_name);
        builder.setNegativeButton(17039360, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton(17039370, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                final String obj = editText.getText().toString();
                if (TextUtils.isEmpty(obj)) {
                    FilePicker.this.showToast(C0354R.string.file_picker_error_no_text);
                    FilePicker.this.chooseForSave(null);
                    return;
                }
                Iterator it = FilePicker.filter(FilePicker.this.dir.listFiles(), false, true, false, FilePicker.this.mWithSub).iterator();
                while (true) {
                    String str = ".bcd";
                    if (it.hasNext()) {
                        File file = (File) it.next();
                        if (file.isFile()) {
                            String name = file.getName();
                            if (name != null && name.length() > 5 && name.substring(name.length() - 4).equalsIgnoreCase(str) && obj.equals(name.substring(0, name.length() - 4))) {
                                Builder builder = new Builder(FilePicker.this);
                                builder.setMessage(FilePicker.this.getString(C0354R.string.file_picker_error_already_exist, new Object[]{obj}));
                                builder.setNegativeButton(17039360, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        FilePicker.this.chooseForSave(obj);
                                    }
                                });
                                builder.setPositiveButton(17039370, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        FilePicker filePicker = FilePicker.this;
                                        StringBuilder sb = new StringBuilder();
                                        sb.append(FilePicker.this.dir.getAbsolutePath());
                                        sb.append("/");
                                        sb.append(obj);
                                        sb.append(".bcd");
                                        FilePicker.returnDir(filePicker, sb.toString(), FilePicker.this.mRequestCode, FilePicker.this.mIsSD);
                                    }
                                });
                                AlertDialog create = builder.create();
                                create.setCanceledOnTouchOutside(false);
                                create.show();
                                return;
                            }
                        }
                    } else {
                        FilePicker filePicker = FilePicker.this;
                        StringBuilder sb = new StringBuilder();
                        sb.append(FilePicker.this.dir.getAbsolutePath());
                        sb.append("/");
                        sb.append(obj);
                        sb.append(str);
                        FilePicker.returnDir(filePicker, sb.toString(), FilePicker.this.mRequestCode, FilePicker.this.mIsSD);
                        dialogInterface.dismiss();
                        return;
                    }
                }
            }
        });
        AlertDialog create = builder.create();
        create.setCanceledOnTouchOutside(false);
        create.show();
    }

    /* access modifiers changed from: private */
    public void showToast(int i) {
        Toast.makeText(this, getString(i), 0).show();
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int i, int i2, Intent intent) {
        if (i2 == -1 && intent != null) {
            if (i == 1) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    String str = Constants.EXTRA_IMAGE_LIST;
                    if (extras.containsKey(str)) {
                        Intent intent2 = new Intent();
                        intent2.putExtra(str, extras.getIntArray(str));
                        setResult(-1, intent2);
                        finish();
                    }
                }
            }
            super.onActivityResult(i, i2, intent);
        }
    }

    public String[] names(ArrayList<File> arrayList) {
        String[] strArr = new String[arrayList.size()];
        Iterator it = arrayList.iterator();
        int i = 0;
        while (it.hasNext()) {
            strArr[i] = ((File) it.next()).getName();
            i++;
        }
        return strArr;
    }

    public void OnSelectChange(int i) {
        this.mConfirm.setVisibility(i > 0 ? 0 : 8);
    }

    private void showCancelSelectionAlert() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREF_NAME_SETTINGS, 0);
        String str = Constants.PREF_KEY_IS_CANCEL_SELECTION_DLG_SHOWN;
        if (!sharedPreferences.getBoolean(str, false)) {
            Builder builder = new Builder(this);
            builder.setTitle(C0354R.string.file_cancel_by_go_to_folder_title);
            builder.setMessage(C0354R.string.file_cancel_by_go_to_folder_message);
            builder.setNegativeButton(17039370, null);
            builder.setCancelable(false);
            AlertDialog create = builder.create();
            create.setCanceledOnTouchOutside(false);
            create.show();
            sharedPreferences.edit().putBoolean(str, true).apply();
        }
    }

    public void onBackPressed() {
        String str = "";
        String str2 = "/";
        try {
            String absolutePath = this.dir.getAbsolutePath();
            if (!TextUtils.isEmpty(absolutePath)) {
                String replace = absolutePath.replace(str2, str);
                String sDCardPath = this.mIsSD ? StorageUtil.getSDCardPath(this) : Environment.getExternalStorageDirectory().getAbsolutePath();
                if (!TextUtils.isEmpty(sDCardPath) && replace.equalsIgnoreCase(sDCardPath.replace(str2, str))) {
                    super.onBackPressed();
                    return;
                }
            }
            String parent = this.dir.getParent();
            if (TextUtils.isEmpty(parent) || !new File(parent).canRead()) {
                super.onBackPressed();
            } else {
                goToDir(parent);
            }
        } catch (Exception unused) {
            super.onBackPressed();
        }
    }
}
