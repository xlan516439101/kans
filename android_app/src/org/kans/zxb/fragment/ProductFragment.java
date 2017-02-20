package org.kans.zxb.fragment;

import org.kans.zxb.R;
import org.kans.zxb.presenter.RouductPresenter;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

@ContentView(value = R.layout.rouduct_fragment)
public class ProductFragment extends KFragment<RouductPresenter, RouductPresenter.Ui> implements RouductPresenter.Ui {

	public class ViewHold {
		public EditText mNameView;
		public EditText mPriceView;
		public ImageView mPriceIcon;
		public Spinner mPriceClass;
		public EditText mPlanarCodeText;
		public ImageView mPlanarCode;
		public EditText mRemarkView;
		public ViewHold(EditText mNameView, EditText mPriceView, ImageView mPriceIcon, Spinner mPriceClass, EditText mPlanarCodeText, ImageView mPlanarCode, EditText mRemarkView) {
			super();
			this.mNameView = mNameView;
			this.mPriceView = mPriceView;
			this.mPriceIcon = mPriceIcon;
			this.mPriceClass = mPriceClass;
			this.mPlanarCodeText = mPlanarCodeText;
			this.mPlanarCode = mPlanarCode;
			this.mRemarkView = mRemarkView;
		}
	}

	@ViewInject(R.id.roduct_name)
	EditText mNameView;

	@ViewInject(R.id.roduct_price)
	EditText mPriceView;

	@ViewInject(R.id.roduct_icon)
	ImageView mPriceIcon;

	@ViewInject(R.id.roduct_class)
	Spinner mPriceClass;

	@ViewInject(R.id.roduct_planar_code_text)
	EditText mPlanarCodeText;
	
	@ViewInject(R.id.roduct_planar_code_image)
	ImageView mPlanarCode;
	
	@ViewInject(R.id.roduct_remark)
	EditText mRemarkView;

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
	public RouductPresenter createPresenter() {
		return new RouductPresenter();
	}

	@Override
	public Context getContext() {
		return getActivity();
	}

	@Override
	public boolean onMenuButtonClick() {
		if (getPresenter().isEditRoductMode()) {
			showSaveDialog(false);
		}
		getPresenter().setEditRoductMode(!getPresenter().isEditRoductMode());
		return super.onMenuButtonClick();
	}

	@Override
	public boolean onBackButtonClick() {
		if (getPresenter().isModify()) {
			showSaveDialog(true);
			return true;
		} else {
			Activity().setResult(Activity.RESULT_OK);
			return false;
		}
	}

	public void showSaveDialog(final boolean finish) {
		new AlertDialog.Builder(Activity()).setTitle(R.string.kans_positive_save_text)
		.setCancelable(false)
		.setPositiveButton(R.string.kans_positive_yes_save_text, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				getPresenter().addOrUpdateProduct();
				if (finish) {
					Activity().setResult(Activity.RESULT_OK);
					Activity().finish();
				}
			}
		})
		.setNegativeButton(R.string.kans_negative_no_save_text, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (finish) {
					Activity().setResult(Activity.RESULT_CANCELED);
					Activity().finish();
				}
			}
		}).create().show();
	}

	@Override
	public ProductFragment getUi() {
		return this;
	}

	@Override
	public ProductFragment.ViewHold getViewHold() {
		return new ViewHold(mNameView, mPriceView, mPriceIcon, mPriceClass, mPlanarCodeText, mPlanarCode, mRemarkView);
	}

	@Override
	public void setTitle(int titleId) {
		Activity().setTitle(titleId);
	}

	@Override
	public void showIconSelect() {
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

}
