package com.lyndonding.projecttemplate.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * file utils
 * @author dingfangchao
 *
 */
public class FileUtils {
	static final boolean DEBUG = false;

	private static final String TAG = FileUtils.class.getSimpleName();

	private static String BASE_DIR = "**";

	public static byte[] getBytes(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] bytes = new byte[1024];
        int length = 0;
        while ((length = fis.read(bytes)) != -1) {
            baos.write(bytes, 0, length);
        }
        IoUtils.close(baos, fis);
        return baos.toByteArray();
	}
	
	public static File getDirectory (String path) {
		File file = new File(path);
		if (makeDir(path)) {
			return file;
		}
		return null;
	}
	
	public static File getImageCacheDir() throws IOException {
		File dir = new File(Environment.getExternalStorageDirectory()
				+ BASE_DIR + "/.image");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		new File(dir, ".nomedia");
		return dir;
	}

	public static Drawable getDrawableFromFile(Context ctx, String filePath)
			throws RuntimeException {
		Bitmap bitmap = BitmapFactory.decodeFile(filePath);
		return new BitmapDrawable(ctx.getResources(), bitmap);
	}

	public static String getNameFromUrl(String url) {
		if (url != null && !url.endsWith("/")) {
			return url.substring(url.lastIndexOf("/") + 1);
		} else {
			return "noname";
		}
	}
	
	/**
	 * 文件是否存在
	 * @param filePath
	 * @return
	 */
	public static boolean isFileExists(String filePath) {
		if (TextUtils.isEmpty(filePath)) return false;
		
		File file = new File(filePath);
		return file.exists() && file.isFile();
	}
	
	/**
	 * 目录是否存在
	 * @param filePath
	 * @return
	 */
	public static boolean isDirExists(String dirPath) {
		if (TextUtils.isEmpty(dirPath)) return false;
		
		File file = new File(dirPath);
		return file.exists() && file.isDirectory();
	}
	
	public static boolean makeDir(String path) {
		return makeDir(new File(path));
	}
	
	/**
     * 创建文件夹
     * @param path
     * @return
     */
    public static boolean makeDir(File dir) {
    	if (dir == null) return false;
    	
    	if (!dir.exists()) {
    		return dir.mkdirs();
    	} else if (dir.isFile()) {
			deleteFile(dir);
			return dir.mkdirs();
    	} else {
    		return true;
    	}
    }
    
    public static boolean createFile(File dir, String fileName) {
    	if (dir == null) return false;
    	return createFile(new File(dir, fileName));
    }
    
    /**
     * 创建文件
     * @param path
     * @return
     */
    public static boolean createFile(String path) {
    	return createFile(new File(path));
    }
    
    public static boolean createFile(File file) {
    	try {
	    	if (!file.exists()) {
	    		if (makeDir(file.getParent())) {
	    				return file.createNewFile();
	    		}
	    	} else if (file.isDirectory()) {
	    		deleteFile(file);
	    		return file.createNewFile();
	    	} else {
	    		return true;
	    	}
    	} catch (IOException e) {
    		Flog.e(TAG, e);
    	}
    	return false;
    }

	public static boolean deleteFile(File file) {
		boolean isDelete = false;

		if (file == null || !file.exists()) {
			return true;
		} else if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files != null) { 
				for (File f : files) {
					isDelete = deleteFile(f);
					if (!isDelete) {
						return isDelete;
					}
				}
			}
			isDelete = file.delete();
		} else {
			isDelete = file.delete();
		}
		return isDelete;
	}

	public static boolean deleteFile(String path) {
		return deleteFile(new File(path));
	}

	public static File saveBitmapToFile(Bitmap bitmap, File dir, String fileName) {
		if (!dir.exists()) {
			dir.mkdirs();
		}
		if (bitmap == null) {
			return null;
		}
		File bitmapFile = new File(dir, fileName);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(bitmapFile);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			fos.close();
			return bitmapFile;
		} catch (FileNotFoundException e) {
			Flog.e(TAG, "FileNotFoundException", e);
		} catch (Exception e) {
			FileUtils.deleteFile(bitmapFile);
		}
		return null;
	}
	
	public static File writeString(String text, File dir, String fileName) {
		return writeString(text, dir, fileName, true);
	}

	public static File writeString(String text, File dir, String fileName, boolean isAppend) {
		if (createFile(dir, fileName)) {
			if (text == null) return null;
			
			FileWriter writer = null;
			BufferedReader reader = null;
			try {
				File file = new File(dir, fileName);
				writer = new FileWriter(file, isAppend);
				
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
				if (!TextUtils.isEmpty(reader.readLine())) {
					writer.write("\n");
				}
				writer.write(text);
				return file;
			} catch (IOException e) {
				Flog.e(e);
			} finally {
				IoUtils.close(writer, reader);
			}
		}
		return null;
	}

	public static void copy(File fromFile, File toFile, boolean isRewrite) {
		if (!fromFile.exists()) {
			return;
		}
		if (!fromFile.isFile()) {
			return;
		}
		if (!fromFile.canRead()) {
			return;
		}

		if (toFile.getParentFile().exists()) {
			toFile.getParentFile().mkdirs();
		}
		if (toFile.exists() && isRewrite) {
			toFile.delete();
		}

		FileInputStream fis = null;
		FileOutputStream fos = null;
		byte[] buffer = new byte[1024];
		int byteCount = 0;
		try {
			fis = new FileInputStream(fromFile);
			fos = new FileOutputStream(toFile);
			while ((byteCount = fis.read()) != -1) {
				fos.write(buffer, 0, byteCount);
			}
		} catch (FileNotFoundException e) {
			Flog.e(TAG, "FileNotFoundException", e);
		} catch (IOException e) {
			Flog.e(TAG, "IOException", e);
		}
	}
	
	public static void writeByteArrayToFile(File file, byte[] bytes) throws IOException {
		FileOutputStream fout = new FileOutputStream(file);
		try {
			fout.write(bytes);
		} finally {
			fout.close();
		}
	}


	public static void openFile(Context context, String path, String mimeType) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse("file://" + path), mimeType);
		if (context instanceof Activity) {
			((Activity) context).startActivity(intent);
		} else {
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		}
	}

	/**
	 * 判断文件的编码格式
	 * 
	 * @param filePath
	 * @return 文件编码格式
	 * @throws Exception
	 */
	public static String getTextCodeFormat(String filePath) {
		String code = null;
		BufferedInputStream bis = null;
		int p = 0;
		try {
			bis = new BufferedInputStream(new FileInputStream(filePath));
			p = (bis.read() << 8) + bis.read();
		} catch (Exception e) {
			Flog.e(TAG, e);
		} finally {
			IoUtils.close(bis);
		}
		switch (p) {
		case 0xefbb:
			code = "UTF-8";
			break;
		case 0xfffe:
			code = "Unicode";
			break;
		case 0xfeff:
			code = "UTF-16BE";
			break;
		default:
			code = "GBK";
		}
		return code;
	}
	
	public static void zipFile(String baseDir, String fileName) throws Exception {
		
		List<File> fileList = getSubFiles(new File(baseDir));
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(fileName));
		ZipEntry entry = null;
		byte[] buf = new byte[1024];
		int readLen = 0;
		InputStream is = null;
		for(int i = 0; i < fileList.size(); i++) {
			File f = (File) fileList.get(i);
			entry = new ZipEntry(getAbsoluteFileName(baseDir, f));
			entry.setSize(f.length());
			entry.setTime(f.lastModified());
			zos.putNextEntry(entry);
			is = new BufferedInputStream(new FileInputStream(f));
			
			while ((readLen=is.read(buf, 0, 1024)) != -1) {
				zos.write(buf, 0, readLen);
			}
		}
		IoUtils.close(is, zos);
   }
   
   private static String getAbsoluteFileName(String baseDir, File realFileName) {  
       File realName = realFileName;  
       File base = new File(baseDir);  
       String name = realName.getName();  
       while (true) {  
    	   realName = realName.getParentFile();  
           if(realName == null) break;
           
           if(realName.equals(base)) {
        	   break;
           } else {  
               name = realName.getName() + "/" + name; 
           }
       }  
       return name;  
   }
   
   public static List<File> getSubFiles(File baseDir){  
       List<File> fileList = new ArrayList<File>();
       File[] tmp = baseDir.listFiles();
       for (int i = 0; i<tmp.length; i++) {
           if(tmp[i].isFile())
               fileList.add(tmp[i]);
           if(tmp[i].isDirectory())
               fileList.addAll(getSubFiles(tmp[i]));
       }
       return fileList;
   }
	
	/**
     * 解压一个压缩文档 到指定位置
     * 
     * @param inFilePath 压缩包的名字
     * @param outFilePath 指定的路径
     * @throws Exception
     */
    public static void unzipFile(String inFilePath, String outFilePath) throws Exception {
        ZipInputStream inZip = new ZipInputStream(new FileInputStream(inFilePath));
        ZipEntry zipEntry = null;
        String szName = "";

        while ((zipEntry = inZip.getNextEntry()) != null) {
            szName = zipEntry.getName();

            if (zipEntry.isDirectory()) {
                szName = szName.substring(0, szName.length() - 1);
                FileUtils.makeDir(outFilePath + File.separator + szName);
            } else {
                FileUtils.createFile(outFilePath + File.separator + szName);
                FileOutputStream out = new FileOutputStream(new File(outFilePath + File.separator + szName));
                int len = 0;
                byte[] buffer = new byte[1024];
                while ((len = inZip.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                    out.flush();
                }
                out.close();
            }
        }
        inZip.close();
    }
    
    public static File writeFile(String dir, String fileName, InputStream inStream) {
    	File file = null;  
        OutputStream outStream = null;  
        try {
            if (!makeDir(dir)) return null;
            file = new File(dir, fileName);
            if (!createFile(new File(dir, fileName))) return null;
            outStream = new FileOutputStream(file);
            byte[] buffer = new byte[4 * 1024];
            int byteRead = -1;
            while((byteRead = inStream.read(buffer)) != -1){
                outStream.write(buffer, 0, byteRead);
            }
            outStream.flush();
        } catch(Exception e){
        	if (DEBUG) Flog.e(e);
        } finally{  
            IoUtils.close(outStream);
        } 
        return file;
    }
    
    public static long getFileSize(File file) {
    	if (file != null && file.exists()) {
    		return file.length();
    	}
    	return 0;
    }
    
    /**
     * @param context
     * @param uniqueName customer file name of cache
     * @return
     */
    public static File getDiskCacheDir(Context context, String uniqueName) {
    	String cachePath;
    	if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
    			|| !Environment.isExternalStorageRemovable()) {
    		cachePath = context.getExternalCacheDir().getPath();
    	} else {
    		cachePath = context.getCacheDir().getPath();
    	}
    	return new File(cachePath + File.separator + uniqueName);
    }


	private FileUtils() {/* Do not new me */};
}
