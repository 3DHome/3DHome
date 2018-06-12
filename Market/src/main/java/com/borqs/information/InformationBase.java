package com.borqs.information;

import android.net.Uri;


public class InformationBase {

	public long _id = 0;
	public long id;
	public String appId;
	public String type;
	public String image_url;
	public String receiverId;
	public String senderId;
	public long   date;
	public String title;
	public String body;
	public String uri;
	public String body_html;
	public String title_html;
	
	public Uri apppickurl;
	public String data;
	public long lastModified;
	public boolean read;
	
	protected void parseData(){}
	protected void callAction(){}	
	
	public InformationBase() {
		super();
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Notification id=" + id + " ");
		buffer.append("to=" + receiverId + " ");
		buffer.append("appid=" + appId + " ");
		buffer.append("type=" + type + " ");
		buffer.append("uri=" + uri + " ");
		buffer.append("title=" + title + " ");
		buffer.append("lastModified=" + lastModified + " ");
		return buffer.toString();
	}
	
	public void assignFrom(InformationBase info) {
		_id = info._id;
		id = info.id;
		appId = info.appId;
		type = info.type;
		image_url = info.image_url;
		receiverId = info.receiverId;
		senderId = info.senderId;
		date = info.date;
		title = info.title;
		title_html = info.title_html;
		body = info.body;
		body_html = info.body_html;
		uri = info.uri;	
		data = info.data;
		apppickurl = info.apppickurl == null?null:Uri.parse(info.apppickurl.toString());
		lastModified = info.lastModified;
		read = info.read;		
	}	
}
