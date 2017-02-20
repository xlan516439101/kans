package org.kans.zxb.presenter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.kans.zxb.KApplication;
import org.kans.zxb.KUi;
import org.kans.zxb.R;
import org.kans.zxb.entity.DataBase;
import org.kans.zxb.entity.ProductEntity;
import org.kans.zxb.entity.VipBuy;
import org.kans.zxb.entity.VipGroup;
import org.kans.zxb.entity.VipGroupLink;
import org.kans.zxb.entity.VipPhone;
import org.kans.zxb.entity.VipQQ;
import org.kans.zxb.entity.VipRemark;
import org.kans.zxb.entity.VipUser;
import org.kans.zxb.entity.VipWechat;
import org.kans.zxb.fragment.VipAttachEditFragment;
import org.kans.zxb.fragment.VipUserFragment;
import org.kans.zxb.fragment.VipUserFragment.ViewHold;
import org.kans.zxb.ui.DatePickerUI;
import org.kans.zxb.util.KansUtils;
import org.kans.zxb.view.KItemView;
import org.xutils.DbManager;
import org.xutils.x;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class VipUserPresenter extends KPresenter<VipUserPresenter.Ui> implements TextWatcher, Spinner.OnItemSelectedListener, OnCheckedChangeListener {
	public static final int REQUEST_ICON_CODE = 0x101;
	private static final int REQUEST_ADD_IN_GROUP_CODE = 0x102;
	private static final int REQUEST_ADD_PHONE_CODE = 0x103;
	private static final int REQUEST_ADD_WECHAT_CODE = 0x104;
	private static final int REQUEST_ADD_QQ_CODE = 0x105;
	private static final int REQUEST_CREDIT_CODE = 0x106;
	
	private VipUser mVipUser;
	private VipUserFragment.ViewHold mViewHold;
	private boolean isEditRoductMode = false;
	private List<VipGroup> mVipGroupes;
	private VipGroup mVipGroup;
	private List<KItemView> mItemViews;
	private String mProductString = "";

	public interface Ui extends KUi {
		Context getContext();

		Resources getResources();

		VipUserFragment getUi();

		ViewHold getViewHold();

		void setTitle(int titleId);

		void setMenuDrawable(int resId);

		void showUserIconSelect();

		void showBirthdayDatePicker(VipUser mVipUser);

		void startActivity(Intent intent);

		void startActivityForResult(Intent intent, int requestCode);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mVipGroupes = new ArrayList<VipGroup>();
		mItemViews = new ArrayList<KItemView>();
		Intent intent = getUi().getUi().Activity().getIntent();
		if (intent != null) {
			mVipUser = intent.getParcelableExtra("vip_user_entity");
			isEditRoductMode = intent.getBooleanExtra("vip_user_edit", false);
			getUi().setTitle(R.string.kans_edit_vip_user);
		}
		if (mVipUser == null) {
			mVipUser = new VipUser();
			getUi().setTitle(R.string.kans_add_vip_user);
			isEditRoductMode = true;
		}
		Log.i("xlan", " "+KansUtils.isStandardString(mVipUser.vipId));
		if(!KansUtils.isStandardString(mVipUser.vipId)){
			mVipUser.vipId = KansUtils.getVipId(1);
		}
		mProductString = mVipUser.toString().trim();
		initDate();
	}

	private void initDate(){
		DbManager mDbManager = x.getDb(KApplication.localDaoConfig);
		initGroupDate(mDbManager);
		initPhoneDate(mDbManager);
		initQQDate(mDbManager);
		initWechatDate(mDbManager);
	}
	
	private void initGroupDate(DbManager mDbManager){
		mVipUser.groupList.clear();
		try {
			List<VipGroupLink>  mlinks = mDbManager.selector(VipGroupLink.class).where(VipGroupLink._VIP_ID, "=", mVipUser.vipId).findAll();
			if(mlinks!=null && mlinks.size()>0){
				for(VipGroupLink mlink:mlinks){
					VipGroup mVipGroup  = mDbManager.selector(VipGroup.class).where(VipGroup._ID, "=", mlink.groupId).findFirst();
					if(mVipGroup != null){
						mVipUser.groupList.add(mVipGroup);
					}
				}
			}
		} catch (DbException e) {
			e.printStackTrace();
		}
	}

	private void initPhoneDate(DbManager mDbManager){
		mVipUser.phoneList.clear();
		try {
			List<VipPhone>  mPhones = mDbManager.selector(VipPhone.class).where(VipPhone._VIP_ID, "=", mVipUser.vipId).findAll();
			if(mPhones!=null && mPhones.size()>0){
				for(VipPhone mPhone:mPhones){
					mVipUser.phoneList.add(mPhone);
				}
			}
		} catch (DbException e) {
			e.printStackTrace();
		}
	}

	private void initQQDate(DbManager mDbManager){

		mVipUser.qqList.clear();
		try {
			List<VipQQ>  mVipQQs = mDbManager.selector(VipQQ.class).where(VipQQ._VIP_ID, "=", mVipUser.vipId).findAll();
			if(mVipQQs!=null && mVipQQs.size()>0){
				for(VipQQ mVipQQ:mVipQQs){
					mVipUser.qqList.add(mVipQQ);
				}
			}
		} catch (DbException e) {
			e.printStackTrace();
		}
	}

	private void initWechatDate(DbManager mDbManager){

		mVipUser.wechatList.clear();
		try {
			List<VipWechat>  mWechats = mDbManager.selector(VipWechat.class).where(VipWechat._VIP_ID, "=", mVipUser.vipId).findAll();
			if(mWechats!=null && mWechats.size()>0){
				for(VipWechat mWechat:mWechats){
					mVipUser.wechatList.add(mWechat);
				}
			}
		} catch (DbException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isEditRoductMode() {
		return isEditRoductMode;
	}

	public void setEditRoductMode(boolean isEditRoductMode) {
		this.isEditRoductMode = isEditRoductMode;
		getUi().setMenuDrawable(isEditRoductMode ? R.drawable.k_check_icon : R.drawable.k_edit_icon);
		mViewHold.setEnabled(isEditRoductMode);
		Log.i("xlan", "mItemViews:"+mItemViews.size());
		for(KItemView item:mItemViews){
			item.setEnabled(isEditRoductMode);
		}
		x.task().postDelayed(new Runnable() {
			@Override
			public void run() {
				mViewHold.mNameView.setFocusable(true);
				KansUtils.setImeVisibility(false, getUi().getContext(), mViewHold.mNameView);
			}
		}, 200);
	}

	public boolean isModify() {
		mViewHold.catProduct(mVipUser, false);
		return !mProductString.equals(mVipUser.toString().trim());
	}

	@Override
	public void onStart() {
		super.onStart();
		mViewHold = getUi().getViewHold();
		setEditRoductMode(isEditRoductMode);
		mViewHold.setOnClickListener(this);
		mViewHold.setOnCheckedChangeListener(this);
		mViewHold.addTextChangedListener(this);
		mViewHold.init(mVipUser, mItemViews);
	}

	@Override
	public void onResume() {
		super.onResume();
		mViewHold.updateBuyCount(mVipUser);
		mViewHold.updateRemarkCount(mVipUser);
	}

	@Override
	public void onPause() {
		super.onPause();
		mViewHold.catProduct(mVipUser, false);
	}

	@Override
	public void onStop() {
		super.onStop();
		mViewHold.setOnClickListener(null);
		mViewHold.setOnCheckedChangeListener(null);
		mViewHold.removeTextChangedListener(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		mViewHold.catProduct(mVipUser, false);
		if(mVipUser.name == null || mVipUser.name.equals("") || mVipUser.name.length() == 0){
			Toast.makeText(getUi().getContext(), R.string.kans_please_input_name, Toast.LENGTH_SHORT).show();
			return;
		}
		switch (v.getId()) {
		case R.id.camera:
			getUi().getUi().Activity().hidePopuView();
			Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			getUi().getUi().startActivityForResult(cameraIntent, REQUEST_ICON_CODE);
			getUi().getUi().Activity().hidePopuView();
			break;
		case R.id.gallery:
			getUi().getUi().Activity().hidePopuView();
			Intent galleryIntent = new Intent(Intent.ACTION_PICK);
			galleryIntent.setType("image/*");
			getUi().getUi().startActivityForResult(galleryIntent, REQUEST_ICON_CODE);
			break;
		case R.id.cancel:
			getUi().getUi().Activity().hidePopuView();
			break;

		case R.id.vip_user_icon:
			getUi().showUserIconSelect();
			break;

		case R.id.vip_birthday_date_picker:
			getUi().showBirthdayDatePicker(mVipUser);
			break;
			
		case R.id.vip_credit_view:
			Intent addCreditIntent = new Intent();
			addCreditIntent.setClassName(getUi().getContext(), "org.kans.zxb.VipCreditListActivity");
			addCreditIntent.putExtra(VipUser._VIP_ID, mVipUser.vipId);
			getUi().startActivityForResult(addCreditIntent, REQUEST_CREDIT_CODE);
			break;
			
		case R.id.vip_user_add_in_group_view:
			Intent addGroupIntent = new Intent();
			addGroupIntent.setClassName(getUi().getContext(), "org.kans.zxb.VipGroupActivity");
			addGroupIntent.putParcelableArrayListExtra("data", (ArrayList<? extends Parcelable>) mVipUser.groupList);
			getUi().startActivityForResult(addGroupIntent, REQUEST_ADD_IN_GROUP_CODE);
			break;
			
		case R.id.vip_user_add_phone_view:
			Intent addPhoneIntent = new Intent();
			addPhoneIntent.setClassName(getUi().getContext(), "org.kans.zxb.VipAttachEditActivity");
			VipPhone mVipPhone = new VipPhone();
			mVipPhone.vipId = mVipUser.vipId;
			addPhoneIntent.putExtra("data", mVipPhone);
			getUi().startActivityForResult(addPhoneIntent, REQUEST_ADD_PHONE_CODE);
			break;
			
		case R.id.vip_user_add_wechat_view:
			Intent addWechatIntent = new Intent();
			addWechatIntent.setClassName(getUi().getContext(), "org.kans.zxb.VipAttachEditActivity");
			VipWechat mVipWechat = new VipWechat();
			mVipWechat.vipId = mVipUser.vipId;
			addWechatIntent.putExtra("data", mVipWechat);
			getUi().startActivityForResult(addWechatIntent, REQUEST_ADD_WECHAT_CODE);
			break;
			
		case R.id.vip_user_add_qq_view:
			Log.i("xlan", "vip_user_add_qq_view");
			Intent addQQIntent = new Intent();
			addQQIntent.setClassName(getUi().getContext(), "org.kans.zxb.VipAttachEditActivity");
			VipQQ mVipQQ = new VipQQ();
			mVipQQ.vipId = mVipUser.vipId;
			addQQIntent.putExtra("data", mVipQQ);
			getUi().startActivityForResult(addQQIntent, REQUEST_ADD_QQ_CODE);
			break;
			
		case R.id.vip_user_buy_view:
			Intent mBuyIntent = new Intent();
			mBuyIntent.setClassName(getUi().getContext(), "org.kans.zxb.VipBuyListActivity");
			mBuyIntent.putExtra(VipUser._VIP_ID, mVipUser.vipId);
			getUi().startActivity(mBuyIntent);
			break;
			
		case R.id.vip_user_remark_view:
			Intent mRemarkIntent = new Intent();
			mRemarkIntent.setClassName(getUi().getContext(), "org.kans.zxb.VipRemarkListActivity");
			mRemarkIntent.putExtra(VipUser._VIP_ID, mVipUser.vipId);
			getUi().startActivity(mRemarkIntent);
			break;
			
		default:
			break;

		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.vip_sex_boy_radio:
			mVipUser.sexuality = 1;
			break;
		case R.id.vip_sex_girl_radio:
			mVipUser.sexuality = 0;
			break;

		case R.id.vip_favorite_check_box:
			mVipUser.favorite = isChecked?1:0;
			break;

		default:
			break;
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		if (view != null) {
			Object tag = view.getTag();
			if (tag instanceof VipGroup) {
				if (!mVipGroup.name.equals(((VipGroup) tag).name)) {
					mVipGroup = (VipGroup) tag;
				}
			} else {
				Intent intent = new Intent();
				intent.setClassName(getUi().getContext().getPackageName(), "org.kans.zxb.VipGroupActivity");
				intent.putExtra("add", true);
				getUi().startActivity(intent);
			}
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		Log.i("xlan", "requestCode:" + requestCode + " resultCode:" + resultCode + " data:" + intent);
		Object mData = null;
		DbManager mDbManager = x.getDb(KApplication.localDaoConfig);
		if (intent != null) {
			if (intent.getExtras() != null && intent.getExtras().get("data") != null) {
				Object obj = intent.getExtras().get("data");
				if(obj instanceof DataBase){
					mData = (DataBase)obj;
				}
			}
		}
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
			case REQUEST_ICON_CODE:

				if (intent != null) {
					if (intent.getData() != null) {
						saveIconBitmap(intent.getData());
					} else if (intent.getExtras() != null && intent.getExtras().containsKey("data")) {
						Bitmap bitmap = intent.getExtras().getParcelable("data");
						saveIconBitmap(bitmap);
					} else {
						Bundle mBundle = intent.getExtras();
						Iterator<String> keys = mBundle.keySet().iterator();
						while (keys.hasNext()) {
							String key = keys.next();
							Log.i("xlan", "key:"+key+" value:"+mBundle.getParcelable(key));
						}
						
					}
				}
				break;
				
			case REQUEST_ADD_IN_GROUP_CODE:
				if (mData != null && mData instanceof List) {
					List<VipGroup> mVipGroups = (List<VipGroup>) mData;
					try {
						mDbManager.delete(VipGroupLink.class, WhereBuilder.b(VipGroupLink._VIP_ID, "=", mVipUser.vipId));
						long lastRefreshTime = System.currentTimeMillis();
						for(VipGroup mVipGroup:mVipGroups){
							mDbManager.saveBindingId(new VipGroupLink(mVipUser.vipId, mVipGroup.id, "", lastRefreshTime));
						}
					} catch (DbException e) {
						e.printStackTrace();
					}
					initGroupDate(mDbManager);
					mViewHold.updateInGroupListView(mVipUser, mItemViews);
				}	
				break;
				
			case REQUEST_ADD_PHONE_CODE:
				if (mData != null && mData instanceof VipPhone) {
					VipPhone mVipPhone = (VipPhone) mData;
					if(KansUtils.isNumberString(mVipPhone.phoneNum) && mVipPhone.phoneNum.length() > 4){
						boolean isAdd = false;
						if(mVipPhone.id>0){
							for(VipPhone mPhone:mVipUser.phoneList){
								if(mPhone.id == mVipPhone.id){
									mPhone.phoneNum = mVipPhone.phoneNum;
									mPhone.remark = mVipPhone.remark;
									mPhone.lastRefreshTime = System.currentTimeMillis();
									isAdd = true;
								}
							}
						}
						try {
							mVipPhone.vipId = mVipUser.vipId;
							if(!isAdd){
								mVipUser.phoneList.add(mVipPhone);
								mDbManager.saveBindingId(mVipPhone);
							}else{
								mDbManager.saveOrUpdate(mVipPhone);
							}
						} catch (DbException e) {
							e.printStackTrace();
						}
						mViewHold.updatePhoneListView(mVipUser, mItemViews);
					}
				}	
				break;
				
			case REQUEST_ADD_WECHAT_CODE:
				if (mData != null && mData instanceof VipWechat) {
					final VipWechat mVipWechat = (VipWechat) mData;
					boolean isAdd = false;
					if(mVipWechat.id>0){
						for(VipWechat mWechat:mVipUser.wechatList){
							if(mWechat.id == mVipWechat.id){
								mWechat.name = mVipWechat.name;
								mWechat.remark = mVipWechat.remark;
								mWechat.lastRefreshTime = System.currentTimeMillis();
								
								isAdd = true;
							}
						}
					}
					try {
						if(!isAdd){
							mVipUser.wechatList.add(mVipWechat);
							mDbManager.saveBindingId(mVipWechat);
						}else{
							mDbManager.saveOrUpdate(mVipWechat);
						}
					} catch (DbException e) {
						e.printStackTrace();
					}
					mViewHold.updateWechatListView(mVipUser, mItemViews);
				}	
				break;
				
			case REQUEST_ADD_QQ_CODE:
				if (mData != null && mData instanceof VipQQ) {
					VipQQ mVipQQ = (VipQQ) mData;
					boolean isAdd = false;
					if(mVipQQ.id>0){
						for(VipQQ mQQ:mVipUser.qqList){
							if(mQQ.id == mVipQQ.id){
								mQQ.name = mVipQQ.name;
								mQQ.remark = mVipQQ.remark;
								mQQ.lastRefreshTime = System.currentTimeMillis();
								isAdd = true;
							}
						}
					}
					try {
						if(!isAdd){
							mVipUser.qqList.add(mVipQQ);
							mDbManager.saveBindingId(mVipQQ);
						}else{
							mDbManager.saveOrUpdate(mVipQQ);
						}
					} catch (DbException e) {
						e.printStackTrace();
					}
					mViewHold.updateQQListView(mVipUser, mItemViews);
				}	
				break;
				
			case REQUEST_CREDIT_CODE:
				if (intent.getExtras() != null) {
					mVipUser.credit = intent.getExtras().getInt(VipUser._CREDIT, mVipUser.credit);
					mViewHold.updateCreditView(mVipUser);
				}	
				break;
				
			default:
				break;
			}
		}
	}

	private void saveIconBitmap(Uri mUri) {
		Bitmap bitmap = KansUtils.getBitmapFromUri(getUi().getContext(), mUri);
		saveIconBitmap(bitmap);
	}

	private void saveIconBitmap(Bitmap bitmap) {
		Bitmap saveBitmap = KansUtils.getRoundIconBitmap(bitmap, getUi().getContext().getResources().getDimensionPixelSize(R.dimen.kans_rouduct_icon_size));
		mViewHold.mIconView.setImageBitmap(saveBitmap);
		KansUtils.saveVipIconPath(mVipUser, saveBitmap);
	}


	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public void afterTextChanged(Editable s) {
	}

	class PriceClassAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mVipGroupes.size() + 1;
		}

		@Override
		public Object getItem(int position) {
			return (position < getCount() - 1) ? mVipGroupes.get(position) : null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			VipGroup mVipGroup = (VipGroup) getItem(position);
			TextView textView = (TextView) View.inflate(getUi().getContext(), android.R.layout.simple_list_item_1, null);
			textView.setTextColor(getUi().getContext().getResources().getColor(R.color.k_text_color));
			textView.setTextSize(20f);
			textView.setBackgroundColor(getUi().getContext().getResources().getColor(R.color.k_background_color));
			textView.setPadding(50, 0, 0, 0);
			textView.setGravity(Gravity.CENTER_VERTICAL);
			if (mVipGroup != null) {
				textView.setText(mVipGroup.name);
				textView.setTag(mVipGroup);
			} else {
				textView.setText(R.string.vip_group_fragment_add_vip_group);
				textView.setTag(mVipGroup);
			}
			return textView;
		}
	}

	public String getProductName(int product_id) {
		DbManager manager = x.getDb(KApplication.localDaoConfig);
		try {
			ProductEntity mProductEntity = manager.selector(ProductEntity.class).and(ProductEntity._ID, "=", product_id).findFirst();
			if (mProductEntity != null) {
				return mProductEntity.name;
			}
		} catch (DbException e) {
			Log.w("xlan", getUi().getContext().getResources().getString(R.string.kans_save_fail));
		}
		return null;
	}

	public int[] getBirthday() {
		String birthday = mVipUser.birthday;
		if (birthday != null) {
			String[] strs = birthday.split("-");
			if (strs.length == 3) {
				int year = Integer.valueOf(strs[0]);
				int month = Integer.valueOf(strs[1]);
				int day = Integer.valueOf(strs[2]);
				return new int[] { year, month, day };
			}
		}
		return KansUtils.getDate();
	}

	public void updateBirthday(boolean isLunar, int year, int month, int day) {
		mVipUser.birthday_isLunar = isLunar ? 1 : 0;
		mVipUser.birthday = year + "-" + month + "-" + day;
		mViewHold.updateUserBirthday(mVipUser);
	}

	public void onBackButtonClick() {
		mViewHold.catProduct(mVipUser, false);
		if(mVipUser.name==null || mVipUser.name.length()==0){
			mVipUser.name = "VIP";
		}
		DbManager mDbManager = x.getDb(KApplication.localDaoConfig);
		try {
			if(mVipUser.id>0){
				mDbManager.saveOrUpdate(mVipUser);
			}else{
				mDbManager.saveBindingId(mVipUser);
			}
		} catch (DbException e) {
			e.printStackTrace();
		}
		try {
			VipUser m =x.getDb(KApplication.localDaoConfig).selector(VipUser.class).where(VipUser._VIP_ID, "=", mVipUser.vipId).findFirst();

			Log.i("xlan", "saveOrUpdate:"+m);
		} catch (DbException e) {
			e.printStackTrace();
		}
	}

}
