package com.borqs.se.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class SEEventQueue {
    private List<SECommand> mQueue = new ArrayList<SECommand>();
    private ReentrantLock _lock;

    public SEEventQueue() {
        _lock = new ReentrantLock();
    }

    public void addEvent(SECommand item) {
        _lock.lock();
        if (!mQueue.contains(item)) {
            mQueue.add(item);
        }
        _lock.unlock();
        SESceneManager.getInstance().requestRender();
    }

    public void removeEvent(int id) {
        _lock.lock();
        int size = mQueue.size();
        for (int i = 0; i < size; i++) {
            SECommand c = mQueue.get(i);
            if (c.getID() == id) {
                mQueue.remove(c);
                i--;
                size--;
            }
        }
        _lock.unlock();
    }

    public void clear() {
        _lock.lock();
        mQueue.clear();
        _lock.unlock();
    }

    public boolean render() {
        boolean needRequestRender = false;
        List<SECommand> exeQuene = new ArrayList<SECommand>();
        List<SECommand> lazyQuene = new ArrayList<SECommand>();
        _lock.lock();
        for (SECommand c : mQueue) {
            if (c.isLazy()) {
                lazyQuene.add(c);
            } else {
                exeQuene.add(c);
            }
        }
        _lock.unlock();
        for (SECommand c : exeQuene) {
            if (!c.isFinish()) {
                c.executeRun();
                if (!c.isFinish()) {
                    needRequestRender = true;
                }
            }
        }
        for (SECommand c : lazyQuene) {
            if (!c.isFinish()) {
                c.executeRun();
            }
        }
        _lock.lock();
        for (SECommand c : exeQuene) {
            if (c.isFinish()) {
                mQueue.remove(c);
            }
        }
        for (SECommand c : lazyQuene) {
            if (c.isFinish()) {
                mQueue.remove(c);
            }
        }
        _lock.unlock();
        exeQuene = null;
        lazyQuene = null;
        return needRequestRender;
    }
}
