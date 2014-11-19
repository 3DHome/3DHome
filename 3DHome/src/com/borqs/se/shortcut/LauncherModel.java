package com.borqs.se.shortcut;

import java.util.ArrayList;
import java.util.List;

import com.borqs.se.home3d.HomeApplication;
import com.borqs.se.home3d.HomeUtils;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

public class LauncherModel extends BroadcastReceiver {

    public interface AppCallBack {
        public void bindAppsAdded(List<ItemInfo> apps);

        public void bindAppsUpdated(List<ItemInfo> apps);

        public void bindAppsRemoved(List<ItemInfo> apps);

        public void bindAllPackagesUpdated();

        public void bindAllPackages();

        public void bindUnavailableApps(List<ItemInfo> apps);

        public void bindAvailableApps(List<ItemInfo> apps);
    }

    public interface LoadAppFinishedListener {
        public void onFinished();
    }

    public interface ShortcutCallBack {

        public void shortcutAction(Context context, Intent data);
    }

    public interface LanguageChangeCallBack {
        public void onLanguageChanged();
    }

    private List<AppCallBack> mAppCallBacks;
    private List<LanguageChangeCallBack> mLanguageChangeCallBacks;
    private LoadAppFinishedListener mLoadAppFinishedListener;
    private ShortcutCallBack mShortcutCallBack;

    private static LauncherModel mLauncherModel;
    private AllAppsList mAllAppsList;
    private boolean mHasLoaded = false;
    private Configuration mPreviousConfig;
    private Object mLock = new Object();

    private static final HandlerThread sWorkerThread = new HandlerThread("launcher-loader");
    static {
        sWorkerThread.start();
    }
    private static final Handler sWorker = new Handler(sWorkerThread.getLooper());

    public static LauncherModel getInstance() {
        if (mLauncherModel == null) {
            mLauncherModel = new LauncherModel();
        }
        return mLauncherModel;
    }

    private LauncherModel() {

    }

    public void init(Context context) {
        mAppCallBacks = new ArrayList<AppCallBack>();
        mLanguageChangeCallBacks = new ArrayList<LanguageChangeCallBack>();
        mAllAppsList = new AllAppsList(context);
        mPreviousConfig = new Configuration();
        mPreviousConfig.setTo(context.getResources().getConfiguration());
    }

    public void addAppCallBack(final AppCallBack callBack) {
        sWorker.post(new Runnable() {
            public void run() {
                if (!mAppCallBacks.contains(callBack)) {
                    mAppCallBacks.add(callBack);
                    if (mHasLoaded) {
                        callBack.bindAllPackages();
                    }
                }
            }
        });
    }

    public void setLoadAppFinishedListener(final LoadAppFinishedListener l) {
        sWorker.post(new Runnable() {
            public void run() {
                mLoadAppFinishedListener = l;
                if (mHasLoaded) {
                    mLoadAppFinishedListener.onFinished();
                    mLoadAppFinishedListener = null;
                }

            }
        });
    }

    public void removeAppCallBack(final AppCallBack callBack) {
        sWorker.post(new Runnable() {
            public void run() {
                if (callBack != null) {
                    mAppCallBacks.remove(callBack);
                }
            }
        });
    }

    public void addLanguageChangeCallBack(LanguageChangeCallBack callBack) {
        if (!mLanguageChangeCallBacks.contains(callBack)) {
            mLanguageChangeCallBacks.add(callBack);
            callBack.onLanguageChanged();
        }
    }

    public void removeLanguageChangeCallBack(LanguageChangeCallBack callBack) {
        if (mLanguageChangeCallBacks != null && callBack != null) {
            mLanguageChangeCallBacks.remove(callBack);
        }
    }

    public void setShortcutCallBack(ShortcutCallBack callBack) {
        mShortcutCallBack = callBack;
    }

    public static Handler getWorker() {
        return sWorker;
    }

    public void removeTask(Runnable task) {
        sWorker.removeCallbacks(task);
    }

    public void process(Runnable task) {
        if (sWorkerThread.getThreadId() == android.os.Process.myTid()) {
            task.run();
        } else {
            sWorker.post(task);
        }
    }

    public void process(Runnable task, long delayMillis) {
        sWorker.postDelayed(task, delayMillis);
    }

    public void processInBackground(final Runnable task) {
        if (sWorkerThread.getThreadId() == android.os.Process.myTid()) {
            task.run();
        } else {
            Runnable newTask = new Runnable() {
                public void run() {
                    sWorkerThread.setPriority(Thread.MIN_PRIORITY);
                    task.run();
                }
            };
            sWorker.post(newTask);
        }
    }

    public void processInBackground(final Runnable task, long delayMillis) {
        Runnable newTask = new Runnable() {
            public void run() {
                sWorkerThread.setPriority(Thread.MIN_PRIORITY);
                task.run();
            }
        };
        sWorker.postDelayed(newTask, delayMillis);
    }

    public void wait(Runnable task, long delayMillis) {
        process(task, delayMillis);
    }

    public void processInForeground(final Runnable task, long delayMillis) {
        Runnable newTask = new Runnable() {
            public void run() {
                sWorkerThread.setPriority(Thread.MAX_PRIORITY);
                task.run();
            }
        };
        sWorker.postDelayed(newTask, delayMillis);
    }

    public void processInForeground(final Runnable task) {
        if (sWorkerThread.getThreadId() == android.os.Process.myTid()) {
            task.run();
        } else {
            Runnable newTask = new Runnable() {
                public void run() {
                    sWorkerThread.setPriority(Thread.MAX_PRIORITY);
                    task.run();
                }
            };
            sWorker.post(newTask);
        }
    }

    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (HomeUtils.DEBUG)
            Log.d("LauncherModel", "onReceive : " + action);
        if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
            return;
        }
        if (Intent.ACTION_PACKAGE_CHANGED.equals(action) || Intent.ACTION_PACKAGE_REMOVED.equals(action)
                || Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            final String packageName = intent.getData().getSchemeSpecificPart();
            final boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
            int op = PackageUpdatedTask.OP_NONE;

            if (packageName == null || packageName.length() == 0) {
                // they sent us a bad intent
                return;
            }

            if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
                op = PackageUpdatedTask.OP_UPDATE;
            } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                if (!replacing) {
                    op = PackageUpdatedTask.OP_REMOVE;
                }
            } else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                if (replacing) {
                    op = PackageUpdatedTask.OP_UPDATE;
                } else {
                    op = PackageUpdatedTask.OP_ADD;
                }
            }

            if (op != PackageUpdatedTask.OP_NONE) {
                enqueuePackageUpdated(new PackageUpdatedTask(op, new String[] { packageName }));
            }

        } else if (Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(action)) {
            // First, schedule to add these apps back in.
            String[] packages = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
            enqueuePackageUpdated(new PackageUpdatedTask(PackageUpdatedTask.OP_AVAILABLE, packages));
        } else if (Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.equals(action)) {
            String[] packages = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
            enqueuePackageUpdated(new PackageUpdatedTask(PackageUpdatedTask.OP_UNAVAILABLE, packages));
        } else if (Intent.ACTION_CONFIGURATION_CHANGED.equals(action) || Intent.ACTION_LOCALE_CHANGED.equals(action)) {
            // Check if configuration change was an mcc/mnc change which
            // would affect app resources
            // and we would need to clear out the labels in all
            // apps/workspace. Same handling as
            // above for ACTION_LOCALE_CHANGED
            Configuration currentConfig = context.getResources().getConfiguration();
            boolean languageChanged = !mPreviousConfig.locale.equals(currentConfig.locale);
            if (mPreviousConfig.mcc != currentConfig.mcc || mPreviousConfig.fontScale != currentConfig.fontScale
                    || languageChanged) {
                loadAllData(true);
            }
            if (languageChanged) {
                for (LanguageChangeCallBack c : mLanguageChangeCallBacks) {
                    c.onLanguageChanged();
                }
            }
            // Update previousConfig
            mPreviousConfig.setTo(currentConfig);
        } else if (HomeApplication.ACTION_INSTALL_SHORTCUT.equals(intent.getAction())
                || HomeApplication.ACTION_UNINSTALL_SHORTCUT.equals(intent.getAction())) {
            if (mShortcutCallBack != null) {
                mShortcutCallBack.shortcutAction(context, intent);
            }
        }
    }

    void enqueuePackageUpdated(PackageUpdatedTask task) {
        processInBackground(task, 500); // wait for widget service
    }

    private class PackageUpdatedTask implements Runnable {
        int mOp;
        String[] mPackages;

        public static final int OP_NONE = 0;
        public static final int OP_ADD = 1;
        public static final int OP_UPDATE = 2;
        public static final int OP_REMOVE = 3; // uninstlled
        public static final int OP_UNAVAILABLE = 4; // external media unmounted
        public static final int OP_LOADALL = 5;
        public static final int OP_AVAILABLE = 6;// external media mounted.

        public PackageUpdatedTask(int op, String[] packages) {
            mOp = op;
            mPackages = packages;
        }

        public void run() {
            final String[] packages = mPackages;
            final int N;
            if (packages == null) {
                N = 0;
            } else {
                N = packages.length;
            }
            switch (mOp) {
            case OP_ADD:
                if (!mHasLoaded) {
                    return;
                }
                for (int i = 0; i < N; i++) {
                    mAllAppsList.addPackage(packages[i]);
                }
                updateAll();
                break;
            case OP_UPDATE:
                if (!mHasLoaded) {
                    return;
                }
                for (int i = 0; i < N; i++) {
                    mAllAppsList.updatePackage(packages[i]);
                }
                updateAll();
                break;
            case OP_REMOVE:
                if (!mHasLoaded) {
                    return;
                }
                for (int i = 0; i < N; i++) {
                    mAllAppsList.removePackage(packages[i]);
                }
                updateAll();
                break;
            case OP_UNAVAILABLE:
                if (!mHasLoaded) {
                    return;
                }
                for (int i = 0; i < N; i++) {
                    mAllAppsList.setPackageUnavailable(packages[i]);
                }
                updateAll();
                break;
            case OP_AVAILABLE:
                if (!mHasLoaded) {
                    return;
                }
                for (int i = 0; i < N; i++) {
                    mAllAppsList.setPackageAvailable(packages[i]);
                }
                updateAll();
                break;
            case OP_LOADALL:
                mAllAppsList.clear();
                mAllAppsList.loadAll();
                updateAll();
                if (mAppCallBacks != null) {
                    for (AppCallBack callBack : mAppCallBacks) {
                        if (mHasLoaded) {
                            callBack.bindAllPackagesUpdated();
                        } else {
                            callBack.bindAllPackages();
                        }
                    }
                }
                if (!mHasLoaded) {
                    if (mLoadAppFinishedListener != null) {
                        mLoadAppFinishedListener.onFinished();
                        mLoadAppFinishedListener = null;
                    }
                }
                mHasLoaded = true;
                return;
            }

            List<ItemInfo> added = null;
            List<ItemInfo> removed = null;
            List<ItemInfo> modified = null;
            List<ItemInfo> unavailable = null;
            List<ItemInfo> available = null;

            if (mAllAppsList.added.size() > 0) {
                added = mAllAppsList.added;
                mAllAppsList.added = new ArrayList<ItemInfo>();
            }
            if (mAllAppsList.removed.size() > 0) {
                removed = mAllAppsList.removed;
                mAllAppsList.removed = new ArrayList<ItemInfo>();
            }
            if (mAllAppsList.modified.size() > 0) {
                modified = mAllAppsList.modified;
                mAllAppsList.modified = new ArrayList<ItemInfo>();
            }
            if (mAllAppsList.unavailable.size() > 0) {
                unavailable = mAllAppsList.unavailable;
                mAllAppsList.unavailable = new ArrayList<ItemInfo>();
            }
            if (mAllAppsList.available.size() > 0) {
                available = mAllAppsList.available;
                mAllAppsList.available = new ArrayList<ItemInfo>();
            }

            if (added != null) {
                final List<ItemInfo> addedFinal = added;
                if (mAppCallBacks != null) {
                    for (AppCallBack callBack : mAppCallBacks) {
                        callBack.bindAppsAdded(addedFinal);
                    }
                }
            }
            if (modified != null) {
                final List<ItemInfo> modifiedFinal = modified;
                if (mAppCallBacks != null) {
                    for (AppCallBack callBack : mAppCallBacks) {
                        callBack.bindAppsUpdated(modifiedFinal);
                    }
                }
            }
            if (removed != null) {
                final List<ItemInfo> removedFinal = removed;
                if (mAppCallBacks != null) {
                    for (AppCallBack callBack : mAppCallBacks) {
                        callBack.bindAppsRemoved(removedFinal);
                    }
                }
            }
            if (unavailable != null) {
                final List<ItemInfo> unavailableFinal = unavailable;
                if (mAppCallBacks != null) {
                    for (AppCallBack callBack : mAppCallBacks) {
                        callBack.bindUnavailableApps(unavailableFinal);
                    }
                }
            }
            if (available != null) {
                final List<ItemInfo> availableFinal = available;
                if (mAppCallBacks != null) {
                    for (AppCallBack callBack : mAppCallBacks) {
                        callBack.bindAvailableApps(availableFinal);
                    }
                }
            }
        }
    }

    public void loadAllData(boolean force) {
        if (force || !mHasLoaded) {
            enqueuePackageUpdated(new PackageUpdatedTask(PackageUpdatedTask.OP_LOADALL, null));
        }
    }

    private ArrayList<ItemInfo> mAllDatas = new ArrayList<ItemInfo>();
    private ArrayList<AppItemInfo> mApps = new ArrayList<AppItemInfo>();
    private ArrayList<WidgetItemInfo> mWidgets = new ArrayList<WidgetItemInfo>();

    private void updateAll() {
        synchronized (mLock) {
            mAllDatas = new ArrayList<ItemInfo>();
            mApps = new ArrayList<AppItemInfo>();
            mWidgets = new ArrayList<WidgetItemInfo>();
            mAllDatas.addAll(mAllAppsList.data);
            for (ItemInfo item : mAllDatas) {
                if (item.mItemType == ItemInfo.ITEM_TYPE_WIDGET) {
                    mWidgets.add((WidgetItemInfo) item);
                } else if (item.mItemType == ItemInfo.ITEM_TYPE_APP) {
                    mApps.add((AppItemInfo) item);
                }
            }
        }
    }

    public boolean hasLoadedAllApps() {
        return mHasLoaded;
    }

    public AppItemInfo findAppItem(ComponentName componentName) {
        synchronized (mLock) {
            for (AppItemInfo item : mApps) {
                if (componentName.equals(item.getComponentName())) {
                    return item;
                }
            }
        }
        return null;
    }

    public WidgetItemInfo findWidgetItem(ComponentName componentName) {
        synchronized (mLock) {
            for (WidgetItemInfo item : mWidgets) {
                if (componentName.equals(item.getComponentName())) {
                    return item;
                }
            }
        }
        return null;
    }

    public ArrayList<ItemInfo> getAllDatas() {
        synchronized (mLock) {
            return mAllDatas;
        }
    }

    public ArrayList<WidgetItemInfo> getWidgets() {
        synchronized (mLock) {
            return mWidgets;
        }
    }

    public ArrayList<AppItemInfo> getApps() {
        synchronized (mLock) {
            return mApps;
        }
    }
    
}
