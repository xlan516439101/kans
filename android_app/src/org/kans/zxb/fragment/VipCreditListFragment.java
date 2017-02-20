package org.kans.zxb.fragment;

import java.lang.reflect.Field;

import org.kans.zxb.R;
import org.kans.zxb.entity.ProductClass;
import org.kans.zxb.presenter.RouductClassPresenter;
import org.kans.zxb.presenter.VipCreditListPresenter;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

@ContentView(value = R.layout.vip_credit_fragment)
public class VipCreditListFragment extends KFragment<VipCreditListPresenter, VipCreditListPresenter.Ui> implements VipCreditListPresenter.Ui {

	private static final int DIALOG_ADD_TYPE = 0;
	private static final int DIALOG_EDIT_TYPE = 1;
	private static final int DIALOG_DEL_TYPE = 2;

	private ProductClass mDelProductClass;
	private boolean isAddIntent = false;

	@ViewInject(R.id.add_product_class_button)
	View button;

	@ViewInject(R.id.add_product_class_list)
	ListView mListView;

	private ProductClassDilog mProductClassDilog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isAddIntent = Activity().getIntent().getBooleanExtra("add", false);
	}

	@Override
	public void onResume() {
		super.onResume();
		getPresenter().updateData();
		if (isAddIntent) {
			showAddDilog();
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public VipCreditListPresenter createPresenter() {
		return new VipCreditListPresenter();
	}

	@Override
	public VipCreditListFragment getUi() {
		return this;
	}

	@Override
	public Context getContext() {
		return getActivity();
	}

	@Override
	public boolean onBackButtonClick() {
		return false;
	}

	@Event(value = R.id.add_product_class_button, type = View.OnClickListener.class)
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
		return button;
	}

	@Override
	public void showAddDilog() {
		if (mProductClassDilog == null) {
			mProductClassDilog = new ProductClassDilog(getContext(), DIALOG_ADD_TYPE, null);
			mProductClassDilog.show();
		}
	}

	@Override
	public void showDelDilog(ProductClass mProductClass) {
		if (mProductClassDilog == null) {
			mProductClassDilog = new ProductClassDilog(getContext(), DIALOG_DEL_TYPE, mProductClass);
			mProductClassDilog.show();
		}
	}

	@Override
	public void showEditDilog(ProductClass mProductClass) {
		if (mProductClassDilog == null) {
			mProductClassDilog = new ProductClassDilog(getContext(), DIALOG_EDIT_TYPE, mProductClass);
			mProductClassDilog.show();
		}
	}

	class ProductClassDilog implements DialogInterface.OnDismissListener, DialogInterface.OnClickListener {
		final int type;
		final ProductClass mProductClass;
		EditText nameView, remarkView;
		Builder mBuilder;

		public ProductClassDilog(Context context, int type, ProductClass mProductClass) {
			this.type = type;
			if (mProductClass == null) {
				this.mProductClass = new ProductClass();
			} else {
				this.mProductClass = mProductClass;
			}
			mBuilder = new AlertDialog.Builder(context);
			mBuilder.setView(createView());
			mBuilder.setOnDismissListener(this);
			mBuilder.setNegativeButton(R.string.kans_negative_text, this);
			mBuilder.setPositiveButton(R.string.kans_positive_text, this);
			mBuilder.setCancelable(true);
			switch (type) {
			case DIALOG_ADD_TYPE:
				mBuilder.setTitle(R.string.product_class_fragment_add_product_class);
				nameView.setEnabled(true);
				remarkView.setEnabled(true);
				break;
			case DIALOG_EDIT_TYPE:
				mBuilder.setTitle(R.string.product_class_fragment_modify_product_class);
				nameView.setEnabled(true);
				nameView.setText(mProductClass.name);
				remarkView.setEnabled(true);
				remarkView.setText(mProductClass.remark);
				break;
			case DIALOG_DEL_TYPE:
				mBuilder.setTitle(R.string.product_class_fragment_del_product_class);
				nameView.setEnabled(false);
				nameView.setText(mProductClass.remark);
				remarkView.setEnabled(false);
				remarkView.setText(mProductClass.remark);
				break;

			default:
				break;
			}
		}

		public void show() {
			mBuilder.create().show();
		}

		public ProductClass getProductClass() {
			return mProductClass;
		}

		private View createView() {
			View mView = View.inflate(getContext(), R.layout.rouduct_class_alert_dialog, null);
			nameView = (EditText) mView.findViewById(R.id.roduct_class_name);
			remarkView = (EditText) mView.findViewById(R.id.roduct_class_remark);
			return mView;
		}

		@Override
		public void onDismiss(DialogInterface dialog) {
			mProductClassDilog = null;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (which == DialogInterface.BUTTON_POSITIVE) {
				switch (type) {
				case DIALOG_ADD_TYPE:
				case DIALOG_EDIT_TYPE:
					String name = nameView.getText().toString();
					String remark = remarkView.getText().toString();
					if (name.length() == 0) {
						try {
							Field mShowing = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
							mShowing.setAccessible(true);
							mShowing.set(dialog, false);
						} catch (Exception e) {
							e.printStackTrace();
						}
						Toast.makeText(getContext(), R.string.kans_name_is_not_null, Toast.LENGTH_LONG).show();
					} else {
						try {
							Field mShowing = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
							mShowing.setAccessible(true);
							mShowing.set(dialog, true);
						} catch (Exception e) {
							e.printStackTrace();
						}
						mProductClass.name = name;
						mProductClass.remark = remark;
						getPresenter().addOrUpdateProductClass(mProductClass);
						if (isAddIntent) {
							Activity().setResult(android.app.Activity.RESULT_OK);
							Activity().finish();
						}
					}
					break;
				case DIALOG_DEL_TYPE:
					if (mDelProductClass == null || !mProductClass.name.equals(mDelProductClass.name)) {
						mDelProductClass = mProductClass;
						Toast.makeText(getContext(), R.string.kans_del_double_verify, Toast.LENGTH_LONG).show();
					} else {
						getPresenter().delProductClass(mProductClass);
					}
					break;
				default:
					break;
				}
			} else if (which == DialogInterface.BUTTON_NEGATIVE) {
				try {
					Field mShowing = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
					mShowing.setAccessible(true);
					mShowing.set(dialog, true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}
