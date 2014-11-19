package com.borqs.information.db;

import android.net.Uri;

public class Notification {
	
	public static final String AUTHORITY = "com.borqs.appbox.information";
	
	public static final class NotificationColumns {
		
		public static final String _ID = "_id";
		public static final String M_ID = "m_id";
		public static final String TYPE = "type";
		public static final String APP_ID = "app_id";
		public static final String IMAGE_URL = "image_url";
		public static final String RECEIVER_ID = "receiver_id";
		public static final String SENDER_ID = "sender_id";
		public static final String DATE = "date";
		public static final String TITLE = "title";
		public static final String URI = "uri";
		public static final String LAST_MODIFY = "last_modify";
		public static final String DATA = "data";
		public static final String LONGPRESSURI = "apppickurl";
		public static final String IS_READ = "is_read";
		public static final String PROCESSED = "processed";
		public static final String BODY = "body";
		public static final String BODY_HTML = "body_html";
		public static final String TITLE_HTML = "title_html";
		public static final String U_ID = "u_id";
		
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/notification");
		public static final Uri CONTENT_URI_WITHOUT_NOTIFY = Uri.parse("content://" + AUTHORITY + "/notification_without_notify");
	}

}
