package com.borqs.information.util;

public class InformationConstant {
	public static final String USERAGENT = "borqs/notification/1.0";
	
	public static final String HTTP_SCHEME = "http";
	public static final String NOTIFICATION_HOST_LIST_BY_TIME = "bmb/service/informations/listbytime.json";
	public static final String NOTIFICATION_HOST_DONE_PATH = "bmb/service/informations/done.json";
	public static final String NOTIFICATION_HOST_READ_PATH = "bmb/service/informations/read.json";
	public static final String NOTIFICATION_REQUEST_PARAM_STATUS = "status";
	public static final String NOTIFICATION_REQUEST_PARAM_MID = "mid";
	public static final String NOTIFICATION_REQUEST_PARAM_FROM = "from";

	public static final String MARKET_IAB_SERVICE_ACTION = "com.borqs.market.iab.service";
	
	public static final String NOTIFICATION_DOWNLOAD_STATUS = "status";
	public static final int NOTIFICATION_DOWNLOAD_SERVICE_FINISHED = 0;
	public static final int NOTIFICATION_DOWNLOAD_SERVICE_START = 1;
	public static final int NOTIFICATION_DOWNLOAD_SERVICE_FAILED = 2;

    public static final String NOTIFICATION_INTENT_SYNC_TYPE = "sync_type";
    public static final int NOTIFICATION_INTENT_PARAM_SYNC_TYPE_PURCHASE= 0;
    public static final int NOTIFICATION_INTENT_PARAM_SYNC_TYPE_UPLOAD = 1;
    public static final int NOTIFICATION_INTENT_PARAM_SYNC_TYPE_CONSUME = 2;
}
