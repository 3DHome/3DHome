package com.borqs.market.api;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.borqs.market.net.HttpManager;
import com.borqs.market.net.RequestListener;
import com.borqs.market.net.WutongException;
import com.borqs.market.net.WutongParameters;
import com.borqs.market.utils.BLog;

/**
 * API的基类，每个接口类都继承了此抽象类
 * 
 */
public abstract class WutongAPI {
    /**
     * 访问服务接口的地址
     */
     public static final String API_SERVER_TEST = "http://apitest.borqs.com:6789/";
//     public static final String API_SERVER = API_SERVER_TEST;
    public static final String API_SERVER = "http://api.borqs.com:7890/";
    /**
     * post请求方式
     */
    public static final String HTTPMETHOD_POST = "POST";
    /**
     * get请求方式
     */
    public static final String HTTPMETHOD_GET = "GET";
//    private Oauth2AccessToken oAuth2accessToken;
//    private String accessToken;

//    /**
//     * 构造函数，使用各个API接口提供的服务前必须先获取Oauth2AccessToken
//     * 
//     * @param oauth2AccessToken
//     *            Oauth2AccessToken
//     */
//    public WutongAPI(Oauth2AccessToken oauth2AccessToken) {
//        this.oAuth2accessToken = oauth2AccessToken;
//        if (oAuth2accessToken != null) {
//            accessToken = oAuth2accessToken.getToken();
//        }
//
//    }

    public enum FEATURE {
        ALL, ORIGINAL, PICTURE, VIDEO, MUSICE
    }

    public enum SRC_FILTER {
        ALL, WEIBO, WEIQUN
    }

    public enum TYPE_FILTER {
        ALL, ORIGAL
    }

    public enum AUTHOR_FILTER {
        ALL, ATTENTIONS, STRANGER
    }

    public enum TYPE {
        STATUSES, COMMENTS, MESSAGE
    }

    public enum EMOTION_TYPE {
        FACE, ANI, CARTOON
    }

    public enum LANGUAGE {
        cnname, twname
    }

    public enum SCHOOL_TYPE {
        COLLEGE, SENIOR, TECHNICAL, JUNIOR, PRIMARY
    }

    public enum CAPITAL {
        A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z
    }

    public enum FRIEND_TYPE {
        ATTENTIONS, FELLOWS
    }

    public enum RANGE {
        ATTENTIONS, ATTENTION_TAGS, ALL
    }

    public enum USER_CATEGORY {
        DEFAULT, ent, hk_famous, model, cooking, sports, finance, tech, singer, writer, moderator, medium, stockplayer
    }

    public enum STATUSES_TYPE {
        ENTERTAINMENT, FUNNY, BEAUTY, VIDEO, CONSTELLATION, LOVELY, FASHION, CARS, CATE, MUSIC
    }

    public enum COUNT_TYPE {
        /**
	     */
        STATUS,
        /**
         * 新粉丝数
         */
        FOLLOWER,
        /**
         * 新评论数
         */
        CMT,
        /**
         * 新私信数
         */
        DM,
        /**
         * 新提及我的
         */
        MENTION_STATUS,
        /**
         * 新提及我的评论数
         */
        MENTION_CMT
    }

    /**
     * 分类
     * 
     * @author xiaowei6@staff.sina.com.cn
     * 
     */
    public enum SORT {
        Oauth2AccessToken, SORT_AROUND
    }

    public enum SORT2 {
        SORT_BY_TIME, SORT_BY_HOT
    }

    public enum SORT3 {
        SORT_BY_TIME, SORT_BY_DISTENCE
    }

    public enum COMMENTS_TYPE {
        NONE, CUR_STATUSES, ORIGAL_STATUSES, BOTH
    }

    protected void request(final String url, final WutongParameters params,
            final String httpMethod, RequestListener listener, boolean needToken) {
        WutongParameters bundle;
        if (params == null) {
            bundle = new WutongParameters();
        } else {
            bundle = params;
        }

        request(url, bundle, httpMethod, listener);
    }

    /**
     * 请求接口数据，并在获取到数据后通过RequestListener将responsetext回传给调用者
     * 
     * @param url
     *            服务器地址
     * @param params
     *            存放参数的容器
     * @param httpMethod
     *            "GET"or “POST”
     * @param listener
     *            回调对象
     */
    public static void request(final String url, final WutongParameters params,
            final String httpMethod, final RequestListener listener) {
        BLog.d_performance("request beginTime"+ System.currentTimeMillis()+"   url = " + url);
        new Thread() {
            @Override
            public void run() {
                try {
                    String resp = HttpManager.openUrl(url, httpMethod, params);
                    BLog.e("response =  " + resp);
                    JSONObject obj;
                    obj = new JSONObject(resp);
                    int code = obj.optInt("code", -1);
                    if (code != 0) {
                        String error_message = obj.optString("error_message");
                        Log.e(BLog.TAG, error_message);
                        listener.onError(new WutongException(error_message));
                    } else {
                        listener.onComplete(resp);
                    }

                } catch (WutongException e) {
                    listener.onError(e);
                } catch (JSONException e) {
                    e.printStackTrace();
                }finally {
                    BLog.d_performance("request endTime"+ System.currentTimeMillis()+"   url = " + url);
                }
            }
        }.start();

    }
}
