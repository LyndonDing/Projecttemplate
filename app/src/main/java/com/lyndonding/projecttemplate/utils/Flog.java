package com.lyndonding.projecttemplate.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.lyndonding.projecttemplate.Config;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class Flog {
	
	private static final String UPLOAD_URL = "";
	
	private static final int MSG_WHAT_WRITE = 1;
	private static final int MSG_WHAT_UPLOAD = 2;
	private static final int MSG_WHAT_UPLOAD_BY_FTP = 3;
	
	private static Flog mInstance;
	private Context mContext;
	private Executor mThreadPool = Executors.newFixedThreadPool(4);
	private MainHandler mHandler = new MainHandler(Looper.getMainLooper());
	private File mDefaultDir;
	private String mDefaultFileName;
	
	private Flog(Context context) {
		mContext = context;
		mDefaultDir = FileUtils.getDirectory(StorageUtils.getExternalStorageRootDir().toString() + "/" + 
					StringUtils.getSimpleNameFromFullName(mContext.getPackageName()));
		mDefaultFileName = "log-" + TimeUtils.getCurrentDateInString() + ".log";
	}
	
	public static Flog getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new Flog(context);
		}
		return mInstance;
	}
	
	public static void e(Object obj) {
		if(Config.DEBUG) {
			android.util.Log.e(getInvokerClassName(), String.valueOf(obj));
		}
	}
	
	public static void i(Object obj) {
		if(Config.DEBUG) {
			android.util.Log.i(getInvokerClassName(), String.valueOf(obj));
		}
	}
	
	public static void e(Throwable tr) {
		if(Config.DEBUG) {
			android.util.Log.e(getInvokerClassName(), "", tr);
		}
	}
	
	public static void i(String tag, Object obj) {
		if(Config.DEBUG) {
			android.util.Log.i(tag, String.valueOf(obj));
		}
	}

	public static void i(String tag, String format, Object... args) {
		if(Config.DEBUG) {
			android.util.Log.i(tag, String.format(format, args));
		}
	}
	
	public static void e(String tag, Throwable tr) {
		if(Config.DEBUG) {
			android.util.Log.e(tag, "", tr);
		}
	}
	
	public static void e(String tag, String msg, Throwable tr) {
		if(Config.DEBUG) {
			android.util.Log.e(tag, msg, tr);
		}
	}
	
	public static void e(String tag, String format, Object... args) {
		if(Config.DEBUG) {
			android.util.Log.e(tag, String.format(format, args));
		}
	}
	
	public static String getCurrentMethodName() {
		return Thread.currentThread().getStackTrace()[2].getMethodName();
	}
	
	public static void p(Object o) {
		if(Config.DEBUG) {
			System.out.println(o);
		}
	}
	
	public static void p(String TAG, Object obj) {
		if(Config.DEBUG) {
			System.out.println(TextUtils.isEmpty(TAG) ? obj : TAG + ":" + obj);
		}
	}
	
	public static void err(Object o) {
		if(Config.DEBUG) {
			System.err.println(o);
		}
	}
	
	public static String getInvokerClassName() {
		StackTraceElement[] elements = (new Throwable()).getStackTrace();
		return StringUtils.getSimpleNameFromFullName(elements[2].getClassName());
	}
	
	public static String getInvokerMethodName() {
		StackTraceElement[] elements = (new Throwable()).getStackTrace();
		return elements[2].getMethodName();
	}
	
	public void writeLog(String text) {
		writeLog(text, null);
	}
	
	public void writeLog(String text, OnWriteListener listener) {
		writeLog(mDefaultDir, mDefaultFileName, text, listener);
	}
	
	public void writeLog(File dir, String fileName, String text, OnWriteListener listener) {
		mThreadPool.execute(new WriteThread(dir, fileName, text, listener));
	}

	
	static class MainHandler extends Handler {

		public MainHandler(Looper looper) {
			super(looper);
		}
		
		@Override
		public void handleMessage(Message msg) {
			HandlerResult result = (HandlerResult) msg.obj;
			if (msg.what == MSG_WHAT_WRITE) {
				OnWriteListener onWriteListener = null;
				if (result.mOnOpListener instanceof OnWriteListener) {
					onWriteListener = (OnWriteListener)result.mOnOpListener;
				}
				
				if (result.mIsSuccess) {
					if (onWriteListener != null) {
						onWriteListener.onWriteSuccess();
					}
				} else {
					if (onWriteListener != null) {
						onWriteListener.onWriteFailure(msg.what);
					}
				}
			} else if (msg.what == MSG_WHAT_UPLOAD || msg.what == MSG_WHAT_UPLOAD_BY_FTP) {
				OnUploadListener onUploadListener = null;
				if (result.mOnOpListener instanceof OnUploadListener) {
					onUploadListener = (OnUploadListener)result.mOnOpListener;
				}
				
				if (onUploadListener != null) {
					onUploadListener.onUploadResult(result.mResultMap);
				}
			}
			super.handleMessage(msg);
		}
		
	}
	
	static class HandlerResult {
		OnOpListener mOnOpListener;
		boolean mIsSuccess;
		Map<String, Boolean> mResultMap;
		
		public HandlerResult(OnOpListener listener) {
			mOnOpListener = listener;
		}
		
		public void setResult(boolean isSuccess) {
			mIsSuccess = isSuccess;
		}
		
		public void setResultMap(Map<String, Boolean> map) {
			mResultMap = map;
		}
		
	}
	
	/**
	 * 日志线程
	 * @author Yucun
	 *
	 */
	class WriteThread implements Runnable {

		private File mTargetDir;
		private String mFileName;
		private String mText;
		private OnWriteListener mOnWriteListener;
		
		public WriteThread(File dir, String fileName, String text, OnWriteListener listener) {
			mTargetDir = dir;
			mFileName = fileName;
			mText = text;
			mOnWriteListener = listener;
		}
		
		@Override
		public void run() {
			if (mContext == null) {
				Flog.e("mContext can not be null!");
				return;
			}
			
			Message msg = mHandler.obtainMessage(MSG_WHAT_WRITE);
			HandlerResult result = new HandlerResult(mOnWriteListener);
			
			if (StorageUtils.isExternalStorageReady()) {
				File file = FileUtils.writeString(mText, mTargetDir, mFileName);
				if (file == null) {
					result.setResult(false);
				} else {
					result.setResult(true);
				}
			} else {
				result.setResult(false);
			}
			msg.obj = result;
			
			if (mOnWriteListener != null) {
				mHandler.sendMessage(msg);
			}
		}
		
	}


	
	public interface OnWriteListener extends OnOpListener {
		public void onWriteSuccess();
		public void onWriteFailure(int errorCode);
	}
	
	public interface OnUploadListener extends OnOpListener {
		public void onUploadResult(Map<String, Boolean> filePathMap);
	}
	
	interface OnOpListener {
	}
	
	private Flog(){/*Do not new me*/};
}
