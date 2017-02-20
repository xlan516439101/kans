package org.kans.zxb.fragment;

import java.util.ArrayList;
import java.util.List;

import org.kans.zxb.R;
import org.kans.zxb.presenter.MainPresenter;
import org.kans.zxb.view.KViewPager;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

@ContentView(R.layout.fragment_main)
public class MainFragment extends KFragment<MainPresenter, MainPresenter.Ui> implements MainPresenter.Ui, OnClickListener, OnPageChangeListener{
	public interface IsetMainInterface{
		void setMainFragment(MainFragment mFragment);
		void viewPagerCanScroll(boolean canScroll);
		void onPageScrollStateChanged(int state);
	}
	
	
	private final int[] bottomId = new int[]{
			R.id.fragment_main_bottom_vip,
			R.id.fragment_main_bottom_favorite,
			R.id.fragment_main_bottom_group,
			R.id.fragment_main_bottom_setting
	};
	
	private final int[] bottomStringId = new int[]{
			R.string.kans_bottom_vip,
			R.string.kans_bottom_favorite,
			R.string.kans_bottom_group,
			R.string.kans_bottom_setting
	};
	
	private final Class<?>[] fragmentClasses = new Class<?>[]{
			VipUserListFragment.class,
			VipUserFavoriteFragment.class,
			VipUserGroupFragment.class,
			KSettingsFragment.class
	};
	
	List<Fragment> mFragments;
	List<View> bottomViews;
	List<IsetMainInterface> mainInterfaces;
	@ViewInject(value = R.id.fragment_main_pager)
	KViewPager mViewPager;
	

	@ViewInject(value = R.id.fragment_main_bottom)
	LinearLayout bottomView;
	
	private MyViewPagerAdapter mMyViewPagerAdapter;
	private int selectPag = 0;
	
	public void viewPagerCanScroll(boolean canScroll){
		mViewPager.setScanScroll(canScroll);
	}
	
	private void initFragmentList(View view) throws java.lang.InstantiationException, IllegalAccessException {
		
		for (int i = 0; i < fragmentClasses.length; i++) {
			TextView textView = (TextView) bottomView.findViewById(bottomId[i]);
			textView.setText(bottomStringId[i]);
			bottomViews.add(textView);
			Object obj = fragmentClasses[i].newInstance();
			if(obj instanceof IsetMainInterface){
				IsetMainInterface mainInterface = (IsetMainInterface)obj;
				mainInterface.setMainFragment(this);
				mainInterfaces.add(mainInterface);
			}
			mFragments.add((Fragment)(obj));
			bottomViews.get(i).setOnClickListener(this);
		}
	}

	@Override
	public void onCreate(Bundle mBundle) {
		super.onCreate(mBundle);
		if(mBundle != null){
			selectPag = mBundle.getInt("select_pag", 0);
		}
		bottomViews = new ArrayList<View>();
		mFragments = new ArrayList<Fragment>();
		mainInterfaces = new ArrayList<IsetMainInterface>();
		Activity().setMenuDrawable(R.drawable.k_header_menu_more);
		Activity().getPopuMenu().inflate(R.menu.main);
		Activity().getPopuMenu().setOnMenuItemClickListener(getPresenter());
	}
	

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		return super.onContextItemSelected(item);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		try {
			initFragmentList(view);
		} catch (java.lang.InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		mMyViewPagerAdapter = new MyViewPagerAdapter(getChildFragmentManager(), mFragments);
		mViewPager.setAdapter(mMyViewPagerAdapter);
		mViewPager.setOnPageChangeListener(this);
		mViewPager.setCurrentItem(selectPag);
		onPageSelected(selectPag);
		return view;
	}
	@Override
	public boolean onMenuButtonClick() {
		Activity().getPopuMenu().show();
		return true;
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public MainPresenter createPresenter() {
		return new MainPresenter();
	}

	@Override
	public MainFragment getUi() {
		return this;
	}

	@Override
	public Context getContext() {
		return getActivity();
	}

	@Override
	public void onClick(View v) {
		for(int i=0;i<bottomId.length;i++){
			if(bottomId[i] == v.getId()){
				onPageSelected(i);
				mViewPager.setCurrentItem(i, true);
				return;
			}
		}
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		Log.i("xlan", "onPageScrollStateChanged"+state);
		for(IsetMainInterface m:mainInterfaces){
			m.onPageScrollStateChanged(state);
		}
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

	}

	@Override
	public void onPageSelected(int position) {

		for(int i=0;i<bottomId.length;i++){
			TextView textView = (TextView) bottomView.findViewById(bottomId[i]);
			textView.setSelected((position == i));
			textView.setTextColor((position == i)?0XFF007AFF:0XFF929292);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("select_pag", mViewPager.getCurrentItem());
	}
	

	class MyViewPagerAdapter extends FragmentPagerAdapter {
		private List<Fragment> mFragments;
	
		public MyViewPagerAdapter(FragmentManager fm, List<Fragment> mFragments) {
			super(fm);
			this.mFragments = mFragments;
		}
	
		@Override
		public Fragment getItem(int index) {
			return mFragments.get(index);
		}
	
		@Override
		public int getCount() {
			return mFragments.size();
		}
	}


	@Override
	public void showScanString(String result) {
		TextView mTextView = new TextView(getActivity());
		mTextView.setMinWidth(300);
		mTextView.setMinHeight(150);
		mTextView.setTextSize(20);
		mTextView.setBackgroundResource(R.color.k_background_color);
		mTextView.setTextColor(0xff0000ff);
		mTextView.setText(result);
		new AlertDialog.Builder(getContext())
		.setTitle(R.string.kans_menu_scan_callback_title)
		.setView(mTextView)
		.setPositiveButton(R.string.kans_positive_text, null)
		.create()
		.show();
	}

}
