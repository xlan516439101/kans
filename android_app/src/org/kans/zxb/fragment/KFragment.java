package org.kans.zxb.fragment;

import org.kans.zxb.KUi;
import org.kans.zxb.presenter.KPresenter;
import org.kans.zxb.ui.KansActivity;
import org.xutils.x;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class KFragment<T extends KPresenter<U>, U extends KUi> extends Fragment implements KansActivity.IActivityCallBack {

	private static final String KEY_FRAGMENT_HIDDEN = "key_fragment_hidden";

	private T mPresenter;

	public abstract T createPresenter();

	public abstract U getUi();

	private boolean injected = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		injected = true;
		return x.view().inject(this, inflater, container);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mPresenter.onUiReady(getUi());
		if (!injected) {
			x.view().inject(this, this.getView());
		}
	}

	protected KFragment() {
		mPresenter = createPresenter();
	}

	public T getPresenter() {
		return mPresenter;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPresenter.onUiReady(getUi());
		mPresenter.onCreate(savedInstanceState);
		Activity().addIActivityCallBack(this);
		if (savedInstanceState != null) {
			mPresenter.onRestoreInstanceState(savedInstanceState);
			if (savedInstanceState.getBoolean(KEY_FRAGMENT_HIDDEN)) {
				getFragmentManager().beginTransaction().hide(this).commit();
			}
		}
	}
	
	@Override
	public void onStart() {
		mPresenter.onUiReady(getUi());
		super.onStart();
		getPresenter().onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
		getPresenter().onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		getPresenter().onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
		getPresenter().onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Activity().removeIActivityCallBack(this);
		getPresenter().onDestroy();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mPresenter.onUiDestroy(getUi());
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mPresenter.onSaveInstanceState(outState);
		outState.putBoolean(KEY_FRAGMENT_HIDDEN, isHidden());
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((KansActivity) activity).onFragmentAttached(this);

	}

	public KansActivity Activity() {
		return (KansActivity) getActivity();
	}

	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		super.startActivityForResult(Activity().getBackButtonIntent(intent), requestCode);
	}

	@Override
	public void startActivity(Intent intent) {
		super.startActivity(Activity().getBackButtonIntent(intent));
	}

	@Override
	public boolean onBackButtonClick() {
		return false;
	}

	@Override
	public boolean onMenuButtonClick() {
		return false;
	}

	public void setMenuDrawable(Drawable drawable) {
		Activity().setMenuDrawable(drawable);
	}

	public void onClick(View view) {
		getPresenter().onClick(view);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		getPresenter().onActivityResult(requestCode, resultCode, data);
	}
}
