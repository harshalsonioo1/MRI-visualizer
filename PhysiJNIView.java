package com.luolai.droidrender;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;
import com.luolai.base.Entity.JavaString;
import com.luolai.base.GAHelper;
import com.luolai.base.ToolBarLayout;
import com.luolai.droidrender.DialogHelper.FileSaverCallback;
import com.luolai.droidrender.DialogHelper.ResampleCallback;
import com.luolai.droidrender.DialogHelper.ShiftCallback;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

public class PhysiJNIView extends RelativeLayout {
    static final int IDM_MY_VRCM = 34013;
    static final int IDM_SAVE_VRCM = 34014;
    public static final int LAYOUTS_1P3V = 2;
    public static final int LAYOUTS_1V = 0;
    public static final int LAYOUTS_2V = 1;
    public static final int LAYOUTS_4V = 3;
    public static final int LAYOUTS_CUSTOMIZE_1 = 4;
    public static final int LAYOUTS_CUSTOMIZE_2 = 5;
    public static final int LAYOUTS_CUSTOMIZE_3 = 6;
    public static final int LAYOUTS_CUSTOMIZE_4 = 7;
    public static final int LAYOUTS_DEFAULT_2D = -1;
    public static final int LAYOUTS_DEFAULT_3D = -2;
    public static final int MAX_LAYOUTS = 8;
    private static final long MENU_MY_VRCM_START_ID = -100000000;
    private static final String TAG = "PhysiJNIView";
    /* access modifiers changed from: private */
    public static Context mContext;
    private static ProgressDialog mRingDialog;
    private static int sTextHeight;
    Activity mActivity;
    ContextMenuListener mContextMenuListener = new ContextMenuListener();
    private GestureDetector mGestureDetector;
    private OnGestureListener mGestureListener = new OnGestureListener() {
        public boolean onDown(MotionEvent motionEvent) {
            return false;
        }

        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
            return false;
        }

        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
            return false;
        }

        public void onShowPress(MotionEvent motionEvent) {
        }

        public void onLongPress(MotionEvent motionEvent) {
            int i;
            int i2;
            int i3;
            MotionEvent motionEvent2 = motionEvent;
            Point[] pointArr = new Point[motionEvent.getPointerCount()];
            StringBuffer stringBuffer = new StringBuffer();
            for (int i4 = 0; i4 < motionEvent.getPointerCount(); i4++) {
                pointArr[i4] = new Point((int) motionEvent2.getX(i4), (int) motionEvent2.getY(i4));
                stringBuffer.append(String.format("Point %d , x = %d , y = %d . ", new Object[]{Integer.valueOf(i4), Integer.valueOf(pointArr[i4].x), Integer.valueOf(pointArr[i4].y)}));
            }
            if (pointArr.length > 1) {
                i2 = pointArr[1].x;
                i = pointArr[1].y;
                i3 = 2;
            } else {
                i3 = 1;
                i2 = 0;
                i = 0;
            }
            if (MainActivity.touch(3, i3, pointArr[0].x, pointArr[0].y, i2, i, Calendar.getInstance().getTimeInMillis(), 0) == 2) {
                ((Vibrator) PhysiJNIView.this.getContext().getSystemService("vibrator")).vibrate(100);
            }
        }

        public boolean onSingleTapUp(MotionEvent motionEvent) {
            if (motionEvent.getPointerCount() <= 0) {
                return false;
            }
            Point[] pointArr = new Point[motionEvent.getPointerCount()];
            for (int i = 0; i < motionEvent.getPointerCount(); i++) {
                pointArr[i] = new Point((int) motionEvent.getX(i), (int) motionEvent.getY(i));
            }
            if (MainActivity.touch(11, 1, pointArr[0].x, pointArr[0].y, 0, 0, Calendar.getInstance().getTimeInMillis(), 0) != 0) {
                return true;
            }
            return false;
        }
    };
    PGMenuItem[] mItems = null;
    private Paint mJNIPaint;
    ArrayList<String> mMyVRCMPathes = new ArrayList<>();
    public float mOneDP;
    ProgressDialog mProgressDialog = null;
    Paint mSchedulePaint;
    int mTextSize;
    int mTextSizeSmall;
    Toast mToast = null;
    Handler mUIThreadHandler;
    public int mUiType = -3;

    public class ContextMenuListener implements OnMenuItemClickListener {
        public ContextMenuListener() {
        }

        /* JADX WARNING: Removed duplicated region for block: B:15:0x005e  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean onMenuItemClick(android.view.MenuItem r9) {
            /*
                r8 = this;
                com.luolai.droidrender.PhysiJNIView r0 = com.luolai.droidrender.PhysiJNIView.this
                com.luolai.droidrender.PhysiJNIView$PGMenuItem[] r0 = r0.mItems
                int r9 = r9.getItemId()
                r9 = r0[r9]
                long r6 = r9.mID
                android.content.Context r0 = com.luolai.droidrender.PhysiJNIView.mContext
                java.lang.String r1 = "View menu"
                java.lang.String r2 = "click"
                java.lang.String r3 = ""
                r4 = r6
                com.luolai.base.GAHelper.sendTracker(r0, r1, r2, r3, r4)
                int r0 = r9.mType
                r1 = 0
                r2 = 1
                if (r0 != r2) goto L_0x003b
                r3 = 34013(0x84dd, double:1.68047E-319)
                int r9 = (r6 > r3 ? 1 : (r6 == r3 ? 0 : -1))
                if (r9 != 0) goto L_0x002d
                com.luolai.droidrender.PhysiJNIView r9 = com.luolai.droidrender.PhysiJNIView.this
                r9.loadMyVRCM()
                goto L_0x005c
            L_0x002d:
                r3 = 34014(0x84de, double:1.6805E-319)
                int r9 = (r6 > r3 ? 1 : (r6 == r3 ? 0 : -1))
                if (r9 != 0) goto L_0x005b
                com.luolai.droidrender.PhysiJNIView r9 = com.luolai.droidrender.PhysiJNIView.this
                r0 = 0
                r9.chooseForSaveVRCM(r0)
                goto L_0x005c
            L_0x003b:
                int r9 = r9.mType
                r0 = 3
                if (r9 != r0) goto L_0x005b
                r3 = -100000000(0xfffffffffa0a1f00, double:NaN)
                int r9 = (r6 > r3 ? 1 : (r6 == r3 ? 0 : -1))
                if (r9 < 0) goto L_0x005b
                com.luolai.droidrender.PhysiJNIView r9 = com.luolai.droidrender.PhysiJNIView.this
                java.util.ArrayList<java.lang.String> r9 = r9.mMyVRCMPathes
                long r3 = r6 - r3
                int r0 = (int) r3
                java.lang.Object r9 = r9.get(r0)
                java.lang.String r9 = (java.lang.String) r9
                r3 = 32968(0x80c8, double:1.62884E-319)
                com.luolai.droidrender.MainActivity.functions(r3, r9)
                goto L_0x005c
            L_0x005b:
                r2 = 0
            L_0x005c:
                if (r2 != 0) goto L_0x0061
                com.luolai.droidrender.MainActivity.menuFunction(r6)
            L_0x0061:
                return r1
            */
            throw new UnsupportedOperationException("Method not decompiled: com.luolai.droidrender.PhysiJNIView.ContextMenuListener.onMenuItemClick(android.view.MenuItem):boolean");
        }
    }

    public static class PGMenuItem {
        String mCaption;
        long mID;
        boolean mIsChecked;
        int mType;

        public PGMenuItem(String str, byte[][] bArr, long j, boolean z, int i) {
            this.mCaption = new JavaString(str, bArr).parsingStringWithIndex(PhysiJNIView.mContext);
            this.mID = j;
            this.mIsChecked = z;
            this.mType = i;
        }

        public PGMenuItem(String str, long j, boolean z, int i) {
            this.mCaption = str;
            this.mID = j;
            this.mIsChecked = z;
            this.mType = i;
        }
    }

    public PhysiJNIView(Context context) {
        super(context);
        init();
    }

    public PhysiJNIView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    public PhysiJNIView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init();
    }

    public static byte[][] newByteDoubleArray(int i) {
        return new byte[i][];
    }

    public static void sendGA(byte[] bArr, byte[] bArr2, byte[] bArr3, long j) {
        if (bArr != null && bArr2 != null) {
            GAHelper.sendTracker(null, new String(bArr), new String(bArr2), bArr3 != null ? new String(bArr3) : "", j);
        }
    }

    public static void showRingDialog(Context context, boolean z, int i, int i2) {
        String str = null;
        String string = i > 0 ? context.getString(i) : null;
        if (i2 > 0) {
            str = context.getString(i2);
        }
        showRingDialog(context, z, string, str);
    }

    public static void showRingDialog(Context context, boolean z, byte[] bArr, byte[] bArr2) {
        String str = null;
        String parsingStringWithIndex = (bArr == null || bArr.length <= 0) ? null : new JavaString(new String(bArr), null).parsingStringWithIndex(context);
        if (bArr2 != null && bArr2.length > 0) {
            str = new JavaString(new String(bArr2), null).parsingStringWithIndex(context);
        }
        showRingDialog(context, z, parsingStringWithIndex, str);
    }

    public static void showRingDialog(Context context, boolean z, String str, String str2) {
        if (z) {
            ProgressDialog progressDialog = mRingDialog;
            if (progressDialog == null) {
                mRingDialog = ProgressDialog.show(context, str, str2);
                mRingDialog.setCancelable(false);
                mRingDialog.setCanceledOnTouchOutside(false);
                return;
            }
            progressDialog.setTitle(str);
            mRingDialog.setMessage(str2);
            return;
        }
        ProgressDialog progressDialog2 = mRingDialog;
        if (progressDialog2 != null) {
            try {
                progressDialog2.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mRingDialog = null;
        }
    }

    public static long getTime() {
        return System.currentTimeMillis();
    }

    public static void setDebug(boolean z) {
        Constants.sDebug = z;
    }

    public static byte[] getFileShortName(byte[] bArr, boolean z) {
        String str = "/";
        String str2 = "Unknown";
        try {
            String str3 = new String(bArr);
            if (str3.contains(str)) {
                String[] split = str3.split(str);
                if (split != null && split.length > 0) {
                    str2 = (!z || split.length <= 1) ? split[split.length - 1] : split[split.length - 2];
                }
            } else {
                str2 = str3;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str2.getBytes();
    }

    public void setMainActivity(Activity activity) {
        this.mActivity = activity;
    }

    private void init() {
        mContext = getContext();
        this.mUIThreadHandler = new Handler();
        this.mGestureDetector = new GestureDetector(getContext(), this.mGestureListener);
        this.mJNIPaint = new Paint();
        this.mTextSize = (int) getContext().getResources().getDimension(C0354R.dimen.size_text);
        this.mTextSizeSmall = (int) getContext().getResources().getDimension(C0354R.dimen.size_text_small);
        this.mJNIPaint.setTextSize((float) this.mTextSize);
        this.mSchedulePaint = new Paint();
        this.mOneDP = getResources().getDimension(C0354R.dimen.onedp);
    }

    public long getAvailableMegs() {
        MemoryInfo memoryInfo = new MemoryInfo();
        ((ActivityManager) this.mActivity.getSystemService("activity")).getMemoryInfo(memoryInfo);
        return memoryInfo.availMem;
    }

    public int getTextWidth(String str) {
        Rect rect = new Rect();
        this.mJNIPaint.getTextBounds(str, 0, str.length(), rect);
        return (int) (((float) rect.width()) + (this.mOneDP * 3.0f));
    }

    public int getTextHeight() {
        if (sTextHeight == 0) {
            Rect rect = new Rect();
            this.mJNIPaint.getTextBounds("ABCj", 0, 4, rect);
            double height = (double) rect.height();
            Double.isNaN(height);
            sTextHeight = (int) (height * 1.2d);
        }
        return sTextHeight;
    }

    public String getStringForJNI(String str, byte[][] bArr) {
        return new JavaString(str, bArr).parsingStringWithIndex(getContext());
    }

    public void onPatientChange(byte[] bArr, int i, boolean z, boolean z2) {
        Activity activity = this.mActivity;
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).onPatientChanged(Constants.getStringFromByteWithLocale(bArr), i, z, z2);
        }
    }

    public int[] getStringBitmap(String str, int i, byte[][] bArr, int i2, int i3) {
        String parsingStringWithIndex = new JavaString(str, bArr).parsingStringWithIndex(getContext());
        if (TextUtils.isEmpty(parsingStringWithIndex)) {
            return null;
        }
        if (i3 == 0) {
            i3 = this.mTextSize;
        } else if (i3 < 0) {
            i3 = this.mTextSizeSmall;
        }
        this.mJNIPaint.setTextSize((float) i3);
        this.mJNIPaint.setColor(i);
        this.mJNIPaint.setAlpha(255);
        Rect rect = new Rect();
        this.mJNIPaint.getTextBounds(parsingStringWithIndex, 0, parsingStringWithIndex.length(), rect);
        double d = (double) rect.right;
        Double.isNaN(d);
        double d2 = d * 1.1d;
        double d3 = (double) i2;
        Double.isNaN(d3);
        rect.right = (int) (d2 + d3);
        int i4 = i2 * 2;
        int[] iArr = new int[(((rect.width() + i4) * (rect.height() + i4)) + 2)];
        Bitmap createBitmap = Bitmap.createBitmap(rect.width() + i4, rect.height() + i4, Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawText(parsingStringWithIndex, (float) i2, (float) ((-rect.top) + i2), this.mJNIPaint);
        iArr[0] = rect.width() + i4;
        iArr[1] = rect.height() + i4;
        createBitmap.getPixels(iArr, 2, rect.width() + i4, 0, 0, rect.width() + i4, rect.height() + i4);
        return iArr;
    }

    public void createMenu(final PGMenuItem[] pGMenuItemArr) {
        this.mUIThreadHandler.post(new Runnable() {
            public void run() {
                PGMenuItem[] pGMenuItemArr = pGMenuItemArr;
                if (pGMenuItemArr.length > 0) {
                    PhysiJNIView physiJNIView = PhysiJNIView.this;
                    physiJNIView.mItems = pGMenuItemArr;
                    ((MainActivity) physiJNIView.mActivity).mIsMenuShowing = true;
                    PhysiJNIView.this.mActivity.openContextMenu(PhysiJNIView.this);
                }
            }
        });
    }

    public void functionsForJNI(int i) {
        if (i == 32968) {
            Activity activity = this.mActivity;
            if (activity instanceof MainActivity) {
                activity.startActivity(MainActivity.getFilePickerIntent(activity, 3, false, ".vrcm", null));
            }
        }
    }

    public void editString(byte[] bArr) {
        DialogHelper.CreateStringDialog(getContext(), (bArr == null || bArr.length <= 0) ? "" : new String(bArr));
    }

    public boolean canExportFile() {
        return MainActivity.supportExport(getContext());
    }

    /* access modifiers changed from: protected */
    public void onCreateContextMenu(ContextMenu contextMenu) {
        PGMenuItem[] pGMenuItemArr;
        int i = 0;
        for (PGMenuItem pGMenuItem : this.mItems) {
            if (pGMenuItem != null) {
                if (pGMenuItem.mID == 0) {
                    contextMenu.setHeaderTitle(pGMenuItem.mCaption);
                } else {
                    String str = pGMenuItem.mCaption;
                    if (pGMenuItem.mIsChecked) {
                        str = mContext.getString(C0354R.string.menu_checked, new Object[]{str});
                    }
                    if (pGMenuItem.mType == 2 || (pGMenuItem.mType == 1 && pGMenuItem.mID == 34013)) {
                        str = mContext.getString(C0354R.string.menu_more, new Object[]{str});
                    }
                    contextMenu.add(0, i, 0, str).setOnMenuItemClickListener(this.mContextMenuListener);
                }
                i++;
            }
        }
        super.onCreateContextMenu(contextMenu);
    }

    public void showToast(String str, byte[][] bArr) {
        showToast(new JavaString(str, bArr).parsingStringWithIndex(getContext()));
    }

    public void showToast(String str) {
        Toast toast = this.mToast;
        if (toast != null) {
            toast.setText(str);
        } else {
            this.mToast = Toast.makeText(getContext(), str, 0);
        }
        this.mToast.show();
    }

    public void showDialog(final String str, byte[][] bArr, String str2, byte[][] bArr2) {
        String parsingStringWithIndex = new JavaString(str, bArr).parsingStringWithIndex(getContext());
        String parsingStringWithIndex2 = !TextUtils.isEmpty(str2) ? new JavaString(str2, bArr2).parsingStringWithIndex(getContext()) : "";
        Builder builder = new Builder(getContext());
        if (!TextUtils.isEmpty(parsingStringWithIndex2)) {
            builder.setTitle(parsingStringWithIndex2);
        }
        builder.setMessage(parsingStringWithIndex);
        builder.setNegativeButton(17039370, new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                if (!TextUtils.isEmpty(str) && str.equals("message_load_bcd_error")) {
                    PhysiJNIView.this.goToLandingPage();
                }
            }
        });
        builder.create().show();
    }

    /* access modifiers changed from: private */
    public void loadMyVRCM() {
        this.mMyVRCMPathes.clear();
        File file = new File(getVRCMPath());
        if (file.exists()) {
            String str = ".vrcm";
            ArrayList filter = FilePicker.filter(file.listFiles(), false, true, false, str);
            if (filter.size() > 0) {
                ArrayList arrayList = new ArrayList();
                Iterator it = filter.iterator();
                while (it.hasNext()) {
                    File file2 = (File) it.next();
                    if (file2.isFile()) {
                        String name = file2.getName();
                        if (name != null && name.length() > 6 && name.substring(name.length() - 5).equalsIgnoreCase(str)) {
                            this.mMyVRCMPathes.add(file2.getAbsolutePath());
                            PGMenuItem pGMenuItem = new PGMenuItem(name.substring(0, name.length() - 5), (((long) this.mMyVRCMPathes.size()) + MENU_MY_VRCM_START_ID) - 1, false, 3);
                            arrayList.add(pGMenuItem);
                        }
                        if (arrayList.size() > 0) {
                            PGMenuItem pGMenuItem2 = new PGMenuItem(getContext().getString(C0354R.string.menu_my_vrcm), 0, false, 3);
                            arrayList.add(0, pGMenuItem2);
                        }
                    }
                }
                if (arrayList.size() > 0) {
                    createMenu((PGMenuItem[]) arrayList.toArray(new PGMenuItem[arrayList.size()]));
                    return;
                }
            }
        }
        showToast(getContext().getString(C0354R.string.error_no_my_vrcm));
    }

    private String getVRCMPath() {
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.getAppDataPath(getContext()));
        sb.append("/VRCM");
        return sb.toString();
    }

    /* access modifiers changed from: private */
    public void chooseForSaveVRCM(String str) {
        DialogHelper.CreateFileSaveDialog(getContext(), null, ".vrcm", getVRCMPath(), new FileSaverCallback() {
            public void onFileSaved(String str) {
                MainActivity.functions(34014, str);
            }
        });
    }

    public int createView(int i, boolean z) {
        PhysiJNIChildView physiJNIChildView = new PhysiJNIChildView(getContext(), z);
        LayoutParams layoutParams = new LayoutParams(-1, -1);
        physiJNIChildView.setLayoutParams(layoutParams);
        if (i > getChildCount()) {
            i = getChildCount();
        }
        if (z) {
            physiJNIChildView.createToolBarLayout(physiJNIChildView.hashCode());
            physiJNIChildView.mToolBarLayout.setLayoutParams(layoutParams);
            addView(physiJNIChildView.mToolBarLayout, i);
        }
        addView(physiJNIChildView, i);
        return physiJNIChildView.hashCode();
    }

    private View getChildFromHash(int i) {
        if (getChildCount() > 0 && i != 0) {
            for (int i2 = 0; i2 < getChildCount(); i2++) {
                View childAt = getChildAt(i2);
                if (childAt != null && childAt.hashCode() == i) {
                    return childAt;
                }
            }
        }
        return null;
    }

    public void deleteView(int i) {
        final View childFromHash = getChildFromHash(i);
        if (childFromHash != null) {
            this.mUIThreadHandler.postDelayed(new Runnable() {
                public void run() {
                    View view = childFromHash;
                    if ((view instanceof PhysiJNIChildView) && ((PhysiJNIChildView) view).mToolBarLayout != null) {
                        PhysiJNIView.this.removeView(((PhysiJNIChildView) childFromHash).mToolBarLayout);
                    }
                    PhysiJNIView.this.removeView(childFromHash);
                }
            }, 100);
        }
    }

    public void setInvalid(int i, boolean z, int i2) {
        if (Constants.sDebug) {
            StringBuilder sb = new StringBuilder();
            sb.append("setInvalid with hash ");
            sb.append(i);
            sb.append(" isDynamic = ");
            sb.append(z);
            sb.append(" Delay = ");
            sb.append(i2);
            Log.i(TAG, sb.toString());
        }
        if (getChildCount() > 0) {
            int i3 = 0;
            if (i != 0) {
                View childFromHash = getChildFromHash(i);
                if (childFromHash != null && (childFromHash instanceof PhysiJNIChildView)) {
                    ((PhysiJNIChildView) childFromHash).postDraw(z, i2);
                    if (!z) {
                        while (i3 < getChildCount()) {
                            View childAt = getChildAt(i3);
                            if (childAt != childFromHash && (childAt instanceof PhysiJNIChildView)) {
                                ((PhysiJNIChildView) childAt).userActionOnOtherView();
                            }
                            i3++;
                        }
                        return;
                    }
                    return;
                }
                return;
            }
            while (i3 < getChildCount()) {
                View childAt2 = getChildAt(i3);
                if (childAt2 instanceof PhysiJNIChildView) {
                    ((PhysiJNIChildView) childAt2).postDraw(z, i2);
                }
                i3++;
            }
        }
    }

    public void invalidToolbar(int i) {
        View childFromHash = getChildFromHash(i);
        if (childFromHash != null && (childFromHash instanceof PhysiJNIChildView)) {
            ((PhysiJNIChildView) childFromHash).invalidToolbar();
        }
    }

    public void setChildSizeFromHash(int i, int i2, int i3, int i4, int i5) {
        if (getChildCount() > 0 && i != 0) {
            View childFromHash = getChildFromHash(i);
            if (childFromHash != null && (childFromHash instanceof PhysiJNIChildView)) {
                PhysiJNIChildView physiJNIChildView = (PhysiJNIChildView) childFromHash;
                LayoutParams layoutParams = new LayoutParams(i4, i5);
                layoutParams.setMargins(i2, i3, 0, 0);
                physiJNIChildView.setInputMatrix(i2, i3);
                physiJNIChildView.setLayoutParams(layoutParams);
                if (physiJNIChildView.mToolBarLayout != null) {
                    physiJNIChildView.mToolBarLayout.setLayoutParams(layoutParams);
                }
            }
        }
    }

    public boolean getPrefBoolean(String str, boolean z) {
        return this.mActivity.getSharedPreferences(Constants.PREF_NAME_SETTINGS, 0).getBoolean(str, z);
    }

    private void setUIInner(int i, int i2, int i3, int i4, int i5, int i6, boolean z) {
        MainActivity.setUI(i, i2, i3, i4, i5, z ? 0 : i6);
    }

    public boolean setUI(int i, boolean z) {
        int i2 = i;
        boolean z2 = z;
        boolean z3 = false;
        if (z2) {
            i2 = this.mUiType;
        } else if (i2 < -2 || i2 >= 8 || this.mUiType == i2) {
            return false;
        }
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(Constants.PREF_NAME_SETTINGS, 0);
        String str = "%d";
        if (i2 == -1) {
            i2 = Integer.parseInt(sharedPreferences.getString(Constants.PREF_KEY_DEFAULT_2D, String.format(str, new Object[]{Integer.valueOf(0)})));
        } else if (i2 == -2) {
            i2 = Integer.parseInt(sharedPreferences.getString(Constants.PREF_KEY_DEFAULT_3D, String.format(str, new Object[]{Integer.valueOf(3)})));
        }
        this.mUiType = i2;
        String str2 = "Layout";
        String str3 = "Layout change";
        GAHelper.sendTracker(getContext(), str2, str3, String.format(str, new Object[]{Integer.valueOf(i2)}), (long) i2);
        int width = getWidth();
        int height = getHeight();
        setUIInner(-1, 0, 0, 0, 0, 0, false);
        if (getResources().getConfiguration().orientation == 2) {
            z3 = true;
        }
        int i3 = this.mUiType;
        switch (i3) {
            case 0:
                setUIInner(0, 0, 0, width, height, 66, z);
                break;
            case 1:
                if (!z3) {
                    int i4 = height / 2;
                    int i5 = width;
                    int i6 = i4;
                    boolean z4 = z;
                    setUIInner(1, 0, 0, i5, i6, 66, z4);
                    setUIInner(0, 0, i4, i5, i6, 1, z4);
                    break;
                } else {
                    int i7 = width / 2;
                    int i8 = i7;
                    int i9 = height;
                    boolean z5 = z;
                    setUIInner(1, 0, 0, i8, i9, 66, z5);
                    setUIInner(0, i7, 0, i8, i9, 1, z5);
                    break;
                }
            case 2:
                if (!z3) {
                    int i10 = (height * 2) / 3;
                    boolean z6 = z;
                    setUIInner(0, 0, 0, width, i10, 1, z6);
                    int i11 = width / 3;
                    int i12 = i10;
                    int i13 = i11;
                    int i14 = height / 3;
                    setUIInner(1, 0, i12, i13, i14, 66, z6);
                    setUIInner(2, i11, i12, i13, i14, 68, z6);
                    setUIInner(3, (width * 2) / 3, i12, i13, i14, 72, z6);
                    break;
                } else {
                    int i15 = (width * 2) / 3;
                    boolean z7 = z;
                    setUIInner(0, 0, 0, i15, height, 1, z7);
                    int i16 = height / 3;
                    int i17 = i15;
                    int i18 = width / 3;
                    int i19 = i16;
                    setUIInner(1, i17, 0, i18, i19, 66, z7);
                    setUIInner(2, i17, (height * 2) / 3, i18, i19, 68, z7);
                    setUIInner(3, i17, i16, i18, i19, 72, z7);
                    break;
                }
            case 3:
                int i20 = width / 2;
                int i21 = height / 2;
                int i22 = i20;
                int i23 = i21;
                boolean z8 = z;
                setUIInner(0, 0, 0, i22, i23, 1, z8);
                setUIInner(1, i20, 0, i22, i23, 66, z8);
                int i24 = i21;
                setUIInner(2, 0, i24, i22, i23, 68, z8);
                setUIInner(3, i20, i24, i22, i23, 72, z8);
                break;
            case 4:
            case 5:
            case 6:
            case 7:
                setCustomizeLayout(i3 - 4, z2);
                break;
        }
        setUIInner(-1, 0, 0, 0, 0, -1, false);
        return true;
    }

    private boolean setCustomizeLayout(int i, boolean z) {
        int i2 = i;
        int width = getWidth();
        int height = getHeight();
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(Constants.PREF_NAME_LAYOUTS, 0);
        int layoutPrefValue = Constants.getLayoutPrefValue(sharedPreferences, PrefKeyOrder.num, i2, 0);
        if (layoutPrefValue <= 0) {
            return false;
        }
        for (int i3 = 0; i3 < layoutPrefValue; i3++) {
            int layoutPrefValue2 = Constants.getLayoutPrefValue(sharedPreferences, PrefKeyOrder.left, i2, i3);
            int layoutPrefValue3 = Constants.getLayoutPrefValue(sharedPreferences, PrefKeyOrder.top, i2, i3);
            int layoutPrefValue4 = Constants.getLayoutPrefValue(sharedPreferences, PrefKeyOrder.width, i2, i3);
            int layoutPrefValue5 = Constants.getLayoutPrefValue(sharedPreferences, PrefKeyOrder.height, i2, i3);
            int layoutPrefValue6 = Constants.getLayoutPrefValue(sharedPreferences, PrefKeyOrder.type, i2, i3);
            if (layoutPrefValue4 <= 0 || layoutPrefValue5 <= 0) {
                Log.e(TAG, String.format("View number %d incorrect, L:%d T:%d W:%d H:%d", new Object[]{Integer.valueOf(i3), Integer.valueOf(layoutPrefValue2), Integer.valueOf(layoutPrefValue3), Integer.valueOf(layoutPrefValue4), Integer.valueOf(layoutPrefValue5)}));
            } else {
                setUIInner((layoutPrefValue - i3) - 1, (int) (((float) (layoutPrefValue2 * width)) / 100.0f), (int) (((float) (layoutPrefValue3 * height)) / 100.0f), (int) (((float) (layoutPrefValue4 * width)) / 100.0f), (int) (((float) (layoutPrefValue5 * height)) / 100.0f), layoutPrefValue6, z);
            }
        }
        return true;
    }

    public void onExportFile(int i) {
        if (!(this.mActivity instanceof MainActivity)) {
            return;
        }
        if (canExportFile()) {
            ((MainActivity) this.mActivity).onExportFile(FileType.values()[i]);
        } else {
            showDonaDialog();
        }
    }

    public void showProgressDialog(int i, int i2, byte[] bArr, byte[] bArr2) {
        Handler handler = this.mUIThreadHandler;
        final byte[] bArr3 = bArr;
        final byte[] bArr4 = bArr2;
        final int i3 = i;
        final int i4 = i2;
        C03496 r1 = new Runnable() {
            public void run() {
                if (PhysiJNIView.this.mProgressDialog == null) {
                    PhysiJNIView physiJNIView = PhysiJNIView.this;
                    physiJNIView.mProgressDialog = new ProgressDialog(physiJNIView.getContext());
                    PhysiJNIView.this.mProgressDialog.setProgressStyle(1);
                }
                byte[] bArr = bArr3;
                if (bArr != null && bArr.length > 0) {
                    PhysiJNIView.this.mProgressDialog.setTitle(new JavaString(new String(bArr), null).parsingStringWithIndex(PhysiJNIView.this.getContext()));
                }
                byte[] bArr2 = bArr4;
                if (bArr2 != null && bArr2.length > 0) {
                    PhysiJNIView.this.mProgressDialog.setMessage(new JavaString(new String(bArr2), null).parsingStringWithIndex(PhysiJNIView.this.getContext()));
                }
                if (i3 >= 0) {
                    PhysiJNIView.this.mProgressDialog.setProgress(i3);
                }
                if (i4 > 0) {
                    PhysiJNIView.this.mProgressDialog.setMax(i4);
                }
                if (!PhysiJNIView.this.mProgressDialog.isShowing()) {
                    PhysiJNIView.this.mProgressDialog.show();
                    return;
                }
                int i = i4;
                if (i == i3 || i < 0) {
                    PhysiJNIView.this.mProgressDialog.dismiss();
                }
            }
        };
        handler.postAtFrontOfQueue(r1);
    }

    public void showDonaDialog() {
        Builder builder = new Builder(mContext);
        builder.setTitle(C0354R.string.settings_support);
        builder.setMessage(C0354R.string.dlg_enable_feature);
        builder.setPositiveButton(17039370, new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent();
                intent.setClass(PhysiJNIView.mContext, SupportActivity.class);
                PhysiJNIView.mContext.startActivity(intent);
            }
        });
        builder.setNegativeButton(C0354R.string.f13no, null);
        builder.create().show();
    }

    public void recordLastReadFile(byte[] bArr) {
        getContext().getSharedPreferences(Constants.PREF_NAME_SETTINGS, 0).edit().putString("LAST_READ_FILE_NAME", new String(bArr)).apply();
    }

    public void goToLandingPage() {
        MainActivity.goToLandingPageIfNoPatient(this.mActivity);
    }

    public void asyncLoadPixelData(int i) {
        if (i > 400) {
            DialogHelper.createResampleDialog(this.mActivity, i, new ResampleCallback() {
                public void onResample(int[] iArr) {
                    PhysiJNIView.this.asyncLoadPixelData(iArr[0], iArr[2]);
                }
            });
        } else {
            asyncLoadPixelData(1, 1);
        }
    }

    /* access modifiers changed from: private */
    public void asyncLoadPixelData(final int i, final int i2) {
        new AsyncTask<Void, Void, Integer>() {
            /* access modifiers changed from: protected */
            public void onPreExecute() {
                PhysiJNIView.showRingDialog(PhysiJNIView.this.getContext(), true, (int) C0354R.string.read_file_progress_title_pixel, 0);
            }

            /* access modifiers changed from: protected */
            public Integer doInBackground(Void... voidArr) {
                return Integer.valueOf(MainActivity.loadPixelAsync(i, i2));
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Integer num) {
                if (num == null || num.intValue() == 0) {
                    MainActivity.loadPixelAsync(-1, -1);
                    PhysiJNIView.showRingDialog(PhysiJNIView.this.getContext(), false, 0, 0);
                    return;
                }
                PhysiJNIView.showRingDialog(PhysiJNIView.this.getContext(), false, 0, 0);
                DialogHelper.createShiftDialog(PhysiJNIView.this.mActivity, new ShiftCallback(num.intValue()) {
                    public void onShift(int i) {
                        PhysiJNIView.this.asyncShiftPixelData(i);
                    }
                });
            }
        }.execute(new Void[0]);
    }

    /* access modifiers changed from: private */
    public void asyncShiftPixelData(final int i) {
        new AsyncTask<Void, Void, Void>() {
            /* access modifiers changed from: protected */
            public void onPreExecute() {
                PhysiJNIView.showRingDialog(PhysiJNIView.this.getContext(), true, (int) C0354R.string.read_file_progress_title_pixel, 0);
            }

            /* access modifiers changed from: protected */
            public Void doInBackground(Void... voidArr) {
                MainActivity.loadPixelDataAndDoShift(i);
                return null;
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Void voidR) {
                MainActivity.loadPixelAsync(-1, -1);
                PhysiJNIView.showRingDialog(PhysiJNIView.this.getContext(), false, 0, 0);
            }
        }.execute(new Void[0]);
    }

    public void showView(boolean z, int i) {
        View childFromHash = getChildFromHash(i);
        if (childFromHash != null) {
            int i2 = 0;
            childFromHash.setVisibility(z ? 0 : 4);
            if (childFromHash instanceof PhysiJNIChildView) {
                PhysiJNIChildView physiJNIChildView = (PhysiJNIChildView) childFromHash;
                if (physiJNIChildView.mToolBarLayout != null) {
                    ToolBarLayout toolBarLayout = physiJNIChildView.mToolBarLayout;
                    if (!z) {
                        i2 = 4;
                    }
                    toolBarLayout.setVisibility(i2);
                }
            }
        }
    }

    public void updateToolMenu() {
        this.mActivity.invalidateOptionsMenu();
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        return onTouchOnView(motionEvent, 0);
    }

    public boolean onTouchOnView(MotionEvent motionEvent, int i) {
        int i2;
        int i3;
        int i4;
        MotionEvent motionEvent2 = motionEvent;
        Point[] pointArr = new Point[motionEvent.getPointerCount()];
        for (int i5 = 0; i5 < motionEvent.getPointerCount(); i5++) {
            pointArr[i5] = new Point((int) motionEvent2.getX(i5), (int) motionEvent2.getY(i5));
        }
        if (pointArr.length > 1) {
            int i6 = pointArr[1].x;
            i2 = pointArr[1].y;
            i3 = i6;
            i4 = 2;
        } else {
            i4 = 1;
            i3 = 0;
            i2 = 0;
        }
        byte b = MainActivity.touch(motionEvent.getAction(), i4, pointArr[0].x, pointArr[0].y, i3, i2, Calendar.getInstance().getTimeInMillis(), i);
        if (Constants.sDebug) {
            Log.i(TAG, String.format("onTouchEvent , action:%d fingers:%d X1:%d,Y1:%d X2:%d,Y2:%d, viewHash:%d, result:%d", new Object[]{Integer.valueOf(motionEvent.getAction()), Integer.valueOf(i4), Integer.valueOf(pointArr[0].x), Integer.valueOf(pointArr[0].y), Integer.valueOf(i3), Integer.valueOf(i2), Integer.valueOf(i), Byte.valueOf(b)}));
        }
        if (b != 0) {
            this.mGestureDetector.onTouchEvent(motionEvent2);
        }
        if (b != 0) {
            return true;
        }
        return false;
    }
}
