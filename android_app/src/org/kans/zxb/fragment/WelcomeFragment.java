package org.kans.zxb.fragment;

import org.kans.zxb.R;
import org.kans.zxb.ui.KansActivity;
import org.xutils.x;
import org.xutils.view.annotation.ContentView;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

@ContentView(value = R.layout.welcome_fragment)
public class WelcomeFragment extends Fragment implements Runnable {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onStart() {
		super.onStart();
		x.task().postDelayed(this, 200);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		x.task().removeCallbacks(this);
	}

	@Override
	public void run() {
		getActivity().finish();
		getActivity().overridePendingTransition(0, 0);
		Intent mIntent = new Intent(getActivity(), KansActivity.class);
		startActivity(mIntent);
		getActivity().overridePendingTransition(0, 0);
	}
}
