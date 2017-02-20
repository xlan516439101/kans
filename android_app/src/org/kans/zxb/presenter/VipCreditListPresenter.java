package org.kans.zxb.presenter;

import java.util.ArrayList;
import java.util.List;

import org.kans.zxb.KApplication;
import org.kans.zxb.KUi;
import org.kans.zxb.R;
import org.kans.zxb.entity.ProductClass;
import org.kans.zxb.fragment.VipCreditListFragment;
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

public class VipCreditListPresenter extends KPresenter<VipCreditListPresenter.Ui> implements OnScrollListener {

	private VipCreditAdapter mVipCreditAdapter;
	private Runnable notifyRun;

	public interface Ui extends KUi {
		VipCreditListFragment getUi();

		Context getContext();

		ListView getListView();

		View getAddButton();

		void showAddDilog();

		void showDelDilog(ProductClass mProductClass);

		void showEditDilog(ProductClass mProductClass);
	}

	class ItemViewHold {
		KItemView mKItemView;
		TextView mNameView;
		TextView mRemarkView;
		Button delButton;
		Button editButton;
		ProductClass mProductClass;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mVipCreditAdapter = new VipCreditAdapter();
		updateData();
	}

	@Override
	public void onStart() {
		super.onStart();
		getUi().getListView().setAdapter(mVipCreditAdapter);
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
					final List<ProductClass> mProductClasses = manager.selector(ProductClass.class).orderBy(ProductClass._ID).findAll();
					notifyRun = new Runnable() {
						@Override
						public void run() {
							notifyRun = null;
							mVipCreditAdapter.notifyDataSetChanged(mProductClasses);
						}
					};
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
		case R.id.add_product_class_button:
			getUi().showAddDilog();
			break;

		case R.id.edit_item:
			if (tag != null && tag instanceof ProductClass) {
				getUi().showEditDilog((ProductClass) tag);
			}
			break;

		case R.id.del_item:
			if (tag != null && tag instanceof ProductClass) {
				getUi().showDelDilog((ProductClass) tag);
			}
			break;

		default:
			break;

		}
	}

	public void addOrUpdateProductClass(final ProductClass mProductClass) {
		if (mProductClass != null) {
			x.task().run(new Runnable() {

				@Override
				public void run() {
					mProductClass.lastRefreshTime = System.currentTimeMillis();
					DbManager manager = x.getDb(KApplication.localDaoConfig);
					try {
						manager.saveOrUpdate(mProductClass);
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

	public class VipCreditAdapter extends BaseAdapter implements KListItemCallback {
		List<ProductClass> mProductClasses = new ArrayList<ProductClass>();

		public void notifyDataSetChanged(List<ProductClass> mProductClasses) {
			this.mProductClasses.clear();
			this.mProductClasses = mProductClasses;
			super.notifyDataSetChanged();
			listItemReset();
		}

		@Override
		public int getCount() {
			return mProductClasses.size();
		}

		@Override
		public Object getItem(int position) {
			return mProductClasses.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ProductClass mProductClass = (ProductClass) getItem(position);
			ItemViewHold mViewHold = null;
			KItemView mKItemView = null;
			if (convertView != null) {
				mKItemView = (KItemView) convertView;
				mViewHold = (ItemViewHold) mKItemView.getTag();
				mViewHold.mProductClass = mProductClass;
			} else {
				mViewHold = new ItemViewHold();
				mKItemView = KItemView.onCreateKItemView(getUi().getContext(), R.layout.rouduct_class_list_item);
				View editView = View.inflate(getUi().getContext(), R.layout.item_del_and_edit, null);
				mKItemView.setEditView(editView);
				mViewHold.mKItemView = mKItemView;
				mViewHold.mNameView = (TextView) mKItemView.findViewById(R.id.name);
				mViewHold.mRemarkView = (TextView) mKItemView.findViewById(R.id.remark);
				mViewHold.editButton = (Button) editView.findViewById(R.id.edit_item);
				mViewHold.delButton = (Button) editView.findViewById(R.id.del_item);
				mViewHold.mProductClass = mProductClass;
				mKItemView.setTag(mViewHold);
			}
			initView(mViewHold);
			return mKItemView;
		}

		private void initView(ItemViewHold mViewHold) {
			mViewHold.mKItemView.setKListItemCallback(this);
			mViewHold.delButton.setTag(mViewHold.mProductClass);
			mViewHold.delButton.setOnClickListener(VipCreditListPresenter.this);
			mViewHold.editButton.setTag(mViewHold.mProductClass);
			mViewHold.editButton.setOnClickListener(VipCreditListPresenter.this);
			mViewHold.mNameView.setText(mViewHold.mProductClass.name);
			mViewHold.mRemarkView.setText(mViewHold.mProductClass.remark);
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
				getUi().showEditDilog(((ItemViewHold) tag).mProductClass);
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
