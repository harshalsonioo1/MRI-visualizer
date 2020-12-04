package com.luolai.droidrender;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import com.luolai.base.Entity;
import com.luolai.base.GAHelper;
import java.util.ArrayList;
import java.util.Iterator;

public class LayoutEditorActivity extends BaseActivity {
    private static final int ID_MENU_EDIT = 0;
    private static final int ID_MENU_MOVE_TO_BOTTOM = 3;
    private static final int ID_MENU_MOVE_TO_TOP = 2;
    private static final int ID_MENU_REMOVE = 1;
    public static final String KEY_INITIAL_LAYOUT = "key_init_layout";
    private static final String TAG = LayoutEditorActivity.class.getSimpleName();
    private int mCurrentLayout = 0;
    /* access modifiers changed from: private */
    public LayoutView mImageView;
    /* access modifiers changed from: private */
    public LayoutAdapter mLayoutAdapter;
    private ArrayList<LayoutItem> mLayoutItems = new ArrayList<>();

    /* renamed from: com.luolai.droidrender.LayoutEditorActivity$8 */
    static /* synthetic */ class C03068 {
        static final /* synthetic */ int[] $SwitchMap$com$luolai$droidrender$Constants$PrefKeyOrder = new int[PrefKeyOrder.values().length];

        /* JADX WARNING: Can't wrap try/catch for region: R(10:0|1|2|3|4|5|6|7|8|10) */
        /* JADX WARNING: Can't wrap try/catch for region: R(8:0|1|2|3|4|5|6|(3:7|8|10)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0014 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001f */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x002a */
        static {
            /*
                com.luolai.droidrender.Constants$PrefKeyOrder[] r0 = com.luolai.droidrender.Constants.PrefKeyOrder.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$luolai$droidrender$Constants$PrefKeyOrder = r0
                int[] r0 = $SwitchMap$com$luolai$droidrender$Constants$PrefKeyOrder     // Catch:{ NoSuchFieldError -> 0x0014 }
                com.luolai.droidrender.Constants$PrefKeyOrder r1 = com.luolai.droidrender.Constants.PrefKeyOrder.left     // Catch:{ NoSuchFieldError -> 0x0014 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0014 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0014 }
            L_0x0014:
                int[] r0 = $SwitchMap$com$luolai$droidrender$Constants$PrefKeyOrder     // Catch:{ NoSuchFieldError -> 0x001f }
                com.luolai.droidrender.Constants$PrefKeyOrder r1 = com.luolai.droidrender.Constants.PrefKeyOrder.top     // Catch:{ NoSuchFieldError -> 0x001f }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001f }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001f }
            L_0x001f:
                int[] r0 = $SwitchMap$com$luolai$droidrender$Constants$PrefKeyOrder     // Catch:{ NoSuchFieldError -> 0x002a }
                com.luolai.droidrender.Constants$PrefKeyOrder r1 = com.luolai.droidrender.Constants.PrefKeyOrder.width     // Catch:{ NoSuchFieldError -> 0x002a }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x002a }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x002a }
            L_0x002a:
                int[] r0 = $SwitchMap$com$luolai$droidrender$Constants$PrefKeyOrder     // Catch:{ NoSuchFieldError -> 0x0035 }
                com.luolai.droidrender.Constants$PrefKeyOrder r1 = com.luolai.droidrender.Constants.PrefKeyOrder.height     // Catch:{ NoSuchFieldError -> 0x0035 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0035 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0035 }
            L_0x0035:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.luolai.droidrender.LayoutEditorActivity.C03068.<clinit>():void");
        }
    }

    private static class ContextMenuListener implements OnCreateContextMenuListener, OnMenuItemClickListener {
        LayoutEditorActivity mActivity;
        SingleView mItem;

        public ContextMenuListener(LayoutEditorActivity layoutEditorActivity) {
            this.mActivity = layoutEditorActivity;
        }

        public boolean onMenuItemClick(MenuItem menuItem) {
            int itemId = menuItem.getItemId();
            if (itemId == 0) {
                this.mActivity.addOrEditView(this.mItem);
            } else if (itemId == 1) {
                this.mActivity.removeView(this.mItem);
            } else if (itemId == 2) {
                this.mActivity.changeViewOrder(this.mItem, true);
            } else if (itemId == 3) {
                this.mActivity.changeViewOrder(this.mItem, false);
            }
            return true;
        }

        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenuInfo contextMenuInfo) {
            this.mItem = null;
            if (view instanceof LayoutView) {
                this.mItem = ((LayoutView) view).getViewFromLastPosition();
            }
            if (this.mItem != null) {
                contextMenu.add(0, 0, 0, view.getContext().getString(C0354R.string.menu_edit)).setOnMenuItemClickListener(this);
                contextMenu.add(0, 1, 1, view.getContext().getString(C0354R.string.menu_remove_tag)).setOnMenuItemClickListener(this);
                contextMenu.add(0, 2, 2, view.getContext().getString(C0354R.string.menu_move_to_top)).setOnMenuItemClickListener(this);
                contextMenu.add(0, 3, 3, view.getContext().getString(C0354R.string.menu_move_to_bottom)).setOnMenuItemClickListener(this);
            }
        }
    }

    private static class LayoutAdapter extends BaseAdapter {
        private Context mContext;
        private LayoutItem mLayoutItem;
        private LayoutView mLayoutView;

        public Object getItem(int i) {
            return null;
        }

        public long getItemId(int i) {
            return 0;
        }

        public LayoutAdapter(Context context, LayoutItem layoutItem, LayoutView layoutView) {
            this.mContext = context;
            this.mLayoutItem = layoutItem;
            this.mLayoutView = layoutView;
        }

        public void notifyDataSetChanged() {
            this.mLayoutView.invalidate();
        }

        public void switchLayout(LayoutItem layoutItem) {
            if (this.mLayoutItem != layoutItem) {
                this.mLayoutItem = layoutItem;
                notifyDataSetChanged();
            }
        }

        public void addView(SingleView singleView) {
            this.mLayoutItem.addView(singleView);
            notifyDataSetChanged();
        }

        public void removeView(SingleView singleView) {
            this.mLayoutItem.removeView(singleView);
            notifyDataSetChanged();
        }

        public void changeViewOrder(SingleView singleView, boolean z) {
            if (this.mLayoutItem.mViews.contains(singleView)) {
                this.mLayoutItem.mViews.remove(singleView);
                if (z) {
                    this.mLayoutItem.mViews.add(singleView);
                } else {
                    this.mLayoutItem.mViews.add(0, singleView);
                }
                notifyDataSetChanged();
            }
        }

        public int getCount() {
            return this.mLayoutItem.getViewNum();
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = View.inflate(this.mContext, C0354R.layout.list_item, null);
            }
            if (view instanceof TextView) {
                ((TextView) view).setText(this.mLayoutItem.getViewTitle(this.mContext, i));
            }
            return view;
        }
    }

    public static class LayoutItem {
        private int mLayoutNum;
        private int mOrientation;
        public ArrayList<SingleView> mViews = new ArrayList<>();

        public LayoutItem(SharedPreferences sharedPreferences, int i) {
            this.mLayoutNum = i;
            int layoutPrefValue = Constants.getLayoutPrefValue(sharedPreferences, PrefKeyOrder.num, i, 0);
            this.mOrientation = Constants.getLayoutPrefValue(sharedPreferences, PrefKeyOrder.orientation, i, 0, 1);
            for (int i2 = 0; i2 < layoutPrefValue; i2++) {
                this.mViews.add(new SingleView(sharedPreferences, this.mLayoutNum, i2));
            }
        }

        public SingleView getViewFromPosition(float f, float f2) {
            if (this.mViews.size() > 0) {
                for (int size = this.mViews.size() - 1; size >= 0; size--) {
                    SingleView singleView = (SingleView) this.mViews.get(size);
                    if (singleView.inArea(f, f2)) {
                        return singleView;
                    }
                }
            }
            return null;
        }

        public int getViewNum() {
            return this.mViews.size();
        }

        public String getViewTitle(Context context, int i) {
            return (i < 0 || i >= getViewNum()) ? "" : ((SingleView) this.mViews.get(i)).getDisplayString(context);
        }

        public void addView(SingleView singleView) {
            this.mViews.add(singleView);
        }

        public void removeView(SingleView singleView) {
            this.mViews.remove(singleView);
        }

        public SingleView getView(int i) {
            if (i < 0 || i >= this.mViews.size()) {
                return null;
            }
            return (SingleView) this.mViews.get(i);
        }

        public void writeToPref(Editor editor) {
            if (getViewNum() > 0) {
                int i = 0;
                editor.putInt(Constants.getLayoutPrefKey(PrefKeyOrder.num, this.mLayoutNum, 0), getViewNum());
                editor.putInt(Constants.getLayoutPrefKey(PrefKeyOrder.orientation, this.mLayoutNum, 0), this.mOrientation);
                Iterator it = this.mViews.iterator();
                while (it.hasNext()) {
                    ((SingleView) it.next()).writeToPref(editor, this.mLayoutNum, i);
                    i++;
                }
            }
        }
    }

    public static class SingleView {
        public int mHeight;
        public int mLeft;
        public int mTop;
        public int mViewType;
        public int mWidth;

        public SingleView(SharedPreferences sharedPreferences, int i, int i2) {
            this.mLeft = Constants.getLayoutPrefValue(sharedPreferences, PrefKeyOrder.left, i, i2);
            this.mTop = Constants.getLayoutPrefValue(sharedPreferences, PrefKeyOrder.top, i, i2);
            this.mWidth = Constants.getLayoutPrefValue(sharedPreferences, PrefKeyOrder.width, i, i2);
            this.mHeight = Constants.getLayoutPrefValue(sharedPreferences, PrefKeyOrder.height, i, i2);
            this.mViewType = Constants.getLayoutPrefValue(sharedPreferences, PrefKeyOrder.type, i, i2);
        }

        public SingleView() {
            this.mLeft = 0;
            this.mTop = 0;
            this.mWidth = 10;
            this.mHeight = 10;
            this.mViewType = 1;
        }

        public SingleView(SingleView singleView) {
            copy(singleView);
        }

        public void copy(SingleView singleView) {
            this.mLeft = singleView.mLeft;
            this.mTop = singleView.mTop;
            this.mWidth = singleView.mWidth;
            this.mHeight = singleView.mHeight;
            this.mViewType = singleView.mViewType;
        }

        public String getDisplayString(Context context) {
            return context.getString(C0354R.string.single_view_display, new Object[]{Float.valueOf(((float) this.mLeft) / 100.0f), Float.valueOf(((float) this.mTop) / 100.0f), Float.valueOf(((float) this.mWidth) / 100.0f), Float.valueOf(((float) this.mHeight) / 100.0f), Constants.getViewTypeString(context, this.mViewType)});
        }

        public boolean inArea(float f, float f2) {
            int i = (int) (f * 100.0f);
            int i2 = (int) (f2 * 100.0f);
            int i3 = this.mLeft;
            if (i >= i3 && i < i3 + this.mWidth) {
                int i4 = this.mTop;
                if (i2 >= i4 && i2 < i4 + this.mHeight) {
                    return true;
                }
            }
            return false;
        }

        public void writeToPref(Editor editor, int i, int i2) {
            editor.putInt(Constants.getLayoutPrefKey(PrefKeyOrder.left, i, i2), this.mLeft);
            editor.putInt(Constants.getLayoutPrefKey(PrefKeyOrder.top, i, i2), this.mTop);
            editor.putInt(Constants.getLayoutPrefKey(PrefKeyOrder.width, i, i2), this.mWidth);
            editor.putInt(Constants.getLayoutPrefKey(PrefKeyOrder.height, i, i2), this.mHeight);
            editor.putInt(Constants.getLayoutPrefKey(PrefKeyOrder.type, i, i2), this.mViewType);
        }

        public void draw(Canvas canvas, Paint paint, Paint paint2, Paint paint3, Context context) {
            String string;
            Paint paint4 = paint;
            Context context2 = context;
            int i = this.mViewType;
            if (i == 1) {
                string = context2.getString(C0354R.string.viewtype_3d);
                paint4.setARGB(255, 60, 255, 255);
            } else if (i == 66) {
                string = context2.getString(C0354R.string.viewtype_2d_xy);
                paint4.setARGB(255, 255, 60, 60);
            } else if (i == 68) {
                string = context2.getString(C0354R.string.viewtype_2d_yz);
                paint4.setARGB(255, 60, 255, 60);
            } else if (i == 72) {
                string = context2.getString(C0354R.string.viewtype_2d_xz);
                paint4.setARGB(255, 60, 60, 255);
            } else if (i != 81) {
                string = "Unknown";
            } else {
                string = context2.getString(C0354R.string.viewtype_2d_any);
                paint4.setARGB(255, 255, 60, 255);
            }
            String str = string;
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            int i2 = (int) (((float) (this.mLeft * width)) / 100.0f);
            int i3 = (int) (((float) (this.mTop * height)) / 100.0f);
            int i4 = (int) (((float) (width * this.mWidth)) / 100.0f);
            int i5 = (int) (((float) (height * this.mHeight)) / 100.0f);
            int i6 = i4 + i2;
            int i7 = i5 + i3;
            canvas.drawRect((float) i2, (float) i3, (float) i6, (float) i7, paint2);
            canvas.drawRect((float) (i2 + 2), (float) (i3 + 2), (float) (i6 - 2), (float) (i7 - 2), paint);
            canvas.drawText(str, (float) (i2 + (i4 / 2)), (float) (i3 + (i5 / 2)), paint3);
        }
    }

    /* access modifiers changed from: private */
    public static void setProgressTextView(PrefKeyOrder prefKeyOrder, TextView textView, int i, SingleView singleView) {
        int i2 = C03068.$SwitchMap$com$luolai$droidrender$Constants$PrefKeyOrder[prefKeyOrder.ordinal()];
        if (i2 == 1) {
            textView.setText(textView.getResources().getString(C0354R.string.unit_screen_left, new Object[]{Float.valueOf(((float) i) / 100.0f)}));
            if (singleView != null) {
                singleView.mLeft = i;
            }
        } else if (i2 == 2) {
            textView.setText(textView.getResources().getString(C0354R.string.unit_screen_top, new Object[]{Float.valueOf(((float) i) / 100.0f)}));
            if (singleView != null) {
                singleView.mTop = i;
            }
        } else if (i2 == 3) {
            int i3 = i + 10;
            textView.setText(textView.getResources().getString(C0354R.string.unit_screen_width, new Object[]{Float.valueOf(((float) i3) / 100.0f)}));
            if (singleView != null) {
                singleView.mWidth = i3;
            }
        } else if (i2 == 4) {
            int i4 = i + 10;
            textView.setText(textView.getResources().getString(C0354R.string.unit_screen_height, new Object[]{Float.valueOf(((float) i4) / 100.0f)}));
            if (singleView != null) {
                singleView.mHeight = i4;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0354R.layout.layout_edit_main);
        loadLayoutFromPref();
        this.mImageView = (LayoutView) findViewById(C0354R.C0356id.layoutview);
        this.mLayoutAdapter = new LayoutAdapter(this, (LayoutItem) this.mLayoutItems.get(this.mCurrentLayout), this.mImageView);
        switchLayout(0);
        setupActionBarDropdown(getIntent().getIntExtra(KEY_INITIAL_LAYOUT, 0));
        registerForContextMenu(this.mImageView);
        this.mImageView.setOnCreateContextMenuListener(new ContextMenuListener(this));
        this.mImageView.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                SingleView viewFromLastPosition = LayoutEditorActivity.this.mImageView.getViewFromLastPosition();
                if (viewFromLastPosition != null) {
                    LayoutEditorActivity.this.addOrEditView(viewFromLastPosition);
                }
            }
        });
        ((Button) findViewById(C0354R.C0356id.btncancel)).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                LayoutEditorActivity.this.finish();
            }
        });
        ((Button) findViewById(C0354R.C0356id.btnsave)).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                LayoutEditorActivity.this.saveLayoutToPref();
                Intent intent = new Intent();
                intent.setClass(LayoutEditorActivity.this, MainActivity.class);
                intent.setAction(Constants.ACTION_EDIT_LAYOUT);
                LayoutEditorActivity.this.startActivity(intent);
                LayoutEditorActivity.this.finish();
            }
        });
        GAHelper.recordScreen(this, TAG);
    }

    private void setupActionBarDropdown(int i) {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(1);
        actionBar.setListNavigationCallbacks(new ArrayAdapter(this, 17367048, new String[]{getString(C0354R.string.ui_set_layout_5), getString(C0354R.string.ui_set_layout_6), getString(C0354R.string.ui_set_layout_7), getString(C0354R.string.ui_set_layout_8)}), new OnNavigationListener() {
            public boolean onNavigationItemSelected(int i, long j) {
                LayoutEditorActivity.this.switchLayout(i);
                return true;
            }
        });
        actionBar.setSelectedNavigationItem(i);
    }

    /* access modifiers changed from: private */
    public void switchLayout(int i) {
        this.mCurrentLayout = i;
        LayoutItem layoutItem = (LayoutItem) this.mLayoutItems.get(this.mCurrentLayout);
        this.mLayoutAdapter.switchLayout(layoutItem);
        this.mImageView.setLayoutItem(layoutItem);
    }

    private void loadLayoutFromPref() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREF_NAME_LAYOUTS, 0);
        for (int i = 0; i < 4; i++) {
            this.mLayoutItems.add(new LayoutItem(sharedPreferences, i));
        }
    }

    /* access modifiers changed from: private */
    public void saveLayoutToPref() {
        Editor edit = getSharedPreferences(Constants.PREF_NAME_LAYOUTS, 0).edit();
        edit.clear();
        for (int i = 0; i < 4; i++) {
            LayoutItem layoutItem = (LayoutItem) this.mLayoutItems.get(i);
            if (layoutItem.getViewNum() > 0) {
                layoutItem.writeToPref(edit);
            }
        }
        edit.apply();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(C0354R.menu.layout_set, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() != C0354R.C0356id.menu_add) {
            return super.onOptionsItemSelected(menuItem);
        }
        addOrEditView(null);
        return true;
    }

    /* access modifiers changed from: private */
    public void removeView(SingleView singleView) {
        this.mLayoutAdapter.removeView(singleView);
    }

    /* access modifiers changed from: private */
    public void changeViewOrder(SingleView singleView, boolean z) {
        this.mLayoutAdapter.changeViewOrder(singleView, z);
    }

    /* access modifiers changed from: private */
    public void addOrEditView(SingleView singleView) {
        final SingleView singleView2;
        Builder builder = new Builder(this);
        View inflate = View.inflate(this, C0354R.layout.layout_edit_dialog, null);
        builder.setView(inflate);
        boolean z = singleView == null;
        if (singleView == null) {
            builder.setTitle(C0354R.string.edit_layout_dialog_title_add);
            singleView2 = new SingleView();
        } else {
            builder.setTitle(C0354R.string.edit_layout_dialog_title_edit);
            singleView2 = singleView;
        }
        final SingleView singleView3 = new SingleView(singleView2);
        View findViewById = inflate.findViewById(C0354R.C0356id.left);
        TextView textView = (TextView) findViewById.findViewById(C0354R.C0356id.text);
        SeekBar seekBar = (SeekBar) findViewById.findViewById(C0354R.C0356id.seekbar);
        View findViewById2 = inflate.findViewById(C0354R.C0356id.top);
        TextView textView2 = (TextView) findViewById2.findViewById(C0354R.C0356id.text);
        SeekBar seekBar2 = (SeekBar) findViewById2.findViewById(C0354R.C0356id.seekbar);
        View findViewById3 = inflate.findViewById(C0354R.C0356id.width);
        TextView textView3 = (TextView) findViewById3.findViewById(C0354R.C0356id.text);
        SeekBar seekBar3 = (SeekBar) findViewById3.findViewById(C0354R.C0356id.seekbar);
        View findViewById4 = inflate.findViewById(C0354R.C0356id.height);
        TextView textView4 = (TextView) findViewById4.findViewById(C0354R.C0356id.text);
        SeekBar seekBar4 = (SeekBar) findViewById4.findViewById(C0354R.C0356id.seekbar);
        seekBar.setMax(100);
        SeekBar seekBar5 = seekBar3;
        TextView textView5 = textView4;
        boolean z2 = z;
        SeekBar seekBar6 = seekBar2;
        TextView textView6 = textView2;
        Builder builder2 = builder;
        SeekBar seekBar7 = seekBar;
        SingleView singleView4 = singleView2;
        AnonymousClass1SeekListener r0 = new OnSeekBarChangeListener(textView, PrefKeyOrder.left, seekBar5, singleView4) {
            PrefKeyOrder mPrefKeyOrder;
            SeekBar mRelatedBar;
            TextView mTextView;
            final /* synthetic */ SingleView val$fitem;

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            {
                this.val$fitem = r5;
                this.mTextView = r2;
                this.mPrefKeyOrder = r3;
                this.mRelatedBar = r4;
            }

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                LayoutEditorActivity.setProgressTextView(this.mPrefKeyOrder, this.mTextView, i, this.val$fitem);
                SeekBar seekBar2 = this.mRelatedBar;
                if (seekBar2 != null) {
                    seekBar2.setMax((100 - i) - 10);
                }
            }
        };
        seekBar7.setOnSeekBarChangeListener(r0);
        seekBar7.setProgress(singleView2.mLeft);
        setProgressTextView(PrefKeyOrder.left, textView, singleView2.mLeft, null);
        seekBar6.setMax(100);
        AnonymousClass1SeekListener r02 = new OnSeekBarChangeListener(textView6, PrefKeyOrder.top, seekBar4, singleView4) {
            PrefKeyOrder mPrefKeyOrder;
            SeekBar mRelatedBar;
            TextView mTextView;
            final /* synthetic */ SingleView val$fitem;

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            {
                this.val$fitem = r5;
                this.mTextView = r2;
                this.mPrefKeyOrder = r3;
                this.mRelatedBar = r4;
            }

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                LayoutEditorActivity.setProgressTextView(this.mPrefKeyOrder, this.mTextView, i, this.val$fitem);
                SeekBar seekBar2 = this.mRelatedBar;
                if (seekBar2 != null) {
                    seekBar2.setMax((100 - i) - 10);
                }
            }
        };
        seekBar6.setOnSeekBarChangeListener(r02);
        seekBar6.setProgress(singleView2.mTop);
        setProgressTextView(PrefKeyOrder.top, textView6, singleView2.mTop, null);
        SeekBar seekBar8 = seekBar5;
        seekBar8.setMax(90);
        SeekBar seekBar9 = seekBar7;
        AnonymousClass1SeekListener r7 = r0;
        SingleView singleView5 = singleView2;
        AnonymousClass1SeekListener r03 = new OnSeekBarChangeListener(textView3, PrefKeyOrder.width, seekBar9, singleView5) {
            PrefKeyOrder mPrefKeyOrder;
            SeekBar mRelatedBar;
            TextView mTextView;
            final /* synthetic */ SingleView val$fitem;

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            {
                this.val$fitem = r5;
                this.mTextView = r2;
                this.mPrefKeyOrder = r3;
                this.mRelatedBar = r4;
            }

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                LayoutEditorActivity.setProgressTextView(this.mPrefKeyOrder, this.mTextView, i, this.val$fitem);
                SeekBar seekBar2 = this.mRelatedBar;
                if (seekBar2 != null) {
                    seekBar2.setMax((100 - i) - 10);
                }
            }
        };
        seekBar8.setOnSeekBarChangeListener(r7);
        seekBar8.setProgress(singleView2.mWidth - 10);
        setProgressTextView(PrefKeyOrder.width, textView3, seekBar8.getProgress(), null);
        seekBar4.setMax(90);
        TextView textView7 = textView5;
        AnonymousClass1SeekListener r04 = new OnSeekBarChangeListener(textView7, PrefKeyOrder.height, seekBar6, singleView5) {
            PrefKeyOrder mPrefKeyOrder;
            SeekBar mRelatedBar;
            TextView mTextView;
            final /* synthetic */ SingleView val$fitem;

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            {
                this.val$fitem = r5;
                this.mTextView = r2;
                this.mPrefKeyOrder = r3;
                this.mRelatedBar = r4;
            }

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                LayoutEditorActivity.setProgressTextView(this.mPrefKeyOrder, this.mTextView, i, this.val$fitem);
                SeekBar seekBar2 = this.mRelatedBar;
                if (seekBar2 != null) {
                    seekBar2.setMax((100 - i) - 10);
                }
            }
        };
        seekBar4.setOnSeekBarChangeListener(r04);
        seekBar4.setProgress(singleView2.mHeight - 10);
        setProgressTextView(PrefKeyOrder.height, textView7, seekBar4.getProgress(), null);
        Spinner spinner = (Spinner) inflate.findViewById(C0354R.C0356id.type);
        spinner.setAdapter(new ArrayAdapter(this, 17367048, new String[]{getString(C0354R.string.viewtype_3d), getString(C0354R.string.viewtype_2d_xy), getString(C0354R.string.viewtype_2d_yz), getString(C0354R.string.viewtype_2d_xz), getString(C0354R.string.viewtype_2d_any)}));
        spinner.setSelection(Entity.getViewOrderByType(singleView2.mViewType));
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onNothingSelected(AdapterView<?> adapterView) {
            }

            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long j) {
                singleView2.mViewType = Entity.getViewTypeByOrder(i);
            }
        });
        Builder builder3 = builder2;
        builder3.setNegativeButton(17039360, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                singleView2.copy(singleView3);
                dialogInterface.dismiss();
            }
        });
        final boolean z3 = z2;
        builder3.setPositiveButton(17039370, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                if (z3) {
                    LayoutEditorActivity.this.mLayoutAdapter.addView(singleView2);
                } else {
                    LayoutEditorActivity.this.mLayoutAdapter.notifyDataSetChanged();
                }
                dialogInterface.dismiss();
            }
        });
        builder3.create().show();
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        if (getIntent() != null && getIntent().getBooleanExtra(Constants.EXTRA_FROM_MAINACTIVITY, false)) {
            MainActivity.gotoMainActivity(this);
        }
        super.onDestroy();
    }
}
