package org.kans.zxb.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.kans.zxb.KApplication;
import org.kans.zxb.R;
import org.kans.zxb.entity.VipBuy;
import org.kans.zxb.entity.VipGroup;
import org.kans.zxb.entity.VipPhone;
import org.kans.zxb.entity.VipQQ;
import org.kans.zxb.entity.VipRemark;
import org.kans.zxb.entity.VipUser;
import org.kans.zxb.entity.VipWechat;
import org.kans.zxb.presenter.VipUserPresenter;
import org.kans.zxb.ui.DatePickerUI;
import org.kans.zxb.util.KansUtils;
import org.kans.zxb.view.KItemView;
import org.xutils.DbManager;
import org.xutils.x;
import org.xutils.ex.DbException;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

@ContentView(value = R.layout.vip_user_fragment)
public class VipUserFragment extends KFragment<VipUserPresenter, VipUserPresenter.Ui> implements VipUserPresenter.Ui {

	@ViewInject(R.id.vip_user_icon)
	public ImageView mIconView;

	@ViewInject(R.id.vip_id_view)
	public TextView idView;

	@ViewInject(R.id.vip_credit_view)
	public TextView creditView;

	@ViewInject(R.id.vip_user_name)
	public EditText mNameView;

	@ViewInject(R.id.vip_sex_boy_radio)
	public RadioButton boyRadio;

	@ViewInject(R.id.vip_sex_girl_radio)
	public RadioButton girlRadio;

	@ViewInject(R.id.vip_favorite_check_box)
	public CheckBox favoriteCheckBox;

	@ViewInject(R.id.vip_birthday_date_picker)
	public EditText birthdayDatePicker;

	@ViewInject(R.id.vip_email_view)
	public EditText mEmailView;

	@ViewInject(R.id.vip_user_add_in_group_view)
	public ImageView addInGroupView;

	@ViewInject(R.id.vip_user_in_group_list_view)
	public LinearLayout inGroupListView;

	@ViewInject(R.id.vip_user_add_phone_view)
	public ImageView addPhoneView;

	@ViewInject(R.id.vip_user_phone_list_view)
	public LinearLayout phoneListView;

	@ViewInject(R.id.vip_user_add_wechat_view)
	public ImageView addWechatView;

	@ViewInject(R.id.vip_user_wechat_list_view)
	public LinearLayout wechatListView;

	@ViewInject(R.id.vip_user_add_qq_view)
	public ImageView addQqView;

	@ViewInject(R.id.vip_user_qq_list_view)
	public LinearLayout qqListView;

	@ViewInject(R.id.vip_user_buy_view)
	public View mBuyView;
	
	@ViewInject(R.id.vip_user_buy_count_view)
	TextView mBuyCountView;
	
	@ViewInject(R.id.vip_user_remark_view)
	public View mRemarkView;

	@ViewInject(R.id.vip_user_remark_count_view)
	TextView mRemarkCountView;

	boolean isShowBirthdayDatePicker = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Activity().setMenuDrawable(R.drawable.k_check_icon);
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
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public VipUserPresenter createPresenter() {
		return new VipUserPresenter();
	}

	@Override
	public Context getContext() {
		return getActivity();
	}

	@Override
	public boolean onMenuButtonClick() {
		getPresenter().setEditRoductMode(!getPresenter().isEditRoductMode());
		return super.onMenuButtonClick();
	}

	@Override
	public boolean onBackButtonClick() {
		if (getPresenter().isModify()) {
			getPresenter().onBackButtonClick();
			Activity().setResult(Activity.RESULT_OK);
		}else{
			Activity().setResult(Activity.RESULT_CANCELED);
		}
		return false;
	}

	@Override
	public void showBirthdayDatePicker(VipUser mVipUser) {
		if(!isShowBirthdayDatePicker){
			isShowBirthdayDatePicker = true;
			boolean isLunar = mVipUser.birthday_isLunar==1;
			String birthday = mVipUser.birthday;
			DatePickerUI mBirthdayDatePickerUI = null;
			if(birthday != null){
				String[] strs = birthday.split("-");
				if(strs.length==3){
					int year = Integer.valueOf(strs[0]);
					int month = Integer.valueOf(strs[1]);
					int day = Integer.valueOf(strs[2]);
					mBirthdayDatePickerUI = new DatePickerUI(isLunar, year, month, day, getContext());
				}
			}
			if(mBirthdayDatePickerUI == null){
				int[] date = KansUtils.getDate();
				mBirthdayDatePickerUI = new DatePickerUI(isLunar, date[0], date[1], date[2], getContext());
			}
			mBirthdayDatePickerUI.setBirthdayDateCallBack(new DatePickerUI.BirthdayDateCallBack() {
				
				@Override
				public void onDateChage(boolean isLunar, int year, int month, int day) {
					getPresenter().updateBirthday(isLunar, year, month, day);
				}

				@Override
				public void onDismiss() {
					isShowBirthdayDatePicker = false;
				}
			});
			mBirthdayDatePickerUI.show();
		}
	}
	
	@Override
	public VipUserFragment getUi() {
		return this;
	}

	@Override
	public VipUserFragment.ViewHold getViewHold() {
		return new ViewHold(mIconView, idView, creditView, mNameView, boyRadio, girlRadio, favoriteCheckBox, birthdayDatePicker, mEmailView, addInGroupView, inGroupListView, addPhoneView, phoneListView, addWechatView, wechatListView, addQqView, qqListView, mBuyView, mBuyCountView, mRemarkView, mRemarkCountView);
	}

	@Override
	public void setTitle(int titleId) {
		Activity().setTitle(titleId);
	}

	@Override
	public void showUserIconSelect() {
		View view = View.inflate(getContext(), R.layout.k_icon_choice_view, null);
		view.findViewById(R.id.gallery).setOnClickListener(getPresenter());
		view.findViewById(R.id.camera).setOnClickListener(getPresenter());
		view.findViewById(R.id.cancel).setOnClickListener(getPresenter());
		Activity().showPopuView(view);
	}

	@Override
	public void setMenuDrawable(int resId) {
		Activity().setMenuDrawable(resId);
	}
	
	public class ViewHold {
		public ImageView mIconView;
		public TextView idView;
		public TextView creditView;
		public EditText mNameView;
		public RadioButton boyRadio;
		public RadioButton girlRadio;
		public CheckBox favoriteCheckBox;
		public EditText birthdayDatePicker;
		public EditText mEmailView;
		public ImageView addInGroupView;
		public LinearLayout inGroupListView;
		public ImageView addPhoneView;
		public LinearLayout phoneListView;
		public ImageView addWechatView;
		public LinearLayout wechatListView;
		public ImageView addQqView;
		public LinearLayout qqListView;
		public View mBuyView;
		public TextView mBuyCountView;
		public View mRemarkView;
		public TextView mRemarkCountView;
		
		

		public ViewHold(ImageView mIconView, TextView idView, TextView creditView, EditText mNameView, RadioButton boyRadio, RadioButton girlRadio, CheckBox favoriteCheckBox, EditText birthdayDatePicker, EditText mEmailView, ImageView addInGroupView, LinearLayout inGroupListView, ImageView addPhoneView, LinearLayout phoneListView, ImageView addWechatView, LinearLayout wechatListView, ImageView addQqView, LinearLayout qqListView, View mBuyView, TextView mBuyCountView, View mRemarkView,
				TextView mRemarkCountView) {
			super();
			this.mIconView = mIconView;
			this.idView = idView;
			this.creditView = creditView;
			this.mNameView = mNameView;
			this.boyRadio = boyRadio;
			this.girlRadio = girlRadio;
			this.favoriteCheckBox = favoriteCheckBox;
			this.birthdayDatePicker = birthdayDatePicker;
			this.mEmailView = mEmailView;
			this.addInGroupView = addInGroupView;
			this.inGroupListView = inGroupListView;
			this.addPhoneView = addPhoneView;
			this.phoneListView = phoneListView;
			this.addWechatView = addWechatView;
			this.wechatListView = wechatListView;
			this.addQqView = addQqView;
			this.qqListView = qqListView;
			this.mBuyView = mBuyView;
			this.mBuyCountView = mBuyCountView;
			this.mRemarkView = mRemarkView;
			this.mRemarkCountView = mRemarkCountView;
		}

		public void setEnabled(boolean isEditRoductMode) {
			mIconView.setEnabled(isEditRoductMode);
			idView.setEnabled(isEditRoductMode);
			creditView.setEnabled(isEditRoductMode);
			mNameView.setEnabled(isEditRoductMode);
			boyRadio.setEnabled(isEditRoductMode);
			girlRadio.setEnabled(isEditRoductMode);
			favoriteCheckBox.setEnabled(isEditRoductMode);
			birthdayDatePicker.setEnabled(isEditRoductMode);
			mEmailView.setEnabled(isEditRoductMode);
			addInGroupView.setEnabled(isEditRoductMode);
			addInGroupView.setVisibility(isEditRoductMode?View.VISIBLE:View.GONE);
			inGroupListView.setEnabled(isEditRoductMode);
			addPhoneView.setEnabled(isEditRoductMode);
			addPhoneView.setVisibility(isEditRoductMode?View.VISIBLE:View.GONE);
			phoneListView.setEnabled(isEditRoductMode);
			addWechatView.setEnabled(isEditRoductMode);
			addWechatView.setVisibility(isEditRoductMode?View.VISIBLE:View.GONE);
			wechatListView.setEnabled(isEditRoductMode);
			addQqView.setVisibility(isEditRoductMode?View.VISIBLE:View.GONE);
			addQqView.setEnabled(isEditRoductMode);
			qqListView.setEnabled(isEditRoductMode);
			mBuyView.setEnabled(true);
			mRemarkView.setEnabled(true);
		}

		public void setOnClickListener(View.OnClickListener mOnClickListener) {
			mIconView.setOnClickListener(mOnClickListener);
			creditView.setOnClickListener(mOnClickListener);
			birthdayDatePicker.setOnClickListener(mOnClickListener);
			addInGroupView.setOnClickListener(mOnClickListener);
			addPhoneView.setOnClickListener(mOnClickListener);
			addWechatView.setOnClickListener(mOnClickListener);
			addQqView.setOnClickListener(mOnClickListener);
			mBuyView.setOnClickListener(mOnClickListener);
			mRemarkView.setOnClickListener(mOnClickListener);
		}

		public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {

			boyRadio.setOnCheckedChangeListener(listener);
			girlRadio.setOnCheckedChangeListener(listener);
			favoriteCheckBox.setOnCheckedChangeListener(listener);
		}

		public void addTextChangedListener(TextWatcher watcher) {
			idView.addTextChangedListener(watcher);
			creditView.addTextChangedListener(watcher);
			mNameView.addTextChangedListener(watcher);
			mEmailView.addTextChangedListener(watcher);
		}

		public void removeTextChangedListener(TextWatcher watcher) {
			idView.removeTextChangedListener(watcher);
			creditView.addTextChangedListener(watcher);
			mNameView.removeTextChangedListener(watcher);
			mEmailView.removeTextChangedListener(watcher);
		}

		public void catProduct(VipUser mVipUser,boolean all) {
			mVipUser.iconPath = mVipUser.iconPath;
			mVipUser.vipId = mVipUser.vipId;
			mVipUser.credit = mVipUser.credit;
			mVipUser.name = mNameView.getText().toString();
			mVipUser.sexuality = boyRadio.isChecked()?1:0;
			mVipUser.favorite = favoriteCheckBox.isChecked()?1:0;
			mVipUser.birthday = mVipUser.birthday;
			mVipUser.email = mEmailView.getText().toString();
			
			if(all){
				List<VipGroup> groupList = new ArrayList<VipGroup>();
				for(int index=0;index<inGroupListView.getChildCount();index++){
					Object tag = inGroupListView.getChildAt(index).getTag();
					if(tag!=null && tag instanceof VipGroup){
						groupList.add((VipGroup)tag);
					}
				}
				mVipUser.groupList.clear();
				mVipUser.groupList.addAll(groupList);

				List<VipPhone> phoneList = new ArrayList<VipPhone>();
				for(int index=0;index<phoneListView.getChildCount();index++){
					Object tag = phoneListView.getChildAt(index).getTag();
					if(tag!=null && tag instanceof VipPhone){
						phoneList.add((VipPhone)tag);
					}
				}
				mVipUser.phoneList.clear();
				mVipUser.phoneList.addAll(phoneList);

				List<VipWechat> wechatList = new ArrayList<VipWechat>();
				for(int index=0;index<wechatListView.getChildCount();index++){
					Object tag = wechatListView.getChildAt(index).getTag();
					if(tag!=null && tag instanceof VipWechat){
						wechatList.add((VipWechat)tag);
					}
				}
				mVipUser.wechatList.clear();
				mVipUser.wechatList.addAll(wechatList);

				List<VipQQ> qqList = new ArrayList<VipQQ>();
				for(int index=0;index<qqListView.getChildCount();index++){
					Object tag = qqListView.getChildAt(index).getTag();
					if(tag!=null && tag instanceof VipQQ){
						qqList.add((VipQQ)tag);
					}
				}
				mVipUser.qqList.clear();
				mVipUser.qqList.addAll(qqList);
			}
		}

		public void updateUserIcon(VipUser mVipUser) {
			if (mVipUser.iconPath != null && new File(mVipUser.iconPath).isFile()) {
				mIconView.setImageBitmap(BitmapFactory.decodeFile(mVipUser.iconPath));
			}else{
				mIconView.setImageBitmap(KansUtils.getPlanarCode(mVipUser.vipId,((BitmapDrawable)getResources().getDrawable(R.drawable.k_icon_normal)).getBitmap()));
			}
		}
		
		public void updateUserBirthday(VipUser mVipUser){
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
				birthdayDatePicker.setText(showBirthday);
			}else{
				String strings = showBirthday+"    ("+days+")";
				Spannable spannable = new SpannableStringBuilder(strings);
				spannable.setSpan(new ForegroundColorSpan(Color.RED), showBirthday.length(),strings.length() , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				spannable.setSpan(new AbsoluteSizeSpan(30), showBirthday.length(),strings.length() , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				birthdayDatePicker.setText(spannable);
			}
		}
		
		public void updateInGroupListView(VipUser mVipUser,List<KItemView> mItemViews){
			int count = mItemViews.size();
			for(int index=0;index<count;index++){
				KItemView mKItemView = mItemViews.get(index);
				if(mKItemView.getTag() instanceof VipGroup){
					mItemViews.remove(index);
					index--;
				}
			}
			inGroupListView.removeAllViews();
			for(int index=0;index<mVipUser.groupList.size();index++){
				VipGroup mVipGroup = mVipUser.groupList.get(index);
				KItemView mKItemView = KItemView.onCreateKItemView(getUi().getContext(), R.layout.k_default_list_item);
				TextView mNameView = (TextView) mKItemView.findViewById(R.id.name);
				TextView mRemarkView = (TextView) mKItemView.findViewById(R.id.remark);
				mNameView.setText((mVipGroup.name!=null && !mVipGroup.name.equals("null"))?mVipGroup.name:"");
				mRemarkView.setText((mVipGroup.remark!=null && !mVipGroup.remark.equals("null"))?mVipGroup.remark:"");
				mKItemView.setTag(mVipGroup);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				inGroupListView.addView(mKItemView, params);
			}
		}
		
		public void updatePhoneListView(VipUser mVipUser,List<KItemView> mItemViews){
			int count = mItemViews.size();
			for(int index=0;index<count;index++){
				KItemView mKItemView = mItemViews.get(index);
				if(mKItemView.getTag() instanceof VipPhone){
					mItemViews.remove(index);
					index--;
				}
			}
			phoneListView.removeAllViews();
			for(int index=0;index<mVipUser.phoneList.size();index++){
				VipPhone mVipPhone = mVipUser.phoneList.get(index);
				KItemView mKItemView = KItemView.onCreateKItemView(getUi().getContext(), R.layout.k_default_list_item);
				TextView mNameView = (TextView) mKItemView.findViewById(R.id.name);
				TextView mRemarkView = (TextView) mKItemView.findViewById(R.id.remark);
				mNameView.setText((mVipPhone.phoneNum!=null && !mVipPhone.phoneNum.equals("null"))?mVipPhone.phoneNum:"");
				mRemarkView.setText((mVipPhone.remark!=null && !mVipPhone.remark.equals("null"))?mVipPhone.remark:"");
				mKItemView.setTag(mVipPhone);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				phoneListView.addView(mKItemView, params);
				if(mVipUser.phoneList.size()>1 && index != mVipUser.phoneList.size()-1){
					mKItemView.findViewById(android.R.id.content).setBackgroundResource(R.drawable.k_list_item_bottom_normal);
				}
			}
		}
		
		public void updateWechatListView(VipUser mVipUser,List<KItemView> mItemViews){
			int count = mItemViews.size();
			for(int index=0;index<count;index++){
				KItemView mKItemView = mItemViews.get(index);
				if(mKItemView.getTag() instanceof VipWechat){
					mItemViews.remove(index);
					index--;
				}
			}
			wechatListView.removeAllViews();
			for(int index=0;index<mVipUser.wechatList.size();index++){
				VipWechat mVipWechat = mVipUser.wechatList.get(index);
				KItemView mKItemView = KItemView.onCreateKItemView(getUi().getContext(), R.layout.k_default_list_item);
				TextView mNameView = (TextView) mKItemView.findViewById(R.id.name);
				TextView mRemarkView = (TextView) mKItemView.findViewById(R.id.remark);
				mNameView.setText((mVipWechat.name!=null && !mVipWechat.name.equals("null"))?mVipWechat.name:"");
				mRemarkView.setText((mVipWechat.remark!=null && !mVipWechat.remark.equals("null"))?mVipWechat.remark:"");
				mKItemView.setTag(mVipWechat);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				wechatListView.addView(mKItemView, params);
				if(mVipUser.wechatList.size()>1 && index != mVipUser.wechatList.size()-1){
					mKItemView.findViewById(android.R.id.content).setBackgroundResource(R.drawable.k_list_item_bottom_normal);
				}
			}
		}

		public void updateQQListView(VipUser mVipUser,List<KItemView> mItemViews){
			int count = mItemViews.size();
			for(int index=0;index<count;index++){
				KItemView mKItemView = mItemViews.get(index);
				if(mKItemView.getTag() instanceof VipQQ){
					mItemViews.remove(index);
					index--;
				}
			}
			qqListView.removeAllViews();
			for(int index=0;index<mVipUser.qqList.size();index++){
				VipQQ mVipQQ = mVipUser.qqList.get(index);
				KItemView mKItemView = KItemView.onCreateKItemView(getUi().getContext(), R.layout.k_default_list_item);
				TextView mNameView = (TextView) mKItemView.findViewById(R.id.name);
				TextView mRemarkView = (TextView) mKItemView.findViewById(R.id.remark);
				mNameView.setText((mVipQQ.name!=null && !mVipQQ.name.equals("null"))?mVipQQ.name:"");
				mRemarkView.setText((mVipQQ.remark!=null && !mVipQQ.remark.equals("null"))?mVipQQ.remark:"");
				mKItemView.setTag(mVipQQ);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				qqListView.addView(mKItemView, params);
				if(mVipUser.qqList.size()>1 && index != mVipUser.qqList.size()-1){
					mKItemView.findViewById(android.R.id.content).setBackgroundResource(R.drawable.k_list_item_bottom_normal);
				}
			}
		}
		
		public void updateCreditView(VipUser mVipUser){
			creditView.setText(mVipUser.credit>0?String.valueOf(mVipUser.credit):"0");
		}
		
		public void updateVipId(VipUser mVipUser){
			idView.setText(mVipUser.vipId);
		}

		public void updateNameView(VipUser mVipUser){
			mNameView.setText(mVipUser.name);
		}
		
		public void updateSexView(VipUser mVipUser){
			if(mVipUser.sexuality == 1){
				boyRadio.setChecked(true);
			}else{
				girlRadio.setChecked(true);
			}
		}

		public void updateFavoriteView(VipUser mVipUser){
			favoriteCheckBox.setChecked(mVipUser.favorite == 1);
		}
		
		public void updateEmailView(VipUser mVipUser){
			mEmailView.setText((mVipUser.email!=null && !mVipUser.email.equals("null"))?mVipUser.email:"");
		}
		
		public void init(VipUser mVipUser,List<KItemView> mItemViews) {
			updateUserIcon(mVipUser);
			updateCreditView(mVipUser);
			updateVipId(mVipUser);
			updateNameView(mVipUser);
			updateSexView(mVipUser);
			updateUserBirthday(mVipUser);
			updateFavoriteView(mVipUser);
			updateEmailView(mVipUser);
			updateInGroupListView(mVipUser, mItemViews);
			updatePhoneListView(mVipUser, mItemViews);
			updateWechatListView(mVipUser, mItemViews);
			updateQQListView(mVipUser, mItemViews);
		}

		public void updateBuyCount(VipUser mVipUser) {
			if(mVipUser.vipId!=null && mVipUser.vipId.length()>0){
				DbManager db = x.getDb(KApplication.localDaoConfig);
				try {
					List<VipBuy> buys = db.selector(VipBuy.class).where(VipBuy._VIP_ID, "=", mVipUser.vipId).findAll();
					if(buys!= null){
						mBuyCountView.setText(String.valueOf(buys.size()));
						return;
					}
				} catch (DbException e) {
					e.printStackTrace();
				}
			}
			mBuyCountView.setText(String.valueOf(0));
		}
		
		public void updateRemarkCount(VipUser mVipUser) {
			if(mVipUser.vipId!=null && mVipUser.vipId.length()>0){
				DbManager db = x.getDb(KApplication.localDaoConfig);
				try {
					List<VipRemark> remarks = db.selector(VipRemark.class).where(VipRemark._VIP_ID, "=", mVipUser.vipId).findAll();
					if(remarks!= null){
						mRemarkCountView.setText(String.valueOf(remarks.size()));
						return;
					}
				} catch (DbException e) {
					e.printStackTrace();
				}
			}
			mRemarkCountView.setText(String.valueOf(0));
		}
	}

}
