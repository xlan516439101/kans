package org.kans.zxb.presenter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.kans.zxb.KApplication;
import org.kans.zxb.KUi;
import org.kans.zxb.R;
import org.kans.zxb.entity.ProductClass;
import org.kans.zxb.entity.ProductEntity;
import org.kans.zxb.fragment.ProductFragment;
import org.kans.zxb.fragment.ProductFragment.ViewHold;
import org.kans.zxb.util.KansUtils;
import org.xutils.DbManager;
import org.xutils.x;
import org.xutils.ex.DbException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class RouductPresenter extends KPresenter<RouductPresenter.Ui> implements TextWatcher, Spinner.OnItemSelectedListener {
	public static final int REQUEST_ICON_CODE = 0x101;
	public static final int REQUEST_PLANAR_CODE = 0x102;
	private ProductEntity mProductEntity;
	private ProductFragment.ViewHold mViewHold;
	private boolean isEditRoductMode = false;
	List<ProductClass> mProductClasses;
	private ProductClass mProductClass;
	String mProductString = "";

	public interface Ui extends KUi {
		ProductFragment getUi();

		ViewHold getViewHold();

		Context getContext();

		void setTitle(int titleId);

		void setMenuDrawable(int resId);

		void showIconSelect();

		void startActivity(Intent intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mProductClasses = new ArrayList<ProductClass>();
		Intent intent = getUi().getUi().Activity().getIntent();
		if (intent != null) {
			mProductEntity = intent.getParcelableExtra("product_entity");
			isEditRoductMode = intent.getBooleanExtra("product_edit", false);
			getUi().setTitle(R.string.kans_edit_product);
		}
		if (mProductEntity == null) {
			mProductEntity = new ProductEntity();
			getUi().setTitle(R.string.kans_add_product);
			isEditRoductMode = true;
		}
		mProductString = mProductEntity.toString().trim();
	}

	public boolean isEditRoductMode() {
		return isEditRoductMode;
	}

	public void setEditRoductMode(boolean isEditRoductMode) {
		this.isEditRoductMode = isEditRoductMode;
		mViewHold.mNameView.setEnabled(isEditRoductMode);
		mViewHold.mPriceView.setEnabled(isEditRoductMode);
		mViewHold.mPriceIcon.setEnabled(isEditRoductMode);
		mViewHold.mPriceIcon.setEnabled(isEditRoductMode);
		mViewHold.mPriceClass.setEnabled(isEditRoductMode);
		mViewHold.mPlanarCode.setEnabled(isEditRoductMode);
		mViewHold.mPlanarCodeText.setEnabled(isEditRoductMode);
		mViewHold.mRemarkView.setEnabled(isEditRoductMode);
		getUi().setMenuDrawable(isEditRoductMode ? R.drawable.k_check_icon : R.drawable.k_edit_icon);
		x.task().postDelayed(new Runnable() {
			@Override
			public void run() {
				KansUtils.setImeVisibility(false, getUi().getContext(), mViewHold.mNameView);
			}
		}, 200);
	}

	public boolean isModify() {
		catProduct();
		try {
			return !new ProductEntity(new JSONObject(mProductString)).equals(mProductEntity);
		} catch (JSONException e) {
			e.printStackTrace();
			return true;
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		mViewHold = getUi().getViewHold();
		mViewHold.mNameView.addTextChangedListener(this);
		mViewHold.mPriceView.addTextChangedListener(this);
		mViewHold.mPriceIcon.setOnClickListener(this);
		mViewHold.mPlanarCodeText.addTextChangedListener(this);
		mViewHold.mPlanarCode.setOnClickListener(this);
		mViewHold.mRemarkView.addTextChangedListener(this);
		mViewHold.mPriceClass.setAdapter(new PriceClassAdapter());
		mViewHold.mPriceClass.setOnItemSelectedListener(this);
		setEditRoductMode(isEditRoductMode);
	}

	private void setProductClass() {
		try {
			mProductClasses.clear();
			mProductClasses.addAll(x.getDb(KApplication.localDaoConfig).selector(ProductClass.class).findAll());
		} catch (DbException e) {
			e.printStackTrace();
		}
		if (mProductClass != null) {
			for (int i = 0; i < mProductClasses.size(); i++) {
				if (mProductClasses.get(i).id == mProductEntity.class_id) {
					mProductClass = mProductClasses.get(i);
					mViewHold.mPriceClass.setSelection(i);
					break;
				}
			}
		}
		if (mProductClass == null && mProductClasses.size() > 0) {
			mProductClass = mProductClasses.get(0);
		}

	}

	@Override
	public void onResume() {
		super.onResume();
		if (mProductEntity.name != null && mProductEntity.name.length() > 0) {
			mViewHold.mNameView.setText(mProductEntity.name);
			mViewHold.mNameView.requestFocusFromTouch();
		}
		if (mProductEntity.price > 0) {
			mViewHold.mPriceView.setText(String.valueOf(mProductEntity.price));
		}
		setPriveIcon(mProductEntity.iconPath);
		if (mProductEntity.remark != null && mProductEntity.remark.length() > 0) {
			mViewHold.mRemarkView.setText(String.valueOf(mProductEntity.remark));
		}
		updatePlanarCode();
		setProductClass();
	}

	@Override
	public void onPause() {
		super.onPause();
		catProduct();
	}

	private void catProduct() {
		mProductEntity.name = mViewHold.mNameView.getText().toString();
		try {
			mProductEntity.price = Double.valueOf(mViewHold.mPriceView.getText().toString());
		} catch (NumberFormatException e) {
			mProductEntity.price = 0;
			e.printStackTrace();
		}
		mProductEntity.iconPath = mProductEntity.iconPath;
		if (mProductClass != null) {
			mProductEntity.class_id = mProductClass.id;
		}
		mProductEntity.planarCode = mViewHold.mPlanarCodeText.getText().toString();
		mProductEntity.remark = mViewHold.mRemarkView.getText().toString();
	}

	@Override
	public void onStop() {
		super.onStop();
		mViewHold = getUi().getViewHold();
		mViewHold.mNameView.removeTextChangedListener(this);
		mViewHold.mPriceView.removeTextChangedListener(this);
		mViewHold.mPriceIcon.setOnClickListener(null);
		mViewHold.mPlanarCodeText.removeTextChangedListener(this);
		mViewHold.mPlanarCode.setOnClickListener(null);
		mViewHold.mRemarkView.removeTextChangedListener(this);
		mViewHold.mPriceClass.setAdapter(null);
		mViewHold.mPriceClass.setOnItemSelectedListener(null);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.roduct_icon:
			getUi().showIconSelect();
			break;
		case R.id.roduct_planar_code_image:
			Intent scanIntent = new Intent();
			scanIntent.setClassName(getUi().getContext(), "org.kans.zxb.KScanPlanarActivity");
			getUi().getUi().startActivityForResult(scanIntent, REQUEST_PLANAR_CODE);
			break;
		case R.id.camera:
			getUi().getUi().Activity().hidePopuView();
			Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			getUi().getUi().startActivityForResult(cameraIntent, REQUEST_ICON_CODE);
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
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		if (view != null) {
			Object tag = view.getTag();
			if (tag instanceof ProductClass) {
				if (!mProductClass.name.equals(((ProductClass) tag).name)) {
					mProductClass = (ProductClass) tag;
				}
			} else {
				setProductClass();
				Intent intent = new Intent();
				intent.setClassName(getUi().getContext().getPackageName(), "org.kans.zxb.ProductClassActivity");
				intent.putExtra("add", true);
				getUi().startActivity(intent);
			}
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.i("xlan", "requestCode:" + requestCode + " resultCode:" + resultCode + " data:" +(data!=null? data.getData():"null"));
		if (REQUEST_ICON_CODE == requestCode && resultCode == Activity.RESULT_OK) {
			if (data != null) {
				if (data.getData() != null) {
					saveIconBitmap(data.getData());
				} else if (data.getExtras() != null) {
					Bundle mBundle = data.getExtras();
					if (mBundle != null) {
						saveIconBitmap((Bitmap) mBundle.get("data"));
					}
				}
			}
		}else if(REQUEST_PLANAR_CODE == requestCode && resultCode == Activity.RESULT_OK) {
			String result = data.getStringExtra("result");
			if(result!=null && result.length()>0){
				mProductEntity.planarCode = result;
				updatePlanarCode();
			}
		}
	}

	private void saveIconBitmap(Uri mUri) {
		Bitmap bitmap = KansUtils.getBitmapFromUri(getUi().getContext(), mUri);
		if(bitmap != null){
			saveIconBitmap(bitmap);
		}
	}

	private void saveIconBitmap(Bitmap bitmap) {
		Bitmap saveBitmap = KansUtils.getRoundIconBitmap(bitmap, getUi().getContext().getResources().getDimensionPixelSize(R.dimen.kans_rouduct_icon_size));
		mViewHold.mPriceIcon.setImageBitmap(saveBitmap);
		KansUtils.saveProductIconPath(mProductEntity, saveBitmap);
	}

	private void setPriveIcon(String path) {
		if (path != null && new File(path).isFile()) {
			mViewHold.mPriceIcon.setImageBitmap(BitmapFactory.decodeFile(path));
		}
	}

	public void addOrUpdateProduct() {
		catProduct();
		if (mProductEntity.name != null && mProductEntity.name.length() > 0 && mProductEntity.price > 0) {
			if (mProductEntity.iconPath == null || mProductEntity.iconPath.length() == 0) {
				if(mViewHold.mPriceIcon.getDrawable() != null){
					Bitmap saveBitmap = ((BitmapDrawable) mViewHold.mPriceIcon.getDrawable()).getBitmap();
					KansUtils.saveProductIconPath(mProductEntity, saveBitmap);
				}
			}
			mProductEntity.lastRefreshTime = System.currentTimeMillis();
			DbManager manager = x.getDb(KApplication.localDaoConfig);
			try {
				manager.saveOrUpdate(mProductEntity);
				mProductString = mProductEntity.toString();
				Log.i("xlan", getUi().getContext().getResources().getString(R.string.kans_save_succeed));
			} catch (DbException e) {
				e.printStackTrace();
				Log.w("xlan", getUi().getContext().getResources().getString(R.string.kans_save_fail));
			}
		} else {
			Log.i("xlan", mProductEntity.toString());
			Toast.makeText(getUi().getContext(), R.string.kans_product_please_input_all, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public void afterTextChanged(Editable s) {
		if(mViewHold.mPlanarCodeText.getEditableText() == s){
			if(!KansUtils.equals(s.toString(), mProductEntity.planarCode)){
				mProductEntity.planarCode = s.toString();
				updatePlanarCode();
				mViewHold.mPlanarCodeText.setSelection(mProductEntity.planarCode.length());
			}
		}
	}

	public void updatePlanarCode(){
		Log.i("xlan", "mProductEntity.planarCode"+mProductEntity.planarCode);
		if(mProductEntity.planarCode!=null&&mProductEntity.planarCode.length()>0&&!mProductEntity.planarCode.equals("null")){
			mViewHold.mPlanarCodeText.setText(mProductEntity.planarCode);
			if(mViewHold.mPlanarCode.getWidth()>0 && mViewHold.mPlanarCode.getHeight()>0){
				mViewHold.mPlanarCode.setImageBitmap(KansUtils.getBarCode(mProductEntity.planarCode, mViewHold.mPlanarCode.getWidth(), mViewHold.mPlanarCode.getHeight()));
			}else{
				x.task().postDelayed(new Runnable() {
					@Override
					public void run() {
						updatePlanarCode();
					}
				}, 200);
			}
		}else{
			mViewHold.mPlanarCodeText.setText("");
			mViewHold.mPlanarCode.setImageResource(R.drawable.k_scan_button_image);
		}
	}
	
	class PriceClassAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mProductClasses.size() + 1;
		}

		@Override
		public Object getItem(int position) {
			return (position < getCount() - 1) ? mProductClasses.get(position) : null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ProductClass mProductClass = (ProductClass) getItem(position);
			TextView textView = (TextView) View.inflate(getUi().getContext(), android.R.layout.simple_list_item_1, null);
			textView.setTextColor(getUi().getContext().getResources().getColor(R.color.k_text_color));
			textView.setTextSize(20f);
			textView.setBackgroundColor(getUi().getContext().getResources().getColor(R.color.k_background_color));
			textView.setPadding(50, 0, 0, 0);
			textView.setGravity(Gravity.CENTER_VERTICAL);
			if (mProductClass != null) {
				textView.setText(mProductClass.name);
				textView.setTag(mProductClass);
			} else {
				textView.setText(R.string.product_class_fragment_add_product_class);
				textView.setTag(mProductClass);
			}
			return textView;
		}
	}
}
