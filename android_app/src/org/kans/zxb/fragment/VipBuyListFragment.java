package org.kans.zxb.fragment;

import java.lang.reflect.Field;

import org.kans.zxb.R;
import org.kans.zxb.entity.ProductEntity;
import org.kans.zxb.presenter.RouductListPresenter;
import org.kans.zxb.presenter.VipBuyListPresenter;
import org.kans.zxb.presenter.VipRemarkListPresenter;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

@ContentView(value = R.layout.rouduct_list_fragment)
public class VipBuyListFragment extends KFragment<VipBuyListPresenter, VipBuyListPresenter.Ui> implements VipBuyListPresenter.Ui {

	public static final int MODEL_NORNAL = 0;
	public static final int MODEL_SEARCH = 1;
	private int current_model = MODEL_NORNAL;

	@ViewInject(R.id.product_add_button)
	View addButton;

	@ViewInject(R.id.product_search_view)
	View searchView;

	@ViewInject(R.id.product_list)
	ListView mListView;

	@ViewInject(R.id.product_search_edittext)
	EditText searchEditText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Activity().setMenuDrawable(R.drawable.k_ic_search);
		current_model = MODEL_NORNAL;
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
	public VipBuyListPresenter createPresenter() {
		return new VipBuyListPresenter();
	}

	@Override
	public VipBuyListFragment getUi() {
		return this;
	}

	@Override
	public EditText getSearchEditText() {
		return searchEditText;
	}

	@Override
	public Context getContext() {
		return getActivity();
	}

	@Override
	public boolean onMenuButtonClick() {
		setSearchModel(true);
		return true;
	}

	@Override
	public boolean onBackButtonClick() {
		if (current_model == MODEL_SEARCH) {
			setSearchModel(false);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void setSearchModel(boolean show) {

		current_model = show ? MODEL_SEARCH : MODEL_NORNAL;
		searchEditText.setText("");
		Activity().setMenuDrawable(show ? 0 : R.drawable.k_ic_search);
		getPresenter().updateData();
		if (current_model == MODEL_NORNAL) {
			addButton.setVisibility(View.VISIBLE);
			searchView.setVisibility(View.GONE);
		} else {
			addButton.setVisibility(View.GONE);
			searchView.setVisibility(View.VISIBLE);
		}
	}

	@Event(value = { R.id.product_add_button }, type = View.OnClickListener.class)
	public void onViewClick(View view) {
		Log.i("xlan", "onViewClick" + view);
		super.onClick(view);
	}

	@Override
	public ListView getListView() {
		return mListView;
	}

	@Override
	public View getAddButton() {
		return addButton;
	}

	@Override
	public void showDelDilog(ProductEntity mProductEntity) {
		new ProductDilog(getContext(), mProductEntity).show();
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

	class ProductDilog implements DialogInterface.OnDismissListener, DialogInterface.OnClickListener {
		final ProductEntity mProductEntity;
		EditText nameView, remarkView;
		Builder mBuilder;
		int count = 0;

		public ProductDilog(Context context, ProductEntity mProductEntity) {
			this.mProductEntity = mProductEntity;
			// mBuilder = new
			// AlertDialog.Builder(context,android.R.style.Theme_Material_Light_Dialog);
			mBuilder = new AlertDialog.Builder(context);
			mBuilder.setView(createView());
			mBuilder.setOnDismissListener(this);
			mBuilder.setNegativeButton(R.string.kans_negative_text, this);
			mBuilder.setPositiveButton(R.string.kans_positive_text, this);
			mBuilder.setCancelable(true);
			mBuilder.setTitle(R.string.product_fragment_del_product);
			nameView.setEnabled(false);
			nameView.setText(mProductEntity.name);
			remarkView.setEnabled(false);
			remarkView.setText(mProductEntity.remark);
		}

		public void show() {
			mBuilder.create().show();
		}

		public ProductEntity getProductEntity() {
			return mProductEntity;
		}

		private View createView() {
			View mView = View.inflate(getContext(), R.layout.rouduct_alert_dialog, null);
			nameView = (EditText) mView.findViewById(R.id.roduct_name);
			nameView.setBackground(null);
			remarkView = (EditText) mView.findViewById(R.id.roduct_remark);
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
				getPresenter().delProductEntity(mProductEntity);
			} else if (which == DialogInterface.BUTTON_NEGATIVE) {
			}
		}
	}

	@Override
	public int getCurrentModel() {
		return current_model;
	}

}
