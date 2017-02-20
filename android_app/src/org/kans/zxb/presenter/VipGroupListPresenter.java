package org.kans.zxb.presenter;

import java.util.ArrayList;
import java.util.List;

import org.kans.zxb.KApplication;
import org.kans.zxb.KUi;
import org.kans.zxb.R;
import org.kans.zxb.entity.VipGroup;
import org.kans.zxb.fragment.VipGroupListFragment;
import org.kans.zxb.view.KItemView;
import org.kans.zxb.view.KItemView.KListItemCallback;
import org.xutils.DbManager;
import org.xutils.x;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class VipGroupListPresenter extends KPresenter<VipGroupListPresenter.Ui> implements OnScrollListener {

	private VipGroupAdapter mVipGroupAdapter;
	private Runnable notifyRun;

	public interface Ui extends KUi {
		VipGroupListFragment getUi();

		Context getContext();

		ListView getListView();

		View getAddButton();

		void showAddDilog();

		void showDelDilog(VipGroup mVipGroup);

		void showEditDilog(VipGroup mVipGroup);
	}

	class ItemViewHold {
		KItemView mKItemView;
		TextView mNameView;
		TextView mRemarkView;
		Button delButton;
		Button editButton;
		VipGroup mVipGroup;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mVipGroupAdapter = new VipGroupAdapter();
		updateData();
	}

	@Override
	public void onStart() {
		super.onStart();
		getUi().getListView().setAdapter(mVipGroupAdapter);
		getUi().getListView().setOnScrollListener(this);
		getUi().getAddButton().setOnClickListener(this);
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
				try {
					final List<VipGroup> mVipGroupes = manager.selector(VipGroup.class).orderBy(VipGroup._ID).findAll();
					if(mVipGroupes != null){
						notifyRun = new Runnable() {
							@Override
							public void run() {
								notifyRun = null;
								mVipGroupAdapter.notifyDataSetChanged(mVipGroupes);
							}
						};
						x.task().post(notifyRun);
					}
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
		case R.id.add_vip_group_button:
			getUi().showAddDilog();
			break;

		case R.id.edit_item:
			if (tag != null && tag instanceof VipGroup) {
				getUi().showEditDilog((VipGroup) tag);
			}
			break;

		case R.id.del_item:
			if (tag != null && tag instanceof VipGroup) {
				getUi().showDelDilog((VipGroup) tag);
			}
			break;

		default:
			break;

		}
	}

	public void addOrUpdateVipGroup(final VipGroup mVipGroup) {
		if (mVipGroup != null) {
			x.task().run(new Runnable() {

				@Override
				public void run() {
					mVipGroup.lastRefreshTime = System.currentTimeMillis();
					DbManager manager = x.getDb(KApplication.localDaoConfig);
					try {
						manager.saveOrUpdate(mVipGroup);
						updateData();
					} catch (DbException e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	public void delVipGroup(final VipGroup mVipGroup) {
		if (mVipGroup != null) {
			x.task().run(new Runnable() {

				@Override
				public void run() {
					DbManager manager = x.getDb(KApplication.localDaoConfig);
					try {
						if (manager.selector(VipGroup.class).findAll().size() > 1) {
							int count = manager.delete(VipGroup.class, WhereBuilder.b().and(VipGroup._NAME, "=", mVipGroup.name));
							updateData();
							Log.i("xlan", "del:" + mVipGroup.name + " count:" + count);
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

	public class VipGroupAdapter extends BaseAdapter implements KListItemCallback {
		List<VipGroup> mVipGroupes = new ArrayList<VipGroup>();

		public void notifyDataSetChanged(List<VipGroup> mVipGroupes) {
			this.mVipGroupes.clear();
			this.mVipGroupes.addAll(mVipGroupes);
			super.notifyDataSetChanged();
			listItemReset();
		}

		@Override
		public int getCount() {
			return mVipGroupes.size();
		}

		@Override
		public Object getItem(int position) {
			return mVipGroupes.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			VipGroup mVipGroup = (VipGroup) getItem(position);
			ItemViewHold mViewHold = null;
			KItemView mKItemView = null;
			if (convertView != null) {
				mKItemView = (KItemView) convertView;
				mViewHold = (ItemViewHold) mKItemView.getTag();
				mViewHold.mVipGroup = mVipGroup;
			} else {
				mViewHold = new ItemViewHold();
				mKItemView = KItemView.onCreateKItemView(getUi().getContext(), R.layout.vip_group_list_item);
				View editView = View.inflate(getUi().getContext(), R.layout.item_del_and_edit, null);
				mKItemView.setEditView(editView);
				mViewHold.mKItemView = mKItemView;
				mViewHold.mNameView = (TextView) mKItemView.findViewById(R.id.name);
				mViewHold.mRemarkView = (TextView) mKItemView.findViewById(R.id.remark);
				mViewHold.editButton = (Button) editView.findViewById(R.id.edit_item);
				mViewHold.delButton = (Button) editView.findViewById(R.id.del_item);
				mViewHold.mVipGroup = mVipGroup;
				mKItemView.setTag(mViewHold);
			}
			initView(mViewHold);
			return mKItemView;
		}

		private void initView(ItemViewHold mViewHold) {
			mViewHold.mKItemView.setKListItemCallback(this);
			mViewHold.delButton.setTag(mViewHold.mVipGroup);
			mViewHold.delButton.setOnClickListener(VipGroupListPresenter.this);
			mViewHold.editButton.setTag(mViewHold.mVipGroup);
			mViewHold.editButton.setOnClickListener(VipGroupListPresenter.this);
			mViewHold.mNameView.setText(mViewHold.mVipGroup.name);
			mViewHold.mRemarkView.setText(mViewHold.mVipGroup.remark);
		}

		@Override
		public void delItem(KItemView itemView) {

		}

		@Override
		public void onItemClick(KItemView itemView) {
			listItemReset();
		}

		@Override
		public void onItemLongClick(KItemView itemView) {
			Log.i("xlan", "onItemLongClick:" + itemView.getTag());
			Object tag = itemView.getTag();
			if (tag != null && tag instanceof ItemViewHold) {
				getUi().showEditDilog(((ItemViewHold) tag).mVipGroup);
			}
		}

		@Override
		public void endIconClick(KItemView itemView) {

		}
	}

	private void listItemReset() {
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
