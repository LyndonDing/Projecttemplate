package com.lyndonding.projecttemplate;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.lyndonding.projecttemplate.base.BaseActivity;

import java.util.List;
import java.util.Stack;

/**
 * Class Info ：customer application
 * Created by dingfangchao on 2016/3/2.
 */
public class AppApplication extends Application {
    private static final boolean DEBUG = true;

    public static AppApplication mApplication;

    private Stack<BaseActivity> mActivityStack = new Stack<>();

    public int mScreenWidth;
    public int mScreenHeight;
    public float mDensity;
    public int mAndroidVersion;

    private JasonActivityLifecycleCallbacks mActivityLifecycleCallbacks;
    private boolean mIsMonitorAppRunningBackground = false;
    private boolean mIsAppRunningForground = false;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    @TargetApi(14)
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

    @TargetApi(14)
    private void init() {
        mScreenWidth = getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getResources().getDisplayMetrics().heightPixels;
        mDensity = getResources().getDisplayMetrics().density;
        mAndroidVersion = Build.VERSION.SDK_INT;

        mApplication = this;

        if (mAndroidVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (mActivityLifecycleCallbacks == null) {
                mActivityLifecycleCallbacks = new JasonActivityLifecycleCallbacks();
            }
            registerActivityLifecycleCallbacks(mActivityLifecycleCallbacks);
        }
    }

    public void addActivity(BaseActivity a) {
        if (a == null) return;
        mActivityStack.add(a);
    }

    public void removeActivity(BaseActivity a) {
        if (a == null) return;

        if (mActivityStack.contains(a)) {
            mActivityStack.remove(a);
        }
    }

    public void exit() {
        closeAllActivities();
        //Process.killProcess(Process.myPid());
        unregisterActivityLifecycleCallbacks();
    }

    @TargetApi(14)
    private void unregisterActivityLifecycleCallbacks() {
        if (mActivityLifecycleCallbacks != null) {
            unregisterActivityLifecycleCallbacks(mActivityLifecycleCallbacks);
            mActivityLifecycleCallbacks = null;
        }
    }

    private void closeAllActivities() {
        if (mActivityStack.empty()) return;

        for (BaseActivity a : mActivityStack) {
            if (a == null || a.isFinishing()) continue;
            ActivityCompat.finishAffinity(a);
        }
        mActivityStack.clear();
    }

    /**
     * 设置是否监听App运行在后�?
     * @param enabled
     */
    protected void setMonitorAppRunningBackgroundEnabled(boolean enabled) {
        mIsMonitorAppRunningBackground = enabled;
    }

    @TargetApi(14)
    class JasonActivityLifecycleCallbacks implements ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }

        @Override
        public void onActivityResumed(Activity activity) {
            if (!mIsMonitorAppRunningBackground) return;
            if (isAppRunningForeground() && !mIsAppRunningForground) {
                mIsAppRunningForground = true;
                onAppRunningForground();
            }
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
            if (!mIsMonitorAppRunningBackground) return;
            if (!isAppRunningForeground()) {
                mIsAppRunningForground = false;
                onAppRunningBackground();
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
        }

    }

    private boolean isAppRunningForeground() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = getPackageName();

        List<ActivityManager.RunningAppProcessInfo> processList = activityManager.getRunningAppProcesses();
        if (processList == null) return false;

        for (ActivityManager.RunningAppProcessInfo info : processList) {
            if (info.processName.equals(packageName)
                    && info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

    protected void onAppRunningBackground() {
    }

    protected void onAppRunningForground() {
    }
}
