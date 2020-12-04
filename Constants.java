package com.luolai.droidrender;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.text.TextUtils;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

public class Constants {
    public static final String ACTION_EDIT_LAYOUT = "action_edit_layout";
    public static final String ACTION_EXIT = "action_exit";
    public static final String ACTION_LOAD_FILE = "action_load_file";
    public static final String ACTION_LOAD_IMAGE = "action_load_image";
    public static final String ACTION_SELECT_PATIENT = "action_select_patient";
    public static final int CLEAR_CACHE_DEFAULT_THRESHOLD = 30;
    public static final int CLEAR_CACHE_DEFAULT_THRESHOLD_GAP = 20;
    public static final String CLOUD_CACHE_FOLDER_PATH = "/Cloud_cache/";
    public static final String EXTERNAL_STORAGE_PATH = "storage";
    public static final String EXTRA_FILE_LIST = "extra_file_list";
    public static final String EXTRA_FROM_MAINACTIVITY = "extra_from_main";
    public static final String EXTRA_IMAGE_LIST = "extra_image_list";
    public static final String EXTRA_IS_LOAD_BY_OUTER_FILE = "extra_is_load_by_outer_file";
    public static final String EXTRA_PATIENT_ORDER = "extra_patient_order";
    public static final String EXTRA_REQUEST_CODE = "extra_request_code";
    public static final int IDM_3DVIEW_VRBASE_POINT = 20;
    public static final int IDM_3DVIEW_VRBASE_POLY = 21;
    public static final int IDM_DRAWINGTOOL_BIG_BRUSH = 3;
    public static final int IDM_DRAWINGTOOL_COLOR_BLACK = 11;
    public static final int IDM_DRAWINGTOOL_COLOR_BLUE = 15;
    public static final int IDM_DRAWINGTOOL_COLOR_GREEN = 13;
    public static final int IDM_DRAWINGTOOL_COLOR_RED = 10;
    public static final int IDM_DRAWINGTOOL_COLOR_WHITE = 12;
    public static final int IDM_DRAWINGTOOL_COLOR_YELLOW = 14;
    public static final int IDM_DRAWINGTOOL_MID_BRUSH = 2;
    public static final int IDM_DRAWINGTOOL_SMALL_BRUSH = 1;
    public static final int IDM_FILE_READ_VRCM = 32968;
    public static final int IDM_FILE_SAVE_VRCM = 34014;
    public static final long IDM_FUNCTION_APP_APTH = 84;
    public static final long IDM_FUNCTION_CLEAR_ALL_RECENT_IMAGE = 94;
    public static final long IDM_FUNCTION_CLEAR_UNAVAILABLE_RECENT_IMAGE = 87;
    public static final long IDM_FUNCTION_CLOUD_PATH = 85;
    public static final long IDM_FUNCTION_DEFAULT_2D_TOUCH = 91;
    public static final long IDM_FUNCTION_DEFAULT_3D_TOUCH = 92;
    public static final long IDM_FUNCTION_DEFAULT_SYNC_TOUCH = 93;
    public static final int IDM_FUNCTION_DRAWING = 40;
    public static final int IDM_FUNCTION_MERGE_LASTSTEP = 58;
    public static final int IDM_FUNCTION_MERGE_NEXTSTEP = 57;
    public static final int IDM_FUNCTION_MERGE_SETBASE = 59;
    public static final long IDM_FUNCTION_NEXT_PATIENT = 90;
    public static final int IDM_FUNCTION_REGION_DIMINISH = 53;
    public static final int IDM_FUNCTION_REGION_EXTENSION = 54;
    public static final int IDM_FUNCTION_REGION_MERGE = 55;
    public static final int IDM_FUNCTION_REGION_MULTIGROW = 52;
    public static final int IDM_FUNCTION_REGION_REGIONSEG = 51;
    public static final int IDM_FUNCTION_REGION_SEGAGAIN = 56;
    public static final int IDM_FUNCTION_REGION_STEPGROWING = 50;
    public static final long IDM_FUNCTION_SELECT_PATIENT = 80;
    public static final long IDM_FUNCTION_SET_3D_QUALITY = 81;
    public static final long IDM_FUNCTION_SET_DEFAULT_DICOM_LIBRARY = 88;
    public static final long IDM_FUNCTION_SET_DEFAULT_VR_TYPE = 83;
    public static final long IDM_FUNCTION_SET_SHADING_LEVEL = 89;
    public static final long IDM_FUNCTION_SHOW_TREE = 82;
    public static final long IDM_FUNCTION_TOOL_MENU = 86;
    public static final long IDM_FUNCTION_VR_3D_THRESHOLD = 71;
    public static final long IDM_FUNCTION_VR_TYPE = 70;
    public static final int IDM_LBUTTON_CONTRAST = 34;
    public static final int IDM_LBUTTON_MOVESCANLINE = 33;
    public static final int IDM_LBUTTON_ROTATION = 31;
    public static final int IDM_LBUTTON_SELECTION = 35;
    public static final int IDM_LBUTTON_SIZING = 32;
    public static final int IDM_LBUTTON_TRANSLATION = 30;
    public static final int IDM_LIMIT_AREA = 32979;
    public static final int ID_VIEW_BACK_TO_NORMAL = 100;
    public static final int LOAD_TYPE_RECENT_IMAGE = 0;
    public static final int LOAD_TYPE_SAMPLE_IMAGE = 1;
    public static final int MAX_WIDTH_HEIGHT = 100;
    public static final int MIN_WIDTH_HEIGHT = 10;
    public static final String PREF_3D_QUALITY = "pref_key_3d_quality";
    public static final String PREF_CONTROL_TREE_STATUS = "pref_key_show_tree";
    public static final String PREF_IS_NOADS = "pref_is_noads";
    public static final String PREF_IS_VIP = "pref_is_vip";
    public static final String PREF_KEY_CLEAR_CACHE_THRESHOLD = "pref_key_clear_cache_threshold";
    public static final String PREF_KEY_DEFAULT_2D = "pref_key_default_2d";
    public static final String PREF_KEY_DEFAULT_3D = "pref_key_default_3d";
    public static final String PREF_KEY_ENGLISH_ONLY = "pref_language";
    public static final String PREF_KEY_IS_CANCEL_SELECTION_DLG_SHOWN = "pref_is_cancel_selection_dlg_shown";
    public static final String PREF_KEY_IS_HINT_SHOWN = "pref_is_hint_%s_shown";
    public static final String PREF_KEY_LAST_DIR_IMAGE = "pref_last_dir_image";
    public static final String PREF_KEY_LAST_DIR_IMAGE_EXTERNAL = "pref_last_dir_image_external";
    public static final String PREF_KEY_LAST_DIR_IMAGE_USB = "pref_last_dir_image_usb";
    public static final String PREF_KEY_LAST_DIR_VRCM = "pref_last_dir_vrcm";
    public static final String PREF_KEY_USER_AGREE = "pref_key_user_agree";
    public static final String PREF_KEY_WHATS_NEW = "pref_key_whats_new_31";
    public static final String PREF_NAME_LAYOUTS = "layouts";
    public static final String PREF_NAME_LAYOUTS_CUSTOMIZE_VIEW_HEIGHT = "customize_layout_%d_view_%d_height";
    public static final String PREF_NAME_LAYOUTS_CUSTOMIZE_VIEW_LEFT = "customize_layout_%d_view_%d_left";
    public static final String PREF_NAME_LAYOUTS_CUSTOMIZE_VIEW_NUM = "customize_layout_%d";
    public static final String PREF_NAME_LAYOUTS_CUSTOMIZE_VIEW_ORIENTATION = "customize_layout_%d_orientation";
    public static final String PREF_NAME_LAYOUTS_CUSTOMIZE_VIEW_TOP = "customize_layout_%d_view_%d_top";
    public static final String PREF_NAME_LAYOUTS_CUSTOMIZE_VIEW_TYPE = "customize_layout_%d_view_%d_type";
    public static final String PREF_NAME_LAYOUTS_CUSTOMIZE_VIEW_WIDTH = "customize_layout_%d_view_%d_width";
    public static final String PREF_NAME_SETTINGS = "com.luolai.droidrender_preferences";
    public static final String PREF_NAME_SET_DEFAULT_LAYOUTS = "set_default_layouts";
    public static final String PREF_STL_EXPIRED = "pref_stl_expired";
    public static final String RECENT_FILE_FOLDER_PATH = "/RecentFile/";
    public static final String SAMPLE_FILE_FOLDER_PATH = "/SampleBCD/";
    public static final int SUPPORTED_CUSTOMIZE_VIEW_NUMBER = 4;
    public static String SYSTEM_LOCALE = "";
    public static final int VALUE_CURRENT_VR_THRESHOLD = 4;
    public static final int VALUE_HASDIALOG = 5;
    public static final int VALUE_HASIMAGE = 3;
    public static final int VALUE_HASTREE = 6;
    public static final int VALUE_IMAGE_SIZE = 8;
    public static final int VALUE_MAXGRAY = 2;
    public static final int VALUE_MINGRAY = 1;
    public static final int VALUE_NEEDTOOLMENU = 7;
    public static final int VERSION = 31;
    public static boolean sDebug = false;

    enum FileType {
        bcd,
        vrcm,
        stl,
        cloud,
        sample,
        recent,
        snapshot,
        zip,
        content
    }

    enum PrefKeyOrder {
        num,
        orientation,
        left,
        top,
        width,
        height,
        type
    }

    public static String getSamplePath(Context context) {
        return getFolderPath(context, FileType.sample);
    }

    public static String getRecentPath(Context context) {
        return getFolderPath(context, FileType.recent);
    }

    public static void setSystemString() {
        SYSTEM_LOCALE = Locale.getDefault().toString();
    }

    public static String getCharsetNameByLocale(String str) {
        if (TextUtils.isEmpty(str) || !Locale.TAIWAN.toString().equals(str)) {
            return null;
        }
        return "Big5";
    }

    public static String getStringFromByteWithLocale(byte[] bArr) {
        String str;
        if (bArr == null || bArr.length <= 0) {
            return " ";
        }
        String charsetNameByLocale = getCharsetNameByLocale(SYSTEM_LOCALE);
        if (!TextUtils.isEmpty(charsetNameByLocale)) {
            try {
                str = new String(bArr, charsetNameByLocale);
            } catch (UnsupportedEncodingException unused) {
                str = new String(bArr);
            }
        } else {
            str = new String(bArr);
        }
        return str;
    }

    public static String getFolderPath(Context context, FileType fileType) {
        StringBuilder sb = new StringBuilder();
        sb.append(Environment.getExternalStorageDirectory().getAbsolutePath());
        sb.append("/DroidRender/");
        String sb2 = sb.toString();
        switch (fileType) {
            case bcd:
                StringBuilder sb3 = new StringBuilder();
                sb3.append(sb2);
                sb3.append("BCD");
                return sb3.toString();
            case vrcm:
                StringBuilder sb4 = new StringBuilder();
                sb4.append(sb2);
                sb4.append("VRCM");
                return sb4.toString();
            case stl:
                StringBuilder sb5 = new StringBuilder();
                sb5.append(sb2);
                sb5.append("STL");
                return sb5.toString();
            case zip:
                StringBuilder sb6 = new StringBuilder();
                sb6.append(sb2);
                sb6.append("UNZIP");
                return sb6.toString();
            case content:
                StringBuilder sb7 = new StringBuilder();
                sb7.append(sb2);
                sb7.append("CONTENT");
                return sb7.toString();
            case cloud:
                StringBuilder sb8 = new StringBuilder();
                sb8.append(getAppDataPath(context));
                sb8.append(CLOUD_CACHE_FOLDER_PATH);
                return sb8.toString();
            case sample:
                StringBuilder sb9 = new StringBuilder();
                sb9.append(getAppDataPath(context));
                sb9.append(SAMPLE_FILE_FOLDER_PATH);
                return sb9.toString();
            case recent:
                StringBuilder sb10 = new StringBuilder();
                sb10.append(getAppDataPath(context));
                sb10.append(RECENT_FILE_FOLDER_PATH);
                return sb10.toString();
            default:
                return sb2;
        }
    }

    public static String getLayoutPrefKey(PrefKeyOrder prefKeyOrder, int i, int i2) {
        switch (prefKeyOrder) {
            case num:
                return String.format(PREF_NAME_LAYOUTS_CUSTOMIZE_VIEW_NUM, new Object[]{Integer.valueOf(i)});
            case orientation:
                return String.format(PREF_NAME_LAYOUTS_CUSTOMIZE_VIEW_ORIENTATION, new Object[]{Integer.valueOf(i)});
            case left:
                return String.format(PREF_NAME_LAYOUTS_CUSTOMIZE_VIEW_LEFT, new Object[]{Integer.valueOf(i), Integer.valueOf(i2)});
            case top:
                return String.format(PREF_NAME_LAYOUTS_CUSTOMIZE_VIEW_TOP, new Object[]{Integer.valueOf(i), Integer.valueOf(i2)});
            case width:
                return String.format(PREF_NAME_LAYOUTS_CUSTOMIZE_VIEW_WIDTH, new Object[]{Integer.valueOf(i), Integer.valueOf(i2)});
            case height:
                return String.format(PREF_NAME_LAYOUTS_CUSTOMIZE_VIEW_HEIGHT, new Object[]{Integer.valueOf(i), Integer.valueOf(i2)});
            case type:
                return String.format(PREF_NAME_LAYOUTS_CUSTOMIZE_VIEW_TYPE, new Object[]{Integer.valueOf(i), Integer.valueOf(i2)});
            default:
                return "";
        }
    }

    public static int getLayoutPrefValue(SharedPreferences sharedPreferences, PrefKeyOrder prefKeyOrder, int i, int i2) {
        return sharedPreferences.getInt(getLayoutPrefKey(prefKeyOrder, i, i2), 0);
    }

    public static int getLayoutPrefValue(SharedPreferences sharedPreferences, PrefKeyOrder prefKeyOrder, int i, int i2, int i3) {
        return sharedPreferences.getInt(getLayoutPrefKey(prefKeyOrder, i, i2), i3);
    }

    public static String getViewTypeString(Context context, int i) {
        if (i == 1) {
            return context.getString(C0354R.string.viewtype_3d);
        }
        if (i == 66) {
            return context.getString(C0354R.string.viewtype_2d_xy);
        }
        if (i == 68) {
            return context.getString(C0354R.string.viewtype_2d_yz);
        }
        if (i != 72) {
            return i != 81 ? "" : context.getString(C0354R.string.viewtype_2d_any);
        }
        return context.getString(C0354R.string.viewtype_2d_xz);
    }

    public static String getAppDataPath(Context context) {
        PackageInfo packageInfo;
        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();
        try {
            packageInfo = packageManager.getPackageInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            packageInfo = null;
        }
        return packageInfo != null ? packageInfo.applicationInfo.dataDir : packageName;
    }
}
