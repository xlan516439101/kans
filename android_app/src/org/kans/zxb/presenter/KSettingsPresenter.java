package org.kans.zxb.presenter;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.kans.zxb.KApplication;
import org.kans.zxb.KUi;
import org.kans.zxb.R;
import org.kans.zxb.entity.KansTable;
import org.kans.zxb.entity.ProductClass;
import org.kans.zxb.fragment.KSettingsFragment;
import org.kans.zxb.fragment.KSettingsFragment.ViewHold;
import org.kans.zxb.util.KansUtils;
import org.xutils.DbManager;
import org.xutils.x;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;
import org.xutils.view.annotation.ViewInject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Toast;

public class KSettingsPresenter extends KPresenter<KSettingsPresenter.Ui> implements OnScrollListener {
	public static final int REQUEST_ICON_CODE = 0x101;
	private ViewHold mViewHold;
	private Runnable notifyRun;
	private KansTable mKansTable;

	public interface Ui extends KUi {
		KSettingsFragment getUi();
		Context getContext();
		ViewHold getViewHold();
		void showEditDilog(ProductClass mProductClass, int type);
		void showUserIconSelect();
		void startActivity(Intent mIntent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			mKansTable = x.getDb(KApplication.localDaoConfig).selector(KansTable.class).findFirst();
		} catch (DbException e) {
			e.printStackTrace();
		}
		if(mKansTable == null){
			mKansTable = new KansTable();	
		}
		updateData();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		mViewHold = getUi().getViewHold();
		mViewHold.icon.setOnClickListener(this);
		mViewHold.mNameView.setOnClickListener(this);
		mViewHold.mEmailView.setOnClickListener(this);
		mViewHold.mPhoneView.setOnClickListener(this);
		mViewHold.mProductClassView.setOnClickListener(this);
		mViewHold.mProductListView.setOnClickListener(this);
		initIcon();
	}

	@Override
	public void onStop() {
		super.onStop();
		mViewHold.icon.setOnClickListener(null);
		mViewHold.mNameView.setOnClickListener(null);
		mViewHold.mEmailView.setOnClickListener(null);
		mViewHold.mPhoneView.setOnClickListener(null);
		mViewHold.mProductClassView.setOnClickListener(null);
		mViewHold.mProductListView.setOnClickListener(null);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (notifyRun != null) {
			x.task().removeCallbacks(notifyRun);
		}
	}

	public void updateData() {
		x.task().run(new Runnable() {
			@Override
			public void run() {
				DbManager manager = x.getDb(KApplication.localDaoConfig);
			}
		});
	}

	@Override
	public void onClick(View v) {
		Object tag = v.getTag();
		switch (v.getId()) {
		case R.id.k_setting_icon:
			getUi().showUserIconSelect();
			break;
		case R.id.kans_settings_name_view:
			break;
		case R.id.kans_settings_phone_view:
			break;
		case R.id.kans_settings_email_view:
			break;
		case R.id.kans_settings_product_class_view:
			Intent productClassIntent = new Intent();
			productClassIntent.setClassName(getUi().getContext(), "org.kans.zxb.ProductClassActivity");
			getUi().startActivity(productClassIntent);
			break;
		case R.id.kans_settings_product_list_view:
			Intent productListIntent = new Intent();
			productListIntent.setClassName(getUi().getContext(), "org.kans.zxb.ProductListActivity");
			getUi().startActivity(productListIntent);
			break;

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
		default:
			break;

		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
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
			}
		}
	}

	private void initIcon(){
		File file = new File(mKansTable.iconPath);
		if(!file.exists()){
			file = KansUtils.getSettingsIcon();
		}
		if(file.exists()){
			mViewHold.icon.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
		}
	}
	
	private void saveIconBitmap(Uri mUri) {
		Bitmap bitmap = KansUtils.getBitmapFromUri(getUi().getContext(), mUri);
		saveIconBitmap(bitmap);
	}

	private void saveIconBitmap(Bitmap bitmap) {
		Bitmap saveBitmap = KansUtils.getRoundIconBitmap(bitmap, getUi().getContext().getResources().getDimensionPixelSize(R.dimen.kans_rouduct_icon_size));
		mViewHold.icon.setImageBitmap(saveBitmap);
		KansUtils.saveSettingsIconPath(mKansTable, saveBitmap);
	}

	
	public void addOrUpdateProductClass(final ProductClass mProductClass) {
		if (mProductClass != null) {
			x.task().run(new Runnable() {

				@Override
				public void run() {
					mProductClass.lastRefreshTime = System.currentTimeMillis();
					DbManager manager = x.getDb(KApplication.localDaoConfig);
					try {
						if(mProductClass.id>0){
							manager.saveOrUpdate(mProductClass);
						}else{
							manager.saveBindingId(mProductClass);
						}
						updateData();
					} catch (DbException e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	public void delProductClass(final ProductClass mProductClass) {
		if (mProductClass != null) {
			x.task().run(new Runnable() {

				@Override
				public void run() {
					DbManager manager = x.getDb(KApplication.localDaoConfig);
					try {
						if (manager.selector(ProductClass.class).findAll().size() > 1) {
							int count = manager.delete(ProductClass.class, WhereBuilder.b().and(ProductClass._NAME, "=", mProductClass.name));
							updateData();
							Log.i("xlan", "del:" + mProductClass.name + " count:" + count);
						} else {
							x.task().post(new Runnable() {

								@Override
								public void run() {
									Toast.makeText(getUi().getContext(), R.string.kans_del_keep_one, Toast.LENGTH_SHORT).show();
								}
							});
						}
					} catch (DbException e) {
						e.printStackTrace();
					}
				}
			});
		}

	}

	public void listItemReset() {
		if(getUi() != null){
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (OnScrollListener.SCROLL_STATE_TOUCH_SCROLL == scrollState) {
			listItemReset();
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

	}

}
