package com.borqs.market.net;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

import android.text.TextUtils;
import android.util.Log;

import com.borqs.market.utils.MD5;


/**
 * 在发起网络请求时，用来存放请求参数的容器类
 */
class Parameters {
    ArrayList<String> mKeys = new ArrayList<String>();
    private ArrayList<String> mValues = new ArrayList<String>();


    public Parameters() {

    }

    public void add(String key, String value) {
        if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
            this.mKeys.add(key);
            mValues.add(value);
        }

    }

    public void add(String key, int value) {
        this.mKeys.add(key);
        this.mValues.add(String.valueOf(value));
    }

    public void add(String key, long value) {
        this.mKeys.add(key);
        this.mValues.add(String.valueOf(value));
    }

    String remove(String key) {
        int firstIndex = mKeys.indexOf(key);
        if (firstIndex >= 0) {
            this.mKeys.remove(firstIndex);
            return this.mValues.remove(firstIndex);
        }
        return null;
    }

//    public void remove(int i) {
//        if (i < mKeys.size()) {
//            mKeys.remove(i);
//            this.mValues.remove(i);
//        }
//
//    }


    private int getLocation(String key) {
        if (this.mKeys.contains(key)) {
            return this.mKeys.indexOf(key);
        }
        return -1;
    }

    String getKey(int location) {
        if (location >= 0 && location < this.mKeys.size()) {
            return this.mKeys.get(location);
        }
        return "";
    }


    String getValue(String key) {
        int index = getLocation(key);
        if (index >= 0 && index < this.mKeys.size()) {
            return this.mValues.get(index);
        } else {
            return null;
        }


    }

    String getValue(int location) {
        if (location >= 0 && location < this.mKeys.size()) {
            String rlt = this.mValues.get(location);
            return rlt;
        } else {
            return null;
        }

    }


    int size() {
        return mKeys.size();
    }

    boolean isEmpty() {
        return mKeys.isEmpty();
    }

//    public void addAll(Parameters parameters) {
//        for (int i = 0; i < parameters.size(); i++) {
//            this.add(parameters.getKey(i), parameters.getValue(i));
//        }
//
//    }
//
//    public void clear() {
//        this.mKeys.clear();
//        this.mValues.clear();
//    }
}

public class WutongParameters extends Parameters {
    private static final String KEY_IMAGE = "pic";
    private static final String KEY_CONTENT = "content-type";

    private HashMap<String, String> fileMap = new HashMap<String, String>();
    public void add(String key, File file) {
        fileMap.put(key, file.getPath());
    }

    public String popupContentType() {
        final String type = remove(KEY_CONTENT);
        if (TextUtils.isEmpty(type)) {
            return HttpManager.DEFAULT_CONTENT_TYPE;
        } else {
            return type;
        }
    }

    public void addImagePath(String path) {
        if (!TextUtils.isEmpty(path)) {
            fileMap.put(KEY_IMAGE, path);
        }
    }

    public String getImagePath() {
        if (hasImage()) {
            return fileMap.get(KEY_IMAGE);
        }
        return null;
    }

    public boolean hasImage() {
        final boolean ret = fileMap.containsKey(KEY_IMAGE);
        return ret;
    }

    public String encodeUrl() {
   		StringBuilder sb = new StringBuilder();
   		boolean first = true;
   		for (int loc = 0; loc < size(); loc++) {
   			if (first){
   			    first = false;
   			}
   			else{
   			    sb.append("&");
   			}
   			String _key = getKey(loc);
   			String _value = getValue(_key);
   			if(_value==null){
   			    Log.i("encodeUrl", "key:" + _key + " 's value is null");
   			}
   			else{
   			    sb.append(URLEncoder.encode(getKey(loc)) + "="
                           + URLEncoder.encode(getValue(loc)));
   			}

   		}
   		return sb.toString();
   	}

    public void paramToUpload(OutputStream baos)
   			throws WutongException {
   		String key = "";
   		for (int loc = 0; loc < size(); loc++) {
   			key = getKey(loc);
   			StringBuilder temp = new StringBuilder(10);
   			temp.setLength(0);
   			temp.append(HttpManager.MP_BOUNDARY).append("\r\n");
   			temp.append("content-disposition: form-data; name=\"").append(key).append("\"\r\n\r\n");
   			temp.append(getValue(key)).append("\r\n");
   			byte[] res = temp.toString().getBytes();
   			try {
   				baos.write(res);
   			} catch (IOException e) {
   				throw new WutongException(e);
   			}
   		}
   	}

    public boolean isEmpty() {
        return fileMap.isEmpty() && super.isEmpty();

    }

    public String encodeParameters() {
        StringBuilder buf = new StringBuilder();
        int j = 0;
        for (int loc = 0; loc < size(); loc++) {
            String key = getKey(loc);
            if (j != 0) {
                buf.append("&");
            }
            try {
                buf.append(URLEncoder.encode(key, "UTF-8")).append("=")
                        .append(URLEncoder.encode(getValue(key), "UTF-8"));
            } catch (java.io.UnsupportedEncodingException neverHappen) {
            }
            j++;
        }
        return buf.toString();
    }

    public void generateSignature() {
        final String ticket = remove("ticket");

        if (!isEmpty() || !fileMap.isEmpty()) {
            HashSet<String> allKeySet = new HashSet<String>(mKeys);
            if (null != fileMap) {
                allKeySet.addAll(fileMap.keySet());
            }

            TreeSet<String> sortedParamNames = new TreeSet<String>(allKeySet);
            Iterator<String> itr = sortedParamNames.iterator();
//            StringBuilder sb = new StringBuilder(Wutong.app_secret);
            StringBuilder sb = new StringBuilder();
            while (itr.hasNext()) {
                sb.append(itr.next());
            }
//            sb.append(Wutong.app_secret);

            final String str = MD5.md5Base64(sb.toString().getBytes()).replace("\n", "");
            add("sign", str);
        }

        if (!TextUtils.isEmpty(ticket)) {
            add("ticket", ticket);
        }
//        add("appid", Wutong.app_key);
        add("sign_method", "md5");
    }
}
