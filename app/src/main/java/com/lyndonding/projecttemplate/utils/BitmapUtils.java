package com.lyndonding.projecttemplate.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class BitmapUtils {

	/**压缩图片
	 * @param imageFilePath 原始图片路径
	 * @param newFilePath 压缩后图片路径
	 * @param fileType Bitmap.CompressFormat.JPEG，Bitmap.CompressFormat.PNG
	 * @param quality 图片压缩质量 0~100，100表示不压缩
	 */
	public static void compressImage(String imageFilePath,String newFilePath,Bitmap.CompressFormat fileType,int quality){
		FileOutputStream fos = null;
		try {
			FileUtils.createFile(newFilePath);
			fos = new FileOutputStream(new File(newFilePath));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		BitmapFactory.Options opts=new BitmapFactory.Options();
		opts.inJustDecodeBounds=true;
		BitmapFactory.decodeFile(imageFilePath, opts);
		int height=opts.outHeight;
		int width=opts.outWidth;
		int inSampleSize=1;
		if (height>600||width>800) {
			inSampleSize=Math.round((float)width/(float)800)+1;
		}
		opts.inJustDecodeBounds=false;
		opts.inSampleSize=inSampleSize;
		Bitmap bitmap=BitmapFactory.decodeFile(imageFilePath, opts);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int options = quality;
		bitmap.compress(fileType, options, baos);
		//控制最终输出图片大小
		while (baos.toByteArray().length / 1024 > 100) {
			baos.reset();
			options -= 10;
			bitmap.compress(fileType, options, baos);
		}
		bitmap.recycle();
		bitmap=null;
		try {
			fos.write(baos.toByteArray());
			fos.flush();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private BitmapUtils() {/* Do not new me */}
}
