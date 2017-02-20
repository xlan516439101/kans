package org.kans.zxb.fragment;

import java.lang.reflect.Field;

import org.kans.zxb.R;
import org.kans.zxb.entity.ProductClass;
import org.kans.zxb.fragment.MainFragment.IsetMainInterface;
import org.kans.zxb.presenter.KSettingsPresenter;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

@ContentView(value = R.layout.k_settings_fragment)
public class KSettingsFragment extends KFragment<KSettingsPresenter, KSettingsPresenter.Ui> implements KSettingsPresenter.Ui, IsetMainInterface {

	private static final int DIALOG_ADD_TYPE = 0;
	private static final int DIALOG_EDIT_TYPE = 1;
	private static final int DIALOG_DEL_TYPE = 2;

	public class ViewHold {
		public ImageView icon;
		public TextView mNameView;
		public TextView mPhoneView;
		public TextView mEmailView;
		public View mProductClassView;
		public View mProductListView;
		public ViewHold(ImageView icon, TextView mNameView,
				TextView mPhoneView, TextView mEmailView,
				View mProductClassView, View mProductListView) {
			super();
			this.icon = icon;
			this.mNameView = mNameView;
			this.mPhoneView = mPhoneView;
			this.mEmailView = mEmailView;
			this.mProductClassView = mProductClassView;
			this.mProductListView = mProductListView;
		}
		
	}

	@ViewInject(R.id.k_setting_icon)
	ImageView icon;

	@ViewInject(R.id.kans_settings_name_view)
	TextView mNameView;
	
	@ViewInject(R.id.kans_settings_phone_view)
	TextView mPhoneView;
	
	@ViewInject(R.id.kans_settings_email_view)
	TextView mEmailView;

	@ViewInject(R.id.kans_settings_product_class_view)
	View mProductClassView;

	@ViewInject(R.id.kans_settings_product_list_view)
	View mProductListView;
	

	private EditDilog mProductClassDilog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		getPresenter().updateData();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public KSettingsPresenter createPresenter() {
		return new KSettingsPresenter();
	}

	@Override
	public KSettingsFragment getUi() {
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
	public void showEditDilog(ProductClass mProductClass, int type) {
		if (mProductClassDilog == null) {
			mProductClassDilog = new EditDilog(getContext(), type, mProductClass);
			mProductClassDilog.show();
		}
	}

	class EditDilog implements DialogInterface.OnDismissListener, DialogInterface.OnClickListener {
		final int type;
		final ProductClass mProductClass;
		EditText nameView, remarkView;
		Builder mBuilder;

		public EditDilog(Context context, int type, ProductClass mProductClass) {
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
					}
					break;
				case DIALOG_DEL_TYPE:
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

	MainFragment mMainFragment;
	@Override
	public void setMainFragment(MainFragment mFragment) {
		this.mMainFragment = mFragment;
	}

	@Override
	public void viewPagerCanScroll(boolean canScroll) {
		if(mMainFragment != null){
			mMainFragment.viewPagerCanScroll(canScroll);
		}
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		if(state == ViewPager.SCROLL_STATE_DRAGGING){
			getPresenter().listItemReset();
		}
	}

	@Override
	public ViewHold getViewHold() {
		return new ViewHold(icon, mNameView, mPhoneView, mEmailView, mProductClassView, mProductListView);
	}

	@Override
	public void showUserIconSelect() {
		View view = View.inflate(getContext(), R.layout.k_icon_choice_view, null);
		view.findViewById(R.id.gallery).setOnClickListener(getPresenter());
		view.findViewById(R.id.camera).setOnClickListener(getPresenter());
		view.findViewById(R.id.cancel).setOnClickListener(getPresenter());
		Activity().showPopuView(view);
	}

}
