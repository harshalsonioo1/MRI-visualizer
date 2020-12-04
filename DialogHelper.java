package com.luolai.droidrender;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.Iterator;

public class DialogHelper {
    public static final double LOG_2 = Math.log(2.0d);
    public static final int RESAMPLE_SCALE_LIMIT = 12;
    public static final int RESAMPLE_UP_LIMIT = 400;

    public interface FileSaverCallback {
        void onFileSaved(String str);
    }

    enum HintType {
        main,
        loading,
        selection,
        sample,
        file_picker,
        saf,
        working_area
    }

    public static abstract class ResampleCallback {
        public int mResampleLevel;

        public abstract void onResample(int[] iArr);
    }

    public static abstract class ShiftCallback {
        public int mShift;

        public abstract void onShift(int i);

        public ShiftCallback(int i) {
            this.mShift = i;
        }
    }

    public static void CreateStringDialog(Context context, String str) {
        final EditText editText = new EditText(context);
        editText.setText(str);
        Builder builder = new Builder(context);
        builder.setView(editText);
        builder.setNegativeButton(17039360, new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton(17039370, new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.setString(editText.getText().toString().getBytes());
                dialogInterface.dismiss();
            }
        });
        AlertDialog create = builder.create();
        create.setCanceledOnTouchOutside(false);
        create.show();
    }

    public static void CreateFileSaveDialog(Context context, String str, String str2, String str3, FileSaverCallback fileSaverCallback) {
        final EditText editText = new EditText(context);
        final File file = new File(str3);
        if (!file.exists()) {
            file.mkdirs();
        }
        if (!TextUtils.isEmpty(str)) {
            editText.setText(str);
        }
        Builder builder = new Builder(context);
        builder.setView(editText);
        builder.setTitle(C0354R.string.file_picker_title_name);
        builder.setNegativeButton(17039360, new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        final Context context2 = context;
        final String str4 = str2;
        final String str5 = str3;
        final FileSaverCallback fileSaverCallback2 = fileSaverCallback;
        C02704 r0 = new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                final String obj = editText.getText().toString();
                if (TextUtils.isEmpty(obj)) {
                    Context context = context2;
                    Toast.makeText(context, context.getString(C0354R.string.file_picker_error_no_text), 0).show();
                    DialogHelper.CreateFileSaveDialog(context2, null, str4, str5, fileSaverCallback2);
                    return;
                }
                Iterator it = FilePicker.filter(file.listFiles(), false, true, false, str4).iterator();
                while (it.hasNext()) {
                    File file = (File) it.next();
                    if (file.isFile()) {
                        String name = file.getName();
                        if (name != null && name.length() > str4.length() + 1 && name.substring(name.length() - str4.length()).equalsIgnoreCase(str4) && obj.equals(name.substring(0, name.length() - str4.length()))) {
                            Builder builder = new Builder(context2);
                            builder.setMessage(context2.getString(C0354R.string.file_picker_error_already_exist, new Object[]{obj}));
                            builder.setNegativeButton(17039360, new OnClickListener() {
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    DialogHelper.CreateFileSaveDialog(context2, obj, str4, str5, fileSaverCallback2);
                                }
                            });
                            builder.setPositiveButton(17039370, new OnClickListener() {
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    FileSaverCallback fileSaverCallback = fileSaverCallback2;
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(file.getAbsolutePath());
                                    sb.append("/");
                                    sb.append(obj);
                                    sb.append(str4);
                                    fileSaverCallback.onFileSaved(sb.toString());
                                }
                            });
                            AlertDialog create = builder.create();
                            create.setCanceledOnTouchOutside(false);
                            create.show();
                            return;
                        }
                    }
                }
                FileSaverCallback fileSaverCallback = fileSaverCallback2;
                StringBuilder sb = new StringBuilder();
                sb.append(file.getAbsolutePath());
                sb.append("/");
                sb.append(obj);
                sb.append(str4);
                fileSaverCallback.onFileSaved(sb.toString());
                dialogInterface.dismiss();
            }
        };
        builder.setPositiveButton(17039370, r0);
        AlertDialog create = builder.create();
        create.setCanceledOnTouchOutside(false);
        create.show();
    }

    public static int[] getResampleLevel(int i) {
        int[] iArr = new int[3];
        int pow = (int) Math.pow(2.0d, (double) (i / 3));
        iArr[2] = pow;
        iArr[1] = pow;
        iArr[0] = pow;
        int i2 = i % 3;
        if (i2 == 1) {
            iArr[2] = iArr[2] * 2;
        } else if (i2 == 2) {
            iArr[0] = iArr[0] * 2;
            iArr[1] = iArr[1] * 2;
        }
        for (int i3 = 0; i3 < 3; i3++) {
            if (iArr[i3] <= 0) {
                iArr[i3] = 1;
            }
        }
        return iArr;
    }

    public static int calculateResampleLevelFromFileSize(int i) {
        double d = (double) i;
        Double.isNaN(d);
        int log = ((int) (Math.log(d / 400.0d) / LOG_2)) + 1;
        if (log > 12) {
            return 12;
        }
        return log;
    }

    public static void setResampleText(int i, int i2, TextView textView) {
        int[] resampleLevel = getResampleLevel(i);
        if (i > 0) {
            int i3 = i2 / ((resampleLevel[0] * resampleLevel[1]) * resampleLevel[2]);
            if (i3 <= 1) {
                i3 = 1;
            }
            textView.setText(textView.getContext().getResources().getString(C0354R.string.warning_too_many_files_resample, new Object[]{Integer.valueOf(resampleLevel[0]), Integer.valueOf(resampleLevel[1]), Integer.valueOf(resampleLevel[2]), Integer.valueOf(i3)}));
            return;
        }
        textView.setText(textView.getContext().getResources().getString(C0354R.string.no_resample));
    }

    public static void createResampleDialog(Context context, final int i, final ResampleCallback resampleCallback) {
        View inflate = View.inflate(context, C0354R.layout.resample_dlg, null);
        ((TextView) inflate.findViewById(C0354R.C0356id.body)).setText(context.getResources().getString(C0354R.string.warning_too_many_files_message, new Object[]{Integer.valueOf(i), Integer.valueOf(RESAMPLE_UP_LIMIT)}));
        resampleCallback.mResampleLevel = calculateResampleLevelFromFileSize(i);
        final TextView textView = (TextView) inflate.findViewById(C0354R.C0356id.resample_message);
        setResampleText(resampleCallback.mResampleLevel, i, textView);
        ((Button) inflate.findViewById(C0354R.C0356id.btnup)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                resampleCallback.mResampleLevel++;
                if (resampleCallback.mResampleLevel > 12) {
                    resampleCallback.mResampleLevel = 12;
                }
                DialogHelper.setResampleText(resampleCallback.mResampleLevel, i, textView);
            }
        });
        ((Button) inflate.findViewById(C0354R.C0356id.btndown)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                resampleCallback.mResampleLevel--;
                if (resampleCallback.mResampleLevel < 0) {
                    resampleCallback.mResampleLevel = 0;
                }
                DialogHelper.setResampleText(resampleCallback.mResampleLevel, i, textView);
            }
        });
        Builder builder = new Builder(context);
        builder.setView(inflate);
        builder.setPositiveButton(17039370, new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                ResampleCallback resampleCallback = resampleCallback;
                resampleCallback.onResample(DialogHelper.getResampleLevel(resampleCallback.mResampleLevel));
                dialogInterface.dismiss();
            }
        });
        builder.setTitle(C0354R.string.warning_too_many_files_title);
        AlertDialog create = builder.create();
        create.setCanceledOnTouchOutside(false);
        create.show();
    }

    public static void createShiftDialog(Context context, final ShiftCallback shiftCallback) {
        Builder builder = new Builder(context);
        builder.setTitle(C0354R.string.dlg_overflow_title);
        builder.setMessage(C0354R.string.dlg_overflow_message);
        builder.setPositiveButton(17039370, new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                ShiftCallback shiftCallback = shiftCallback;
                shiftCallback.onShift(shiftCallback.mShift);
            }
        });
        builder.setNegativeButton(C0354R.string.f13no, new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                shiftCallback.onShift(0);
            }
        });
        builder.setCancelable(false);
        AlertDialog create = builder.create();
        create.setCanceledOnTouchOutside(false);
        create.show();
    }

    public static void createHintDialog(Context context, HintType hintType, OnClickListener onClickListener) {
        createHintDialog(context, hintType, onClickListener, null);
    }

    public static void createHintDialog(Context context, HintType hintType, OnClickListener onClickListener, OnClickListener onClickListener2) {
        int i;
        String format = String.format(Constants.PREF_KEY_IS_HINT_SHOWN, new Object[]{hintType.toString()});
        switch (hintType) {
            case main:
                i = C0354R.string.hint_main_change_patient;
                break;
            case loading:
                i = C0354R.string.hint_manage_patient_loading;
                break;
            case selection:
                i = C0354R.string.hint_manage_patient_selection;
                break;
            case file_picker:
                i = C0354R.string.hint_file_picker;
                break;
            case sample:
                i = C0354R.string.hint_manage_patient_sample;
                break;
            case saf:
                i = C0354R.string.hint_saf;
                break;
            case working_area:
                i = C0354R.string.dlg_set_limit_body;
                break;
            default:
                i = 0;
                break;
        }
        if (i != 0) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREF_NAME_SETTINGS, 0);
            if (hintType == HintType.working_area || !sharedPreferences.getBoolean(format, false)) {
                Builder builder = new Builder(context);
                builder.setTitle(C0354R.string.hint_title);
                builder.setMessage(i);
                builder.setPositiveButton(17039370, onClickListener);
                if (onClickListener2 != null) {
                    builder.setNegativeButton(C0354R.string.f13no, onClickListener2);
                }
                builder.setCancelable(false);
                AlertDialog create = builder.create();
                create.setCanceledOnTouchOutside(false);
                create.show();
                sharedPreferences.edit().putBoolean(format, true).apply();
            } else if (onClickListener != null) {
                onClickListener.onClick(null, 0);
            }
        }
    }
}
