package org.kans.zxb.presenter;

import org.kans.zxb.KUi;
import org.kans.zxb.entity.DataBase;
import org.kans.zxb.entity.VipPhone;
import org.kans.zxb.entity.VipQQ;
import org.kans.zxb.entity.VipWechat;
import org.kans.zxb.fragment.VipAttachEditFragment.ViewHold;
import org.kans.zxb.ui.KansActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;


public class VipAttachEditPresenter extends KPresenter<VipAttachEditPresenter.Ui>{
	
	public interface Ui extends KUi {
		ViewHold getViewHold();
        Context getContext();
        KansActivity Activity();
        Intent getIntent();
    }
	public DataBase data;	
	public int showViewType;
	private ViewHold mViewHold;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		data = getUi().getIntent().getParcelableExtra("data");
		Log.i("xlan","data:"+ data);
		if(data == null){
			getUi().Activity().setResult(Activity.RESULT_CANCELED, null);
			getUi().Activity().finish();
		}
		showViewType = data.getType();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		mViewHold = getUi().getViewHold();
	}


	@Override
	public void onStop() {
		super.onStop();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		
	}

	public boolean onBackButtonClick() {
		String name = mViewHold.roductName.getText().toString().trim();
		String remark = mViewHold.roductRemark.getText().toString().trim();
		if(name != null && name.length()>0){
			Intent intent = new Intent();
			switch (showViewType) {
			case DataBase.VIP_PHONE_TYPE:
				((VipPhone)data).phoneNum = name;
				((VipPhone)data).remark = remark;
				break;
			case DataBase.VIP_QQ_TYPE:
				((VipQQ)data).name = name;
				((VipQQ)data).remark = remark;
				break;
			case DataBase.VIP_WECHAT_TYPE:
				((VipWechat)data).name = name;
				((VipWechat)data).remark = remark;
				break;

			default:
				break;
			}
			data.lastRefreshTime = System.currentTimeMillis();
			intent.putExtra("data", data);
			getUi().Activity().setResult(Activity.RESULT_OK, intent);
		}else{
			getUi().Activity().setResult(Activity.RESULT_CANCELED, null);
		}
		getUi().Activity().finish();
		return true;
	}

}
