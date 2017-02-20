package org.kans.zxb.fragment;

import java.lang.reflect.Field;

import org.kans.zxb.R;
import org.kans.zxb.entity.VipGroup;
import org.kans.zxb.presenter.VipGroupListPresenter;
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

@ContentView(value = R.layout.vip_group_list_fragment)
public class VipGroupListFragment extends KFragment<VipGroupListPresenter, VipGroupListPresenter.Ui> implements VipGroupListPresenter.Ui {

	private static final int DIALOG_ADD_TYPE = 0;
	private static final int DIALOG_EDIT_TYPE = 1;
	private static final int DIALOG_DEL_TYPE = 2;

	private VipGroup mDelVipGroup;
	private boolean isAddIntent = false;

	@ViewInject(R.id.add_vip_group_button)
	View button;

	@ViewInject(R.id.add_vip_group_list)
	ListView mListView;

	private VipGroupDilog mVipGroupDilog;

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
	public VipGroupListPresenter createPresenter() {
		return new VipGroupListPresenter();
	}

	@Override
	public VipGroupListFragment getUi() {
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

	@Event(value = R.id.add_vip_group_button, type = View.OnClickListener.class)
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
		if (mVipGroupDilog == null) {
			mVipGroupDilog = new VipGroupDilog(getContext(), DIALOG_ADD_TYPE, null);
			mVipGroupDilog.show();
		}
	}

	@Override
	public void showDelDilog(VipGroup mVipGroup) {
		if (mVipGroupDilog == null) {
			mVipGroupDilog = new VipGroupDilog(getContext(), DIALOG_DEL_TYPE, mVipGroup);
			mVipGroupDilog.show();
		}
	}

	@Override
	public void showEditDilog(VipGroup mVipGroup) {
		if (mVipGroupDilog == null) {
			mVipGroupDilog = new VipGroupDilog(getContext(), DIALOG_EDIT_TYPE, mVipGroup);
			mVipGroupDilog.show();
		}
	}

	class VipGroupDilog implements DialogInterface.OnDismissListener, DialogInterface.OnClickListener {
		final int type;
		final VipGroup mVipGroup;
		EditText nameView, remarkView;
		Builder mBuilder;

		public VipGroupDilog(Context context, int type, VipGroup mVipGroup) {
			this.type = type;
			if (mVipGroup == null) {
				this.mVipGroup = new VipGroup();
			} else {
				this.mVipGroup = mVipGroup;
			}
			// mBuilder = new
			// AlertDialog.Builder(context,android.R.style.Theme_Material_Light_Dialog);
			mBuilder = new AlertDialog.Builder(context);
			mBuilder.setView(createView());
			mBuilder.setOnDismissListener(this);
			mBuilder.setNegativeButton(R.string.kans_negative_no_save_text, this);
			mBuilder.setPositiveButton(R.string.kans_positive_yes_save_text, this);
			mBuilder.setCancelable(true);
			switch (type) {
			case DIALOG_ADD_TYPE:
				mBuilder.setTitle(R.string.vip_group_fragment_add_vip_group);
				nameView.setEnabled(true);
				remarkView.setEnabled(true);
				break;
			case DIALOG_EDIT_TYPE:
				mBuilder.setTitle(R.string.vip_group_fragment_modify_vip_group);
				nameView.setEnabled(true);
				nameView.setText(mVipGroup.name);
				remarkView.setEnabled(true);
				remarkView.setText(mVipGroup.remark);
				break;
			case DIALOG_DEL_TYPE:
				mBuilder.setTitle(R.string.product_class_fragment_del_product_class);
				nameView.setEnabled(false);
				nameView.setText(mVipGroup.remark);
				remarkView.setEnabled(false);
				remarkView.setText(mVipGroup.remark);
				break;

			default:
				break;
			}
		}

		public void show() {
			mBuilder.create().show();
		}

		public VipGroup getVipGroup() {
			return mVipGroup;
		}

		private View createView() {
			View mView = View.inflate(getContext(), R.layout.vip_group_list_alert_dialog, null);
			nameView = (EditText) mView.findViewById(R.id.vip_group_name);
			remarkView = (EditText) mView.findViewById(R.id.vip_group_remark);
			return mView;
		}

		@Override
		public void onDismiss(DialogInterface dialog) {
			mVipGroupDilog = null;
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
						mVipGroup.name = name;
						mVipGroup.remark = remark;
						getPresenter().addOrUpdateVipGroup(mVipGroup);
						if (isAddIntent) {
							Activity().setResult(android.app.Activity.RESULT_OK);
							Activity().finish();
						}
					}
					break;
				case DIALOG_DEL_TYPE:
					if (mDelVipGroup == null || !mVipGroup.name.equals(mDelVipGroup.name)) {
						mDelVipGroup = mVipGroup;
						Toast.makeText(getContext(), R.string.kans_del_double_verify, Toast.LENGTH_LONG).show();
					} else {
						getPresenter().delVipGroup(mVipGroup);
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
