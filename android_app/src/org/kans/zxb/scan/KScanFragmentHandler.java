/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kans.zxb.scan;

import java.util.Vector;

import org.kans.zxb.fragment.KScanPlanarFragment;
import org.kans.zxb.presenter.KScanPlanarPresenter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultPointCallback;

/**
 * This class handles all the messaging which comprises the state machine for
 * capture.
 */
public final class KScanFragmentHandler extends Handler {

	private static final String TAG = KScanFragmentHandler.class.getSimpleName();

	private final KScanPlanarFragment mFragment;
	private KScanPlanarPresenter mPresenter;
	private final DecodeThread decodeThread;
	private State state;

	private enum State {
		PREVIEW, SUCCESS, DONE
	}

	public KScanFragmentHandler(KScanPlanarPresenter mPresenter, KScanPlanarFragment mFragment, Vector<BarcodeFormat> decodeFormats, String characterSet, ResultPointCallback resultPointCallback) {
		this.mPresenter = mPresenter;
		this.mFragment = mFragment;
		decodeThread = new DecodeThread(mFragment, decodeFormats, characterSet, resultPointCallback);
		decodeThread.start();
		state = State.SUCCESS;
		// Start ourselves capturing previews and decoding.
		CameraManager.get().startPreview();
		restartPreviewAndDecode();
	}

	public static final int FRAGMENT_HANDLER_WHAT_AUTO_FOCUS = 0x101;
	public static final int FRAGMENT_HANDLER_WHAT_RESTART_PREVIEW = 0x102;
	public static final int FRAGMENT_HANDLER_WHAT_DECODE_SUCCEEDED = 0x103;
	public static final int FRAGMENT_HANDLER_WHAT_DECODE_FAILED = 0x104;
	public static final int FRAGMENT_HANDLER_WHAT_RETURN_SCAN_RESULT = 0x105;
	public static final int FRAGMENT_HANDLER_WHAT_LAUNCH_PRODUCT_QUERY = 0x106;

	@Override
	public void handleMessage(Message message) {
		switch (message.what) {
		case FRAGMENT_HANDLER_WHAT_AUTO_FOCUS:
			// Log.d(TAG, "Got auto-focus message");
			// When one auto focus pass finishes, start another. This is the
			// closest thing to
			// continuous AF. It does seem to hunt a bit, but I'm not sure what
			// else to do.
			if (state == State.PREVIEW) {
				CameraManager.get().requestAutoFocus(this, FRAGMENT_HANDLER_WHAT_AUTO_FOCUS);
			}
			break;
		case FRAGMENT_HANDLER_WHAT_RESTART_PREVIEW:
			Log.d(TAG, "Got restart preview message");
			restartPreviewAndDecode();
			break;
		case FRAGMENT_HANDLER_WHAT_DECODE_SUCCEEDED:
			Log.d(TAG, "Got decode succeeded message");
			state = State.SUCCESS;
			Bundle bundle = message.getData();

			Bitmap barcode = bundle == null ? null : (Bitmap) bundle.getParcelable(DecodeThread.BARCODE_BITMAP);

			mPresenter.handleDecode((Result) message.obj, barcode);
			break;
		case FRAGMENT_HANDLER_WHAT_DECODE_FAILED:
			// We're decoding as fast as possible, so when one decode fails,
			// start another.
			state = State.PREVIEW;
			CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), DecodeHandler.DECODE_WHAT_DECODE);
			break;
		case FRAGMENT_HANDLER_WHAT_RETURN_SCAN_RESULT:
			Log.d(TAG, "Got return scan result message");
			mPresenter.getUi().finish((Intent) message.obj);
			break;
		case FRAGMENT_HANDLER_WHAT_LAUNCH_PRODUCT_QUERY:
			Log.d(TAG, "Got product query message");
			String url = (String) message.obj;
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			mPresenter.startActivity(intent);
			break;
		}
	}

	public void quitSynchronously() {
		state = State.DONE;
		CameraManager.get().stopPreview();
		Message quit = Message.obtain(decodeThread.getHandler(), DecodeHandler.DECODE_WHAT_QUIT);
		quit.sendToTarget();
		try {
			decodeThread.join();
		} catch (InterruptedException e) {
			// continue
		}

		// Be absolutely sure we don't send any queued up messages
		removeMessages(FRAGMENT_HANDLER_WHAT_DECODE_SUCCEEDED);
		removeMessages(FRAGMENT_HANDLER_WHAT_DECODE_FAILED);
	}

	private void restartPreviewAndDecode() {
		if (state == State.SUCCESS) {
			state = State.PREVIEW;
			CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), DecodeHandler.DECODE_WHAT_DECODE);
			CameraManager.get().requestAutoFocus(this, FRAGMENT_HANDLER_WHAT_AUTO_FOCUS);
			mPresenter.drawViewfinder();
		}
	}

}
