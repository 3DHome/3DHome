package com.borqs.market.utils.thread;

import java.util.EventListener;

public interface LifeCycle {
    /* ------------------------------------------------------------ */
    /**
     * Starts the component.
     * 
     * @throws Exception
     *             If the component fails to start
     * @see #isStarted()
     * @see #stop()
     * @see #isFailed()
     */
    void start() throws Exception;

    /* ------------------------------------------------------------ */
    /**
     * Stops the component. The component may wait for current activities to
     * complete normally, but it can be interrupted.
     * 
     * @exception Exception
     *                If the component fails to stop
     * @see #isStopped()
     * @see #start()
     * @see #isFailed()
     */
    void stop() throws Exception;

    /* ------------------------------------------------------------ */
    /**
     * @return true if the component is starting or has been started.
     */
    boolean isRunning();

    /* ------------------------------------------------------------ */
    /**
     * @return true if the component has been started.
     * @see #start()
     * @see #isStarting()
     */
    boolean isStarted();

    /* ------------------------------------------------------------ */
    /**
     * @return true if the component is starting.
     * @see #isStarted()
     */
    boolean isStarting();

    /* ------------------------------------------------------------ */
    /**
     * @return true if the component is stopping.
     * @see #isStopped()
     */
    boolean isStopping();

    /* ------------------------------------------------------------ */
    /**
     * @return true if the component has been stopped.
     * @see #stop()
     * @see #isStopping()
     */
    boolean isStopped();

    /* ------------------------------------------------------------ */
    /**
     * @return true if the component has failed to start or has failed to stop.
     */
    boolean isFailed();

    /* ------------------------------------------------------------ */
    void addLifeCycleListener(LifeCycle.Listener listener);

    /* ------------------------------------------------------------ */
    void removeLifeCycleListener(LifeCycle.Listener listener);

    /* ------------------------------------------------------------ */
    /**
     * Listener. A listener for Lifecycle events.
     */
    interface Listener extends EventListener {
        void lifeCycleStarting(LifeCycle event);

        void lifeCycleStarted(LifeCycle event);

        void lifeCycleFailure(LifeCycle event, Throwable cause);

        void lifeCycleStopping(LifeCycle event);

        void lifeCycleStopped(LifeCycle event);
    }
}
