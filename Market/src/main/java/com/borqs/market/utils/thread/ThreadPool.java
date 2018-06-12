package com.borqs.market.utils.thread;

public interface ThreadPool {
    /* ------------------------------------------------------------ */
    boolean dispatch(Runnable job);

    /* ------------------------------------------------------------ */
    /**
     * Blocks until the thread pool is {@link LifeCycle#stop stopped}.
     */
    void join() throws InterruptedException;

    /* ------------------------------------------------------------ */
    /**
     * @return The total number of threads currently in the pool
     */
    int getThreads();

    /* ------------------------------------------------------------ */
    /**
     * @return The number of idle threads in the pool
     */
    int getIdleThreads();

    /* ------------------------------------------------------------ */
    /**
     * @return True if the pool is low on threads
     */
    boolean isLowOnThreads();
}
