package com.borqs.se.home3d;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.borqs.se.engine.SEScene;
import com.borqs.se.engine.SESceneManager;
import com.borqs.se.home3d.XmlUtils.AnalysisException;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

public class ProviderUtils {
    public static final String DATABASE_NAME = "3Dhome.db";
    public static final int DATABASE_VERSION = 97;

    public static final String AUTHORITY = HomeUtils.encodeProviderAuthority();
    public static final String PARAMETER_NOTIFY = "notify";
    public static final String SPLIT_SYMBOL = ",";

    public interface Tables {
        String MODEL_INFO = "model_info";
        String IMAGE_INFO = "image_info";
        String COMPONENT_INFO = "component_info";

        String OBJECTS_INFO = "Objects_Config";
        String VESSEL = "Vessel";
        String OBJECT_LEFT_JOIN_ALL = "Objects_Config LEFT JOIN Vessel ON Objects_Config._id=Vessel.objectID";
        
        String FILE_URL_INFO = "file_url_info";
        
        String THEME = "theme";
        String CAMERA_INFO = "camrea_info";
        String HOUSE_INFO = "house_info";
        String THEME_LEFT_JOIN_ALL = THEME + " LEFT JOIN " + CAMERA_INFO + " USING(_id) LEFT JOIN "
                + HOUSE_INFO + " USING(_id)";

        String APPS_DRAWER = "appdrawer";

    }

    public static final class ObjectInfoColumns implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + Tables.OBJECTS_INFO + "?"
                + PARAMETER_NOTIFY + "=false");
        public static final Uri OBJECT_LEFT_JOIN_ALL = Uri.parse("content://" + AUTHORITY + "/" + Tables.OBJECT_LEFT_JOIN_ALL + "?"
                + PARAMETER_NOTIFY + "=false");
        public static final String OBJECT_ID = "_id";
        
        public static final String OBJECT_NAME = "name";

        public static final String OBJECT_TYPE = "type";

        public static final String OBJECT_INDEX = "objectIndex";

        public static final String SHADER_TYPE = "shaderType";

        public static final String SCENE_NAME = "sceneName";

        public static final String COMPONENT_NAME = "componentName";

        public static final String CLASS_NAME = "className";

        public static final String SHORTCUT_ICON = "shortcutIcon";

        public static final String SHORTCUT_URL = "shortcutUri";

        public static final String WIDGET_ID = "widgetId";

        public static final String IS_NATIVE_OBJ = "isNativeObj";

        public static final String SLOT_TYPE = "slotType";

        public static final String DISPLAY_NAME = "display_name";

        public static final String FACE_COLOR = "faceColor";
        
        public static final String FACE_IMAGE_PATH = "faceImagePath";
    }

    public static final class ModelColumns implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + Tables.MODEL_INFO + "?"
                + PARAMETER_NOTIFY + "=true");
        public static final Uri IMAGE_INFO_URI = Uri.parse("content://" + AUTHORITY + "/" + Tables.IMAGE_INFO);
        public static final Uri COMPONENT_INFO_URI = Uri.parse("content://" + AUTHORITY + "/" + Tables.COMPONENT_INFO);

        public static final String OBJECT_NAME = "name";
        /**
         * 当物体绑定在主题里面时，主题删除物体也删除
         */
        public static final String BOUNDING_THEME_NAME = "boundingThemeName";

        public static final String COMPONENT_NAME = "componentName";

        public static final String TYPE = "type";
        
        public static final String SCENE_FILE = "sceneFile";

        public static final String BASEDATA_FILE = "basedataFile";

        public static final String ASSETS_PATH = "assetsPath";

        public static final String LOCAL_TRANS = "localTrans";

        public static final String KEY_WORDS = "keyWords";

        public static final String SLOT_TYPE = "slotType";

        public static final String SLOT_SPANX = "spanX";

        public static final String SLOT_SPANY = "spanY";
        // 物体不做任何平移，直接加到场景后的最小点，以及最大点。通过这两个点能够大概知道物体中心点位置以及物体的大小
        public static final String MIN_POINT = "minPoint";

        public static final String MAX_POINT = "maxPoint";

        public static final String PRODUCT_ID = "product_id";

        public static final String IS_DOWNLOADED = "isDownloaded";
        
        public static final String ACTION_STATUS = "actionStatus";

        // below are image items info
        public static final String IMAGE_NAME = "imageName";
        public static final String IMAGE_PATH = "imagePath";
        public static final String IMAGE_NEW_PATH = "imageNewPath";
        
        // below are component objects info
        public static final String COMPONENT_OBJECT = "componentObject";
        public static final String COMPONENT_REGULAR_NAME = "regularName";
        public static final String LIGHT_NAMES = "lightNames";
        public static final String STATUS_VALUE = "statusValue";
        public static final String SPATIAL_DATA_VALUE = "spatialDataValue";
        public static final String ALPHA_VALUE = "alphaValue";
        public static final String EFFECT_DATA = "effectData";


    }

    public static final class VesselColumns implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + Tables.VESSEL + "?"
                + PARAMETER_NOTIFY + "=false");
        public static final String VESSEL_ID = "vesselID";
        public static final String OBJECT_ID = "objectID";
        public static final String SLOT_INDEX = "slot_Index";
        public static final String SLOT_StartX = "slot_StartX";
        public static final String SLOT_StartY = "slot_StartY";
        public static final String SLOT_SpanX = "slot_SpanX";
        public static final String SLOT_SpanY = "slot_SpanY";
    }

    public static final class FileURLInfoColumns implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + Tables.FILE_URL_INFO);

        public static final String NAME = "name";
        public static final String SERVER_PATH = "server_path";
        public static final String LOCAL_PATH = "local_path";
        public static final String TYPE = "type";
        public static final String FILE_LENGTH = "fileLength";
        public static final String THREAD_INFOS = "threadInfos";
        public static final String DOWNLOAD_STATUS = "download_status";
        public static final String APPLY_STATUS = "apply_status";

    }

    public static final class ThemeColumns implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + Tables.THEME + "?"
                + PARAMETER_NOTIFY + "=true");
        public static final Uri CAMERA_INFO_URI = Uri.parse("content://" + AUTHORITY + "/" + Tables.CAMERA_INFO);
        public static final Uri HOUSE_INFO_URI = Uri.parse("content://" + AUTHORITY + "/" + Tables.HOUSE_INFO);
        public static final Uri THEME_LEFT_JOIN_ALL = Uri.parse("content://" + AUTHORITY + "/"
                + Tables.THEME_LEFT_JOIN_ALL + "?" + PARAMETER_NOTIFY + "=false");
        public static final String NAME = "name";
        public static final String SCENE_NAME = "scene_name";
        public static final String FILE_PATH = "file_path";
        public static final String IS_DOWNLOADED = "is_downloaded";
        public static final String IS_APPLY = "is_apply";
        public static final String CONFIG = "config";
        public static final String PRODUCT_ID = "product_id";
        public static final String HOUSE_NAME = "house_name";
        public static final String CAMERA_LOCATION = "camera_location";
        public static final String CAMERA_FOV = "camera_fov";
        public static final String WALL_INDEX = "wall_index";
        
        // below are house config

        public static final String SKY_RADIUS = "sky_radius";
        public static final String WALL_PADDINGTOP = "wall_paddingTop";
        public static final String WALL_PADDINGBOTTOM = "wall_paddingBottom";
        public static final String WALL_PADDINGLEFT = "wall_paddingLeft";
        public static final String WALL_PADDINGRIGHT = "wall_paddingRight";        
        public static final String WALL_NUM = "wall_num";
        public static final String WALL_RADIUS = "wall_radius";        
        public static final String WALL_SPANX = "wall_spanX";
        public static final String WALL_SPANY = "wall_spanY";
        public static final String CELL_WIDTH = "cell_width";
        public static final String CELL_HEIGHT = "cell_height";
        public static final String CELL_GAP_WIDTH = "cell_gapWidth";
        public static final String CELL_GAP_HEIGHT = "cell_gapHeight";
        
        //below are camera data
        public static final String FOV = "fov";
        public static final String NEAR = "near";
        public static final String FAR = "far";
        public static final String LOCATION = "location";
        public static final String ZAXIS = "zaxis";
        public static final String UP = "up";
        public static final String BEST_LOCATION = "best_location";
        public static final String BEST_FOV = "best_fov";
        public static final String NEAREST_LOCATION = "nearest_location";
        public static final String NEAREST_FOV = "nearest_fov";
        public static final String FARTHEST_LOCATION = "farthest_location";
        public static final String FARTHEST_FOV = "farthest_fov";
    }

    public static final class AppsDrawerColumns implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + Tables.APPS_DRAWER);
        public static final String COMPONENTNAME = "componentName";
        public static final String LAUNCHERCOUNT = "launchercount";
    }

    public static int searchMaxIndex(SEScene scene, Uri uri, String name) {
        String where = ObjectInfoColumns.SCENE_NAME + "='" + scene.getSceneName() + "' AND "
                + ObjectInfoColumns.OBJECT_NAME + "='" + name + "'";
        Context context = SESceneManager.getInstance().getContext();
        Cursor cursor = context.getContentResolver().query(uri,
                new String[] { "max(" + ObjectInfoColumns.OBJECT_INDEX + ") as max_index" }, where, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int max_index = cursor.getInt(0);
            cursor.close();
            return max_index;
        }
        return 0;
    }

    public static int searchMaxIndex(SEScene scene, String table, String name) {
        String where = ObjectInfoColumns.SCENE_NAME + "='" + scene.getSceneName() + "' AND "
                + ObjectInfoColumns.OBJECT_NAME + "='" + name + "'";
        Context context = SESceneManager.getInstance().getContext();
        Cursor cursor = HomeDataBaseHelper
                .getInstance(context)
                .getReadableDatabase()
                .query(table, new String[] { "max(" + ObjectInfoColumns.OBJECT_INDEX + ") as max_index" }, where,
                        null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int max_index = cursor.getInt(0);
            cursor.close();
            return max_index;
        }
        return 0;
    }

    public static float[] getFloatArray(String str, int checking) {
        String[] strArray = str.split(ProviderUtils.SPLIT_SYMBOL);
        if (strArray.length % checking != 0) {
            throw new AnalysisException("The length of array is wrong," + str);
        } else {
            float[] floatArray = new float[strArray.length];
            int index = 0;
            for (String s : strArray) {
                floatArray[index++] = Float.parseFloat(s.trim());
            }
            return floatArray;
        }
    }

    public static int[] getIntArray(String str, int checking) {
        String[] strArray = str.split(ProviderUtils.SPLIT_SYMBOL);
        if (strArray.length % checking != 0) {
            throw new AnalysisException("The length of array is wrong," + str);
        } else {
            int[] intArray = new int[strArray.length];
            int index = 0;
            for (String s : strArray) {
                intArray[index++] = Integer.parseInt(s.trim());
            }
            return intArray;
        }
    }

    public static String[] getStringArray(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        String newStr = str;
        Pattern p = Pattern.compile("\\s*|\t|\r|\n");
        Matcher m = p.matcher(newStr);
        newStr = m.replaceAll("");
        return newStr.split(ProviderUtils.SPLIT_SYMBOL);
    }

    public static String floatArrayToString(float[] floatArray) {
        String strs = "";
        for (float s : floatArray) {
            strs = strs + s + ",";
        }
        if (strs.length() > 0) {
            strs = strs.substring(0, strs.length() - 1);
        }
        return strs;
    }

    public static String intArrayToString(int[] intArray) {
        String strs = "";
        for (int s : intArray) {
            strs = strs + s + ",";
        }
        if (strs.length() > 0) {
            strs = strs.substring(0, strs.length() - 1);
        }
        return strs;
    }

    public static String stringArrayToString(String[] strArray) {
        String strs = "";
        for (String s : strArray) {
            strs = strs + s + ",";
        }
        if (strs.length() > 0) {
            strs = strs.substring(0, strs.length() - 1);
        }
        return strs;
    }

    public static String stringArrayToString(List<String> strArray) {
        String strs = "";
        for (String s : strArray) {
            strs = strs + s + ",";
        }
        if (strs.length() > 0) {
            strs = strs.substring(0, strs.length() - 1);
        }
        return strs;
    }
}
