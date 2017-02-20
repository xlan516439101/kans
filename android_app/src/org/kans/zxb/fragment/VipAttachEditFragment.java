package org.kans.zxb.fragment;

import org.kans.zxb.R;
import org.kans.zxb.entity.DataBase;
import org.kans.zxb.entity.VipPhone;
import org.kans.zxb.entity.VipQQ;
import org.kans.zxb.entity.VipWechat;
import org.kans.zxb.presenter.VipAttachEditPresenter;
import org.kans.zxb.presenter.VipAttachEditPresenter.Ui;
import org.xutils.x;
import org.xutils.view.annotation.ViewInject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

public class VipAttachEditFragment extends KFragment<VipAttachEditPresenter,VipAttachEditPresenter.Ui> implements VipAttachEditPresenter.Ui {

	
	public static final int SHOW_VIEW_TYPE_ADD = 0x101;
	public static final int SHOW_VIEW_TYPE_EDIT = 0x103;
	private boolean injected = false;
	
	public class ViewHold{
		public EditText roductName;
		public EditText roductRemark;
		
		public ViewHold(EditText roductName, EditText roductRemark) {
			super();
			this.roductName = roductName;
			this.roductRemark = roductRemark;
		}
		
	}
	
	@ViewInject(R.id.attach_name)
	EditText mName;

	@ViewInject(R.id.attach_remark)
	EditText mRemark;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		injected = true;
		int id = getViewId();
		if(id == 0){
			return new View(getContext());
		}
		View view = View.inflate(getContext(), id, null);
		x.view().inject(this, view);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (!injected) {
			x.view().inject(this, this.getView());
		}
	}
	
	@Override
	public boolean onBackButtonClick() {
		getPresenter().onBackButtonClick();
		return true;
	}
	
	private int getViewId(){
		switch (getPresenter().showViewType) {
		case DataBase.VIP_QQ_TYPE:
		case DataBase.VIP_WECHAT_TYPE:
		case DataBase.VIP_PHONE_TYPE:
			return R.layout.vip_attach_edit_default_fragment;
		case DataBase.VIP_GROUP_TYPE:
		case DataBase.VIP_CREDIT_TYPE:
		case DataBase.VIP_REMARK_TYPE:
		case DataBase.VIP_BUY_TYPE:
		default:
			Log.i("xlan", "getViewId"+getPresenter().showViewType);
			getUi().Activity().setResult(android.app.Activity.RESULT_CANCELED);
			getUi().Activity().finish();
			break;
		}
		return 0;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		setTitle();
	}

	@Override
	public void onResume() {
		super.onResume();
		switch (getPresenter().showViewType) {
		case DataBase.VIP_PHONE_TYPE:
			mName.setInputType(EditorInfo.TYPE_CLASS_PHONE);
			break;
		case DataBase.VIP_QQ_TYPE:
			mName.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
			break;
		case DataBase.VIP_WECHAT_TYPE:
			mName.setInputType(EditorInfo.TYPE_CLASS_TEXT);
			break;

		default:
			break;
		}
	}
	
	@Override
	public Context getContext() {
		return getActivity();
	}


	@Override
	public VipAttachEditPresenter createPresenter() {
		return new VipAttachEditPresenter();
	}

	@Override
	public Ui getUi() {
		return this;
	}

	@Override
	public Intent getIntent() {
		return Activity().getIntent();
	}

	private void setTitle(){
		switch (getPresenter().showViewType) {
		case DataBase.VIP_BUY_TYPE:
			Activity().setTitle(R.string.kans_vip_buy);
			break;
		case DataBase.VIP_CREDIT_TYPE:
			Activity().setTitle(R.string.kans_vip_credit);
			break;
		case DataBase.VIP_GROUP_TYPE:
			Activity().setTitle(R.string.kans_vip_group);
			break;
		case DataBase.VIP_QQ_TYPE:
			Activity().setTitle(R.string.kans_vip_qq);
			break;
		case DataBase.VIP_WECHAT_TYPE:
			Activity().setTitle(R.string.kans_vip_wechat);
			break;
		case DataBase.VIP_PHONE_TYPE:
			Activity().setTitle(R.string.kans_vip_phone);
			break;
		case DataBase.VIP_REMARK_TYPE:
			Activity().setTitle(R.string.kans_vip_remark);
			break;
		}
	}

	@Override
	public ViewHold getViewHold() {
		return new ViewHold(mName, mRemark);
	}
	
}
