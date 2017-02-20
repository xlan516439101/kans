package org.kans.zxb.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kans.zxb.KApplication;
import org.kans.zxb.R;
import org.kans.zxb.entity.KansTable;
import org.kans.zxb.entity.ProductEntity;
import org.kans.zxb.entity.VipUser;
import org.kans.zxb.util.HanZiToPinyin.Token;
import org.xutils.x;
import org.xutils.ex.DbException;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore.Images;
import android.provider.Telephony.Mms.Part;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;

public final class KansUtils {

	// ServDroid.web http服务器
	
	private static final Paint sPaint = new Paint();
	
	public static final int[] BUATFUL_COLORS = new int[]{
		0xff80f522, 0xffeef522, 0xff228ff5, 0xfff71862,
		0xffe518f7, 0xffc2367b, 0xffe9c32d, 0xffe9472d,
		0xff2de931, 0xff2de9e7, 0xff09c30e, 0xff08efc6,
		0xff71ee34, 0xff3446ee, 0xffec34ee, 0xffee3494,
		0xffef67ad, 0xff2948cb, 0xff4e67d0, 0xff4ed05b,
		0xff4ed0b3, 0xff674ed0, 0xff8372cf, 0xff93cf72
	};
	
	public static String getUpperCase(String str) {
		
		ArrayList<HanZiToPinyin.Token> tokens = HanZiToPinyin.getInstance().get(str); 
		StringBuilder sb = new StringBuilder(); 
		
		if ((tokens != null) && (tokens.size() > 0)) {
			for (HanZiToPinyin.Token token : tokens) {
				if (Token.PINYIN == token.type) { 
					sb.append(token.target);
				} else {
					sb.append(token.source); 
				}
			}
		}
		return sb.toString().toUpperCase();
	}
	
	public static Bitmap getStringBitmap(int height, int width, String name, boolean mRoundRect, int color) {
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		final Rect bounds = new Rect(0, 0, width, height);
		Canvas canvas = new Canvas(bitmap);
		Path mPath = new Path();
		mPath.addRoundRect(new RectF(0, 0, width, height), width/8, height/8, Direction.CCW);
		canvas.clipPath(mPath);
		canvas.drawColor(color);
		int save = canvas.save();
		sPaint.setTypeface(Typeface.DEFAULT_BOLD);
		float textSize = 2f * height;
		sPaint.setTextSize(textSize);
		sPaint.setColor(0X77FFFFFF);
		canvas.rotate(45f, bounds.centerX(), bounds.centerY());
		String displayName = getUpperCase(name);
		if(displayName.length()==0){
			displayName = name;
		}
		if(displayName.length()==0){
			displayName = "#";
		}
		canvas.drawText(displayName, 0, 1, bounds.centerX() - textSize * 0.4f, bounds.centerY() + textSize / 2, sPaint);
		canvas.restoreToCount(save);
		canvas.rotate(55f, bounds.centerX(), bounds.centerY());
		if (displayName.length() > 1) {
			canvas.drawText(displayName, 1, 2, bounds.centerX() - textSize * 0.2f, bounds.centerY() + textSize / 2, sPaint);
		}

		canvas.restoreToCount(save);
		return bitmap;
	}
	public static boolean isStandardString(String str){
		if(str==null||str.length()==0){
			return false;
		}
        Pattern pattern = Pattern.compile("[0-9|a-z|A-Z| ]*");//.line 160
        Matcher isNum = pattern.matcher(str);//
        if(!isNum.matches()) {//
            return false;//
        }
		return true;
	}

    public static boolean isNumberString(String phoneNum) {
		if(phoneNum==null||phoneNum.length()==0){
			return false;
		}
        Pattern pattern = Pattern.compile("[0-9|-| ]*");//.line 160
        Matcher isNum = pattern.matcher(phoneNum);//
        if(!isNum.matches()) {//
            return false;//
        }
        return true;//
    }
	public static boolean delFile(String path){
		File file = new File(path);
		if(file.exists()){
			return delFile(file);
		}
		return false;
	}
	
	public static boolean delFile(File file){
		if(file.exists()){
			if(file.isDirectory()){
				File[] childs = file.listFiles();
				if(childs !=null && childs.length>0){
					for(File child:childs){
						delFile(child);
					}
				}
			}
			return file.delete();
		}
		return false;
	}
	
	public static Bitmap getPlanarCode(String url,int width,int height) {
		if(url == null || "".equals(url) || url.length()<1 || width<1 || height<1){
			return null;
		}
		
		Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
		hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
		try {
			//图像数据转换，使用矩阵转换
			BitMatrix bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, width, height);
			int[] pixels = new int[width*height];
			for(int y=0;y<height;y++){
				for(int x = 0;x<width;x++){
					if(bitMatrix.get(x, y)){
						pixels[y*width+x]=0xff111111;
					}else{
						pixels[y*width+x]=0xffeeeeee;
					}
				}
			}
			Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
			return bitmap;
		} catch (WriterException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static Bitmap getPlanarCodeSrcBitmap(String url,Bitmap srcBitmap) {
		if(url == null || "".equals(url) || url.length()<1 || srcBitmap==null || srcBitmap.isRecycled()){
			return null;
		}
		int width = srcBitmap.getWidth();
		int height = srcBitmap.getHeight();
		Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
		hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
		try {
			//图像数据转换，使用矩阵转换
			BitMatrix bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, width, height);
			int[] pixels = new int[width*height];
			srcBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
			for(int y=0;y<height;y++){
				for(int x = 0;x<width;x++){
					if(bitMatrix.get(x, y)){
						pixels[y*width+x]=0xffffffff&pixels[y*width+x];
					}else{
						if((pixels[y*width+x]&0xff000000) != 0){
							pixels[y*width+x]=0xffdddddd|pixels[y*width+x];
						}
					}
				}
			}
			Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
			return bitmap;
		} catch (WriterException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static Bitmap getPlanarCode(String url,Bitmap srcBitmap) {
		if(url == null || "".equals(url) || url.length()<1 || srcBitmap==null || srcBitmap.isRecycled()){
			return null;
		}
		int width = srcBitmap.getWidth();
		int height = srcBitmap.getHeight();
		Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
		hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
		try {
			//图像数据转换，使用矩阵转换
			BitMatrix bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, width, height);
			int[] pixels = new int[width*height];
			srcBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
			for(int y=0;y<height;y++){
				for(int x = 0;x<width;x++){
					if(bitMatrix.get(x, y)){
						pixels[y*width+x]=0xff406ca9;
					}else{
						if((pixels[y*width+x]&0xff000000) != 0){
							pixels[y*width+x]=(pixels[y*width+x]&0xff000000)|0xd2d5d8;
						}
					}
				}
			}
			Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
			return bitmap;
		} catch (WriterException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public static Bitmap getBarCode(String url,int width,int height) {
		if(url == null || "".equals(url) || url.length()<1 || width<50 || height<50){
			return null;
		}
		char[] chars = url.toCharArray();
		url = "";
		for(char c:chars){
			if(c >= '0' && c <= '9'){
				url += c;
			}
		}
		Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
		hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
		try {
			//图像数据转换，使用矩阵转换
			BitMatrix bitMatrix = new MultiFormatWriter().encode(url, BarcodeFormat.CODE_128, width, height, hints);
			int[] pixels = new int[width*height];
			for(int y=0;y<height;y++){
				for(int x = 0;x<width;x++){
					if(bitMatrix.get(x, y)){
						pixels[y*width+x]=0xff000000;
					}else{
						pixels[y*width+x]=0xffeeeeee;
					}
				}
			}
			Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
			Canvas mCanvas = new Canvas(bitmap);
			Paint mPaint = new Paint();
			mPaint.setTextSize(30);
			float[] measuredWidth =new float[2];
			mPaint.breakText(url, true, width, measuredWidth);
			float x = (width-measuredWidth[0])/2;
			float y = height-50;
			mPaint.setColor(Color.WHITE);
			mPaint.setStyle(Paint.Style.FILL);
			mCanvas.drawRect(new Rect((int)(x-20), (int)y, (int)(x+measuredWidth[0]+20), (int)(y+35)), mPaint);
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setColor(Color.BLACK);
			mCanvas.drawText(url, x, y+27, mPaint);
			return bitmap;
		} catch (WriterException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public static Bitmap getVideoThumbnail(String path) {
		Bitmap bitmap = null;
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		try {
			retriever.setDataSource(path);
			bitmap = retriever.getFrameAtTime(-1);
		} catch (IllegalArgumentException ex) {
		} catch (RuntimeException ex) {
		} finally {
			try {
				retriever.release();
			} catch (RuntimeException ex) {
			}
		}
		return bitmap;
	}

	public static String getBirthdayDays(Context context, VipUser mVipUser) {
		StringBuffer sb = new StringBuffer();
		int days = toBirthdayDays(mVipUser);
		switch (days) {
		case -1:
			return "";
		case 0:
			sb.append(context.getResources().getString(R.string.birthday_today));
			break;

		case 1:
			sb.append(context.getResources().getString(R.string.birthday_tomorrow));
			break;
		default:
			sb.append(days + context.getResources().getString(R.string.birthday__day));
			break;
		}
		return sb.toString();
	}

	public static int toBirthdayDays(VipUser mVipUser) {
		int[] currentDate = getDate();
		int currentYear = currentDate[0];
		int currentMonth = currentDate[1];
		int currentDay = currentDate[2];
		int[] data = null;
		String birthday = mVipUser.birthday;
		if (birthday != null) {
			String[] strs = birthday.split("-");
			if (strs.length == 3) {
				int year = Integer.valueOf(strs[0]);
				int month = Integer.valueOf(strs[1]);
				int day = Integer.valueOf(strs[2]);
				data = new int[] { year, month, day };
			}
		}
		if (data == null) {
			data = currentDate;
		}
		int month = data[1];
		int day = data[2];
		if (mVipUser.birthday_isLunar == 1) {// 如果是农历就转换成阳历日期
			int[] solar = ChineseCalendar.sCalendarLundarToSolar(currentYear, month, day);
			Log.i("xlan", "toBirthdayDays isAfter" + month + "---" + day);
			month = solar[1];
			day = solar[2];
			Log.i("xlan", "toBirthdayDays isAfter" + month + "---" + day);
		}
		if (month == currentMonth && day == currentDay) {
			return 0;
		}
		boolean isAfter = true;
		if (month > currentMonth || (month == currentMonth && day > currentDay)) {
			isAfter = true;
		} else {
			isAfter = false;
		}

		Log.i("xlan", "toBirthdayDays isAfter" + isAfter);
		try {
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Date dateCurrent = format.parse(currentYear + "-" + currentMonth + "-" + currentDay);
			Date dateBirthday;
			if (isAfter) {// 今年有生日
				dateBirthday = format.parse(currentYear + "-" + month + "-" + day);
			} else {// 生日在明年
				if (mVipUser.birthday_isLunar == 1) {// 如果是农历就转换成阳历日期
					int[] solar = ChineseCalendar.sCalendarLundarToSolar(currentYear + 1, data[1], data[2]);
					month = solar[1];
					day = solar[2];
				}
				dateBirthday = format.parse((currentYear + 1) + "-" + month + "-" + day);
			}
			long time = dateBirthday.getTime() - dateCurrent.getTime();// 毫秒数
			int days = (int) (time / (24 * 60 * 60l * 1000l));
			return days;
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return -1;
	}

	public static boolean equals(CharSequence a, CharSequence b) {
		if (b != null && (b.equals("null") || b.equals(""))) {
			b = null;
		}
		if (a != null && (a.equals("null") || a.equals(""))) {
			a = null;
		}
		if (a == null && b == null) {
			return true;
		} else if ((a == null && b != null) || (a != null && b == null)) {
			return false;
		} else {
			return TextUtils.equals(a, b);
		}
	}

	public static void setImeVisibility(final boolean visible, final Context context, final View view) {
		Runnable mShowImeRunnable = new Runnable() {
			public void run() {
				InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

				if (imm != null) {
					imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
				}
			}
		};
		if (visible) {
			x.task().post(mShowImeRunnable);
		} else {
			x.task().removeCallbacks(mShowImeRunnable);
			InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			if (imm != null) {
				imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
			}
		}
	}
	
	public static String getVipId(int count){
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		String last = "";
		if(count>1000){
			last = String.valueOf(count%1000);
		}else if(count/100>0){
			last = String.valueOf(count);
		}else if(count/10>0){
			last = "0"+String.valueOf(count);
		}else if(count>0){
			last = "00"+String.valueOf(count);
		}
		String vipId = "69"+format.format(new Date(System.currentTimeMillis()))+last;
		try {
			VipUser user =x.getDb(KApplication.localDaoConfig).selector(VipUser.class).where(VipUser._VIP_ID, "=", vipId).findFirst();
			if(user == null){
				return vipId;
			}else{
				return getVipId(++count);
			}
		} catch (DbException e) {
			e.printStackTrace();
			return "666"+new SimpleDateFormat("yyyyMMddhhmmss").format(new Date(System.currentTimeMillis()));
		}
	}
	
	public static File getVipIconThumbnail(VipUser VipUser) {
		if (VipUser == null || VipUser.name == null) {
			return null;
		} else {
			File vipRootDir = new File(KApplication.rootFilePath, "vip");
			if (!vipRootDir.exists()) {
				vipRootDir.mkdirs();
			}
			File vipDir = new File(vipRootDir, VipUser.vipId);
			if (!vipDir.exists()) {
				vipDir.mkdirs();
			}
			File picDir = new File(vipDir, "icons");
			if (!picDir.exists()) {
				picDir.mkdirs();
			}
			return new File(picDir, "icon_thumbnail.png");
		}
	}
	
	public static File getVipIcon(VipUser VipUser) {
		if (VipUser == null || VipUser.name == null) {
			return null;
		} else {
			File vipRootDir = new File(KApplication.rootFilePath, "vip");
			if (!vipRootDir.exists()) {
				vipRootDir.mkdirs();
			}
			File vipDir = new File(vipRootDir, VipUser.vipId);
			if (!vipDir.exists()) {
				vipDir.mkdirs();
			}
			File picDir = new File(vipDir, "icons");
			if (!picDir.exists()) {
				picDir.mkdirs();
			}
			return new File(picDir, "icon.png");
		}
	}

	public static void saveVipIconPath(VipUser VipUser, Bitmap bitmap) {
		File mFile = getVipIcon(VipUser);
		if (mFile != null) {
			VipUser.iconPath = mFile.getAbsolutePath();
			if (!mFile.getParentFile().exists()) {
				mFile.getParentFile().mkdirs();
			}
			try {
				mFile.createNewFile();
				FileOutputStream fos = new FileOutputStream(mFile);
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
				fos.flush();
				fos.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			File mThumbnailFile = getVipIconThumbnail(VipUser);
			Bitmap thumbnail = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(thumbnail);
			Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
			Rect dst = new Rect(0, 0, thumbnail.getWidth(), thumbnail.getHeight());
			canvas.drawBitmap(bitmap, src, dst, new Paint(Paint.FILTER_BITMAP_FLAG));

			if (!mThumbnailFile.getParentFile().exists()) {
				mThumbnailFile.getParentFile().mkdirs();
			}
			try {
				mThumbnailFile.createNewFile();
				FileOutputStream fos = new FileOutputStream(mThumbnailFile);
				thumbnail.compress(Bitmap.CompressFormat.PNG, 100, fos);
				fos.flush();
				fos.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			VipUser.iconPath = null;
		}
	}
	public static File getProductIconThumbnail(ProductEntity mProductEntity) {
		if (mProductEntity == null || mProductEntity.name == null) {
			return null;
		} else {
			File fileDir = new File(KApplication.rootFilePath, "product");
			if (!fileDir.exists()) {
				fileDir.mkdirs();
			}
			File iconDir = new File(fileDir, "icons");
			if (!iconDir.exists()) {
				iconDir.mkdirs();
			}
			return new File(iconDir, mProductEntity.name + "_thumbnail.png");
		}
	}
	public static File getSettingsIcon() {
		File fileDir = new File(KApplication.rootFilePath, "settings");
		if (!fileDir.exists()) {
			fileDir.mkdirs();
		}
		return new File(fileDir, "icon.png");
	}

	public static void saveSettingsIconPath(KansTable mKansTable, Bitmap bitmap) {
		File mFile = getSettingsIcon();
		if (mFile != null) {
			mKansTable.iconPath = mFile.getAbsolutePath();
			if (!mFile.getParentFile().exists()) {
				mFile.getParentFile().mkdirs();
			}
			try {
				mFile.createNewFile();
				FileOutputStream fos = new FileOutputStream(mFile);
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
				fos.flush();
				fos.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			mKansTable.iconPath = null;
		}
	}

	public static File getProductIcon(ProductEntity mProductEntity) {
		if (mProductEntity == null || mProductEntity.name == null) {
			return null;
		} else {
			File fileDir = new File(KApplication.rootFilePath, "product");
			if (!fileDir.exists()) {
				fileDir.mkdirs();
			}
			File iconDir = new File(fileDir, "icons");
			if (!iconDir.exists()) {
				iconDir.mkdirs();
			}
			return new File(iconDir, mProductEntity.name + ".png");
		}
	}

	public static void saveProductIconPath(ProductEntity mProductEntity, Bitmap bitmap) {
		File mFile = getProductIcon(mProductEntity);
		if (mFile != null) {
			mProductEntity.iconPath = mFile.getAbsolutePath();
			if (!mFile.getParentFile().exists()) {
				mFile.getParentFile().mkdirs();
			}
			try {
				mFile.createNewFile();
				FileOutputStream fos = new FileOutputStream(mFile);
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
				fos.flush();
				fos.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			File mThumbnailFile = getProductIconThumbnail(mProductEntity);
			Bitmap thumbnail = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(thumbnail);
			Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
			Rect dst = new Rect(0, 0, thumbnail.getWidth(), thumbnail.getHeight());
			canvas.drawBitmap(bitmap, src, dst, new Paint(Paint.FILTER_BITMAP_FLAG));

			if (!mThumbnailFile.getParentFile().exists()) {
				mThumbnailFile.getParentFile().mkdirs();
			}
			try {
				mThumbnailFile.createNewFile();
				FileOutputStream fos = new FileOutputStream(mThumbnailFile);
				thumbnail.compress(Bitmap.CompressFormat.PNG, 100, fos);
				fos.flush();
				fos.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			mProductEntity.iconPath = null;
		}
	}

	public static Bitmap getRoundIconBitmap(Bitmap src, int wh) {
		Bitmap roundBitmap = getRoundIconBitmap(src);
		Bitmap bitmap = Bitmap.createBitmap(wh, wh, Bitmap.Config.ARGB_8888);
		Canvas mCanvas = new Canvas(bitmap);
		mCanvas.drawBitmap(roundBitmap, new Rect(0, 0, roundBitmap.getWidth(), roundBitmap.getHeight()), new Rect(0, 0, wh, wh), new Paint(Paint.FILTER_BITMAP_FLAG));
		roundBitmap.recycle();
		return bitmap;
	}

	public static Bitmap getRoundIconBitmap(Bitmap src) {
		// 圆角 0.18
		int width = src.getWidth();
		int height = src.getHeight();

		int bitmapWH = width < height ? width : height;
		float radius = bitmapWH * 0.19f;
		Bitmap bitmap = Bitmap.createBitmap(bitmapWH, bitmapWH, Bitmap.Config.ARGB_8888);
		Canvas mCanvas = new Canvas(bitmap);
		Path mPath = new Path();
		mPath.addRoundRect(new RectF(0, 0, bitmapWH, bitmapWH), radius, radius, Path.Direction.CCW);
		Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
		mPaint.setAntiAlias(true);
		mCanvas.clipPath(mPath);
		mCanvas.drawBitmap(src, -(width - bitmapWH) / 2, -(height - bitmapWH) / 2, mPaint);
		return bitmap;
	}
	
	public static Bitmap getRoundBitmap(Bitmap src) {
		// 圆角 0.18
		int width = src.getWidth();
		int height = src.getHeight();

		int bitmapWH = width < height ? width : height;
		float radius = bitmapWH * 0.19f;
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas mCanvas = new Canvas(bitmap);
		Path mPath = new Path();
		mPath.addRoundRect(new RectF(0, 0, width, height), radius, radius, Path.Direction.CCW);
		Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
		mPaint.setAntiAlias(true);
		mCanvas.clipPath(mPath);
		mCanvas.drawBitmap(src, 0, 0, mPaint);
		return bitmap;
	}
	public static boolean isMmsUri(Uri uri) {
		return uri.getAuthority().startsWith("mms");
	}

	public static boolean sendMmsToWechat(Context context, String name, String message) {
		String title = "对话框标题，分享文字";
		String subject = "我的主题";
		String content = "内容";
		Uri imagePaht = Uri.fromFile(new File(""));

		return false;
	}

	public static void shareText(String dlgTitle, String subject, String content) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		intent.putExtra(Intent.EXTRA_TEXT, content);

	}

	public static void shareImg(String dlgTitle, String subject, String content, Uri uri) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("image/*");
		intent.putExtra(Intent.EXTRA_STREAM, uri);
		intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		intent.putExtra(Intent.EXTRA_TEXT, content);

	}

	public static int copy(InputStream is, OutputStream os) throws IOException {
		byte[] buffer = new byte[0x4000];
		int i = 0;
		int leng = 0;
		while ((i = is.read(buffer, 0, buffer.length)) > 0) {
			os.write(buffer, 0, i);
			leng += i;
		}
		return leng;
	}

	public static boolean isNetAvailable(Context context) {
		ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (mConnectivityManager != null) {
			NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
			if (info == null || !info.isAvailable()) {
				return false;
			}
		} else {
			return false;
		}
		return true;
	}

	public static Bitmap getBlurBitmap(Bitmap bkg, Context context, int blurRadius) {
		if (blurRadius < 1) {
			return bkg;
		}
		final int minScaleFactor = 3;
		final int maxScaleFactor = 7;
		float scaleFactor = blurRadius / minScaleFactor;
		if (scaleFactor < minScaleFactor) {
			scaleFactor = minScaleFactor;
		} else if (scaleFactor > maxScaleFactor) {
			scaleFactor = maxScaleFactor;
		}
		int radius = (int) (blurRadius * minScaleFactor / scaleFactor);
		radius = radius > minScaleFactor ? radius : minScaleFactor;

		Bitmap overlay = Bitmap.createBitmap((int) (bkg.getWidth() / scaleFactor), (int) (bkg.getHeight() / scaleFactor), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(overlay);
		canvas.scale(1 / scaleFactor, 1 / scaleFactor);
		Paint paint = new Paint();
		paint.setFlags(Paint.FILTER_BITMAP_FLAG);
		// if(!bkg.isRecycled())
		// android.util.Log.v(""+)
		canvas.drawBitmap(bkg, 0, 0, paint);
		overlay = doBlur(overlay, radius, true);

		Bitmap newBitmap = Bitmap.createBitmap(bkg.getWidth(), bkg.getHeight(), Bitmap.Config.ARGB_8888);
		canvas = new Canvas(newBitmap);
		int save = canvas.save();
		if (blurRadius < 8) {
			canvas.drawBitmap(bkg, 0, 0, paint);
		}
		canvas.restoreToCount(save);
		canvas.scale(newBitmap.getWidth() / (overlay.getWidth() * 1f), newBitmap.getHeight() / (overlay.getHeight() * 1f));
		paint = new Paint();
		paint.setFlags(Paint.FILTER_BITMAP_FLAG);
		if (blurRadius < 8) {
			int alpha = ((8 - blurRadius) / 8) * 255;
			alpha = Math.max(0, alpha);
			alpha = Math.min(255, alpha);
			paint.setAlpha(alpha);
		}
		canvas.drawBitmap(overlay, 0, 0, paint);
		overlay.recycle();
		// bkg.recycle();
		return newBitmap;

	}

	private static Bitmap doBlur(Bitmap sentBitmap, int radius, boolean canReuseInBitmap) {

		Bitmap bitmap;
		if (canReuseInBitmap) {
			bitmap = sentBitmap;
		} else {
			bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
		}
		if (radius < 1) {
			return (null);
		}
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		int[] pix = new int[w * h];
		bitmap.getPixels(pix, 0, w, 0, 0, w, h);
		int wm = w - 1;
		int hm = h - 1;
		int wh = w * h;
		int div = radius + radius + 1;
		int r[] = new int[wh];
		int g[] = new int[wh];
		int b[] = new int[wh];
		int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
		int vmin[] = new int[Math.max(w, h)];
		int divsum = (div + 1) >> 1;
		divsum *= divsum;
		int dv[] = new int[256 * divsum];
		for (i = 0; i < 256 * divsum; i++) {
			dv[i] = (i / divsum);
		}
		yw = yi = 0;
		int[][] stack = new int[div][3];
		int stackpointer;
		int stackstart;
		int[] sir;
		int rbs;
		int r1 = radius + 1;
		int routsum, goutsum, boutsum;
		int rinsum, ginsum, binsum;
		for (y = 0; y < h; y++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			for (i = -radius; i <= radius; i++) {
				p = pix[yi + Math.min(wm, Math.max(i, 0))];
				sir = stack[i + radius];
				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);
				rbs = r1 - Math.abs(i);
				rsum += sir[0] * rbs;
				gsum += sir[1] * rbs;
				bsum += sir[2] * rbs;
				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}
			}
			stackpointer = radius;
			for (x = 0; x < w; x++) {
				r[yi] = dv[rsum];
				g[yi] = dv[gsum];
				b[yi] = dv[bsum];
				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;
				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];
				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];
				if (y == 0) {
					vmin[x] = Math.min(x + radius + 1, wm);
				}
				p = pix[yw + vmin[x]];
				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);
				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];
				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;
				stackpointer = (stackpointer + 1) % div;
				sir = stack[(stackpointer) % div];
				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];
				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];
				yi++;
			}
			yw += w;
		}
		for (x = 0; x < w; x++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			yp = -radius * w;
			for (i = -radius; i <= radius; i++) {
				yi = Math.max(0, yp) + x;
				sir = stack[i + radius];
				sir[0] = r[yi];
				sir[1] = g[yi];
				sir[2] = b[yi];
				rbs = r1 - Math.abs(i);
				rsum += r[yi] * rbs;
				gsum += g[yi] * rbs;
				bsum += b[yi] * rbs;
				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}
				if (i < hm) {
					yp += w;
				}
			}
			yi = x;
			stackpointer = radius;
			for (y = 0; y < h; y++) {
				// Preserve alpha channel: ( 0xff000000 & pix[yi] )
				pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];
				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;
				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];
				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];
				if (x == 0) {
					vmin[y] = Math.min(y + r1, hm) * w;
				}
				p = x + vmin[y];
				sir[0] = r[p];
				sir[1] = g[p];
				sir[2] = b[p];
				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];
				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;
				stackpointer = (stackpointer + 1) % div;
				sir = stack[stackpointer];
				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];
				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];
				yi += w;
			}
		}
		bitmap.setPixels(pix, 0, w, 0, 0, w, h);
		return (bitmap);
	}

	public static Bitmap getBlurBitmapRS(Bitmap bkg, Context context, int radius) {

		int scaleFactor = 4;
		Bitmap overlay = Bitmap.createBitmap(bkg.getWidth() / scaleFactor, bkg.getHeight() / scaleFactor, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(overlay);
		canvas.scale(1 / scaleFactor, 1 / scaleFactor);
		Paint paint = new Paint();
		paint.setFlags(Paint.FILTER_BITMAP_FLAG);
		canvas.drawBitmap(bkg, 0, 0, paint);

		RenderScript rs = RenderScript.create(context);
		Allocation overlayAlloc = Allocation.createFromBitmap(rs, overlay);
		ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, overlayAlloc.getElement());
		blur.setInput(overlayAlloc);
		blur.setRadius(radius / (scaleFactor * 1f));
		blur.forEach(overlayAlloc);
		overlayAlloc.copyTo(overlay);
		rs.destroy();
		if (radius < 10) {
			Bitmap newBitmap = Bitmap.createBitmap(bkg.getWidth(), bkg.getHeight(), Bitmap.Config.ARGB_8888);
			canvas = new Canvas(newBitmap);
			canvas.scale(scaleFactor, scaleFactor);
			paint = new Paint();
			paint.setFlags(Paint.FILTER_BITMAP_FLAG);
			int alpha = ((10 - radius) / 10) * 255;
			alpha = Math.max(0, alpha);
			alpha = Math.min(255, alpha);
			// paint.setAlpha(alpha);
			canvas.drawBitmap(overlay, 0, 0, paint);
			return newBitmap;
		}
		return overlay;

	}

	public static Bitmap getBitmapFromUri(Context context, Uri mUri) {
		String scheme = mUri.getScheme();
		String path = null;
		if (scheme.equals("content")) {
			path = pathFromContentUri(context, mUri);
		} else if (mUri.getScheme().equals("file")) {
			path = pathFromFile(context, mUri);
		}
		if (path != null && new File(path).isFile()) {
			return BitmapFactory.decodeFile(path);
		} else {
			return null;
		}
	}

	private static String pathFromFile(Context context, Uri uri) {
		String mPath = uri.getPath();
		MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
		String extension = MimeTypeMap.getFileExtensionFromUrl(mPath).toLowerCase();
		if (TextUtils.isEmpty(extension)) {
			int dotPos = mPath.lastIndexOf('.');
			if (0 <= dotPos) {
				extension = mPath.substring(dotPos + 1);
				extension = extension.toLowerCase();
			}
		}
		String mContentType = mimeTypeMap.getMimeTypeFromExtension(extension);
		if (mContentType == null) {
			mContentType = "application/octet-stream";
		}
		return mPath;
	}

	private static String pathFromContentUri(Context context, Uri uri) {
		String mContentType;
		ContentResolver resolver = context.getContentResolver();
		Cursor c = resolver.query(uri, null, null, null, null);

		String mSrc = null;
		if (c == null) {
			throw new IllegalArgumentException("Query on " + uri + " returns null result.");
		}

		try {
			if ((c.getCount() != 1) || !c.moveToFirst()) {
				throw new IllegalArgumentException("Query on " + uri + " returns 0 or multiple rows.");
			}

			String filePath;
			if (KansUtils.isMmsUri(uri)) {
				filePath = c.getString(c.getColumnIndexOrThrow(Part.FILENAME));
				if (TextUtils.isEmpty(filePath)) {
					filePath = c.getString(c.getColumnIndexOrThrow(Part._DATA));
				}
				mContentType = c.getString(c.getColumnIndexOrThrow(Part.CONTENT_TYPE));
			} else {
				try {
					filePath = c.getString(c.getColumnIndexOrThrow(Images.Media.DATA));
				} catch (IllegalArgumentException e) {
					filePath = uri.getPath();
				}
				if (TextUtils.isEmpty(filePath)) {
					filePath = uri.getPath();
				}
				try {
					mContentType = c.getString(c.getColumnIndexOrThrow(Images.Media.MIME_TYPE)); // mime_type
				} catch (IllegalArgumentException e) {
					try {
						mContentType = c.getString(c.getColumnIndexOrThrow("mimetype"));
					} catch (IllegalArgumentException ex) {
						mContentType = resolver.getType(uri);
						Log.v("xlan", "initFromContentUri: " + uri + ", resolver.getType => " + mContentType);
					}
				}

				int nameIndex = c.getColumnIndex(Images.Media.DISPLAY_NAME);
				if (nameIndex != -1) {
					mSrc = c.getString(nameIndex);
					if (!TextUtils.isEmpty(mSrc)) {
						mSrc = mSrc.replace(' ', '_');
					} else {
						mSrc = null;
					}
				}
			}
			return filePath;
		} catch (IllegalArgumentException e) {
			Log.e("xlan", "initFromContentUri couldn't load image uri: " + uri, e);
		} finally {
			c.close();
		}
		return null;
	}

	public static String getYearMothDayStr(long buy_date) {
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		return format.format(new Date(buy_date));
	}

	public static String getAlarmBeforeTime(Context context, int type) {
		String[] timeArray = context.getResources().getStringArray(R.array.alert_time_array);
		String beforeTime = null;
		int count = 0;
		for (int i = 0; i < timeArray.length; i++) {
			boolean haveType = (((type >> (timeArray.length - 1 - i)) & 0x1) == 0x1);
			if (haveType) {
				if (beforeTime == null) {
					beforeTime = timeArray[i];
				} else {
					if (count % 2 == 0 && i != 0 && i < (timeArray.length - 1)) {
						beforeTime += "\n" + timeArray[i];
					} else {
						beforeTime += "," + timeArray[i];
					}
				}
				count++;
			}
		}
		return beforeTime;
	}

	public static int stringToInt(String str) {
		char[] chars = str.toCharArray();
		int returnInt = 0;
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (c >= '0' && c <= '9') {
				returnInt = returnInt * 10 + (c - '0');
			}
		}
		return returnInt;
	}

	public static String normalizeNumber(String number) {
		number = number.replace(" ", "");
		StringBuffer sb = new StringBuffer();
		if (number.contains("+86")) {
			number = number.replace("+86", "");
		}
		char[] numberChars = number.toCharArray();
		for (char c : numberChars) {
			if (c >= '0' && c <= '9') {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static boolean isSolarLeapYear(int iYear) {
		return ((iYear % 4 == 0) && (iYear % 100 != 0) || iYear % 400 == 0);
	}

	public static int getDayCounts(int year, int month, boolean isLunar) {
		if (isLunar) {
			char[] lunarChars = getLunarCharArray(year);
			int[] months = new int[13];// 13���·ֱ�Ĵ�С
			for (int i = 1; i < months.length; i++) {
				if (i <= 4) {
					months[i] = (lunarChars[0] >> (4 - i)) & 0x1;
				} else {
					months[i] = (lunarChars[1] >> (12 - i)) & 0x1;
				}
			}
			return months[month] == 1 ? 30 : 29;
		} else {
			switch (month) {
			case 1:
			case 3:
			case 5:
			case 7:
			case 8:
			case 10:
			case 12:
				return 31;
			case 2:
				if (isSolarLeapYear(year)) {
					return 29;
				} else {
					return 28;
				}
			case 4:
			case 6:
			case 9:
			case 11:
				return 30;

			default:
				break;
			}
		}
		return month;
	}

	public static char[] getLunarCharArray(int year) {
		if (year > 2099 || year < 1901) {
			return null;
		}
		int lunarYearStart = (year - 1901) * 3;
		char[] lunarChar = new char[3];
		for (int i = 0; i < 3; i++) {
			lunarChar[i] = lunarYear[lunarYearStart + i];
		}
		return lunarChar;
	}

	public static List<String> getMonthStrings() {
		List<String> months = new ArrayList<String>();
		for (int i = 1; i <= 12; i++) {
			if (i < 10) {
				months.add("0" + i);
			} else {
				months.add(String.valueOf(i));
			}
		}
		return months;
	}

	public static List<String> getMonthLunarStrings(Context context) {

		String[] monthStrs = context.getResources().getStringArray(R.array.month_lunar);
		List<String> months = new ArrayList<String>();
		for (int i = 0; i < monthStrs.length; i++) {
			months.add(monthStrs[i]);
		}
		return months;
	}

	public static List<String> getDayStrings(int num) {
		List<String> days = new ArrayList<String>();
		for (int i = 1; i <= num; i++) {
			if (i < 10) {
				days.add("0" + i);
			} else {
				days.add(String.valueOf(i));
			}

		}
		return days;
	}

	public static String getYearLunarString(Context context, int year) {
		String[] yearStrs = context.getResources().getStringArray(R.array.year_lunar);
		StringBuffer sb = new StringBuffer();
		int current = year;
		sb.append(yearStrs[current / 1000]);
		sb.append(yearStrs[(current / 100) % 10]);
		sb.append(yearStrs[(current / 10) % 10]);
		sb.append(yearStrs[current % 10]);
		sb.append(yearStrs[yearStrs.length - 1]);
		return sb.toString();
	}

	public static String getBirthdayString(boolean isLunar, int year, int month, int day, Context context) {
		Resources mResources = context.getResources();
		StringBuilder sb = new StringBuilder();
		sb.append(isLunar ? mResources.getString(R.string.birthday_widget_lundar) : mResources.getString(R.string.birthday_widget_solar));
		sb.append("\t");
		if (isLunar) {
			sb.append(KansUtils.getYearLunarString(context, year));
			sb.append(mResources.getStringArray(R.array.lunar_month_array)[month - 1]);
			sb.append(KansUtils.getDayLunarStrings(context, day).get(day - 1));
		} else {
			sb.append(year + mResources.getString(R.string.birthday_year));
			sb.append(month + mResources.getString(R.string.birthday_month));
			sb.append(day + mResources.getString(R.string.birthday_day));
		}
		return sb.toString();
	}

	public static List<String> getDayLunarStrings(Context context, int num) {
		String[] dayStrs = context.getResources().getStringArray(R.array.day30_lunar);
		List<String> days = new ArrayList<String>();
		for (int i = 1; i <= num; i++) {
			if (i < 10) {
				days.add(dayStrs[dayStrs.length - 1] + dayStrs[i]);
			} else if (i == 10) {
				days.add(dayStrs[dayStrs.length - 1] + dayStrs[0]);
			} else {
				days.add((i / 10 == 1 ? "" : dayStrs[i / 10]) + dayStrs[0] + (i % 10 == 0 ? "" : dayStrs[i % 10]));
			}

		}
		return days;
	}

	public static Bitmap getBitmapFromPath(String path) {
		File file = new File(path);
		if (file.exists()) {
			return BitmapFactory.decodeFile(path);
		}
		return null;
	}

	public static int[] getDate() {
		Calendar mCalendar = Calendar.getInstance(Locale.getDefault());
		Date date = mCalendar.getTime();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String[] formatArray = dateFormat.format(date).split("-");
		int len = formatArray.length;
		int[] intArray = new int[len];
		for (int i = 0; i < len; i++) {
			intArray[i] = new Integer(formatArray[i]);
		}
		return intArray;
	}

	public static int[] getTime() {
		Calendar mCalendar = Calendar.getInstance(Locale.getDefault());
		Date date = mCalendar.getTime();
		DateFormat dateFormat = new SimpleDateFormat("HH-mm");
		String[] formatArray = dateFormat.format(date).split("-");
		int len = formatArray.length;
		int[] intArray = new int[len];
		for (int i = 0; i < len; i++) {
			intArray[i] = new Integer(formatArray[i]);
		}
		return intArray;
	}

	/*
	 * 公历年对应的农历数据,每年三字节, 格式第一字节BIT7-4 位表示闰月月份,值为0 为无闰月,BIT3-0 对应农历第1-4 月的大小
	 * 第二字节BIT7-0 对应农历第5-12 月大小,第三字节BIT7 表示农历第13 个月大小 月份对应的位为1 表示本农历月大(30 天),为0
	 * 表示小(29 天) 第三字节BIT6-5 表示春节的公历月份,BIT4-0 表示春节的公历日期
	 */
	private static final char[] lunarYear = new char[] { 0x04, 0xAe, 0x53, // 1901
			0x0A, 0x57, 0x48, // 1902
			0x55, 0x26, 0xBd, // 1903
			0x0d, 0x26, 0x50, // 1904
			0x0d, 0x95, 0x44, // 1905
			0x46, 0xAA, 0xB9, // 1906
			0x05, 0x6A, 0x4d, // 1907
			0x09, 0xAd, 0x42, // 1908
			0x24, 0xAe, 0xB6, // 1909
			0x04, 0xAe, 0x4A, // 1910
			0x6A, 0x4d, 0xBe, // 1911
			0x0A, 0x4d, 0x52, // 1912
			0x0d, 0x25, 0x46, // 1913
			0x5d, 0x52, 0xBA, // 1914
			0x0B, 0x54, 0x4e, // 1915
			0x0d, 0x6A, 0x43, // 1916
			0x29, 0x6d, 0x37, // 1917
			0x09, 0x5B, 0x4B, // 1918
			0x74, 0x9B, 0xC1, // 1919
			0x04, 0x97, 0x54, // 1920
			0x0A, 0x4B, 0x48, // 1921
			0x5B, 0x25, 0xBC, // 1922
			0x06, 0xA5, 0x50, // 1923
			0x06, 0xd4, 0x45, // 1924
			0x4A, 0xdA, 0xB8, // 1925
			0x02, 0xB6, 0x4d, // 1926
			0x09, 0x57, 0x42, // 1927
			0x24, 0x97, 0xB7, // 1928
			0x04, 0x97, 0x4A, // 1929
			0x66, 0x4B, 0x3e, // 1930
			0x0d, 0x4A, 0x51, // 1931
			0x0e, 0xA5, 0x46, // 1932
			0x56, 0xd4, 0xBA, // 1933
			0x05, 0xAd, 0x4e, // 1934
			0x02, 0xB6, 0x44, // 1935
			0x39, 0x37, 0x38, // 1936
			0x09, 0x2e, 0x4B, // 1937
			0x7C, 0x96, 0xBf, // 1938
			0x0C, 0x95, 0x53, // 1939
			0x0d, 0x4A, 0x48, // 1940
			0x6d, 0xA5, 0x3B, // 1941
			0x0B, 0x55, 0x4f, // 1942
			0x05, 0x6A, 0x45, // 1943
			0x4A, 0xAd, 0xB9, // 1944
			0x02, 0x5d, 0x4d, // 1945
			0x09, 0x2d, 0x42, // 1946
			0x2C, 0x95, 0xB6, // 1947
			0x0A, 0x95, 0x4A, // 1948
			0x7B, 0x4A, 0xBd, // 1949
			0x06, 0xCA, 0x51, // 1950
			0x0B, 0x55, 0x46, // 1951
			0x55, 0x5A, 0xBB, // 1952
			0x04, 0xdA, 0x4e, // 1953
			0x0A, 0x5B, 0x43, // 1954
			0x35, 0x2B, 0xB8, // 1955
			0x05, 0x2B, 0x4C, // 1956
			0x8A, 0x95, 0x3f, // 1957
			0x0e, 0x95, 0x52, // 1958
			0x06, 0xAA, 0x48, // 1959
			0x7A, 0xd5, 0x3C, // 1960
			0x0A, 0xB5, 0x4f, // 1961
			0x04, 0xB6, 0x45, // 1962
			0x4A, 0x57, 0x39, // 1963
			0x0A, 0x57, 0x4d, // 1964
			0x05, 0x26, 0x42, // 1965
			0x3e, 0x93, 0x35, // 1966
			0x0d, 0x95, 0x49, // 1967
			0x75, 0xAA, 0xBe, // 1968
			0x05, 0x6A, 0x51, // 1969
			0x09, 0x6d, 0x46, // 1970
			0x54, 0xAe, 0xBB, // 1971
			0x04, 0xAd, 0x4f, // 1972
			0x0A, 0x4d, 0x43, // 1973
			0x4d, 0x26, 0xB7, // 1974
			0x0d, 0x25, 0x4B, // 1975
			0x8d, 0x52, 0xBf, // 1976
			0x0B, 0x54, 0x52, // 1977
			0x0B, 0x6A, 0x47, // 1978
			0x69, 0x6d, 0x3C, // 1979
			0x09, 0x5B, 0x50, // 1980
			0x04, 0x9B, 0x45, // 1981
			0x4A, 0x4B, 0xB9, // 1982
			0x0A, 0x4B, 0x4d, // 1983
			0xAB, 0x25, 0xC2, // 1984
			0x06, 0xA5, 0x54, // 1985
			0x06, 0xd4, 0x49, // 1986
			0x6A, 0xdA, 0x3d, // 1987
			0x0A, 0xB6, 0x51, // 1988
			0x09, 0x37, 0x46, // 1989
			0x54, 0x97, 0xBB, // 1990
			0x04, 0x97, 0x4f, // 1991
			0x06, 0x4B, 0x44, // 1992
			0x36, 0xA5, 0x37, // 1993
			0x0e, 0xA5, 0x4A, // 1994
			0x86, 0xB2, 0xBf, // 1995
			0x05, 0xAC, 0x53, // 1996
			0x0A, 0xB6, 0x47, // 1997
			0x59, 0x36, 0xBC, // 1998
			0x09, 0x2e, 0x50, // 1999
			0x0C, 0x96, 0x45, // 2000
			0x4d, 0x4A, 0xB8, // 2001
			0x0d, 0x4A, 0x4C, // 2002
			0x0d, 0xA5, 0x41, // 2003
			0x25, 0xAA, 0xB6, // 2004
			0x05, 0x6A, 0x49, // 2005
			0x7A, 0xAd, 0xBd, // 2006
			0x02, 0x5d, 0x52, // 2007
			0x09, 0x2d, 0x47, // 2008
			0x5C, 0x95, 0xBA, // 2009
			0x0A, 0x95, 0x4e, // 2010
			0x0B, 0x4A, 0x43, // 2011
			0x4B, 0x55, 0x37, // 2012
			0x0A, 0xd5, 0x4A, // 2013
			0x95, 0x5A, 0xBf, // 2014
			0x04, 0xBA, 0x53, // 2015
			0x0A, 0x5B, 0x48, // 2016
			0x65, 0x2B, 0xBC, // 2017
			0x05, 0x2B, 0x50, // 2018
			0x0A, 0x93, 0x45, // 2019
			0x47, 0x4A, 0xB9, // 2020
			0x06, 0xAA, 0x4C, // 2021
			0x0A, 0xd5, 0x41, // 2022
			0x24, 0xdA, 0xB6, // 2023
			0x04, 0xB6, 0x4A, // 2024
			0x69, 0x57, 0x3d, // 2025
			0x0A, 0x4e, 0x51, // 2026
			0x0d, 0x26, 0x46, // 2027
			0x5e, 0x93, 0x3A, // 2028
			0x0d, 0x53, 0x4d, // 2029
			0x05, 0xAA, 0x43, // 2030
			0x36, 0xB5, 0x37, // 2031
			0x09, 0x6d, 0x4B, // 2032
			0xB4, 0xAe, 0xBf, // 2033
			0x04, 0xAd, 0x53, // 2034
			0x0A, 0x4d, 0x48, // 2035
			0x6d, 0x25, 0xBC, // 2036
			0x0d, 0x25, 0x4f, // 2037
			0x0d, 0x52, 0x44, // 2038
			0x5d, 0xAA, 0x38, // 2039
			0x0B, 0x5A, 0x4C, // 2040
			0x05, 0x6d, 0x41, // 2041
			0x24, 0xAd, 0xB6, // 2042
			0x04, 0x9B, 0x4A, // 2043
			0x7A, 0x4B, 0xBe, // 2044
			0x0A, 0x4B, 0x51, // 2045
			0x0A, 0xA5, 0x46, // 2046
			0x5B, 0x52, 0xBA, // 2047
			0x06, 0xd2, 0x4e, // 2048
			0x0A, 0xdA, 0x42, // 2049
			0x35, 0x5B, 0x37, // 2050
			0x09, 0x37, 0x4B, // 2051
			0x84, 0x97, 0xC1, // 2052
			0x04, 0x97, 0x53, // 2053
			0x06, 0x4B, 0x48, // 2054
			0x66, 0xA5, 0x3C, // 2055
			0x0e, 0xA5, 0x4f, // 2056
			0x06, 0xB2, 0x44, // 2057
			0x4A, 0xB6, 0x38, // 2058
			0x0A, 0xAe, 0x4C, // 2059
			0x09, 0x2e, 0x42, // 2060
			0x3C, 0x97, 0x35, // 2061
			0x0C, 0x96, 0x49, // 2062
			0x7d, 0x4A, 0xBd, // 2063
			0x0d, 0x4A, 0x51, // 2064
			0x0d, 0xA5, 0x45, // 2065
			0x55, 0xAA, 0xBA, // 2066
			0x05, 0x6A, 0x4e, // 2067
			0x0A, 0x6d, 0x43, // 2068
			0x45, 0x2e, 0xB7, // 2069
			0x05, 0x2d, 0x4B, // 2070
			0x8A, 0x95, 0xBf, // 2071
			0x0A, 0x95, 0x53, // 2072
			0x0B, 0x4A, 0x47, // 2073
			0x6B, 0x55, 0x3B, // 2074
			0x0A, 0xd5, 0x4f, // 2075
			0x05, 0x5A, 0x45, // 2076
			0x4A, 0x5d, 0x38, // 2077
			0x0A, 0x5B, 0x4C, // 2078
			0x05, 0x2B, 0x42, // 2079
			0x3A, 0x93, 0xB6, // 2080
			0x06, 0x93, 0x49, // 2081
			0x77, 0x29, 0xBd, // 2082
			0x06, 0xAA, 0x51, // 2083
			0x0A, 0xd5, 0x46, // 2084
			0x54, 0xdA, 0xBA, // 2085
			0x04, 0xB6, 0x4e, // 2086
			0x0A, 0x57, 0x43, // 2087
			0x45, 0x27, 0x38, // 2088
			0x0d, 0x26, 0x4A, // 2089
			0x8e, 0x93, 0x3e, // 2090
			0x0d, 0x52, 0x52, // 2091
			0x0d, 0xAA, 0x47, // 2092
			0x66, 0xB5, 0x3B, // 2093
			0x05, 0x6d, 0x4f, // 2094
			0x04, 0xAe, 0x45, // 2095
			0x4A, 0x4e, 0xB9, // 2096
			0x0A, 0x4d, 0x4C, // 2097
			0x0d, 0x15, 0x41, // 2098
			0x2d, 0x92, 0xB5, // 2099
	};
}
