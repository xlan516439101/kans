package org.kans.zxb.presenter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.kans.zxb.KApplication;
import org.kans.zxb.KUi;
import org.kans.zxb.R;
import org.kans.zxb.entity.ProductEntity;
import org.kans.zxb.entity.VipUser;
import org.kans.zxb.fragment.ProductListFragment;
import org.kans.zxb.fragment.VipUserListFragment;
import org.kans.zxb.util.KansUtils;
import org.kans.zxb.view.KItemView;
import org.kans.zxb.view.KItemView.KListItemCallback;
import org.kans.zxb.view.KItemView.KListItemStateCallback;
import org.kans.zxb.view.KItemView.STATE;
import org.xutils.DbManager;
import org.xutils.x;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class VipUserListPresenter extends KPresenter<VipUserListPresenter.Ui> implements OnItemClickListener, OnItemLongClickListener, OnScrollListener, TextWatcher {

	private static final int REQUEST_CODE_ADD = 0x1001;
	private static final int REQUEST_CODE_EDIT = 0x1002;

	private VipListAdapter mVipListAdapterAdapter;
	private Runnable notifyRun;

	public interface Ui extends KUi {
		VipUserListFragment getUi();
		void viewPagerCanScroll(boolean canScroll);
		Context getContext();

		ListView getListView();

		EditText getSearchEditText();

		int getCurrentModel();

		void setSearchModel(boolean show);

		void startActivityForResult(Intent intent, int requestCode);

		void startActivity(Intent intent);

		View getAddButton();

		void showDelDilog(VipUser mVipUser);

		Resources getResources();
	}

	class ItemViewHold {
		KItemView mKItemView;
		ImageView mIconView;
		TextView mNameView;
		TextView mRemarkView;
		Button delButton;
		Button editButton;
		VipUser mVipUser;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mVipListAdapterAdapter = new VipListAdapter();
	}

	@Override
	public void onStart() {
		super.onStart();
		getUi().getListView().setAdapter(mVipListAdapterAdapter);
		getUi().getListView().setOnItemClickListener(this);
		getUi().getListView().setOnItemLongClickListener(this);
		getUi().getListView().setOnScrollListener(this);
		getUi().getAddButton().setOnClickListener(this);
		getUi().getSearchEditText().addTextChangedListener(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		getUi().getListView().setAdapter(null);
		getUi().getListView().setOnItemClickListener(null);
		getUi().getListView().setOnItemLongClickListener(null);
		getUi().getListView().setOnScrollListener(null);
		getUi().getAddButton().setOnClickListener(null);
		getUi().getSearchEditText().removeTextChangedListener(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (notifyRun != null) {
			x.task().removeCallbacks(notifyRun);
			notifyRun = null;
		}
	}

	public void updateData() {
		x.task().run(new Runnable() {
			@Override
			public void run() {
				DbManager manager = x.getDb(KApplication.localDaoConfig);
				try {
					String searchText = getUi().getSearchEditText()!=null?getUi().getSearchEditText().getText().toString().trim():"";
					final List<VipUser> mVipUser;
					if (getUi().getCurrentModel() == ProductListFragment.MODEL_NORNAL || searchText.length() == 0) {
						mVipUser = manager.selector(VipUser.class).orderBy(VipUser._ID).findAll();
					} else {
						mVipUser = manager.selector(VipUser.class).where(VipUser._NAME, "LIKE", "%" + searchText + "%")
								.or(VipUser._CITY_NAME, "LIKE", "%" + searchText + "%")
								.or(VipUser._EMAIL, "LIKE", "%" + searchText + "%")
								.orderBy(ProductEntity._ID).findAll();
					}
					if(mVipUser != null){
						notifyRun = new Runnable() {
							@Override
							public void run() {
								notifyRun = null;
								mVipListAdapterAdapter.notifyDataSetChanged(mVipUser);
							}
						};
					}
					x.task().post(notifyRun);
				} catch (DbException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		Object tag = v.getTag();
		switch (v.getId()) {
		case R.id.vip_user_add_button:
			Intent addIntent = new Intent();
			addIntent.setClassName(getUi().getContext(), "org.kans.zxb.VipUserActivity");
			getUi().startActivityForResult(addIntent, REQUEST_CODE_ADD);
			break;

		case R.id.edit_item:
			if (tag != null && tag instanceof VipUser) {
				Intent editIntent = new Intent();
				editIntent.setClassName(getUi().getContext(), "org.kans.zxb.VipUserActivity");
				editIntent.putExtra("vip_user_entity", (VipUser) tag);
				editIntent.putExtra("vip_user_edit", true);
				getUi().startActivityForResult(editIntent, REQUEST_CODE_EDIT);
			}
			break;

		case R.id.del_item:
			if (tag != null && tag instanceof VipUser) {
				getUi().showDelDilog((VipUser) tag);
			}
			break;

		default:
			break;

		}
	}

	public void delVipUser(final VipUser mVipUser) {
		if (mVipUser != null) {
			x.task().run(new Runnable() {

				@Override
				public void run() {
					DbManager manager = x.getDb(KApplication.localDaoConfig);
					try {
						File iconFile = new File(mVipUser.iconPath);
						if(iconFile.getParentFile()!=null && iconFile.getParentFile().getParentFile()!=null){
							KansUtils.delFile(new File(mVipUser.iconPath).getParentFile().getParentFile());
						}
						int count = manager.delete(VipUser.class, WhereBuilder.b().and(mVipUser._VIP_ID, "=", mVipUser.vipId));
						updateData();
						Log.i("xlan", "del:" + mVipUser.name + " count:" + count);
					} catch (DbException e) {
						e.printStackTrace();
					}
				}
			});
		}

	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(REQUEST_CODE_EDIT == requestCode || REQUEST_CODE_ADD == requestCode){
			if(resultCode == Activity.RESULT_OK){
				updateData();
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		Log.i("xlan", "onItemClick:" + view.getTag());
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		Log.i("xlan", "onItemLongClick:" + view.getTag());
		Object tag = view.getTag();
		if (tag != null && tag instanceof ItemViewHold) {
			getUi().showDelDilog(((ItemViewHold) tag).mVipUser);
			return true;
		}
		return false;
	}

	public void listItemReset() {
		if(getUi()!=null){
			int count = getUi().getListView().getChildCount();
			for (int i = 0; i < count; i++) {
				View child = getUi().getListView().getChildAt(i);
				if (child instanceof KItemView) {
					KItemView item = (KItemView) child;
					if (item.getState() != KItemView.STATE.NORMAL || item.getState() != KItemView.STATE.RESETING) {
						item.editReset();
					}
				}
			}
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

	public class VipListAdapter extends BaseAdapter implements KListItemCallback, KListItemStateCallback {
		List<VipUser> mVipUsers = new ArrayList<VipUser>();

		public void notifyDataSetChanged(List<VipUser> mNewVipUsers) {
			this.mVipUsers.clear();
			this.mVipUsers.addAll(mNewVipUsers);
			super.notifyDataSetChanged();
			listItemReset();
		}

		@Override
		public int getCount() {
			return mVipUsers.size();
		}

		@Override
		public Object getItem(int position) {
			return mVipUsers.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			VipUser mVipUser = (VipUser) getItem(position);
			ItemViewHold mViewHold = null;
			KItemView mKItemView = null;
			if (convertView != null) {
				mKItemView = (KItemView) convertView;
				mViewHold = (ItemViewHold) mKItemView.getTag();
				mViewHold.mVipUser = mVipUser;
			} else {
				mViewHold = new ItemViewHold();
				mKItemView = KItemView.onCreateKItemView(getUi().getContext(), R.layout.k_list_item_default);
				View editView = View.inflate(getUi().getContext(), R.layout.item_del_and_edit, null);
				mKItemView.setEditView(editView);
				mViewHold.mKItemView = mKItemView;
				mViewHold.mIconView = (ImageView) mKItemView.findViewById(R.id.icon);
				mViewHold.mNameView = (TextView) mKItemView.findViewById(R.id.name);
				mViewHold.mRemarkView = (TextView) mKItemView.findViewById(R.id.remark);
				mViewHold.editButton = (Button) editView.findViewById(R.id.edit_item);
				mViewHold.delButton = (Button) editView.findViewById(R.id.del_item);
				mViewHold.mVipUser = mVipUser;
				mKItemView.setTag(mViewHold);
			}
			initView(mViewHold);
			return mKItemView;
		}

		private void setPriveIcon(File file, ItemViewHold mViewHold) {
			if (file.isFile()) {
				mViewHold.mIconView.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
			} else {
				mViewHold.mIconView.setImageBitmap(KansUtils.getPlanarCode(mViewHold.mVipUser.vipId,((BitmapDrawable)getUi().getResources().getDrawable(R.drawable.k_icon_thumbnail)).getBitmap()));
			}
		}

		private void initView(ItemViewHold mViewHold) {
			mViewHold.mKItemView.setKListItemCallback(this);
			mViewHold.mKItemView.setKListItemStateCallback(this);
			setPriveIcon(KansUtils.getVipIconThumbnail(mViewHold.mVipUser), mViewHold);
			mViewHold.delButton.setTag(mViewHold.mVipUser);
			mViewHold.delButton.setOnClickListener(VipUserListPresenter.this);
			mViewHold.editButton.setTag(mViewHold.mVipUser);
			mViewHold.editButton.setOnClickListener(VipUserListPresenter.this);
			mViewHold.mNameView.setText(mViewHold.mVipUser.name);
			mViewHold.mRemarkView.setText(String.valueOf(mViewHold.mVipUser.credit));
		}

		@Override
		public void delItem(KItemView itemView) {

		}

		@Override
		public void onItemClick(KItemView itemView) {
			Log.i("xlan", "onItemClick 2:" + itemView.getTag());
			Object tag = itemView.getTag();
			if (tag != null && tag instanceof ItemViewHold) {
				Intent mIntent = new Intent();
				mIntent.setClassName(getUi().getContext(), "org.kans.zxb.VipUserActivity");
				mIntent.putExtra("vip_user_entity", ((ItemViewHold) tag).mVipUser);
				mIntent.putExtra("vip_user_edit", false);
				getUi().startActivityForResult(mIntent, REQUEST_CODE_EDIT);
			}
		}

		@Override
		public void onItemLongClick(KItemView itemView) {

		}

		@Override
		public void endIconClick(KItemView itemView) {

		}

		@Override
		public void onStateChange(KItemView itemView, STATE mState) {
			Log.i("xlan", itemView+"----- "+mState);
			if(mState == STATE.NORMAL || mState == STATE.EDIT || mState == STATE.SHOWDELETE){
				getUi().viewPagerCanScroll(true);
			}else{
				getUi().viewPagerCanScroll(false);
			}
		}

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		updateData();
	}

	@Override
	public void afterTextChanged(Editable s) {
	}
	
	@Override
	public void updateVipUser(VipUser mVipUser) {
		super.updateVipUser(mVipUser);
		updateData();
	}
}
