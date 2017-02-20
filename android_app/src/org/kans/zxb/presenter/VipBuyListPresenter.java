package org.kans.zxb.presenter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.kans.zxb.KApplication;
import org.kans.zxb.KUi;
import org.kans.zxb.R;
import org.kans.zxb.entity.ProductEntity;
import org.kans.zxb.fragment.ProductListFragment;
import org.kans.zxb.fragment.VipBuyListFragment;
import org.kans.zxb.fragment.VipRemarkListFragment;
import org.kans.zxb.util.KansUtils;
import org.kans.zxb.view.KItemView;
import org.kans.zxb.view.KItemView.KListItemCallback;
import org.xutils.DbManager;
import org.xutils.x;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
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

public class VipBuyListPresenter extends KPresenter<VipBuyListPresenter.Ui> implements OnItemClickListener, OnItemLongClickListener, OnScrollListener, TextWatcher {

	private static final int REQUEST_CODE_ADD = 0x1001;
	private static final int REQUEST_CODE_EDIT = 0x1002;

	private RouductListAdapter mRouductListAdapter;
	private Runnable notifyRun;

	public interface Ui extends KUi {
		VipBuyListFragment getUi();

		Context getContext();

		ListView getListView();

		EditText getSearchEditText();

		int getCurrentModel();

		void setSearchModel(boolean show);

		void startActivityForResult(Intent intent, int requestCode);

		void startActivity(Intent intent);

		View getAddButton();

		void showDelDilog(ProductEntity mProductEntity);
	}

	class ItemViewHold {
		KItemView mKItemView;
		ImageView mIconView;
		TextView mNameView;
		TextView mRemarkView;
		Button delButton;
		Button editButton;
		ProductEntity mProductEntity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mRouductListAdapter = new RouductListAdapter();
		updateData();
	}

	@Override
	public void onStart() {
		super.onStart();
		getUi().getListView().setAdapter(mRouductListAdapter);
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
		}
	}

	public void updateData() {
		x.task().run(new Runnable() {
			@Override
			public void run() {
				DbManager manager = x.getDb(KApplication.localDaoConfig);
				try {
					String searchText = getUi().getSearchEditText()!=null?getUi().getSearchEditText().getText().toString().trim():"";
					final List<ProductEntity> mProductEntitys;
					if (getUi().getCurrentModel() == ProductListFragment.MODEL_NORNAL || searchText.length() == 0) {
						mProductEntitys = manager.selector(ProductEntity.class).orderBy(ProductEntity._ID).findAll();
					} else {
						mProductEntitys = manager.selector(ProductEntity.class).where(ProductEntity._NAME, "LIKE", "%" + searchText + "%").or(ProductEntity._REMARK, "LIKE", "%" + searchText + "%").orderBy(ProductEntity._ID).findAll();
					}
					if(mProductEntitys != null){
						notifyRun = new Runnable() {
							@Override
							public void run() {
								notifyRun = null;
								mRouductListAdapter.notifyDataSetChanged(mProductEntitys);
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
		case R.id.product_add_button:
			Intent addIntent = new Intent();
			addIntent.setClassName(getUi().getContext(), "org.kans.zxb.ProductActivity");
			getUi().startActivityForResult(addIntent, REQUEST_CODE_ADD);
			break;

		case R.id.edit_item:
			if (tag != null && tag instanceof ProductEntity) {
				Intent editIntent = new Intent();
				editIntent.setClassName(getUi().getContext(), "org.kans.zxb.ProductActivity");
				editIntent.putExtra("product_entity", (ProductEntity) tag);
				editIntent.putExtra("product_edit", true);
				getUi().startActivityForResult(editIntent, REQUEST_CODE_EDIT);
			}
			break;

		case R.id.del_item:
			if (tag != null && tag instanceof ProductEntity) {
				getUi().showDelDilog((ProductEntity) tag);
			}
			break;

		default:
			break;

		}
	}

	public void addOrUpdateProductEntity(final ProductEntity mProductEntity) {
		if (mProductEntity != null) {
			x.task().run(new Runnable() {

				@Override
				public void run() {
					mProductEntity.lastRefreshTime = System.currentTimeMillis();
					DbManager manager = x.getDb(KApplication.localDaoConfig);
					try {
						manager.saveOrUpdate(mProductEntity);
						updateData();
					} catch (DbException e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	public void delProductEntity(final ProductEntity mProductEntity) {
		if (mProductEntity != null) {
			x.task().run(new Runnable() {

				@Override
				public void run() {
					DbManager manager = x.getDb(KApplication.localDaoConfig);
					try {
						List<ProductEntity> mProductEntities = manager.selector(ProductEntity.class).where(ProductEntity._NAME, "=", mProductEntity.name).findAll();
						if(mProductEntities !=null){
							for(ProductEntity mProductEntity:mProductEntities){
								KansUtils.delFile(mProductEntity.iconPath);
							}
						}
						int count = manager.delete(ProductEntity.class, WhereBuilder.b().and(ProductEntity._NAME, "=", mProductEntity.name));
						updateData();
						Log.i("xlan", "del:" + mProductEntity.name + " count:" + count);
					} catch (DbException e) {
						e.printStackTrace();
					}
				}
			});
		}

	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {

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
			getUi().showDelDilog(((ItemViewHold) tag).mProductEntity);
			return true;
		}
		return false;
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

	public class RouductListAdapter extends BaseAdapter implements KListItemCallback {
		List<ProductEntity> mProductEntityes = new ArrayList<ProductEntity>();

		public void notifyDataSetChanged(List<ProductEntity> mProductEntityes) {
			this.mProductEntityes.clear();
			this.mProductEntityes.addAll(mProductEntityes);
			super.notifyDataSetChanged();
			listItemReset();
		}

		@Override
		public int getCount() {
			return mProductEntityes.size();
		}

		@Override
		public Object getItem(int position) {
			return mProductEntityes.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ProductEntity mProductEntity = (ProductEntity) getItem(position);
			ItemViewHold mViewHold = null;
			KItemView mKItemView = null;
			if (convertView != null) {
				mKItemView = (KItemView) convertView;
				mViewHold = (ItemViewHold) mKItemView.getTag();
				mViewHold.mProductEntity = mProductEntity;
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
				mViewHold.mProductEntity = mProductEntity;
				mKItemView.setTag(mViewHold);
			}
			initView(mViewHold);
			return mKItemView;
		}

		private void setPriveIcon(File file, ImageView icon) {
			if (file.isFile()) {
				icon.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
			} else {
				icon.setImageResource(R.drawable.k_icon_background);
			}
		}

		private void initView(ItemViewHold mViewHold) {
			mViewHold.mKItemView.setKListItemCallback(this);
			setPriveIcon(KansUtils.getProductIconThumbnail(mViewHold.mProductEntity), mViewHold.mIconView);
			mViewHold.delButton.setTag(mViewHold.mProductEntity);
			mViewHold.delButton.setOnClickListener(VipBuyListPresenter.this);
			mViewHold.editButton.setTag(mViewHold.mProductEntity);
			mViewHold.editButton.setOnClickListener(VipBuyListPresenter.this);
			mViewHold.mNameView.setText(mViewHold.mProductEntity.name);
			mViewHold.mRemarkView.setText(mViewHold.mProductEntity.remark);
		}

		@Override
		public void delItem(KItemView itemView) {

		}

		@Override
		public void onItemClick(KItemView itemView) {
			Log.i("xlan", "onItemClick 2:" + itemView.getTag());
			Object tag = itemView.getTag();
			if (tag != null && tag instanceof ItemViewHold) {
				Intent editIntent = new Intent();
				editIntent.setClassName(getUi().getContext(), "org.kans.zxb.ProductActivity");
				editIntent.putExtra("product_entity", ((ItemViewHold) tag).mProductEntity);
				editIntent.putExtra("product_edit", false);
				getUi().startActivity(editIntent);
			}
		}

		@Override
		public void onItemLongClick(KItemView itemView) {

		}

		@Override
		public void endIconClick(KItemView itemView) {

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
}
