package com.luolai.droidrender;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;
import com.luolai.droidrender.LayoutEditorActivity.LayoutItem;
import com.luolai.droidrender.LayoutEditorActivity.SingleView;
import java.util.Iterator;

public class LayoutView extends ImageView {
    private Paint mFramePaint;
    private Paint mInnerPaint;
    private Point mLastPosition = new Point();
    private LayoutItem mLayoutItem;
    private Paint mTextPaint;

    public LayoutView(Context context) {
        super(context);
        init();
    }

    public LayoutView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    public LayoutView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init();
    }

    public void setLayoutItem(LayoutItem layoutItem) {
        this.mLayoutItem = layoutItem;
    }

    private void init() {
        this.mFramePaint = new Paint();
        this.mFramePaint.setStyle(Style.STROKE);
        this.mFramePaint.setARGB(255, 230, 230, 230);
        this.mFramePaint.setStrokeWidth(5.0f);
        this.mInnerPaint = new Paint();
        this.mInnerPaint.setStyle(Style.FILL);
        this.mInnerPaint.setARGB(255, 80, 80, 80);
        this.mTextPaint = new Paint();
        this.mTextPaint.setTextSize((float) ((int) getContext().getResources().getDimension(C0354R.dimen.title_textsize)));
        this.mTextPaint.setARGB(255, 188, 188, 188);
        this.mTextPaint.setShadowLayer(4.0f, 0.0f, 0.0f, Color.argb(255, 0, 0, 0));
        this.mTextPaint.setTextAlign(Align.CENTER);
    }

    public SingleView getViewFromLastPosition() {
        LayoutItem layoutItem = this.mLayoutItem;
        if (layoutItem != null) {
            return layoutItem.getViewFromPosition(((float) this.mLastPosition.x) / ((float) getWidth()), ((float) this.mLastPosition.y) / ((float) getHeight()));
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        LayoutItem layoutItem = this.mLayoutItem;
        if (layoutItem != null && layoutItem.mViews != null && this.mLayoutItem.mViews.size() > 0) {
            Iterator it = this.mLayoutItem.mViews.iterator();
            while (it.hasNext()) {
                ((SingleView) it.next()).draw(canvas, this.mFramePaint, this.mInnerPaint, this.mTextPaint, getContext());
            }
        }
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        this.mLastPosition.x = (int) motionEvent.getX();
        this.mLastPosition.y = (int) motionEvent.getY();
        return super.onTouchEvent(motionEvent);
    }
}
