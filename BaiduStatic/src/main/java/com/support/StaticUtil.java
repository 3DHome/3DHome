package com.support;

import com.baidu.mobstat.SendStrategyEnum;
import com.baidu.mobstat.StatService;

import android.content.Context;


/**
 * Created by b608 on 13-10-16.
 */
public class StaticUtil {
    public static void initGLStatic(Context context) {
        StatService.setAppChannel(context, "all_market", true);
        StatService.setOn(context, StatService.EXCEPTION_LOG);
        StatService.setLogSenderDelayed(10);
        StatService.setSendLogStrategy(context, SendStrategyEnum.APP_START, 1, false);
        StatService.setSessionTimeOut(30);
        StatService.setDebugOn(false);
    }

    public static void reportError(Context context, String message) {
        onEvent(context, "use_error_log", message);
    }

    private static void onEvent(Context context, String event_id) {
        StatService.onEvent(context, event_id, event_id);
    }

    private static void onEvent(Context context, String event_id, String lable) {
        StatService.onEvent(context, event_id, lable);
    }
    public static void staticWallpaper(Context context, String title) {
        StaticUtil.onEvent(context, "wallpaper_event", title);
    }

    public static void staticUsingDesk(Context context, String objName) {
        StaticUtil.onEvent(context, "switch_desk_event", objName);
    }

    public static void staticUsingTheme(Context context, String themeName) {
        StaticUtil.onEvent(context, "switch_theme_event", themeName);
    }

    public static void staticPurchaseEvent(Context context, String resourceID) {
        StaticUtil.onEvent(context, "purchase_event", resourceID);
    }
    public static void startFeedback(Context context) {
//        FeedbackAgent agent = new FeedbackAgent(context);
//        agent.startFeedbackActivity();
    }


    public static void showFeedbackActivity(Context context) {
//        FeedbackAgent agent = new FeedbackAgent(context);
//        agent.startFeedbackActivity();
    }
}
