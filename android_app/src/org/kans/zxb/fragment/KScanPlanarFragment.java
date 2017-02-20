package org.kans.zxb.fragment;

import org.kans.zxb.R;
import org.kans.zxb.presenter.KScanPlanarPresenter;
import org.kans.zxb.scan.CameraManager;
import org.kans.zxb.scan.KScanFragmentHandler;
import org.kans.zxb.view.ScanFinderView;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import com.google.zxing.Result;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

@ContentView(value = R.layout.scan_planar_fragment)
public class KScanPlanarFragment extends KFragment<KScanPlanarPresenter, KScanPlanarPresenter.Ui> implements KScanPlanarPresenter.Ui, SurfaceHolder.Callback {

	@ViewInject(R.id.scan_preview_view)
	SurfaceView mSurfaceView;

	@ViewInject(R.id.scan_viewfinder_view)
	ScanFinderView mViewfinderView;

	private KScanFragmentHandler mHandler;
	private boolean hasSurface;

	public class ViewHold {
		public SurfaceView mSurfaceView;
		public ScanFinderView mViewfinderView;

		public ViewHold(SurfaceView mSurfaceView, ScanFinderView mViewfinderView) {
			super();
			this.mSurfaceView = mSurfaceView;
			this.mViewfinderView = mViewfinderView;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		hasSurface = false;
		Activity().setRequestedOrientation(Configuration.ORIENTATION_PORTRAIT);
	}

	public void setHandler(KScanFragmentHandler mHandler) {
		this.mHandler = mHandler;
	}

	public KScanFragmentHandler getHandler() {
		return mHandler;
	}

	@Override
	public void onResume() {
		super.onResume();
		SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
		if (hasSurface) {
			getPresenter().initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mHandler != null) {
			mHandler.quitSynchronously();
			mHandler = null;
		}
		getPresenter().closeCamera();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public KScanPlanarPresenter createPresenter() {
		return new KScanPlanarPresenter();
	}

	@Override
	public Context getContext() {
		return getActivity();
	}

	@Override
	public boolean onMenuButtonClick() {
		return getPresenter().onMenuButtonClick();
	}

	@Override
	public boolean onBackButtonClick() {
		Activity().setResult(Activity.RESULT_CANCELED);
		return false;
	}

	@Override
	public KScanPlanarFragment getUi() {
		return this;
	}

	@Override
	public KScanPlanarFragment.ViewHold getViewHold() {
		return new ViewHold(mSurfaceView, mViewfinderView);
	}

	@Override
	public void setMenuDrawable(int resId) {
		Activity().setMenuDrawable(resId);
	}

	@Override
	public void finish(Intent intent) {
		if(intent!=null){
			Log.i("xlan", intent.getStringExtra("result"));
			Activity().setResult(Activity.RESULT_OK, intent);
		}else{
			Activity().setResult(Activity.RESULT_CANCELED);
		}
		Activity().finish();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			getPresenter().initCamera(holder);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
	}

}
