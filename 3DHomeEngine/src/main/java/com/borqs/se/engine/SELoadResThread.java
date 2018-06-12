package com.borqs.se.engine;

import android.os.Handler;
import android.os.HandlerThread;

public class SELoadResThread {

    private static final HandlerThread sWorkerThread = new HandlerThread("resource-loader");
    static {
        sWorkerThread.start();
    }
    private static final Handler sWorker = new Handler(sWorkerThread.getLooper());

    private static SELoadResThread mLoadResourceThread;

    public static SELoadResThread getInstance() {
        if (mLoadResourceThread == null) {
            mLoadResourceThread = new SELoadResThread();
        }
        return mLoadResourceThread;
    }

    private SELoadResThread() {

    }
    
    public Handler getHandler() {
        return sWorker;
    }

    public void cancel(Runnable task) {
        sWorker.removeCallbacks(task);
    }

    public void process(Runnable task, long delayMillis) {
        sWorker.postDelayed(task, delayMillis);
    }

    public void process(Runnable task) {
        if (sWorkerThread.getThreadId() == android.os.Process.myTid()) {
            task.run();
        } else {
            sWorker.post(task);
        }
    }

}
