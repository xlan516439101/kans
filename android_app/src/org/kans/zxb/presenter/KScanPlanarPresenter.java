package org.kans.zxb.presenter;

import java.io.IOException;
import java.util.Vector;

import org.kans.zxb.KUi;
import org.kans.zxb.R;
import org.kans.zxb.fragment.KScanPlanarFragment;
import org.kans.zxb.fragment.KScanPlanarFragment.ViewHold;
import org.kans.zxb.scan.CameraManager;
import org.kans.zxb.scan.InactivityTimer;
import org.kans.zxb.scan.KScanFragmentHandler;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Toast;

public class KScanPlanarPresenter extends KPresenter<KScanPlanarPresenter.Ui> implements ResultPointCallback {
	private static final long VIBRATE_DURATION = 200L;
	private ViewHold mViewHold;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.10f;
	private boolean vibrate;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private MediaPlayer mediaPlayer;
	private InactivityTimer inactivityTimer;
	public interface Ui extends KUi {

		KScanPlanarFragment getUi();

		ViewHold getViewHold();

		Context getContext();

		void setMenuDrawable(int resId);

		void finish(Intent intent);

		Resources getResources();

		KScanFragmentHandler getHandler();

		void setHandler(KScanFragmentHandler mHandler);

		void startActivity(Intent intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		CameraManager.init(getUi().getContext());
		inactivityTimer = new InactivityTimer(this);
	}

	@Override
	public void onStart() {
		super.onStart();
		mViewHold = getUi().getViewHold();
	}

	@Override
	public void onResume() {
		super.onResume();
		decodeFormats = null;
		characterSet = null;

		playBeep = true;
		AudioManager audioService = (AudioManager) getUi().getContext().getSystemService(Context.AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		initBeepSound();
		vibrate = true;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
		mViewHold = getUi().getViewHold();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		inactivityTimer.shutdown();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.roduct_icon:
			break;

		default:
			break;

		}
	}

	public boolean onMenuButtonClick() {
		return false;
	}

	public void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder, mViewHold.mSurfaceView);
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (getUi().getHandler() == null) {
			KScanFragmentHandler mHandler = new KScanFragmentHandler(this, getUi().getUi(), decodeFormats, characterSet, this);
			getUi().setHandler(mHandler);
		}
	}

	public void closeCamera() {
		CameraManager.get().closeDriver();
	}

	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getUi().getContext().getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};

	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			getUi().getUi().Activity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getUi().getResources().openRawResourceFd(R.raw.beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	public void handleDecode(Result result, Bitmap barcode) {
		inactivityTimer.onActivity();
		playBeepSoundAndVibrate();
		String resultString = result.getText();
		if (resultString.equals("")) {
			Toast.makeText(getUi().getUi().Activity(), "Scan failed!", Toast.LENGTH_SHORT).show();
		} else {
			Intent resultIntent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putString("result", resultString);
			bundle.putParcelable("bitmap", barcode);
			resultIntent.putExtras(bundle);
			getUi().finish(resultIntent);
		}
	}

	@Override
	public void foundPossibleResultPoint(ResultPoint point) {
		mViewHold.mViewfinderView.addPossibleResultPoint(point);
	}

	public void startActivity(Intent intent) {
		getUi().startActivity(intent);
	}

	public void drawViewfinder() {
		mViewHold.mViewfinderView.drawViewfinder();
	}

}
