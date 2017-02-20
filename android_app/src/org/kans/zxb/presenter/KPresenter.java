package org.kans.zxb.presenter;

import org.kans.zxb.KUi;
import org.kans.zxb.KUpdateItem;
import org.kans.zxb.KUpdateManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public abstract class KPresenter<U extends KUi> extends KUpdateItem implements View.OnClickListener {

	private U mUi;

	public void onUiReady(U ui) {
		mUi = ui;
	}

	public final void onUiDestroy(U ui) {
		onUiUnready(ui);
		mUi = null;
	}

	public void onUiUnready(U ui) {
	}

	public void onSaveInstanceState(Bundle outState) {
	}

	public void onRestoreInstanceState(Bundle savedInstanceState) {
	}

	public U getUi() {
		return mUi;
	}

	public void onCreate(Bundle savedInstanceState) {
	}

	public void onStart() {
	}

	public void onResume() {
		KUpdateManager.getInstance().registerReceiver(this);
	}

	public void onPause() {
		KUpdateManager.getInstance().unregisterReceiver(this);
	}

	public void onStop() {
	}

	public void onDestroy() {
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	}
}
