package org.kans.zxb.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

public class KFastBlur extends Handler implements Runnable {

	private FastBlurCallBack mBack;
	private Context context;
	private Bitmap mBitmap;
	private int radius;
	private boolean blurOk = true;
	private Thread runThread;

	private static final int RUN = 0x1001;
	private static final int CALLBACK = 0x1003;

	public interface FastBlurCallBack {
		void callBack(Bitmap bitmap, int radius);
	}

	public KFastBlur(FastBlurCallBack mBack, Context context, Bitmap mBitmap) {
		super();
		this.mBack = mBack;
		this.context = context;
		this.mBitmap = mBitmap;
	}

	public void setBitmap(Bitmap mBitmap) {
		this.mBitmap = mBitmap;
	}

	public void blurBitmap(int radius) {
		this.radius = radius;
		if (mBitmap != null && context != null && mBack != null) {
			if (blurOk) {
				sendEmptyMessage(RUN);
			}
		}
	}

	public void destroy() {
		removeMessages(RUN);
		removeMessages(CALLBACK);
		if (runThread != null) {
			if (runThread.isAlive()) {
				runThread.stop();
			}
			runThread = null;
		}
	}

	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		switch (msg.what) {
		case RUN:
			runThread = new Thread(this);
			runThread.start();
			break;

		case CALLBACK:
			Bitmap bitmap = (Bitmap) msg.obj;
			int temp = msg.arg1;
			mBack.callBack(bitmap, temp);
			if (temp != radius) {
				removeMessages(RUN);
				blurBitmap(radius);
			}

			break;

		default:
			break;
		}
	}

	@Override
	public void run() {
		removeMessages(RUN);
		blurOk = false;
		int temp = radius;
		Message msg = new Message();
		msg.what = CALLBACK;
		msg.obj = KansUtils.getBlurBitmap(mBitmap, context, temp);
		msg.arg1 = temp;
		sendMessage(msg);
		blurOk = true;
	}

}
