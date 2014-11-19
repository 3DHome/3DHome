package com.borqs.market.db;

import android.provider.BaseColumns;

public class PlugInColumns implements BaseColumns {

    public static final String NAME = "name";
    public static final String PRODUCT_ID = "product_id";
    public static final String VERSION_NAME = "version_name";
    public static final String VERSION_CODE = "version_code";
    public static final String TYPE = "type";
    public static final String LOCAL_JSON_STR = "local_json_str";
    public static final String JSON_STR = "json_str";
    public static final String SUPPORTED_MOD = "supported_mod";
    public static final String IS_APPLY = "is_apply";
    public static final String FILE_PATH = "file_path";
    public static final String BELONG_SYSTEM = "belong_system";
    public static final String UPDATE_TIME = "update_time";
    

    public static final String[] PROJECTION = { NAME, PRODUCT_ID,VERSION_NAME, VERSION_CODE,
        TYPE,LOCAL_JSON_STR,JSON_STR,SUPPORTED_MOD,IS_APPLY,FILE_PATH,BELONG_SYSTEM,UPDATE_TIME};

}
