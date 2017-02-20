package org.kans.zxb.presenter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kans.zxb.KApplication;
import org.kans.zxb.KUi;
import org.kans.zxb.R;
import org.kans.zxb.entity.ProductEntity;
import org.kans.zxb.entity.VipGroup;
import org.kans.zxb.entity.VipGroupLink;
import org.kans.zxb.entity.VipUser;
import org.kans.zxb.fragment.ProductListFragment;
import org.kans.zxb.fragment.VipUserGroupFragment;
import org.kans.zxb.fragment.VipUserListFragment;
import org.kans.zxb.util.KansUtils;
import org.kans.zxb.view.KItemView;
import org.kans.zxb.view.KItemView.KListItemCallback;
import org.xutils.DbManager;
import org.xutils.x;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class VipUserGroupPresenter extends KPresenter<VipUserGroupPresenter.Ui> implements OnScrollListener{

	private static final int REQUEST_CODE_ADD = 0x1001;
	private static final int REQUEST_CODE_EDIT = 0x1002;

	private VipGroupExpandableAdapter mVipListAdapterAdapter;
	private Runnable notifyRun;
	private List<VipGroup> mVipGroups;
	private List<VipUser> mVipUsers;
	private Map<Integer, List<VipGroupLink>> mMaps;
	
	public interface Ui extends KUi {
		VipUserGroupFragment getUi();
		void viewPagerCanScroll(boolean canScroll);
		Context getContext();

		ExpandableListView getListView();

		void startActivityForResult(Intent intent, int requestCode);

		void startActivity(Intent intent);

		void showRemoveUserFromGroupDilog(VipGroupLink mVipGroupLink);
	}

	class ItemViewHold {
		KItemView mKItemView;
		ImageView mIconView;
		TextView mNameView;
		TextView mRemarkView;
		VipGroupLink mVipGroupLink;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mVipListAdapterAdapter = new VipGroupExpandableAdapter();
		mVipGroups = new ArrayList<VipGroup>();
		mVipUsers = new ArrayList<VipUser>();
		mMaps = new HashMap<Integer, List<VipGroupLink>>();
	}

	@Override
	public void onStart() {
		super.onStart();
		getUi().getListView().setAdapter(mVipListAdapterAdapter);
		getUi().getListView().setOnScrollListener(this);
		updateData();
		Log.i("xlan", getClass().getName()+" onStart");
	}

	@Override
	public void onStop() {
		super.onStop();
		getUi().getListView().setAdapter((VipGroupExpandableAdapter)null);
		getUi().getListView().setOnScrollListener(null);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (notifyRun != null) {
			x.task().removeCallbacks(notifyRun);
			notifyRun = null;
		}
	}

	public VipUser getVipUser(VipGroupLink mLink){
		for(VipUser mVipUser:mVipUsers){
			if(KansUtils.equals(mLink.vipId, mVipUser.vipId)){
				return mVipUser;
			}
		}
		return null;
	}
	
	public VipGroup getVipGroup(VipGroupLink mLink){
		for(VipGroup mVipGroup:mVipGroups){
			if(mLink.groupId == mVipGroup.id && mVipGroup.id>0){
				return mVipGroup;
			}
		}
		return null;
	}
	
	public void updateData() {
		DbManager manager = x.getDb(KApplication.localDaoConfig);
		try {
			notifyRun = new Runnable() {
				@Override
				public void run() {
					notifyRun = null;
					mVipListAdapterAdapter.notifyDataSetChanged();
				}
			};
			mVipGroups.clear();
			List<VipGroup> mGroups = manager.selector(VipGroup.class).findAll();
			if(mGroups != null && mGroups.size()>0){
				mVipGroups.addAll(mGroups);
			}
			if(mVipGroups.size()>3){
				throw new IllegalAccessError("xlan");
			}
			mVipUsers.clear();
			List<VipUser> mUsers = manager.selector(VipUser.class).findAll();
			if(mUsers != null && mUsers.size()>0){
				mVipUsers.addAll(mUsers);
			}
			for(VipGroup mVipGroup:mVipGroups){
				List<VipGroupLink>  mVipGroupLinks = mMaps.get(mVipGroup.id);
				if(mVipGroupLinks == null){
					mVipGroupLinks = new ArrayList<VipGroupLink>();
					mMaps.put(mVipGroup.id, mVipGroupLinks);
				}
				List<VipGroupLink>  mLinks = manager.selector(VipGroupLink.class).where(VipGroupLink._GROUP_ID, "=", mVipGroup.id).findAll();
				if(mLinks!=null && mLinks.size()>0){
					mVipGroupLinks.clear();
					mVipGroupLinks.addAll(mLinks);
				}
			}
			if(notifyRun!=null){
				x.task().post(notifyRun);
			}
		} catch (DbException e1) {
			e1.printStackTrace();
		}
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
			if (tag != null && tag instanceof VipGroupLink) {
				getUi().showRemoveUserFromGroupDilog((VipGroupLink) tag);
			}
			break;

		default:
			break;

		}
	}

	public void delVipUserFromGroup(final VipUser mVipUser) {
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

	public void listItemReset() {
		if(getUi() != null){

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

	public class VipGroupExpandableAdapter extends BaseExpandableListAdapter implements KListItemCallback {

		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
			listItemReset();
		}

		private void setPriveIcon(File file, ImageView icon) {
			if (file.isFile()) {
				icon.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
			} else {
				icon.setImageResource(R.drawable.k_icon_background);
			}
		}

		private void initChildView(ItemViewHold mViewHold) {
			VipUser mVipUser = getVipUser(mViewHold.mVipGroupLink);
			mViewHold.mKItemView.setKListItemCallback(this);
			setPriveIcon(KansUtils.getVipIconThumbnail(mVipUser), mViewHold.mIconView);
			mViewHold.mNameView.setText(mVipUser.name);
			mViewHold.mRemarkView.setText(String.valueOf(mVipUser.credit));
		}

		@Override
		public void delItem(KItemView itemView) {
			Object tag = itemView.getTag();
			if (tag != null && tag instanceof ItemViewHold) {
				getUi().showRemoveUserFromGroupDilog(((ItemViewHold) tag).mVipGroupLink);
			}
			listItemReset();
		}

		@Override
		public void onItemClick(KItemView itemView) {
			Log.i("xlan", "onItemClick 2:" + itemView.getTag());
			Object tag = itemView.getTag();
			if (tag != null && tag instanceof ItemViewHold) {
				VipUser mUser = getVipUser(((ItemViewHold) tag).mVipGroupLink);
				if(mUser != null){
					Intent mIntent = new Intent();
					mIntent.setClassName(getUi().getContext(), "org.kans.zxb.VipUserActivity");
					mIntent.putExtra("vip_user_entity", mUser);
					mIntent.putExtra("vip_user_edit", false);
					getUi().startActivityForResult(mIntent, REQUEST_CODE_EDIT);
				}
			}
		}

		@Override
		public void onItemLongClick(KItemView itemView) {

		}

		@Override
		public void endIconClick(KItemView itemView) {
			
		}


		@Override
		public int getGroupCount() {
			return mVipGroups.size();
		}


		@Override
		public int getChildrenCount(int groupPosition) {
			List<VipGroupLink> mLinks = (List<VipGroupLink>) getGroup(groupPosition);
			if(mLinks != null){
				return mLinks.size();
			}
			return 0;
		}


		@Override
		public List<VipGroupLink> getGroup(int groupPosition) {
			return mMaps.get(getVipGroup(groupPosition).id);
		}

		public VipGroup getVipGroup(int groupPosition){
			return mVipGroups.get(groupPosition);
		}
		
		@Override
		public VipGroupLink getChild(int groupPosition, int childPosition) {
			List<VipGroupLink> mLinks = getGroup(groupPosition);
			if(mLinks != null && mLinks.size()>childPosition){
				return mLinks.get(childPosition);
			}
			return null;
		}


		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}


		@Override
		public long getChildId(int groupPosition, int childPosition) {
			long count = 0;
			for(int i=0;i<groupPosition;i++){
				if(getGroup(i) != null){
					count+=getGroup(i).size();
				}
			}
			count+=childPosition;
			return count;
		}


		@Override
		public boolean hasStableIds() {
			return false;
		}


		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			VipGroup mGroup = getVipGroup(groupPosition);
			Bitmap icon = KansUtils.getStringBitmap(54, 54, mGroup.name, true, KansUtils.BUATFUL_COLORS[groupPosition%KansUtils.BUATFUL_COLORS.length]);
			String name = mGroup.name;
			View view = null;
			if(convertView != null){
				view = convertView;
			}else{
				view = View.inflate(getUi().getContext(), R.layout.vip_user_group_group_list_item, null);
			}
			ImageView iconView = (ImageView) view.findViewById(R.id.icon);
			iconView.setImageBitmap(icon);
			TextView nameView = (TextView) view.findViewById(R.id.name);
			nameView.setText(name);
			view.setTag(mGroup);
			return view;
		}


		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			VipGroupLink mVipGroupLink = (VipGroupLink) getChild(groupPosition, childPosition);
			ItemViewHold mViewHold = null;
			KItemView mKItemView = null;
			if (convertView != null) {
				mKItemView = (KItemView) convertView;
				mViewHold = (ItemViewHold) mKItemView.getTag();
				mViewHold.mVipGroupLink = mVipGroupLink;
			} else {
				mViewHold = new ItemViewHold();
				mKItemView = KItemView.onCreateKItemView(getUi().getContext(), R.layout.k_list_item_default);
				mViewHold.mKItemView = mKItemView;
				mViewHold.mIconView = (ImageView) mKItemView.findViewById(R.id.icon);
				mViewHold.mNameView = (TextView) mKItemView.findViewById(R.id.name);
				mViewHold.mRemarkView = (TextView) mKItemView.findViewById(R.id.remark);
				mViewHold.mVipGroupLink = mVipGroupLink;
				mKItemView.setTag(mViewHold);
			}
			initChildView(mViewHold);
			return mKItemView;
		}


		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return false;
		}

	}
}
