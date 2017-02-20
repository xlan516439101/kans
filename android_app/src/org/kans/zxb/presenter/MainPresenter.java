package org.kans.zxb.presenter;

import org.kans.zxb.KApplication;
import org.kans.zxb.KUi;
import org.kans.zxb.R;
import org.kans.zxb.entity.ProductEntity;
import org.kans.zxb.entity.VipUser;
import org.kans.zxb.fragment.MainFragment;
import org.kans.zxb.presenter.VipUserListPresenter.ItemViewHold;
import org.xutils.DbManager;
import org.xutils.x;
import org.xutils.ex.DbException;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu.OnMenuItemClickListener;


public class MainPresenter extends KPresenter<MainPresenter.Ui> implements OnMenuItemClickListener {

	public static final int REQUEST_PLANAR_CODE = 0x102;
	
	public interface Ui extends KUi {
        Context getContext();
		MainFragment getUi();
		void startActivity(Intent mIntent);
		void showScanString(String result);
    }

	@Override
	public void onClick(View v) {
		
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.kans_menu_scan:
			Intent scanIntent = new Intent();
			scanIntent.setClassName(getUi().getContext(), "org.kans.zxb.KScanPlanarActivity");
			getUi().getUi().startActivityForResult(scanIntent, REQUEST_PLANAR_CODE);
			break;
		case R.id.kans_menu_seach:
			
			break;

		default:
			break;
		}
		return true;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == android.app.Activity.RESULT_OK){
			switch (requestCode) {
			case REQUEST_PLANAR_CODE:
				String result = data.getStringExtra("result");
				if(result!=null && result.length()>0){
					onScanCallback(result);
				}
				break;

			default:
				break;
			}
		}
	}
	
	private void onScanCallback(final String result){
		if(result != null){
			DbManager mDbManager = x.getDb(KApplication.localDaoConfig);
			try {
				ProductEntity mProductEntity = mDbManager.selector(ProductEntity.class).where(ProductEntity._PLANAR_CODE, "=", result).findFirst();
				if(mProductEntity != null){
					Intent mIntent = new Intent();
					mIntent.setClassName(getUi().getContext(), "org.kans.zxb.ProductActivity");
					mIntent.putExtra("product_entity", mProductEntity);
					mIntent.putExtra("product_edit", false);
					getUi().startActivity(mIntent);
					return;
				}
			} catch (DbException e) {
				e.printStackTrace();
			}
			try {
				VipUser mVipUser = mDbManager.selector(VipUser.class).where(VipUser._VIP_ID, "=", result).findFirst();
				if(mVipUser != null){
					Intent mIntent = new Intent();
					mIntent.setClassName(getUi().getContext(), "org.kans.zxb.VipUserActivity");
					mIntent.putExtra("vip_user_entity", mVipUser);
					mIntent.putExtra("vip_user_edit", false);
					getUi().startActivity(mIntent);
					return;
				}
			} catch (DbException e) {
				e.printStackTrace();
			}
			getUi().showScanString(result);
		}
	}
	
}
