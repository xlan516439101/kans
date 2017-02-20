package org.kans.zxb.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class FileBrowseUtil {
	public static String DATA_CACHE_PATH = "/data/xhl_cache";
	private static FileBrowseUtil mFileUtil;
	private static SharedPreferences mPreferences;
	private FileBrowseUtil(Context context) {
		super();
		DATA_CACHE_PATH = context.getCacheDir().getAbsolutePath();
	}
	
	public static SharedPreferences getSharedPreferences(Context context){
		if(mPreferences==null){
			mPreferences = context.getSharedPreferences(context.getPackageName()+"_SharedPreferences", Context.MODE_PRIVATE);
		}
		return mPreferences;
	}
	public static FileBrowseUtil getInstance(Context context) {
		if (context != null&&mFileUtil==null) {
			mFileUtil = new FileBrowseUtil(context);
		}
		return mFileUtil;
	}

	public boolean delete(String fileName) {
		return new File(fileName).delete();
	}

	public boolean isFile(String fileName) {
		return new File(fileName).isFile();
	}

	public boolean isDirectory(String fileName) {
		return new File(fileName).isDirectory();
	}

	public long length(String fileName) {
		return new File(fileName).length();
	}

	public long getFreeSpace(String fileName) {
		return new File(fileName).getFreeSpace();
	}

	public long getTotalSpace(String fileName) {
		return new File(fileName).getTotalSpace();
	}

	public long getUsableSpace(String fileName) {
		return new File(fileName).getUsableSpace();
	}

	public String[] list(String fileName) {
		if(new File(fileName).isDirectory()){
			File[] files = new File(fileName).listFiles();
			if(files!=null && files.length>0){
				String[] childs = new String[files.length];
				childs = new String[files.length];
				for(int i=0;i<childs.length;i++){
					childs[i] = files[i].getAbsolutePath();
				}
				return childs;
			}
		}
		return null;
	}

	public boolean mkdirs(String fileName) {
		return new File(fileName).mkdirs();
	}

	public boolean createNewFile(String fileName) throws IOException {
		return new File(fileName).createNewFile();
	}
	
	public boolean renameTo(String fileFrom, String fileTo){
		return new File(fileFrom).renameTo(new File(fileTo));
	}
	
	public static void chmodFile(File file) {
		chmodFile(file.getAbsolutePath());
	}

	public static void chmodFile(String fileName) {
		try {
			Runtime.getRuntime().exec("chmod 777 " + fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static int copyFile(int count, File fileFrom, File fileTo) {
		if(!fileTo.getParentFile().exists()){
			fileTo.getParentFile().mkdirs();
		}
		if (fileFrom.isFile()) {
			FileInputStream fileInputStream = null;
			FileOutputStream fileOutputStream = null;
			try {
				fileInputStream = new FileInputStream(fileFrom);
				fileOutputStream = new FileOutputStream(fileTo);
				byte[] buffer = new byte[5 * 1024];
				int len = 0;
				while ((len = fileInputStream.read(buffer, 0, buffer.length)) > 0) {
					fileOutputStream.write(buffer, 0, len);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (fileInputStream != null) {
					try {
						fileInputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (fileOutputStream != null) {
					try {
						fileOutputStream.flush();
						fileOutputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			chmodFile(fileTo);
			return ++count;
		}
		return count;
	}

	private static int copyFiles(int count, File fileFrom, File fileToPath) {
		if (fileFrom.isDirectory() && !fileToPath.exists()) {
			fileToPath.mkdirs();
		}
		File newFile = new File(fileToPath, fileFrom.getName());
		if (fileFrom.isFile()) {
			return copyFile(count, fileFrom, newFile);
		} else if (fileFrom.isDirectory()) {
			Log.i("xlan", " copy fromDirectory:" + fileFrom.getAbsolutePath() + "--- toPathDirectory:" + fileToPath.getAbsolutePath());
			File[] files = fileFrom.listFiles();
			if (files != null) {
				if (!newFile.exists()) {
					boolean mkdir = newFile.mkdirs();
					Log.i("xlan", mkdir + " copy files:" + fileToPath.getAbsolutePath() + "\n");
				}

				for (File item : files) {
					copyFiles(count, item, newFile);
				}
				chmodFile(fileToPath);
				return ++count;
			}
		}
		return count;
	}

	public static boolean utilCopyFile(File fileFrom, File fileTo) {
		if(fileFrom.isFile()){
			return copyFile(0, fileFrom, fileTo)>0;
		}else{
			return copyFiles(0, fileFrom, fileTo) > 0;
		}
	}
	public static byte[] utilGetFileContent(String fileName){
  		File fromFile = new File(fileName);
  		byte[] buffer = null;
  		if(fromFile.isFile()){
  			FileInputStream fileInputStream = null;
  			try {
				fileInputStream = new FileInputStream(fromFile);
				if(fromFile.length()>(64*1024)){
					buffer = new byte[64*1024];
				}else{
					buffer = new byte[(int)fromFile.length()];
				}
  				fileInputStream.read(buffer, 0, buffer.length);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				if(fileInputStream!=null){
					try {
						fileInputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
  		}
  		if(buffer!=null&&buffer.length>0){
			return buffer;
  		}else{
  			return "Do not file or file is directory!".getBytes();
  		}
  	}
	private static int deleFiles(int count,File file){
		if(file!=null&&file.exists()){
			if(file.isFile()){
				file.delete();
				return ++count;
			}else if(file.isDirectory()){
				File[] files = file.listFiles();
				if(files!=null){
					for(File item:files){
						deleFiles(count, item);
					}
				}
				file.delete();
				return ++count;
			}
		}
		return count;
	}
	
	public static boolean deleFiles(String fileName){
  		return deleFiles(0, new File(fileName))>0;
  	}
	
	private static boolean lessThan(int min,int space,String left,String right){
		int len = left.length();
		if(len>right.length()){
			len = right.length();
		}
		for(int i=0;i<len;i++){
			int l = left.charAt(i);
			if(l>min){
				l=l-space;
			}
			int r = right.charAt(i);
			if(r>min){
				r=r-space;
			}
			if(l<r){
				return true;
			}else if(l>r){
				return false;
			}
		}
		if(left.length()<right.length()){
			return true;
		}
		return false;
	}
	
	private static int getPoint(int min,int space,String str,String[] list,int leng){
		for(int j=0;j<leng;j++){
			String str1 = list[j];
			if(lessThan(min,space,str, str1)){
				return j;
			}
		}
		return leng;
	}
	
	public static String[] sort(String[] src){

		int minChar = Math.min('z', 'Z');
		int spaceChar = Math.abs('z'-'Z');
		String[] newSrc = new String[src.length];
		for(int i=0;i<src.length;i++){
			String str = src[i];
			if(i==0){
				newSrc[0]=str;
			}else{
				int point = getPoint(minChar,spaceChar,str,newSrc,i);
				for(int p=i;p>point;p--){
					newSrc[p] = newSrc[p-1];
				}
				newSrc[point]=str;
			}
		}
		return newSrc;
	}

}
