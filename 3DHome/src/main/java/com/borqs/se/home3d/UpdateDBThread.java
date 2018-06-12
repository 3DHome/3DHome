package com.borqs.se.home3d;

import android.os.Handler;
import android.os.HandlerThread;

public class UpdateDBThread {

    private static final HandlerThread sWorkerThread = new HandlerThread("UpdateDBThread");
    static {
        sWorkerThread.start();
    }
    private static final Handler sWorker = new Handler(sWorkerThread.getLooper());

    private static UpdateDBThread mLoadResourceThread;

    public static UpdateDBThread getInstance() {
        if (mLoadResourceThread == null) {
            mLoadResourceThread = new UpdateDBThread();
        }
        return mLoadResourceThread;
    }

    private UpdateDBThread() {

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
