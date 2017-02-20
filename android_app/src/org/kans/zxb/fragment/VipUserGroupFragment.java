package org.kans.zxb.fragment;

import java.lang.reflect.Field;
import org.kans.zxb.R;
import org.kans.zxb.entity.VipGroupLink;
import org.kans.zxb.entity.VipUser;
import org.kans.zxb.fragment.MainFragment.IsetMainInterface;
import org.kans.zxb.presenter.VipUserGroupPresenter;
import org.kans.zxb.util.KansUtils;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;

@ContentView(value = R.layout.vip_user_group_fragment)
public class VipUserGroupFragment extends KFragment<VipUserGroupPresenter, VipUserGroupPresenter.Ui> implements VipUserGroupPresenter.Ui, IsetMainInterface{

	@ViewInject(R.id.vip_user_list)
	ExpandableListView mListView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		getPresenter().updateData();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public VipUserGroupPresenter createPresenter() {
		return new VipUserGroupPresenter();
	}

	@Override
	public VipUserGroupFragment getUi() {
		return this;
	}

	@Override
	public Context getContext() {
		return getActivity();
	}

	@Event(value = { R.id.product_add_button }, type = View.OnClickListener.class)
	public void onViewClick(View view) {
		Log.i("xlan", "onViewClick" + view);
		super.onClick(view);
	}

	@Override
	public ExpandableListView getListView() {
		return mListView;
	}

	@Override
	public void showRemoveUserFromGroupDilog(VipGroupLink mVipGroupLink) {
		new RemoveUserFromGroupDilog(getContext(), mVipGroupLink).show();
	}

	@Override
	public void startActivityForResult(Intent intent, int requestCode) {

		super.startActivityForResult(intent, requestCode);
	}

	@Override
	public void startActivity(Intent intent) {
		super.startActivity(intent);
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		getPresenter().onActivityResult(requestCode, resultCode, data);
	}

	class RemoveUserFromGroupDilog implements DialogInterface.OnDismissListener, DialogInterface.OnClickListener {
		final VipUser mVipUser;
		final VipGroupLink mVipGroupLink;
		EditText nameView, remarkView;
		Builder mBuilder;
		int count = 0;

		public RemoveUserFromGroupDilog(Context context, VipGroupLink mVipGroupLink) {
			this.mVipGroupLink = mVipGroupLink;
			this.mVipUser = getPresenter().getVipUser(mVipGroupLink);
			mBuilder = new AlertDialog.Builder(context);
			mBuilder.setView(createView());
			mBuilder.setOnDismissListener(this);
			mBuilder.setNegativeButton(R.string.kans_negative_text, this);
			mBuilder.setPositiveButton(R.string.kans_positive_text, this);
			mBuilder.setCancelable(true);
			mBuilder.setTitle(R.string.vip_user_group_fragment_del_group);
			nameView.setEnabled(false);
			nameView.setText(mVipUser.name);
			remarkView.setEnabled(false);
			remarkView.setText(getRemarkString());
		}

		private String getRemarkString(){
			StringBuilder sb = new StringBuilder();
			sb.append(getString(R.string.kans_vip_in_group_text)+"\n");
			sb.append("\t\t"+getPresenter().getVipGroup(mVipGroupLink).name+"\n");
			sb.append(getString(R.string.kans_id_text)+"\n");
			sb.append("\t\t"+mVipUser.vipId+"\n");
			sb.append(getString(R.string.kans_credit_text)+"\n");
			sb.append("\t\t"+mVipUser.credit+getString(R.string.kans_credit_point_text)+"\n");
			sb.append(getString(R.string.kans_birthday_text)+"\n");
			sb.append("\t\t"+getUserBirthday(mVipUser)+"\n");
			return sb.toString();
		}
		
		private String getUserBirthday(VipUser mVipUser){
			String birthday = mVipUser.birthday;
			int[] date = null;
			if(birthday != null){
				String[] strs = birthday.split("-");
				if(strs.length==3){
					int year = Integer.valueOf(strs[0]);
					int month = Integer.valueOf(strs[1]);
					int day = Integer.valueOf(strs[2]);
					date = new int[]{year, month, day};
				}
			}
			if(date == null){
				date = KansUtils.getDate();
				birthday = date[0]+"-"+date[1]+"-"+date[2];
				mVipUser.birthday = birthday;
			}
			String showBirthday = KansUtils.getBirthdayString((mVipUser.birthday_isLunar==1), date[0], date[1], date[2], getContext());
			String days = "";//KansUtils.getBirthdayDays(getContext(), mVipUser);
			if(days.length()==0){
				return showBirthday;
			}else{
				String strings = showBirthday+"    ("+days+")";
				Spannable spannable = new SpannableStringBuilder(strings);
				spannable.setSpan(new ForegroundColorSpan(Color.RED), showBirthday.length(),strings.length() , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				spannable.setSpan(new AbsoluteSizeSpan(30), showBirthday.length(),strings.length() , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				return spannable.toString();
			}
		}
		public void show() {
			mBuilder.create().show();
		}

		public VipUser getVipUser() {
			return mVipUser;
		}

		private View createView() {
			View mView = View.inflate(getContext(), R.layout.vip_user_list_dialog, null);
			nameView = (EditText) mView.findViewById(R.id.vip_user_name);
			nameView.setBackground(null);
			remarkView = (EditText) mView.findViewById(R.id.vip_user_remark);
			remarkView.setBackground(null);
			return mView;
		}

		@Override
		public void onDismiss(DialogInterface dialog) {
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			try {
				Field mShowing = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
				mShowing.setAccessible(true);
				mShowing.set(dialog, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (which == DialogInterface.BUTTON_POSITIVE) {
				getPresenter().delVipUserFromGroup(mVipUser);
			} else if (which == DialogInterface.BUTTON_NEGATIVE) {
			}
		}
	}

	public MainFragment mainFragment;
	@Override
	public void setMainFragment(MainFragment mFragment) {
		this.mainFragment = mFragment;
	}

	@Override
	public void viewPagerCanScroll(boolean canScroll) {
		if(mainFragment != null){
			mainFragment.viewPagerCanScroll(canScroll);
		}
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		if(state == ViewPager.SCROLL_STATE_DRAGGING){
			getPresenter().listItemReset();
		}
	}
}
