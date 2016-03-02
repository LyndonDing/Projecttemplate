package com.lyndonding.projecttemplate.base;

import android.app.Application;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.lyndonding.projecttemplate.AppApplication;
import com.lyndonding.projecttemplate.Config;
import com.lyndonding.projecttemplate.R;
import com.lyndonding.projecttemplate.utils.Flog;
import com.umeng.analytics.MobclickAgent;

public class ThemeActivity extends BaseActivity {
	
	private static final boolean DEBUG = Config.DEBUG;
	
	protected Toolbar mToolbar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Application app = getApplication();
		if (app instanceof AppApplication) {
			mApplication = (AppApplication) app;
		} else {
			if (DEBUG) Flog.e("Must extends KApplication or register in AndroidManifest.xml Application android:name!");
		}
	}

	protected void initToolBar(String title, int logoId) {
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		if(mToolbar != null) {
			mToolbar.setTitle(title);
			if (logoId != -1) {
				mToolbar.setLogo(logoId);
			}
			setSupportActionBar(mToolbar);
		}
	}

	protected void initToolBar(int logoId) {
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		if(mToolbar != null) {
			mToolbar.setTitle("");
			if (logoId != -1) {
				mToolbar.setLogo(logoId);
			}
			setSupportActionBar(mToolbar);
		}
	}

	protected void initToolBar(String title) {
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		if(mToolbar != null) {
			mToolbar.setTitle(title);
			setSupportActionBar(mToolbar);
		}
	}

	protected void initToolBar() {
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		if(mToolbar != null) {
			mToolbar.setTitle("");
			setSupportActionBar(mToolbar);
		}
	}

	protected void initToolBar(boolean showAsHome) {
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		if(mToolbar != null) {
			mToolbar.setTitle("");
			setSupportActionBar(mToolbar);
		}
		if (showAsHome){
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setDisplayShowHomeEnabled(true);
		}
	}

	protected void resetToolbarColor(int colorId){
		if(mToolbar != null) {
			mToolbar.setBackgroundColor(colorId);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final int id = item.getItemId();
		switch (id) {
		case android.R.id.home:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		if (!Config.DEBUG) {
			MobclickAgent.onResume(this);
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (!Config.DEBUG) {
			MobclickAgent.onPause(this);
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
}
