package com.luolai.droidrender;

import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import com.luolai.base.Entity.DicomHeader;
import com.luolai.base.Entity.PatientPreview;
import com.luolai.base.Entity.PreviewSlice;
import com.luolai.base.GAHelper;
import com.luolai.base.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ManagePatientActivity extends BaseActivity {
    public static final String EXTRA_FROM_CLOUD = "extra_from_cloud";
    public static final String EXTRA_USE_FOR = "extra_use_for";
    private static final int RESULT_FAIL_NO_FILE = 1;
    private static final int RESULT_OK = 0;
    private static final String TAG = "ManagePatientActivity";
    public static final int USE_FOR_DELETING = 2;
    public static final int USE_FOR_LOADING = 0;
    public static final int USE_FOR_RECENT_IMAGE = 3;
    public static final int USE_FOR_SAMPLE_IMAGE = 4;
    public static final int USE_FOR_SELECTION = 1;
    public static final int USE_FOR_SNAPSHOT = 6;
    /* access modifiers changed from: private */
    public ImageView mConfirm;
    private Toast mContrastToast;
    /* access modifiers changed from: private */
    public ImageView mDeleteButton;
    /* access modifiers changed from: private */
    public String[] mFilePathes = null;
    private HeaderAdapter mHeaderAdapter;
    /* access modifiers changed from: private */
    public ListView mHeaderInfo;
    /* access modifiers changed from: private */
    public boolean mIsBack = true;
    private boolean mIsCallByOuterFile = false;
    /* access modifiers changed from: private */
    public boolean mIsLoadingCompleted = true;
    private boolean mIsLoadingStarted = false;
    /* access modifiers changed from: private */
    public PatientListAdapter mPatientListAdapter;
    private ImageView mPreview;
    private int[] mPreviewBits;
    private PreviewSlice mPreviewSlice;
    private PreviewTouchListener mPreviewTouchListener;
    private Bitmap mPreviewbitmap;
    private SeekBar mSeekBar;
    private TextView mSeekText;
    private ImageView mShowHeader;
    private TextView mTitle;
    /* access modifiers changed from: private */
    public int mUsedFor = 0;

    private static class HeaderAdapter extends BaseAdapter {
        Context mContext;
        DicomHeader[] mDicomHeader = null;

        public long getItemId(int i) {
            return 0;
        }

        public HeaderAdapter(Context context) {
            this.mContext = context;
        }

        public void update(DicomHeader[] dicomHeaderArr) {
            this.mDicomHeader = dicomHeaderArr;
            notifyDataSetChanged();
        }

        public int getCount() {
            DicomHeader[] dicomHeaderArr = this.mDicomHeader;
            if (dicomHeaderArr == null) {
                return 0;
            }
            return dicomHeaderArr.length;
        }

        public Object getItem(int i) {
            if (i < 0 || i >= getCount()) {
                return null;
            }
            return this.mDicomHeader[i];
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = View.inflate(this.mContext, C0354R.layout.dicom_header_list_item, null);
            }
            DicomHeader dicomHeader = (DicomHeader) getItem(i);
            if (dicomHeader != null) {
                TextView textView = (TextView) view.findViewById(C0354R.C0356id.tag);
                String str = "";
                if (dicomHeader.mTag != null) {
                    textView.setText(new String(dicomHeader.mTag));
                } else {
                    textView.setText(str);
                }
                TextView textView2 = (TextView) view.findViewById(C0354R.C0356id.value);
                if (dicomHeader.mType == 0) {
                    if (dicomHeader.mStringValue != null) {
                        textView2.setText(Constants.getStringFromByteWithLocale(dicomHeader.mStringValue));
                    } else {
                        textView2.setText(str);
                    }
                } else if (dicomHeader.mType == 1) {
                    if (dicomHeader.mLongValue > 19000000) {
                        Calendar instance = Calendar.getInstance();
                        int i2 = (int) (dicomHeader.mLongValue / 10000);
                        instance.set(i2, (int) (((dicomHeader.mLongValue - ((long) (i2 * 10000))) / 100) - 1), (int) (dicomHeader.mLongValue % 100));
                        textView2.setText(DateFormat.getDateFormat(this.mContext).format(instance.getTime()));
                    } else {
                        textView2.setText(str);
                    }
                }
            }
            return view;
        }
    }

    private class PatientListAdapter extends BaseAdapter implements OnItemClickListener {
        private int mHighLightcolor = 0;
        ManagePatientActivity mManagePatientActivity;
        PatientPreview[] mPatientList = null;
        private int mSamllIconsize = 0;
        SelectCallback mSelectCallback;
        /* access modifiers changed from: private */
        public int mSelectedItem = -1;

        public long getItemId(int i) {
            return 0;
        }

        PatientListAdapter(ManagePatientActivity managePatientActivity, SelectCallback selectCallback) {
            this.mManagePatientActivity = managePatientActivity;
            this.mSelectCallback = selectCallback;
            this.mSamllIconsize = managePatientActivity.getResources().getDimensionPixelSize(C0354R.dimen.small_icon_size);
            this.mHighLightcolor = managePatientActivity.getResources().getColor(C0354R.color.theme_primary);
        }

        public void updateContent(PatientPreview[] patientPreviewArr) {
            int i;
            int i2 = 0;
            if (patientPreviewArr == null || patientPreviewArr.length <= 0) {
                this.mManagePatientActivity.finish();
            } else {
                this.mPatientList = patientPreviewArr;
                int i3 = this.mSelectedItem;
                this.mSelectedItem = -1;
                if (i3 < 0 || i3 >= this.mPatientList.length) {
                    i3 = 0;
                }
                highlightChange(i3);
            }
            this.mManagePatientActivity.invalidateOptionsMenu();
            if (this.mManagePatientActivity.mDeleteButton != null) {
                if (patientPreviewArr.length > 1) {
                    i = 0;
                    for (PatientPreview patientPreview : patientPreviewArr) {
                        if (!patientPreview.mActivated) {
                            i++;
                        }
                    }
                } else {
                    i = 0;
                }
                ImageView access$1700 = ManagePatientActivity.this.mDeleteButton;
                if (i <= 0) {
                    i2 = 8;
                }
                access$1700.setVisibility(i2);
            }
        }

        public boolean hasNoSourceImage() {
            if (!hasPatient()) {
                return false;
            }
            for (PatientPreview patientPreview : this.mPatientList) {
                if (!patientPreview.mIsSourceExist) {
                    return true;
                }
            }
            return false;
        }

        private boolean hasPatient() {
            PatientPreview[] patientPreviewArr = this.mPatientList;
            return patientPreviewArr != null && patientPreviewArr.length > 0;
        }

        public void clearImage(boolean z) {
            if (hasPatient()) {
                ArrayList arrayList = new ArrayList();
                for (int length = this.mPatientList.length - 1; length >= 0; length--) {
                    PatientPreview patientPreview = this.mPatientList[length];
                    if (!z || !patientPreview.mIsSourceExist) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(Constants.getAppDataPath(this.mManagePatientActivity));
                        sb.append(Constants.RECENT_FILE_FOLDER_PATH);
                        File file = new File(String.format("%s%d", new Object[]{sb.toString(), Long.valueOf(patientPreview.mLastAccessTime)}));
                        if (file.exists()) {
                            file.delete();
                        }
                        MainActivity.function(87, length);
                    } else {
                        arrayList.add(0, patientPreview);
                    }
                }
                if (arrayList.size() > 0) {
                    this.mPatientList = (PatientPreview[]) arrayList.toArray(new PatientPreview[arrayList.size()]);
                    this.mSelectedItem = -1;
                    highlightChange(0);
                } else {
                    this.mManagePatientActivity.finish();
                }
            }
        }

        /* access modifiers changed from: private */
        public PatientPreview getcurrentHLPatient() {
            if (!hasPatient()) {
                return null;
            }
            return (PatientPreview) getItem(this.mSelectedItem);
        }

        public void selectAll() {
            PatientPreview[] patientPreviewArr;
            if (hasPatient()) {
                for (PatientPreview patientPreview : this.mPatientList) {
                    if (this.mManagePatientActivity.mUsedFor != 2 || !patientPreview.mActivated) {
                        patientPreview.mIsSelected = true;
                    }
                }
                onSelectionChanged();
                notifyDataSetChanged();
            }
        }

        public void deselectAll() {
            if (hasPatient()) {
                for (PatientPreview patientPreview : this.mPatientList) {
                    patientPreview.mIsSelected = false;
                }
                onSelectionChanged();
                notifyDataSetChanged();
            }
        }

        public int[] getSelectedImages() {
            if (!hasPatient()) {
                return null;
            }
            int i = 1;
            if (this.mManagePatientActivity.mUsedFor == 0 || this.mManagePatientActivity.mUsedFor == 2) {
                int selectedNum = getSelectedNum();
                if (selectedNum <= 0) {
                    return null;
                }
                int[] iArr = new int[selectedNum];
                if (this.mSelectedItem >= getCount() || !this.mPatientList[this.mSelectedItem].mIsSelected) {
                    i = 0;
                } else {
                    iArr[0] = this.mSelectedItem;
                }
                int i2 = i;
                int i3 = 0;
                for (PatientPreview patientPreview : this.mPatientList) {
                    if (patientPreview.mIsSelected && i3 != this.mSelectedItem) {
                        iArr[i2] = i3;
                        i2++;
                    }
                    i3++;
                }
                return iArr;
            }
            return new int[]{this.mSelectedItem};
        }

        private int getSelectedNum() {
            if (!hasPatient()) {
                return 0;
            }
            int i = 0;
            for (PatientPreview patientPreview : this.mPatientList) {
                if (patientPreview.mIsSelected) {
                    i++;
                }
            }
            return i;
        }

        /* access modifiers changed from: private */
        public void onSelectionChanged() {
            this.mSelectCallback.onSelectionChange(getSelectedNum());
        }

        public int getCount() {
            if (!hasPatient()) {
                return 0;
            }
            return this.mPatientList.length;
        }

        public Object getItem(int i) {
            if (hasPatient() && i >= 0) {
                PatientPreview[] patientPreviewArr = this.mPatientList;
                if (i < patientPreviewArr.length) {
                    return patientPreviewArr[i];
                }
            }
            return null;
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            String str;
            PatientPreview patientPreview = (PatientPreview) getItem(i);
            View inflate = view == null ? View.inflate(viewGroup.getContext(), C0354R.layout.list_item_text_checkbox, null) : view;
            if (patientPreview != null) {
                TextView textView = (TextView) inflate.findViewById(C0354R.C0356id.text);
                CheckBox checkBox = (CheckBox) inflate.findViewById(C0354R.C0356id.checkbox);
                checkBox.setTag(patientPreview);
                checkBox.setChecked(patientPreview.mIsSelected);
                int i2 = 8;
                if (this.mManagePatientActivity.mUsedFor != 0 && this.mManagePatientActivity.mUsedFor != 2) {
                    checkBox.setVisibility(8);
                } else if (this.mManagePatientActivity.mUsedFor != 2 || !patientPreview.mActivated) {
                    checkBox.setVisibility(0);
                    checkBox.setOnClickListener(new OnClickListener() {
                        public void onClick(View view) {
                            PatientPreview patientPreview = (PatientPreview) view.getTag();
                            if (patientPreview != null) {
                                patientPreview.mIsSelected = !patientPreview.mIsSelected;
                                ((CheckBox) view).setChecked(patientPreview.mIsSelected);
                                PatientListAdapter.this.onSelectionChanged();
                            }
                        }
                    });
                } else {
                    checkBox.setVisibility(4);
                }
                textView.setText(Constants.getStringFromByteWithLocale(patientPreview.mTitle));
                inflate.findViewById(C0354R.C0356id.image).setVisibility(8);
                Bitmap icon = patientPreview.getIcon();
                ImageView imageView = (ImageView) inflate.findViewById(C0354R.C0356id.image2);
                ((LinearLayout) inflate.findViewById(C0354R.C0356id.image2_outer)).setVisibility(0);
                if (icon != null) {
                    imageView.setImageBitmap(icon);
                }
                TextView textView2 = (TextView) inflate.findViewById(C0354R.C0356id.second);
                textView2.setVisibility(0);
                if (patientPreview.mSeriesTime > 19000000) {
                    Calendar instance = Calendar.getInstance();
                    int i3 = patientPreview.mSeriesTime / 10000;
                    instance.set(i3, ((patientPreview.mSeriesTime - (i3 * 10000)) / 100) - 1, patientPreview.mSeriesTime % 100);
                    str = String.format("%s , ", new Object[]{DateFormat.getDateFormat(this.mManagePatientActivity).format(instance.getTime())});
                } else {
                    str = "";
                }
                textView2.setVisibility(0);
                StringBuilder sb = new StringBuilder();
                sb.append(str);
                sb.append(patientPreview.mOutputString.parsingStringWithIndex(this.mManagePatientActivity));
                textView2.setText(sb.toString());
                if (this.mManagePatientActivity.mUsedFor == 3) {
                    TextView textView3 = (TextView) inflate.findViewById(C0354R.C0356id.last_access);
                    Calendar instance2 = Calendar.getInstance();
                    instance2.setTimeInMillis(patientPreview.mLastAccessTime);
                    textView3.setText(this.mManagePatientActivity.getString(C0354R.string.recent_last_access, new Object[]{DateFormat.getDateFormat(this.mManagePatientActivity).format(instance2.getTime()), new SimpleDateFormat("HH:mm:ss").format(instance2.getTime())}));
                    textView3.setVisibility(0);
                }
                TextView textView4 = (TextView) inflate.findViewById(C0354R.C0356id.is_deleted);
                if (!patientPreview.mIsSourceExist) {
                    i2 = 0;
                }
                textView4.setVisibility(i2);
                int i4 = this.mSamllIconsize;
                inflate.setTouchDelegate(new TouchDelegate(new Rect(0, 0, i4 * 2, i4 * 3), checkBox));
            }
            if (this.mSelectedItem == i) {
                inflate.setBackgroundColor(this.mHighLightcolor);
            } else {
                inflate.setBackgroundColor(ManagePatientActivity.this.getResources().getColor(C0354R.color.action_item_bkg_null));
            }
            return inflate;
        }

        private void highlightChange(int i) {
            PatientPreview patientPreview = (PatientPreview) getItem(i);
            if (patientPreview != null && this.mSelectedItem != i) {
                this.mSelectedItem = i;
                this.mSelectCallback.onHighLightChange(patientPreview, this.mSelectedItem, patientPreview.mIsSourceExist);
                notifyDataSetChanged();
            }
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
            highlightChange(i);
            PatientPreview patientPreview = (PatientPreview) getItem(i);
            if (patientPreview != null && !patientPreview.mIsSelected) {
                if (this.mManagePatientActivity.mUsedFor != 2 || !patientPreview.mActivated) {
                    patientPreview.mIsSelected = true;
                    onSelectionChanged();
                    notifyDataSetChanged();
                }
            }
        }
    }

    private class PreviewTouchListener implements OnTouchListener {
        private Point mStartPoint;

        private PreviewTouchListener() {
            this.mStartPoint = new Point();
        }

        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == 0 && motionEvent.getPointerCount() <= 1) {
                this.mStartPoint.x = (int) motionEvent.getX();
                this.mStartPoint.y = (int) motionEvent.getY();
            } else if (motionEvent.getAction() == 2) {
                PatientPreview access$1500 = ManagePatientActivity.this.mPatientListAdapter.getcurrentHLPatient();
                if (access$1500 != null) {
                    int x = (int) (motionEvent.getX() - ((float) this.mStartPoint.x));
                    int y = (int) (motionEvent.getY() - ((float) this.mStartPoint.y));
                    int i = access$1500.mUValue;
                    int i2 = access$1500.mDValue;
                    access$1500.mUValue += x;
                    access$1500.mDValue += x;
                    access$1500.mUValue += y;
                    access$1500.mDValue -= y;
                    if (access$1500.mUValue < access$1500.mDValue + 6) {
                        access$1500.mUValue = access$1500.mDValue + 6;
                    }
                    this.mStartPoint.x = (int) motionEvent.getX();
                    this.mStartPoint.y = (int) motionEvent.getY();
                    if (!(i == access$1500.mUValue && i2 == access$1500.mDValue)) {
                        ManagePatientActivity.this.updateContrast(access$1500, access$1500.mDValue, access$1500.mUValue, true);
                    }
                }
            }
            return true;
        }
    }

    interface SelectCallback {
        void onHighLightChange(PatientPreview patientPreview, int i, boolean z);

        void onSelectionChange(int i);
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        showActionBar();
        createAdapters();
        setupUI();
    }

    private void createAdapters() {
        this.mPatientListAdapter = new PatientListAdapter(this, new SelectCallback() {
            public void onSelectionChange(int i) {
                if (ManagePatientActivity.this.mUsedFor == 0 || ManagePatientActivity.this.mUsedFor == 2) {
                    ManagePatientActivity.this.mConfirm.setVisibility(i > 0 ? 0 : 8);
                }
            }

            public void onHighLightChange(PatientPreview patientPreview, int i, boolean z) {
                ManagePatientActivity managePatientActivity = ManagePatientActivity.this;
                boolean z2 = true;
                int i2 = 0;
                if (managePatientActivity.mUsedFor == 1 || ManagePatientActivity.this.mUsedFor == 2) {
                    z2 = false;
                }
                managePatientActivity.updateUI(patientPreview, MainActivity.getPreviewSlice(z2, i, patientPreview.mCurrentSlice), false);
                if (ManagePatientActivity.this.mUsedFor != 0 && ManagePatientActivity.this.mUsedFor != 2) {
                    ImageView access$100 = ManagePatientActivity.this.mConfirm;
                    if (!z) {
                        i2 = 8;
                    }
                    access$100.setVisibility(i2);
                }
            }
        });
        this.mHeaderAdapter = new HeaderAdapter(this);
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        if (!this.mIsLoadingStarted) {
            this.mIsLoadingStarted = true;
            this.mIsLoadingCompleted = false;
            int i = this.mUsedFor;
            if (i == 0) {
                loadFromIntent(getIntent());
            } else if (i == 1 || i == 2) {
                finishLoading();
                this.mIsLoadingCompleted = true;
            } else if (i == 3) {
                loadFor(0);
            } else if (i == 4) {
                loadFromIntent(null);
            }
        } else if (this.mIsLoadingCompleted) {
            this.mIsLoadingCompleted = false;
            PatientListAdapter patientListAdapter = this.mPatientListAdapter;
            if (patientListAdapter != null) {
                int i2 = this.mUsedFor;
                if (i2 == 4) {
                    loadFromIntent(null);
                    return;
                }
                patientListAdapter.updateContent(MainActivity.getPatientList(i2));
                this.mIsLoadingCompleted = true;
            }
        }
    }

    private void loadFor(final int i) {
        new AsyncTask<Void, Void, Void>() {
            /* access modifiers changed from: protected */
            public void onPreExecute() {
                PhysiJNIView.showRingDialog((Context) ManagePatientActivity.this, true, (int) C0354R.string.read_file_progress_title_header, 0);
            }

            /* access modifiers changed from: protected */
            public Void doInBackground(Void... voidArr) {
                MainActivity.loadFor(i);
                return null;
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Void voidR) {
                ManagePatientActivity.this.mIsLoadingCompleted = true;
                PhysiJNIView.showRingDialog((Context) ManagePatientActivity.this, false, 0, 0);
                if (MainActivity.hasPatient(true)) {
                    ManagePatientActivity.this.finishLoading();
                }
            }
        }.execute(new Void[0]);
    }

    /* access modifiers changed from: private */
    public void finishLoading() {
        long currentTimeMillis = System.currentTimeMillis();
        PatientPreview[] patientList = MainActivity.getPatientList(this.mUsedFor);
        Log.m50w(TAG, String.format("Process time for getPatientList : %f s", new Object[]{Float.valueOf(((float) (System.currentTimeMillis() - currentTimeMillis)) / 1000.0f)}));
        if (patientList == null || patientList.length <= 0) {
            finish();
            return;
        }
        this.mPatientListAdapter.updateContent(patientList);
        int i = this.mUsedFor;
        if (i == 0) {
            DialogHelper.createHintDialog(this, HintType.loading, null);
            this.mPatientListAdapter.selectAll();
        } else if (i == 1) {
            if (patientList.length > 1) {
                DialogHelper.createHintDialog(this, HintType.selection, null);
            }
        } else if (i == 4) {
            DialogHelper.createHintDialog(this, HintType.sample, null);
        } else if (i == 2) {
            this.mPatientListAdapter.deselectAll();
        }
    }

    /* access modifiers changed from: private */
    public static void loadFileFromDir(File file, ArrayList<String> arrayList, boolean z) {
        if (file != null && file.isDirectory() && file.exists() && file.canRead()) {
            File[] listFiles = file.listFiles();
            if (listFiles != null && listFiles.length > 0) {
                for (int i = 0; i < listFiles.length; i++) {
                    if (listFiles[i] != null && listFiles[i].canRead() && listFiles[i].isFile()) {
                        arrayList.add(listFiles[i].getAbsolutePath());
                    } else if (listFiles[i] != null && listFiles[i].canRead() && listFiles[i].isDirectory() && z) {
                        loadFileFromDir(listFiles[i], arrayList, z);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public String[] getSamplePathes() {
        return new String[]{Constants.getSamplePath(this)};
    }

    private void loadFromIntent(final Intent intent) {
        MainActivity.postInitCDib(null, null);
        new AsyncTask<Void, Void, Integer>() {
            String mPath;

            /* access modifiers changed from: protected */
            public void onPreExecute() {
                PhysiJNIView.showRingDialog((Context) ManagePatientActivity.this, true, (int) C0354R.string.read_file_progress_title_header, 0);
            }

            /* access modifiers changed from: protected */
            public Integer doInBackground(Void... voidArr) {
                String[] strArr;
                Integer valueOf = Integer.valueOf(0);
                Intent intent = intent;
                if (intent == null || intent.getExtras() == null) {
                    strArr = ManagePatientActivity.this.getSamplePathes();
                } else {
                    Bundle extras = intent.getExtras();
                    this.mPath = extras.getString(FilePicker.CHOSEN_DIRECTORY, null);
                    strArr = extras.getStringArray(FilePicker.CHOSEN_DIRECTORIES);
                    boolean z = extras.getBoolean(FilePicker.IS_SD, false);
                    String str = this.mPath;
                    if (str != null) {
                        if (strArr == null) {
                            strArr = new String[]{str};
                        }
                        File file = new File(this.mPath);
                        String absolutePath = file.getAbsolutePath();
                        if (!file.isDirectory() && file.getParent() != null) {
                            absolutePath = file.getParent();
                        }
                        String str2 = z ? Constants.PREF_KEY_LAST_DIR_IMAGE_EXTERNAL : Constants.PREF_KEY_LAST_DIR_IMAGE;
                        if (!absolutePath.contains(ManagePatientActivity.this.getFilesDir().getAbsolutePath())) {
                            ManagePatientActivity.this.getSharedPreferences(Constants.PREF_NAME_SETTINGS, 0).edit().putString(str2, absolutePath).apply();
                        }
                    }
                }
                ArrayList arrayList = new ArrayList();
                for (String file2 : strArr) {
                    File file3 = new File(file2);
                    if (file3.isDirectory()) {
                        ManagePatientActivity.loadFileFromDir(file3, arrayList, true);
                    } else {
                        arrayList.add(file3.getAbsolutePath());
                    }
                }
                ArrayList access$700 = ManagePatientActivity.tryUnzip(arrayList, Constants.getFolderPath(ManagePatientActivity.this, FileType.zip));
                Intent intent2 = intent;
                if (!(intent2 == null || intent2.getExtras() == null)) {
                    GAHelper.sendTracker(ManagePatientActivity.this, ManagePatientActivity.TAG, "Load file number", "", (long) access$700.size());
                }
                if (access$700.size() <= 0) {
                    return Integer.valueOf(1);
                }
                ManagePatientActivity.this.mFilePathes = (String[]) access$700.toArray(new String[access$700.size()]);
                MainActivity.loadToJNI(ManagePatientActivity.this.mFilePathes);
                return valueOf;
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Integer num) {
                PhysiJNIView.showRingDialog((Context) ManagePatientActivity.this, false, 0, 0);
                if (num.intValue() == 0) {
                    if (MainActivity.hasPatient(true)) {
                        ManagePatientActivity.this.finishLoading();
                    } else {
                        Builder builder = new Builder(ManagePatientActivity.this);
                        builder.setTitle(C0354R.string.title_load_bcd_error);
                        builder.setMessage(C0354R.string.message_load_bcd_error);
                        builder.setNegativeButton(17039370, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ManagePatientActivity.this.setResult(0);
                                ManagePatientActivity.this.finish();
                            }
                        });
                        builder.setCancelable(false);
                        AlertDialog create = builder.create();
                        create.setCanceledOnTouchOutside(false);
                        if (!ManagePatientActivity.this.isFinishing()) {
                            create.show();
                        }
                    }
                } else if (num.intValue() == 1) {
                    Builder builder2 = new Builder(ManagePatientActivity.this);
                    builder2.setTitle(C0354R.string.title_load_bcd_error);
                    builder2.setMessage(ManagePatientActivity.this.getString(C0354R.string.message_load_filepath_error, new Object[]{this.mPath}));
                    builder2.setNegativeButton(17039370, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ManagePatientActivity.this.finish();
                        }
                    });
                    builder2.setCancelable(false);
                    AlertDialog create2 = builder2.create();
                    create2.setCanceledOnTouchOutside(false);
                    if (!ManagePatientActivity.this.isFinishing()) {
                        create2.show();
                    }
                }
                ManagePatientActivity.this.mIsLoadingCompleted = true;
            }
        }.execute(new Void[0]);
    }

    /* access modifiers changed from: private */
    public static ArrayList<String> tryUnzip(ArrayList<String> arrayList, String str) {
        ArrayList<String> arrayList2 = new ArrayList<>(arrayList.size());
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            String str2 = (String) it.next();
            if (TextUtils.isEmpty(str2) || !str2.endsWith(".zip")) {
                arrayList2.add(str2);
            } else {
                File file = new File(str2);
                if (file.isFile() && file.exists() && file.canRead()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(str);
                    sb.append(File.separator);
                    sb.append(file.getName());
                    sb.append("_");
                    sb.append(file.getParentFile().getAbsolutePath().hashCode());
                    arrayList2.addAll(unzip(str2, sb.toString()));
                }
            }
        }
        return arrayList2;
    }

    private static ArrayList<String> unzip(String str, String str2) {
        ArrayList arrayList = new ArrayList();
        File file = new File(str2);
        if (file.exists()) {
            file.delete();
        }
        file.mkdirs();
        try {
            ZipFile zipFile = new ZipFile(str);
            Enumeration entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry) entries.nextElement();
                String name = zipEntry.getName();
                StringBuilder sb = new StringBuilder();
                sb.append(str2);
                sb.append(File.separator);
                sb.append(name);
                File file2 = new File(sb.toString());
                if (name.endsWith("/")) {
                    file2.mkdirs();
                } else {
                    File parentFile = file2.getParentFile();
                    if (parentFile != null && !parentFile.exists()) {
                        parentFile.mkdirs();
                    }
                    InputStream inputStream = zipFile.getInputStream(zipEntry);
                    FileOutputStream fileOutputStream = new FileOutputStream(file2);
                    byte[] bArr = new byte[1024];
                    while (true) {
                        int read = inputStream.read(bArr);
                        if (read < 0) {
                            break;
                        }
                        fileOutputStream.write(bArr, 0, read);
                    }
                    inputStream.close();
                    fileOutputStream.close();
                }
            }
            zipFile.close();
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
        }
        loadFileFromDir(file, arrayList, true);
        return tryUnzip(arrayList, str2);
    }

    private void setupUI() {
        ListView listView = this.mHeaderInfo;
        boolean z = listView != null && listView.getVisibility() == 0;
        setContentView(C0354R.layout.delete_list);
        ListView listView2 = (ListView) findViewById(16908298);
        listView2.setAdapter(this.mPatientListAdapter);
        listView2.setOnItemClickListener(this.mPatientListAdapter);
        this.mPreview = (ImageView) findViewById(C0354R.C0356id.preview);
        this.mSeekBar = (SeekBar) findViewById(C0354R.C0356id.seekbar);
        this.mSeekText = (TextView) findViewById(C0354R.C0356id.text);
        this.mHeaderInfo = (ListView) findViewById(C0354R.C0356id.header_list);
        if (z) {
            this.mHeaderInfo.setVisibility(0);
        }
        this.mHeaderInfo.setAdapter(this.mHeaderAdapter);
        this.mPreviewTouchListener = new PreviewTouchListener();
        this.mPreview.setOnTouchListener(this.mPreviewTouchListener);
        this.mContrastToast = Toast.makeText(this, "", 0);
    }

    /* access modifiers changed from: private */
    public void updateUI(final PatientPreview patientPreview, PreviewSlice previewSlice, boolean z) {
        this.mPreviewSlice = previewSlice;
        HeaderAdapter headerAdapter = this.mHeaderAdapter;
        PreviewSlice previewSlice2 = this.mPreviewSlice;
        headerAdapter.update(previewSlice2 == null ? null : previewSlice2.mDicomHeader);
        if (previewSlice == null) {
            this.mPreview.setImageDrawable(null);
            this.mSeekText.setVisibility(0);
            this.mSeekText.setText(C0354R.string.patient_manager_error_no_file);
            return;
        }
        if (patientPreview.mDValue == patientPreview.mUValue && patientPreview.mDValue == 0) {
            patientPreview.mDValue = previewSlice.mDValue;
            patientPreview.mUValue = previewSlice.mUValue;
        }
        if (patientPreview.mSliceNumber <= 1) {
            this.mSeekBar.setVisibility(8);
            this.mSeekText.setText("");
        } else {
            this.mSeekBar.setVisibility(0);
            if (!z) {
                this.mSeekBar.setMax(patientPreview.mSliceNumber - 1);
                this.mSeekBar.setProgress(patientPreview.mCurrentSlice);
                this.mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }

                    public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                        if (z) {
                            PatientPreview patientPreview = patientPreview;
                            patientPreview.mCurrentSlice = i;
                            ManagePatientActivity managePatientActivity = ManagePatientActivity.this;
                            managePatientActivity.updateUI(patientPreview, MainActivity.getPreviewSlice((managePatientActivity.mUsedFor == 1 || ManagePatientActivity.this.mUsedFor == 2) ? false : true, ManagePatientActivity.this.mPatientListAdapter.mSelectedItem, patientPreview.mCurrentSlice), true);
                        }
                    }
                });
            }
            this.mSeekText.setText(String.format("%d / %d", new Object[]{Integer.valueOf(patientPreview.mCurrentSlice + 1), Integer.valueOf(patientPreview.mSliceNumber)}));
        }
        if (!z) {
            this.mPreviewBits = new int[(previewSlice.mWidth * previewSlice.mHeight)];
            this.mPreviewbitmap = Bitmap.createBitmap(this.mPreviewBits, previewSlice.mWidth, previewSlice.mHeight, Config.ARGB_8888).copy(Config.ARGB_8888, true);
        }
        updateContrast(patientPreview, patientPreview.mDValue, patientPreview.mUValue, false);
    }

    /* access modifiers changed from: private */
    public void updateContrast(PatientPreview patientPreview, int i, int i2, boolean z) {
        if (this.mPreviewSlice != null) {
            patientPreview.mDValue = i;
            patientPreview.mUValue = i2;
            int i3 = i2 - i;
            float f = 255.0f / ((float) i3);
            for (int i4 = 0; i4 < this.mPreviewSlice.mWidth; i4++) {
                for (int i5 = 0; i5 < this.mPreviewSlice.mHeight; i5++) {
                    if ((this.mPreviewSlice.mWidth * i5) + i4 < this.mPreviewBits.length) {
                        int i6 = (int) (((float) (this.mPreviewSlice.mBits[(this.mPreviewSlice.mWidth * i5) + i4] - i)) * f);
                        if (i6 > 255) {
                            i6 = 255;
                        } else if (i6 < 0) {
                            i6 = 0;
                        }
                        this.mPreviewBits[(this.mPreviewSlice.mWidth * i5) + i4] = i6 | -16777216 | (i6 << 16) | (i6 << 8);
                    }
                }
            }
            this.mPreviewbitmap.setPixels(this.mPreviewBits, 0, this.mPreviewSlice.mWidth, 0, 0, this.mPreviewSlice.mWidth, this.mPreviewSlice.mHeight);
            this.mPreview.setImageBitmap(this.mPreviewbitmap);
            if (z) {
                Object[] objArr = {Integer.valueOf((i2 + i) / 2)};
                String str = "%d";
                this.mContrastToast.setText(getString(C0354R.string.contrast_toast, new Object[]{String.format(str, objArr), String.format(str, new Object[]{Integer.valueOf(i3)})}));
                this.mContrastToast.show();
            }
        }
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
        ((ImageView) inflate.findViewById(C0354R.C0356id.back)).setVisibility(8);
        this.mTitle = (TextView) inflate.findViewById(C0354R.C0356id.path);
        if (getIntent() != null) {
            this.mUsedFor = getIntent().getIntExtra(EXTRA_USE_FOR, 0);
            this.mIsCallByOuterFile = getIntent().getBooleanExtra(Constants.EXTRA_IS_LOAD_BY_OUTER_FILE, false);
        }
        this.mShowHeader = (ImageView) inflate.findViewById(C0354R.C0356id.header);
        this.mShowHeader.setVisibility(0);
        this.mShowHeader.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                int i = 0;
                boolean z = ManagePatientActivity.this.mHeaderInfo.getVisibility() != 0;
                ListView access$1200 = ManagePatientActivity.this.mHeaderInfo;
                if (!z) {
                    i = 4;
                }
                access$1200.setVisibility(i);
                GAHelper.sendTracker(ManagePatientActivity.this, ManagePatientActivity.TAG, "Click", "Show header", z ? 1 : 0);
            }
        });
        String str = TAG;
        int i = this.mUsedFor;
        if (i == 0) {
            this.mTitle.setText(C0354R.string.patient_manager_loading);
            StringBuilder sb = new StringBuilder();
            sb.append(str);
            sb.append(" - Loading");
            str = sb.toString();
        } else if (i == 1) {
            this.mTitle.setText(C0354R.string.patient_manager_selecting);
            StringBuilder sb2 = new StringBuilder();
            sb2.append(str);
            sb2.append(" - Selection");
            str = sb2.toString();
            this.mDeleteButton = (ImageView) inflate.findViewById(C0354R.C0356id.delete);
            this.mDeleteButton.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setClass(ManagePatientActivity.this, ManagePatientActivity.class);
                    intent.putExtra(ManagePatientActivity.EXTRA_USE_FOR, 2);
                    ManagePatientActivity.this.startActivity(intent);
                }
            });
        } else if (i == 2) {
            this.mTitle.setText(C0354R.string.patient_manager_deleting);
            StringBuilder sb3 = new StringBuilder();
            sb3.append(str);
            sb3.append(" - deleting");
            str = sb3.toString();
        } else if (i == 3) {
            this.mTitle.setText(C0354R.string.landing_load_recent);
            StringBuilder sb4 = new StringBuilder();
            sb4.append(str);
            sb4.append(" - Recent image");
            str = sb4.toString();
        } else if (i == 4) {
            this.mTitle.setText(C0354R.string.landing_load_sample);
            StringBuilder sb5 = new StringBuilder();
            sb5.append(str);
            sb5.append(" - Sample image");
            str = sb5.toString();
        }
        this.mConfirm.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                ManagePatientActivity.this.mIsBack = false;
                ManagePatientActivity.this.goToMainActivity();
            }
        });
        this.mConfirm.setVisibility(8);
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra(EXTRA_FROM_CLOUD, false)) {
            StringBuilder sb6 = new StringBuilder();
            sb6.append(str);
            sb6.append(" from Cloud");
            str = sb6.toString();
        }
        GAHelper.recordScreen(this, str);
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        if (this.mIsBack) {
            int i = this.mUsedFor;
            if (!(i == 1 && i == 2)) {
                MainActivity.postInitCDib(this.mUsedFor == 0 ? this : null, null);
            }
        }
        if (getIntent() != null && getIntent().getBooleanExtra(Constants.EXTRA_FROM_MAINACTIVITY, false)) {
            MainActivity.gotoMainActivity(this);
        }
        super.onDestroy();
    }

    /* access modifiers changed from: private */
    public void goToMainActivity() {
        boolean z = this.mIsCallByOuterFile;
        String str = Constants.EXTRA_IMAGE_LIST;
        if (!z || this.mUsedFor != 0) {
            int i = this.mUsedFor;
            if (i == 2) {
                MainActivity.deletePatient(this.mPatientListAdapter.getSelectedImages());
            } else if (i != 1) {
                Intent intent = new Intent();
                intent.putExtra(str, this.mPatientListAdapter.getSelectedImages());
                setResult(-1, intent);
            } else {
                Intent intent2 = new Intent();
                intent2.setClass(this, MainActivity.class);
                intent2.setAction(Constants.ACTION_SELECT_PATIENT);
                intent2.putExtra(Constants.EXTRA_PATIENT_ORDER, this.mPatientListAdapter.mSelectedItem);
                startActivity(intent2);
            }
        } else {
            Intent intent3 = new Intent();
            intent3.setClass(this, MainActivity.class);
            intent3.setAction(Constants.ACTION_LOAD_IMAGE);
            intent3.putExtra(str, this.mPatientListAdapter.getSelectedImages());
            startActivity(intent3);
        }
        finish();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        int i = this.mUsedFor;
        if (i == 3) {
            getMenuInflater().inflate(C0354R.menu.manage_patient, menu);
        } else if (i == 2 || i == 0) {
            getMenuInflater().inflate(C0354R.menu.file_picker, menu);
        } else if (i == 4) {
            getMenuInflater().inflate(C0354R.menu.download_sample, menu);
        }
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        if (this.mUsedFor == 3) {
            MenuItem findItem = menu.findItem(C0354R.C0356id.menu_clear);
            if (findItem != null) {
                PatientListAdapter patientListAdapter = this.mPatientListAdapter;
                findItem.setVisible(patientListAdapter != null && patientListAdapter.hasNoSourceImage());
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case C0354R.C0356id.menu_clear /*2131165246*/:
                PatientListAdapter patientListAdapter = this.mPatientListAdapter;
                if (patientListAdapter != null) {
                    patientListAdapter.clearImage(true);
                    invalidateOptionsMenu();
                    break;
                }
                break;
            case C0354R.C0356id.menu_clear_all /*2131165247*/:
                PatientListAdapter patientListAdapter2 = this.mPatientListAdapter;
                if (patientListAdapter2 != null) {
                    patientListAdapter2.clearImage(false);
                    invalidateOptionsMenu();
                    break;
                }
                break;
            case C0354R.C0356id.menu_deselect_all /*2131165249*/:
                PatientListAdapter patientListAdapter3 = this.mPatientListAdapter;
                if (patientListAdapter3 != null) {
                    patientListAdapter3.deselectAll();
                    break;
                }
                break;
            case C0354R.C0356id.menu_sample /*2131165253*/:
                downloadSample();
                return true;
            case C0354R.C0356id.menu_select_all /*2131165254*/:
                PatientListAdapter patientListAdapter4 = this.mPatientListAdapter;
                if (patientListAdapter4 != null) {
                    patientListAdapter4.selectAll();
                    break;
                }
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public void onConfigurationChanged(Configuration configuration) {
        if (this.mIsLoadingCompleted) {
            setupUI();
            PatientListAdapter patientListAdapter = this.mPatientListAdapter;
            patientListAdapter.updateContent(patientListAdapter.mPatientList);
        }
        super.onConfigurationChanged(configuration);
    }

    private void downloadSample() {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.setData(Uri.parse(FilePicker.SAMPLE_PATH));
        startActivity(intent);
    }
}
