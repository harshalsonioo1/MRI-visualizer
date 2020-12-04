package com.luolai.droidrender;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import androidx.core.view.MotionEventCompat;
import androidx.core.view.ViewCompat;
import com.luolai.base.Entity;
import com.luolai.base.Entity.BitmapPack;
import com.luolai.base.Entity.DrawSchedule;
import com.luolai.base.Entity.JavaString;
import com.luolai.base.Entity.Point;
import com.luolai.base.ToolBarLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class PhysiJNIChildView extends ImageView {
    private static final float BACKGROUND_SCALE = 30.0f;
    private static final float CENTER_GAP = 5.0f;
    private static final float FACTOR = 2.2f;
    private static final int RADIUS = 35;
    private static final String TAG = "PhysiJNIChildView";
    private static final int WHAT_DYNAMIC_DRAW = 1;
    private static final int WHAT_NON_DYNAMIC_DRAW = 2;
    private static int sTextHeight;
    Matrix mAlphaMatrix;
    private Bitmap mBackground;
    Matrix mBackgroundMatrix;
    private HashMap<String, Bitmap> mCachedBitmap;
    private Matrix mEnlargeMatrix = new Matrix();
    Matrix mInputMatrix = new Matrix();
    boolean mIsBioView;
    private Paint mJNIPaint;
    Matrix mMatrix;
    float mOneDP;
    BitmapPack mPack;
    private Path mPath = new Path();
    Paint mSchedulePaint;
    long mStartTime;
    private Rect mTextBounds;
    private Paint mTextPaint;
    int mTextSize;
    ToolBarLayout mToolBarLayout = null;
    Handler mUIHandler = new Handler() {
        public void handleMessage(Message message) {
            if (message != null) {
                int i = message.what;
                String str = "PhysiJNIChildViewpostDraw";
                if (i == 1) {
                    if (Constants.sDebug) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("handle DynamicDraw! hash:");
                        sb.append(PhysiJNIChildView.this.hashCode());
                        Log.w(str, sb.toString());
                    }
                    PhysiJNIChildView.this.invokeDraw(true);
                    PhysiJNIChildView.this.invalidate();
                    return;
                } else if (i == 2) {
                    if (Constants.sDebug) {
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("handle Non-DynamicDraw! hash:");
                        sb2.append(PhysiJNIChildView.this.hashCode());
                        Log.w(str, sb2.toString());
                    }
                    PhysiJNIChildView.this.invokeDraw(false);
                    PhysiJNIChildView.this.invalidate();
                    return;
                }
            }
            super.handleMessage(message);
        }
    };
    int titleOffset = 0;

    public PhysiJNIChildView(Context context, boolean z) {
        super(context);
        this.mIsBioView = z;
        init();
    }

    public PhysiJNIChildView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    public PhysiJNIChildView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init();
    }

    /* access modifiers changed from: protected */
    public void onVisibilityChanged(View view, int i) {
        if (i == 8 || i == 4) {
            BitmapPack bitmapPack = this.mPack;
            if (!(bitmapPack == null || bitmapPack.mBaseBitmap == null)) {
                if (Constants.sDebug) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Recycle mBaseBitmap because this view will become invisible , hash : ");
                    sb.append(hashCode());
                    Log.i(TAG, sb.toString());
                }
                this.mPack.mBaseBitmap.recycle();
                this.mPack.mBaseBitmap = null;
            }
            Bitmap bitmap = this.mBackground;
            if (bitmap != null) {
                bitmap.recycle();
                this.mBackground = null;
            }
        } else if (i == 0) {
            BitmapPack bitmapPack2 = this.mPack;
            if (bitmapPack2 != null) {
                redraw(bitmapPack2);
                invalidate();
            }
        }
        super.onVisibilityChanged(view, i);
    }

    public void createToolBarLayout(int i) {
        if (this.mToolBarLayout == null) {
            this.mToolBarLayout = new ToolBarLayout(getContext(), i);
        }
    }

    public void setInputMatrix(int i, int i2) {
        this.mInputMatrix.reset();
        this.mInputMatrix.preTranslate((float) i, (float) i2);
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        int i;
        int i2;
        int i3;
        if (Constants.sDebug) {
            Point[] pointArr = new Point[motionEvent.getPointerCount()];
            for (int i4 = 0; i4 < motionEvent.getPointerCount(); i4++) {
                pointArr[i4] = new Point((int) motionEvent.getX(i4), (int) motionEvent.getY(i4));
            }
            if (pointArr.length > 1) {
                int i5 = pointArr[1].f11x;
                i = pointArr[1].f12y;
                i2 = i5;
                i3 = 2;
            } else {
                i3 = 1;
                i2 = 0;
                i = 0;
            }
            Log.i(TAG, String.format("onTouchEvent , action:%d fingers:%d X1:%d,Y1:%d X2:%d,Y2:%d, viewHash:%d", new Object[]{Integer.valueOf(motionEvent.getAction()), Integer.valueOf(i3), Integer.valueOf(pointArr[0].f11x), Integer.valueOf(pointArr[0].f12y), Integer.valueOf(i2), Integer.valueOf(i), Integer.valueOf(hashCode())}));
        }
        MotionEvent obtain = MotionEvent.obtain(motionEvent);
        obtain.transform(this.mInputMatrix);
        if (this.mIsBioView) {
            return false;
        }
        return ((PhysiJNIView) getParent()).onTouchOnView(obtain, hashCode());
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

    private Bitmap getBackground(int i, int i2) {
        int i3;
        int i4 = (int) ((((float) i) / BACKGROUND_SCALE) + 1.0f);
        int i5 = (int) ((((float) i2) / BACKGROUND_SCALE) + 1.0f);
        Bitmap bitmap = this.mBackground;
        if (bitmap != null && bitmap.getWidth() == i4 && this.mBackground.getHeight() == i5) {
            return this.mBackground;
        }
        short[][][] sArr = new short[2][][];
        char c = 0;
        int i6 = 0;
        while (true) {
            i3 = 3;
            if (i6 >= 2) {
                break;
            }
            sArr[i6] = new short[2][];
            for (int i7 = 0; i7 < 2; i7++) {
                sArr[i6][i7] = new short[3];
            }
            i6++;
        }
        sArr[0][0][0] = 75;
        sArr[0][0][1] = 132;
        sArr[0][0][2] = 82;
        sArr[1][0][0] = 90;
        sArr[1][0][1] = 82;
        sArr[1][0][2] = 75;
        sArr[0][1][0] = 15;
        sArr[0][1][1] = 35;
        sArr[0][1][2] = 10;
        sArr[1][1][0] = 25;
        sArr[1][1][1] = 25;
        sArr[1][1][2] = 75;
        int i8 = i4 * i5;
        int[] iArr = new int[i8];
        double d = (double) i8;
        Double.isNaN(d);
        double d2 = 1.0d / d;
        int i9 = 0;
        while (i9 < i4) {
            int i10 = 0;
            while (i10 < i5) {
                int i11 = 0;
                int i12 = 0;
                while (i11 < i3) {
                    int i13 = i4 - i9;
                    int[] iArr2 = iArr;
                    double d3 = (double) (sArr[c][1][i11] * i13 * i10);
                    Double.isNaN(d3);
                    double d4 = d3 * d2;
                    short[][][] sArr2 = sArr;
                    double d5 = (double) (sArr[1][1][i11] * i9 * i10);
                    Double.isNaN(d5);
                    double d6 = d4 + (d5 * d2);
                    int i14 = (i5 - i10) - 1;
                    int i15 = sArr2[c][c][i11] * i13 * i14;
                    int i16 = i9;
                    double d7 = (double) i15;
                    Double.isNaN(d7);
                    double d8 = d6 + (d7 * d2);
                    c = 0;
                    double d9 = (double) (sArr2[1][0][i11] * i16 * i14);
                    Double.isNaN(d9);
                    short s = (short) ((int) (d8 + (d9 * d2)));
                    if (s > 255) {
                        s = Entity.MAX_REGION_NUMBER;
                    } else if (s < 0) {
                        s = 0;
                    }
                    i12 |= s << ((2 - i11) * 8);
                    i11++;
                    i9 = i16;
                    iArr = iArr2;
                    sArr = sArr2;
                    i3 = 3;
                }
                short[][][] sArr3 = sArr;
                int i17 = i9;
                iArr[i17 + (i10 * i4)] = -16777216 | i12;
                i10++;
                i9 = i17;
                sArr = sArr3;
                i3 = 3;
            }
            int[] iArr3 = iArr;
            i9++;
            sArr = sArr;
            i3 = 3;
        }
        this.mBackground = Bitmap.createBitmap(iArr, i4, i5, Config.ARGB_8888).copy(Config.ARGB_8888, true);
        return this.mBackground;
    }

    private void init() {
        this.mTextPaint = new Paint();
        this.mTextPaint.setTextSize((float) ((int) getContext().getResources().getDimension(C0354R.dimen.title_textsize)));
        this.mTextPaint.setARGB(255, 188, 188, 188);
        this.mTextPaint.setShadowLayer(4.0f, 0.0f, 0.0f, Color.argb(255, 0, 0, 0));
        this.mJNIPaint = new Paint();
        this.mTextSize = (int) getContext().getResources().getDimension(C0354R.dimen.size_text);
        this.mJNIPaint.setTextSize((float) this.mTextSize);
        this.titleOffset = (int) getContext().getResources().getDimension(C0354R.dimen.title_offset);
        this.mTextBounds = new Rect();
        this.mSchedulePaint = new Paint();
        this.mSchedulePaint.setAntiAlias(true);
        this.mBackgroundMatrix = new Matrix();
        this.mBackgroundMatrix.preScale(BACKGROUND_SCALE, BACKGROUND_SCALE);
        this.mOneDP = getResources().getDimension(C0354R.dimen.onedp);
    }

    public void setInvalid() {
        if (Constants.sDebug) {
            Log.i(TAG, "setInvalid");
        }
        invalidate();
    }

    /* JADX WARNING: Removed duplicated region for block: B:41:0x00f4  */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x0138  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x013c  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0149  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x015b  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x0168  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void redraw(com.luolai.base.Entity.BitmapPack r13) {
        /*
            r12 = this;
            android.graphics.Bitmap r8 = r13.mBaseBitmap
            android.graphics.Bitmap r9 = r13.mAlphaBitmap
            r0 = 0
            if (r13 == 0) goto L_0x018d
            int[] r1 = r13.mBits
            if (r1 == 0) goto L_0x018d
            int r0 = android.os.Build.VERSION.SDK_INT
            r1 = 19
            java.lang.String r10 = "PhysiJNIChildView"
            r11 = 1
            if (r0 < r1) goto L_0x00a8
            if (r8 == 0) goto L_0x0067
            int r0 = r8.getAllocationByteCount()
            int r1 = r13.mSizeX
            int r2 = r13.mSizeY
            int r1 = r1 * r2
            int r1 = r1 * 4
            if (r0 >= r1) goto L_0x0025
            goto L_0x0067
        L_0x0025:
            int r0 = r8.getWidth()
            int r1 = r13.mSizeX
            byte r2 = r13.mResampleLevel
            int r1 = r1 / r2
            if (r0 != r1) goto L_0x003b
            int r0 = r8.getHeight()
            int r1 = r13.mSizeY
            byte r2 = r13.mResampleLevel
            int r1 = r1 / r2
            if (r0 == r1) goto L_0x004a
        L_0x003b:
            int r0 = r13.mSizeX
            byte r1 = r13.mResampleLevel
            int r0 = r0 / r1
            int r1 = r13.mSizeY
            byte r2 = r13.mResampleLevel
            int r1 = r1 / r2
            android.graphics.Bitmap$Config r2 = android.graphics.Bitmap.Config.ARGB_8888
            r8.reconfigure(r0, r1, r2)
        L_0x004a:
            int[] r1 = r13.mBits
            r2 = 0
            int r0 = r13.mSizeX
            byte r3 = r13.mResampleLevel
            int r3 = r0 / r3
            r4 = 0
            r5 = 0
            int r0 = r13.mSizeX
            byte r6 = r13.mResampleLevel
            int r6 = r0 / r6
            int r0 = r13.mSizeY
            byte r7 = r13.mResampleLevel
            int r7 = r0 / r7
            r0 = r8
            r0.setPixels(r1, r2, r3, r4, r5, r6, r7)
            goto L_0x00e6
        L_0x0067:
            int[] r0 = r13.mBits
            int r1 = r13.mSizeX
            int r2 = r13.mSizeY
            android.graphics.Bitmap$Config r3 = android.graphics.Bitmap.Config.ARGB_8888
            android.graphics.Bitmap r0 = android.graphics.Bitmap.createBitmap(r0, r1, r2, r3)
            android.graphics.Bitmap$Config r1 = android.graphics.Bitmap.Config.ARGB_8888
            android.graphics.Bitmap r0 = r0.copy(r1, r11)
            int r1 = r0.getWidth()
            int r2 = r13.mSizeX
            byte r3 = r13.mResampleLevel
            int r2 = r2 / r3
            if (r1 != r2) goto L_0x008f
            int r1 = r0.getHeight()
            int r2 = r13.mSizeY
            byte r3 = r13.mResampleLevel
            int r2 = r2 / r3
            if (r1 == r2) goto L_0x009e
        L_0x008f:
            int r1 = r13.mSizeX
            byte r2 = r13.mResampleLevel
            int r1 = r1 / r2
            int r2 = r13.mSizeY
            byte r3 = r13.mResampleLevel
            int r2 = r2 / r3
            android.graphics.Bitmap$Config r3 = android.graphics.Bitmap.Config.ARGB_8888
            r0.reconfigure(r1, r2, r3)
        L_0x009e:
            boolean r1 = com.luolai.droidrender.Constants.sDebug
            if (r1 == 0) goto L_0x00e5
            java.lang.String r1 = "Create Bitmap"
            android.util.Log.w(r10, r1)
            goto L_0x00e5
        L_0x00a8:
            int r0 = r13.mSizeX
            float r0 = (float) r0
            byte r1 = r13.mResampleLevel
            float r1 = (float) r1
            float r0 = r0 / r1
            int r6 = (int) r0
            int r0 = r13.mSizeY
            float r0 = (float) r0
            byte r1 = r13.mResampleLevel
            float r1 = (float) r1
            float r0 = r0 / r1
            int r7 = (int) r0
            if (r8 == 0) goto L_0x00d2
            int r0 = r8.getWidth()
            if (r0 != r6) goto L_0x00d2
            int r0 = r8.getHeight()
            if (r0 == r7) goto L_0x00c7
            goto L_0x00d2
        L_0x00c7:
            int[] r1 = r13.mBits
            r2 = 0
            r4 = 0
            r5 = 0
            r0 = r8
            r3 = r6
            r0.setPixels(r1, r2, r3, r4, r5, r6, r7)
            goto L_0x00e6
        L_0x00d2:
            if (r8 == 0) goto L_0x00d7
            r8.recycle()
        L_0x00d7:
            int[] r0 = r13.mBits
            android.graphics.Bitmap$Config r1 = android.graphics.Bitmap.Config.ARGB_8888
            android.graphics.Bitmap r0 = android.graphics.Bitmap.createBitmap(r0, r6, r7, r1)
            android.graphics.Bitmap$Config r1 = android.graphics.Bitmap.Config.ARGB_8888
            android.graphics.Bitmap r0 = r0.copy(r1, r11)
        L_0x00e5:
            r8 = r0
        L_0x00e6:
            int r0 = r13.mAlphaSizeX
            if (r0 <= 0) goto L_0x0128
            int r0 = r13.mAlphaSizeY
            if (r0 <= 0) goto L_0x0128
            boolean r0 = r13.mNeedDrawAlpha
            if (r0 == 0) goto L_0x0128
            if (r9 == 0) goto L_0x0115
            int r0 = r9.getWidth()
            int r1 = r13.mAlphaSizeX
            if (r0 != r1) goto L_0x0115
            int r0 = r9.getHeight()
            int r1 = r13.mAlphaSizeY
            if (r0 == r1) goto L_0x0105
            goto L_0x0115
        L_0x0105:
            int[] r1 = r13.mAlphaBits
            r2 = 0
            int r3 = r13.mAlphaSizeX
            r4 = 0
            r5 = 0
            int r6 = r13.mAlphaSizeX
            int r7 = r13.mAlphaSizeY
            r0 = r9
            r0.setPixels(r1, r2, r3, r4, r5, r6, r7)
            goto L_0x0128
        L_0x0115:
            int[] r0 = r13.mAlphaBits
            int r1 = r13.mAlphaSizeX
            int r2 = r13.mAlphaSizeY
            android.graphics.Bitmap$Config r3 = android.graphics.Bitmap.Config.ARGB_8888
            android.graphics.Bitmap r0 = android.graphics.Bitmap.createBitmap(r0, r1, r2, r3)
            android.graphics.Bitmap$Config r1 = android.graphics.Bitmap.Config.ARGB_8888
            android.graphics.Bitmap r0 = r0.copy(r1, r11)
            r9 = r0
        L_0x0128:
            android.graphics.Matrix r0 = new android.graphics.Matrix
            r0.<init>()
            android.graphics.Matrix r1 = new android.graphics.Matrix
            r1.<init>()
            byte r2 = r13.mResampleLevel
            r3 = 2
            r4 = 0
            if (r2 <= r11) goto L_0x013c
            byte r2 = r13.mResampleLevel
            int r2 = r2 / r3
            goto L_0x013d
        L_0x013c:
            r2 = 0
        L_0x013d:
            int r2 = -r2
            float r2 = (float) r2
            r0.preTranslate(r2, r2)
            r1.preTranslate(r2, r2)
            int r2 = r13.mType
            if (r2 != r11) goto L_0x015b
            byte r2 = r13.mResampleLevel
            float r2 = (float) r2
            float r5 = com.luolai.droidrender.MainActivity.m3DResample
            float r2 = r2 * r5
            byte r5 = r13.mResampleLevel
            float r5 = (float) r5
            float r6 = com.luolai.droidrender.MainActivity.m3DResample
            float r5 = r5 * r6
            r0.preScale(r2, r5)
            goto L_0x0164
        L_0x015b:
            byte r2 = r13.mResampleLevel
            float r2 = (float) r2
            byte r5 = r13.mResampleLevel
            float r5 = (float) r5
            r0.preScale(r2, r5)
        L_0x0164:
            boolean r2 = com.luolai.droidrender.Constants.sDebug
            if (r2 == 0) goto L_0x018e
            r2 = 3
            java.lang.Object[] r2 = new java.lang.Object[r2]
            byte r5 = r13.mResampleLevel
            java.lang.Byte r5 = java.lang.Byte.valueOf(r5)
            r2[r4] = r5
            int r4 = r13.mSizeX
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
            r2[r11] = r4
            int r4 = r13.mSizeY
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
            r2[r3] = r4
            java.lang.String r3 = "Draw , resample:%d ; X:%d,y:%d"
            java.lang.String r2 = java.lang.String.format(r3, r2)
            android.util.Log.i(r10, r2)
            goto L_0x018e
        L_0x018d:
            r1 = r0
        L_0x018e:
            r13.mBaseBitmap = r8
            r13.mAlphaBitmap = r9
            if (r0 == 0) goto L_0x0196
            r12.mMatrix = r0
        L_0x0196:
            if (r1 == 0) goto L_0x019a
            r12.mAlphaMatrix = r1
        L_0x019a:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.luolai.droidrender.PhysiJNIChildView.redraw(com.luolai.base.Entity$BitmapPack):void");
    }

    private Bitmap getBitmap(String str) {
        if (this.mCachedBitmap.containsKey(str)) {
            return (Bitmap) this.mCachedBitmap.get(str);
        }
        int identifier = getResources().getIdentifier(str, "drawable", BuildConfig.APPLICATION_ID);
        if (identifier <= 0) {
            return null;
        }
        Bitmap decodeResource = BitmapFactory.decodeResource(getResources(), identifier);
        this.mCachedBitmap.put(str, decodeResource);
        return decodeResource;
    }

    private void drawSchedule(Canvas canvas, DrawSchedule[] drawScheduleArr) {
        int i;
        int i2;
        Canvas canvas2 = canvas;
        DrawSchedule[] drawScheduleArr2 = drawScheduleArr;
        if (drawScheduleArr2 != null && drawScheduleArr2.length > 0) {
            int i3 = 0;
            for (DrawSchedule drawSchedule : drawScheduleArr2) {
                if (drawSchedule != null) {
                    this.mSchedulePaint.setARGB((drawSchedule.mBkColor & ViewCompat.MEASURED_STATE_MASK) >> 24, (drawSchedule.mBkColor & 16711680) >> 16, (drawSchedule.mBkColor & MotionEventCompat.ACTION_POINTER_INDEX_MASK) >> 8, drawSchedule.mBkColor & 255);
                    this.mSchedulePaint.setStrokeWidth(drawSchedule.mLineWidth);
                    if (drawSchedule.mType != 0) {
                        if (drawSchedule.mType == 1) {
                            this.mSchedulePaint.setStyle(Style.FILL_AND_STROKE);
                            Rect rect = drawSchedule.mRect;
                            canvas2.drawRect(rect, this.mSchedulePaint);
                            if (drawSchedule.mLineColor != 0) {
                                this.mSchedulePaint.setStyle(Style.STROKE);
                                this.mSchedulePaint.setARGB((drawSchedule.mLineColor & ViewCompat.MEASURED_STATE_MASK) >> 24, (drawSchedule.mLineColor & 16711680) >> 16, (drawSchedule.mLineColor & MotionEventCompat.ACTION_POINTER_INDEX_MASK) >> 8, drawSchedule.mLineColor & 255);
                                canvas2.drawRect(rect, this.mSchedulePaint);
                            }
                            if (!TextUtils.isEmpty(drawSchedule.mIconResource)) {
                                if ((drawSchedule.mFlag & 8) > 0) {
                                    Bitmap bitmap = getBitmap(drawSchedule.mIconResource);
                                    canvas2.drawBitmap(bitmap, (float) (rect.left + ((rect.width() - bitmap.getWidth()) / 2)), (float) (rect.top + ((rect.height() - bitmap.getHeight()) / 2)), this.mSchedulePaint);
                                } else if ((drawSchedule.mFlag & 4) > 0) {
                                    this.mSchedulePaint.setStyle(Style.FILL);
                                    this.mSchedulePaint.setTextSize(((float) getTextHeight()) * 1.2f);
                                    this.mSchedulePaint.setTextAlign(Align.CENTER);
                                    int width = rect.left + (rect.width() / 2);
                                    int height = (int) (((float) rect.top) + (((((float) rect.height()) - this.mSchedulePaint.descent()) - this.mSchedulePaint.ascent()) / 2.0f));
                                    this.mSchedulePaint.setARGB((drawSchedule.mFontcolor & ViewCompat.MEASURED_STATE_MASK) >> 24, (drawSchedule.mFontcolor & 16711680) >> 16, (drawSchedule.mFontcolor & MotionEventCompat.ACTION_POINTER_INDEX_MASK) >> 8, drawSchedule.mFontcolor & 255);
                                    canvas2.drawText(new JavaString(drawSchedule.mIconResource, drawSchedule.mBindexes).parsingStringWithIndex(getContext()), (float) width, (float) height, this.mSchedulePaint);
                                }
                            }
                        } else if (drawSchedule.mType == 2 && drawSchedule.mPoints != null && drawSchedule.mPoints.length > 0) {
                            this.mSchedulePaint.setStyle(Style.STROKE);
                            boolean z = (drawSchedule.mFlag & 1) > 0;
                            drawSchedule.mRectF.set(drawSchedule.mPoints[0] - drawSchedule.mRadius, drawSchedule.mPoints[1] - drawSchedule.mRadius, drawSchedule.mPoints[0] + drawSchedule.mRadius, drawSchedule.mPoints[1] + drawSchedule.mRadius);
                            if (z) {
                                this.mSchedulePaint.setStrokeWidth(drawSchedule.mLineWidth + 2.0f);
                                i3 = 0;
                                this.mSchedulePaint.setARGB((drawSchedule.mBkColor & ViewCompat.MEASURED_STATE_MASK) >> 24, 0, 0, 0);
                                canvas.drawArc(drawSchedule.mRectF, drawSchedule.mStartAngle, drawSchedule.mSweepAngle, false, this.mSchedulePaint);
                                this.mSchedulePaint.setStrokeWidth(drawSchedule.mLineWidth);
                                this.mSchedulePaint.setARGB((drawSchedule.mLineColor & ViewCompat.MEASURED_STATE_MASK) >> 24, (drawSchedule.mLineColor & 16711680) >> 16, (drawSchedule.mLineColor & MotionEventCompat.ACTION_POINTER_INDEX_MASK) >> 8, drawSchedule.mLineColor & 255);
                            } else {
                                i3 = 0;
                            }
                            canvas.drawArc(drawSchedule.mRectF, drawSchedule.mStartAngle, drawSchedule.mSweepAngle, false, this.mSchedulePaint);
                        }
                        i3 = 0;
                    } else if (drawSchedule.mPoints != null && drawSchedule.mPoints.length > 0) {
                        this.mSchedulePaint.setStyle(Style.STROKE);
                        boolean z2 = (drawSchedule.mFlag & 1) > 0;
                        if (TextUtils.isEmpty(drawSchedule.mIconResource)) {
                            if (z2) {
                                this.mSchedulePaint.setStrokeWidth(drawSchedule.mLineWidth + 2.0f);
                                this.mSchedulePaint.setARGB((drawSchedule.mBkColor & ViewCompat.MEASURED_STATE_MASK) >> 24, i3, i3, i3);
                                canvas2.drawLines(drawSchedule.mPoints, this.mSchedulePaint);
                                this.mSchedulePaint.setStrokeWidth(drawSchedule.mLineWidth);
                                this.mSchedulePaint.setARGB((drawSchedule.mBkColor & ViewCompat.MEASURED_STATE_MASK) >> 24, (drawSchedule.mBkColor & 16711680) >> 16, (drawSchedule.mBkColor & MotionEventCompat.ACTION_POINTER_INDEX_MASK) >> 8, drawSchedule.mBkColor & 255);
                            }
                            canvas2.drawLines(drawSchedule.mPoints, this.mSchedulePaint);
                        } else {
                            this.mSchedulePaint.setStyle(Style.FILL);
                            this.mSchedulePaint.setTextSize(((float) getTextHeight()) * 1.2f);
                            if ((drawSchedule.mFlag & 2) > 0) {
                                this.mSchedulePaint.setTextAlign(Align.LEFT);
                            } else if ((drawSchedule.mFlag & 4) > 0) {
                                this.mSchedulePaint.setTextAlign(Align.RIGHT);
                            } else {
                                this.mSchedulePaint.setTextAlign(Align.CENTER);
                            }
                            int i4 = (int) drawSchedule.mPoints[i3];
                            int i5 = (int) (drawSchedule.mPoints[1] + (drawSchedule.mLineWidth / 2.0f));
                            this.mSchedulePaint.setTextSize(drawSchedule.mLineWidth);
                            this.mSchedulePaint.setARGB((drawSchedule.mFontcolor & ViewCompat.MEASURED_STATE_MASK) >> 24, (drawSchedule.mFontcolor & 16711680) >> 16, (drawSchedule.mFontcolor & MotionEventCompat.ACTION_POINTER_INDEX_MASK) >> 8, drawSchedule.mFontcolor & 255);
                            if (drawSchedule.mPoints.length > 4) {
                                canvas.save();
                                canvas2.rotate(drawSchedule.mPoints[2], (float) i4, (float) i5);
                                i2 = (int) drawSchedule.mPoints[3];
                                i = (int) drawSchedule.mPoints[4];
                            } else {
                                i2 = 0;
                                i = 0;
                            }
                            String parsingStringWithIndex = new JavaString(drawSchedule.mIconResource, drawSchedule.mBindexes).parsingStringWithIndex(getContext());
                            if (z2) {
                                this.mSchedulePaint.setShadowLayer(3.0f, 0.0f, 0.0f, Color.argb(255, i3, i3, i3));
                            } else {
                                this.mSchedulePaint.setShadowLayer(0.0f, 0.0f, 0.0f, i3);
                            }
                            canvas2.drawText(parsingStringWithIndex, (float) (i4 + i2), (float) (i5 + i), this.mSchedulePaint);
                            if (drawSchedule.mPoints.length > 3) {
                                canvas.restore();
                            }
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void invokeDraw(boolean z) {
        this.mStartTime = System.nanoTime();
        BitmapPack draw = MainActivity.draw(hashCode(), z);
        if (draw != null) {
            redraw(draw);
        }
        this.mPack = draw;
    }

    public void userActionOnOtherView() {
        if (this.mUIHandler.hasMessages(1) && !MainActivity.isViewFocused(hashCode())) {
            this.mUIHandler.removeMessages(1);
            this.mUIHandler.sendEmptyMessageDelayed(1, 500);
        }
    }

    public void postDraw(boolean z, int i) {
        String str = "PhysiJNIChildViewpostDraw";
        if (!z) {
            this.mUIHandler.removeMessages(1);
            this.mUIHandler.removeMessages(2);
            this.mUIHandler.sendEmptyMessage(2);
            if (Constants.sDebug) {
                StringBuilder sb = new StringBuilder();
                sb.append("postDraw! hash:");
                sb.append(hashCode());
                Log.w(str, sb.toString());
            }
        } else if (!this.mUIHandler.hasMessages(2)) {
            this.mUIHandler.removeMessages(1);
            this.mUIHandler.sendEmptyMessageDelayed(1, i <= 1 ? 1 : (long) i);
            if (Constants.sDebug) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("post DynamicDraw! delay = ");
                if (i <= 1) {
                    i = 1;
                }
                sb2.append(i);
                sb2.append(" hash:");
                sb2.append(hashCode());
                Log.w(str, sb2.toString());
            }
        } else if (Constants.sDebug) {
            StringBuilder sb3 = new StringBuilder();
            sb3.append("post DynamicDraw but there is already WHAT_NON_DYNAMIC_DRAW ! hash:");
            sb3.append(hashCode());
            Log.w(str, sb3.toString());
        }
    }

    public void invalidToolbar() {
        ToolBarLayout toolBarLayout = this.mToolBarLayout;
        if (toolBarLayout != null) {
            toolBarLayout.updateMenu();
        }
    }

    private void drawEnlargePoints(Canvas canvas, BitmapPack bitmapPack, Bitmap bitmap) {
        Canvas canvas2 = canvas;
        BitmapPack bitmapPack2 = bitmapPack;
        if (bitmapPack2.mEnlargePoints != null && bitmapPack2.mEnlargePoints.size() > 0) {
            ArrayList<Point> arrayList = bitmapPack2.mEnlargePoints;
            Point point = new Point();
            int i = (int) (this.mOneDP * 35.0f);
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                Point point2 = (Point) it.next();
                int i2 = i * 2;
                double d = (double) point2.f12y;
                double d2 = (double) i;
                Double.isNaN(d2);
                if (d < d2 * 2.5d) {
                    i2 = -i2;
                }
                this.mEnlargeMatrix.set(this.mMatrix);
                this.mEnlargeMatrix.preScale(FACTOR, FACTOR);
                if (bitmapPack2.mType == 0) {
                    this.mEnlargeMatrix.postTranslate(((float) (bitmapPack2.mXshift2D - point2.f11x)) * 1.2f, (((float) (bitmapPack2.mYshift2D - point2.f12y)) * 1.2f) - ((float) i2));
                } else {
                    this.mEnlargeMatrix.postTranslate(((float) point2.f11x) * -1.2f, (((float) point2.f12y) * -1.2f) - ((float) i2));
                }
                this.mPath.reset();
                point.f11x = point2.f11x;
                point.f12y = point2.f12y - i2;
                this.mPath.addCircle((float) point.f11x, (float) point.f12y, (float) i, Direction.CW);
                canvas.save();
                canvas2.clipPath(this.mPath);
                canvas2.drawARGB(128, 0, 0, 0);
                canvas2.drawBitmap(bitmap, this.mEnlargeMatrix, null);
                canvas.restore();
                this.mSchedulePaint.setStrokeWidth(4.0f);
                this.mSchedulePaint.setARGB(180, 0, 0, 0);
                canvas.drawLine((((float) point.f11x) - (this.mOneDP * CENTER_GAP)) + 1.0f, (float) point.f12y, (float) (point.f11x - i), (float) point.f12y, this.mSchedulePaint);
                canvas.drawLine((((float) point.f11x) + (this.mOneDP * CENTER_GAP)) - 1.0f, (float) point.f12y, (float) (point.f11x + i), (float) point.f12y, this.mSchedulePaint);
                canvas.drawLine((float) point.f11x, (((float) point.f12y) - (this.mOneDP * CENTER_GAP)) + 1.0f, (float) point.f11x, (float) (point.f12y - i), this.mSchedulePaint);
                canvas.drawLine((float) point.f11x, (((float) point.f12y) + (this.mOneDP * CENTER_GAP)) - 1.0f, (float) point.f11x, (float) (point.f12y + i), this.mSchedulePaint);
                this.mSchedulePaint.setStrokeWidth(2.0f);
                this.mSchedulePaint.setARGB(255, 220, 220, 220);
                canvas.drawLine(((float) point.f11x) - (this.mOneDP * CENTER_GAP), (float) point.f12y, (float) (point.f11x - i), (float) point.f12y, this.mSchedulePaint);
                Canvas canvas3 = canvas;
                canvas3.drawLine((this.mOneDP * CENTER_GAP) + ((float) point.f11x), (float) point.f12y, (float) (point.f11x + i), (float) point.f12y, this.mSchedulePaint);
                canvas.drawLine((float) point.f11x, ((float) point.f12y) - (this.mOneDP * CENTER_GAP), (float) point.f11x, (float) (point.f12y - i), this.mSchedulePaint);
                Canvas canvas4 = canvas;
                canvas4.drawLine((float) point.f11x, (this.mOneDP * CENTER_GAP) + ((float) point.f12y), (float) point.f11x, (float) (point.f12y + i), this.mSchedulePaint);
            }
            arrayList.clear();
        }
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        String str;
        boolean z = Constants.sDebug;
        String str2 = TAG;
        if (z) {
            StringBuilder sb = new StringBuilder();
            sb.append("onDraw, hash : ");
            sb.append(hashCode());
            Log.i(str2, sb.toString());
        }
        BitmapPack bitmapPack = this.mPack;
        if (bitmapPack != null) {
            if (bitmapPack.mType == 1) {
                if (this.mPack.mNeedBackGround) {
                    canvas.drawBitmap(getBackground((int) (((float) this.mPack.mSizeX) * MainActivity.m3DResample), (int) (((float) this.mPack.mSizeY) * MainActivity.m3DResample)), this.mBackgroundMatrix, null);
                } else {
                    canvas.drawARGB(255, 0, 0, 0);
                }
            }
            if (this.mPack.mType == 0) {
                canvas.drawARGB(255, 63, 63, 63);
            }
            if (this.mPack.mBaseBitmap != null) {
                canvas.drawBitmap(this.mPack.mBaseBitmap, this.mMatrix, null);
            }
            if (this.mPack.mAlphaSizeX > 0 && this.mPack.mAlphaSizeY > 0 && this.mAlphaMatrix != null && this.mPack.mNeedDrawAlpha) {
                canvas.drawBitmap(this.mPack.mAlphaBitmap, this.mAlphaMatrix, null);
            }
            drawSchedule(canvas, this.mPack.mDrawSchedule);
            drawSchedule(canvas, this.mPack.mJAVADrawSchedule);
            int i = this.mPack.mViewType;
            int i2 = i != 1 ? i != 66 ? i != 68 ? i != 72 ? i != 81 ? 0 : C0354R.string.viewtitle_2d_any : C0354R.string.viewtitle_2d_xz : C0354R.string.viewtitle_2d_yz : C0354R.string.viewtitle_2d_xy : C0354R.string.viewtitle_3d;
            if (i2 != 0) {
                if (i2 == C0354R.string.viewtitle_3d) {
                    str = getContext().getString(i2, new Object[]{Float.valueOf(this.mPack.mScale), Integer.valueOf(this.mPack.mMaxSlice)});
                } else {
                    str = getContext().getString(i2, new Object[]{Float.valueOf(this.mPack.mScale), new String(this.mPack.mCurrentSlice), Integer.valueOf(this.mPack.mMaxSlice)});
                }
                if (Constants.sDebug && this.mPack.mIsfinalDraw) {
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append(str);
                    sb2.append(" +");
                    str = sb2.toString();
                }
                this.mTextPaint.getTextBounds(str, 0, str.length(), this.mTextBounds);
                int i3 = this.titleOffset;
                canvas.drawText(str, (float) (i3 + 1), (float) ((i3 - this.mTextBounds.top) + 1), this.mTextPaint);
                if (Constants.sDebug) {
                    String format = String.format("Resample:%d", new Object[]{Byte.valueOf(this.mPack.mResampleLevel)});
                    int i4 = this.titleOffset;
                    canvas.drawText(format, (float) (i4 + 1), (float) ((i4 - this.mTextBounds.top) + (this.mTextBounds.height() * 1) + 1), this.mTextPaint);
                }
            }
            if (this.mPack.mBaseBitmap != null) {
                BitmapPack bitmapPack2 = this.mPack;
                drawEnlargePoints(canvas, bitmapPack2, bitmapPack2.mBaseBitmap);
            }
            if (Constants.sDebug) {
                double nanoTime = (double) (System.nanoTime() - this.mStartTime);
                Double.isNaN(nanoTime);
                float f = (float) (nanoTime / 1.0E9d);
                String format2 = String.format("Cost time:%.3f, FPS:%.3f", new Object[]{Float.valueOf(f), Float.valueOf(1.0f / f)});
                int i5 = this.titleOffset;
                canvas.drawText(format2, (float) (i5 + 1), (float) ((i5 - this.mTextBounds.top) + (this.mTextBounds.height() * 2) + 1), this.mTextPaint);
                if (this.mPack.mLastDrawingTime != 0.0f) {
                    String format3 = String.format("Full drawing time:%.3f", new Object[]{Float.valueOf(this.mPack.mLastDrawingTime)});
                    int i6 = this.titleOffset;
                    canvas.drawText(format3, (float) (i6 + 1), (float) ((i6 - this.mTextBounds.top) + (this.mTextBounds.height() * 3) + 1), this.mTextPaint);
                }
                StringBuilder sb3 = new StringBuilder();
                sb3.append("onDraw finish, hash : ");
                sb3.append(hashCode());
                Log.i(str2, sb3.toString());
            }
        }
    }
}
