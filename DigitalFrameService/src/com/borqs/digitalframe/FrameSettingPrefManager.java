package com.borqs.digitalframe;

import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class FrameSettingPrefManager {
    public static Context mContext;
    public static final String FRAME_SETTING_MAP = "frame_setting_map";
    public static final String FRAME_SETTING_PRE = "f_";

    //public static HashMap<String,String> mFrameSettingMap = new HashMap<String,String>();

    public static final String FOLDER_OR_FILE_PATH_KEY = "folder_file_path";
    public static final String ALBUM_TYPE              = "album_type";//0 file, 1 folder, 2 all
    public static final String FRAME_MERERIAL_KEY      = "frame_meterial";
    public static final String INTERVAL_KEY            = "interval";
    public static final String LAYOUT_KEY              = "layout";

    public static final int BLACK                      = 0;
    public static final int WHITE                      = 1;
    public static final int WOOD                       = 2;
    public static final int METAL                      = 3;

    public static final HashMap mLayoutMap = new HashMap();

    public static void initLayoutMap(){
        if(mLayoutMap.size()<11){
            mLayoutMap.clear();
        }

        mLayoutMap.put(0, 1);
        mLayoutMap.put(1, 3);
        mLayoutMap.put(2, 1);
        mLayoutMap.put(3, 1);
        mLayoutMap.put(4, 1);
        mLayoutMap.put(5, 1);
        mLayoutMap.put(6, 1);
        mLayoutMap.put(7, 1);
        mLayoutMap.put(8, 1);
        mLayoutMap.put(9, 1);
        mLayoutMap.put(10, 1);
    }

    public static void putFrameMap(Context context,String key,String value){
        SharedPreferences pref = context.getSharedPreferences(FRAME_SETTING_MAP,Context.MODE_MULTI_PROCESS);
        Editor editor = pref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static void removeFramePrefByFrameID(Context context,String key){
        SharedPreferences pref = context.getSharedPreferences(FRAME_SETTING_MAP,Context.MODE_MULTI_PROCESS);
        Editor editor = pref.edit();
        editor.remove(key);
        editor.commit();
    }

    public static String getFramePrefByFrameID(Context context,String key){
        SharedPreferences pref = context.getSharedPreferences(FRAME_SETTING_MAP,Context.MODE_MULTI_PROCESS);
        return pref.getString(key, "");
    }

    public static void  initContext(Context context){
        mContext =  context;
    }
}
