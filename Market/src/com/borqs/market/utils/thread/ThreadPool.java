package com.borqs.market.utils.thread;

public interface ThreadPool {
    /* ------------------------------------------------------------ */
    public abstract boolean dispatch(Runnable job);

    /* ------------------------------------------------------------ */
    /**
     * Blocks until the thread pool is {@link LifeCycle#stop stopped}.
     */
    public void join() throws InterruptedException;

    /* ------------------------------------------------------------ */
    /**
     * @return The total number of threads currently in the pool
     */
    public int getThreads();

    /* ------------------------------------------------------------ */
    /**
     * @return The number of idle threads in the pool
     */
    public int getIdleThreads();

    /* ------------------------------------------------------------ */
    /**
     * @return True if the pool is low on threads
     */
    public boolean isLowOnThreads();
}
