package org.kans.zxb.presenter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.kans.zxb.KApplication;
import org.kans.zxb.KUi;
import org.kans.zxb.KUpdateManager;
import org.kans.zxb.R;
import org.kans.zxb.entity.VipUser;
import org.kans.zxb.fragment.VipUserFavoriteFragment;
import org.kans.zxb.util.KansUtils;
import org.kans.zxb.view.KItemView;
import org.xutils.DbManager;
import org.xutils.x;
import org.xutils.ex.DbException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class VipUserFavoritePresenter extends KPresenter<VipUserFavoritePresenter.Ui> implements OnItemClickListener, OnItemLongClickListener{

	private static final int REQUEST_CODE_ADD = 0x1001;
	private static final int REQUEST_CODE_EDIT = 0x1002;

	private VipListAdapter mVipListAdapterAdapter;
	private Runnable notifyRun;

	public interface Ui extends KUi {
		VipUserFavoriteFragment getUi();

		Context getContext();

		GridView getGridView();

		void startActivityForResult(Intent intent, int requestCode);

		void startActivity(Intent intent);

		void showRemoveFavorite(VipUser mVipUser);

		Resources getResources();
		
		void showEmptey(boolean isShow);
	}

	class ItemViewHold {
		ImageView mIconView;
		TextView mNameView;
		TextView mCreditView;
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
		getUi().getGridView().setAdapter(mVipListAdapterAdapter);
		getUi().getGridView().setOnItemClickListener(this);
		getUi().getGridView().setOnItemLongClickListener(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		getUi().getGridView().setAdapter(null);
		getUi().getGridView().setOnItemClickListener(null);
		getUi().getGridView().setOnItemLongClickListener(null);
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
					final List<VipUser> mVipUser = manager.selector(VipUser.class).where(VipUser._FAVORITE, "=", "1").findAll();
					if(mVipUser != null){
						notifyRun = new Runnable() {
							@Override
							public void run() {
								notifyRun = null;
								mVipListAdapterAdapter.notifyDataSetChanged(mVipUser);
								getUi().showEmptey(mVipUser.size()<0);
							}
						};
					}else{
						notifyRun = new Runnable() {
							@Override
							public void run() {
								notifyRun = null;
								getUi().showEmptey(true);
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
	}

	public void removeFavorite(final VipUser mVipUser) {
		if (mVipUser != null) {
			x.task().run(new Runnable() {

				@Override
				public void run() {
					DbManager manager = x.getDb(KApplication.localDaoConfig);
					try {
						mVipUser.favorite = 0;
						mVipUser.lastRefreshTime = System.currentTimeMillis();
						manager.update(mVipUser, VipUser._FAVORITE, VipUser._LAST_REFRESH_TIME);
						KUpdateManager.getInstance().updateVipUser(mVipUser);
						updateData();
						Log.i("xlan", "del:" + mVipUser.name );
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
		Object tag = view.getTag();
		if (tag != null && tag instanceof ItemViewHold) {
			Intent mIntent = new Intent();
			mIntent.setClassName(getUi().getContext(), "org.kans.zxb.VipUserActivity");
			mIntent.putExtra("vip_user_entity", ((ItemViewHold) tag).mVipUser);
			mIntent.putExtra("vip_user_edit", false);
			getUi().startActivityForResult(mIntent, REQUEST_CODE_EDIT);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		Log.i("xlan", "onItemLongClick:" + view.getTag());
		Object tag = view.getTag();
		if (tag != null && tag instanceof ItemViewHold) {

			getUi().showRemoveFavorite(((ItemViewHold) tag).mVipUser);
			return true;
		}
		return false;
	}


	public class VipListAdapter extends BaseAdapter {
		List<VipUser> mVipUsers = new ArrayList<VipUser>();

		public void notifyDataSetChanged(List<VipUser> mNewVipUsers) {
			this.mVipUsers.clear();
			this.mVipUsers.addAll(mNewVipUsers);
			super.notifyDataSetChanged();
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
			View view = null;
			if (convertView != null) {
				view = convertView;
				mViewHold = (ItemViewHold) view.getTag();
				mViewHold.mVipUser = mVipUser;
			} else {
				mViewHold = new ItemViewHold();
				view = View.inflate(getUi().getContext(), R.layout.vip_user_favorite_item, null);
				mViewHold.mIconView = (ImageView) view.findViewById(R.id.icon);
				mViewHold.mNameView = (TextView) view.findViewById(R.id.name);
				mViewHold.mCreditView = (TextView) view.findViewById(R.id.credit);
				mViewHold.mVipUser = mVipUser;
				view.setTag(mViewHold);
			}
			initView(mViewHold);
			return view;
		}

		private void setPriveIcon(File file, ItemViewHold mViewHold) {
			if (file.isFile()) {
				mViewHold.mIconView.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
			} else {
				mViewHold.mIconView.setImageBitmap(KansUtils.getPlanarCode(mViewHold.mVipUser.vipId,((BitmapDrawable)getUi().getResources().getDrawable(R.drawable.k_icon_normal)).getBitmap()));
			}
		}

		private void initView(ItemViewHold mViewHold) {
			setPriveIcon(KansUtils.getVipIcon(mViewHold.mVipUser), mViewHold);
			mViewHold.mNameView.setText(mViewHold.mVipUser.name);
			mViewHold.mCreditView.setText(String.valueOf(mViewHold.mVipUser.credit));
		}

	}
}
