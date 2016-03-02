package com.lyndonding.projecttemplate.base;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.inputmethod.InputMethodManager;

import com.lyndonding.projecttemplate.AppApplication;
import com.lyndonding.projecttemplate.utils.AndroidUtils;
import com.lyndonding.projecttemplate.utils.Flog;
import com.lyndonding.projecttemplate.utils.ReflectUtils;
import com.lyndonding.projecttemplate.utils.ToastUtils;

public class BaseActivity extends AppCompatActivity implements OnClickListener{

	protected AppApplication mApplication;

	public int mScreenWidth;
	public int mScreenHeight;
	public float mDensity;

	public int mAndroidVersion;

	private boolean mPressTwoExit = false;
	private long mPressTime = 0;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
	}

	private void init() {
		mScreenWidth = getResources().getDisplayMetrics().widthPixels;
		mScreenHeight = getResources().getDisplayMetrics().heightPixels;
		mDensity = getResources().getDisplayMetrics().density;
		mAndroidVersion = Build.VERSION.SDK_INT;

		if (getApplication() instanceof AppApplication) {
			mApplication = (AppApplication) getApplication();
			mApplication.addActivity(this);
		}

	}

	public void setViewsClickListener(View... views) {
		for (View v : views) {
			if (v != null) {
				v.setOnClickListener(this);
			} else {
				Flog.e("Some Views are null! cannot setOnClickListener!");
			}
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void startActivity(Intent intent) {
		super.startActivity(intent);
	}

	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		super.startActivityForResult(intent, requestCode);
	}

	@Override
	public void finish() {
		if (mApplication != null) {
			mApplication.removeActivity(this);
		}
		super.finish();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final int id = item.getItemId();
		switch (id) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void unbindService(ServiceConnection conn) {
		try {
			super.unbindService(conn);
		} catch (IllegalArgumentException e) {
			Flog.e("Service not registered!");
		}
	}

	@Override
	public boolean stopService(Intent name) {
		if (name == null) {
			Flog.e("intent cannot be null!");
			return true;
		}
		return super.stopService(name);
	}

	@TargetApi(14)
	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
	}

	/**
	 * 禁止截屏
	 * 
	 * @param enable true,不能截屏
	 */
	protected void setWindowSecure(boolean enable) {
		AndroidUtils.setWindowSecure(this, enable);
	}

	/**
	 * 设置全屏
	 * 
	 * @param on true,设置全屏
	 */
	protected void setFullScreen(boolean on) {
		AndroidUtils.setFullScreen(this, on);
	}

	protected void setPressTwoExitEnable(boolean  exit) {
		mPressTwoExit = exit;
	}

	
	protected String[] getStringArray(int resId) {
		return getResources().getStringArray(resId);
	}

	protected DisplayMetrics getDisplayMetrics() {
		return getResources().getDisplayMetrics();
	}

	/**
	 * 设置ActionBar的按钮永远显�?
	 */
	protected void setActionBarMenuVisible() {
		ReflectUtils.setBoolean(ViewConfiguration.get(this),
				"sHasPermanentMenuKey", false);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (mPressTwoExit) {
			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
					&& event.getAction() == KeyEvent.ACTION_UP) {
				if (System.currentTimeMillis() - mPressTime > 2000) {
					ToastUtils.show(this, "再按一次退出" + getPackageManager().getApplicationLabel(
							getApplicationInfo()));
					mPressTime = System.currentTimeMillis();
				} else {
					mApplication.exit();
				}
				return true;
			}
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public void onClick(View v) {
		final int id = v.getId();
		onClick(id);
	}

	public void onClick(int viewId) {
	}

	protected void showInputMethod(boolean visibility) {
		View focusView = getCurrentFocus();

		if (focusView != null) {
			InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			if (visibility) {
				manager.showSoftInput(focusView,InputMethodManager.SHOW_IMPLICIT);
			} else {
				manager.hideSoftInputFromWindow(focusView.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}
	}

}
