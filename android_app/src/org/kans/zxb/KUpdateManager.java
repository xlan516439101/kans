package org.kans.zxb;

import java.util.ArrayList;
import java.util.List;

import org.kans.zxb.entity.ProductClass;
import org.kans.zxb.entity.ProductEntity;
import org.kans.zxb.entity.VipBuy;
import org.kans.zxb.entity.VipCredit;
import org.kans.zxb.entity.VipGroup;
import org.kans.zxb.entity.VipGroupLink;
import org.kans.zxb.entity.VipPhone;
import org.kans.zxb.entity.VipQQ;
import org.kans.zxb.entity.VipRemark;
import org.kans.zxb.entity.VipUser;
import org.kans.zxb.entity.VipWechat;

public class KUpdateManager extends KUpdateItem{
	private static KUpdateManager mKUpdateManager;
	private List<KUpdateItem> mItems;
	
	public KUpdateManager() {
		super();
		mItems = new ArrayList<KUpdateItem>();
	}

	public static KUpdateManager getInstance(){
		if(mKUpdateManager == null){
			mKUpdateManager = new KUpdateManager();
		}
		return mKUpdateManager;
	}
	

	public boolean registerReceiver(KUpdateItem mItem){
		if(mItem != null && !mItems.contains(mItem)){
			mItems.add(mItem);
			return true;
		}
		return false;
	}
	public boolean unregisterReceiver(KUpdateItem mItem){
		if(mItem != null && mItems.contains(mItem)){
			mItems.remove(mItem);
			return true;
		}
		return false;
	}

	@Override
	public void updateProductClass(ProductClass mProductClass) {
		if(mProductClass!=null){
			for(KUpdateItem item:mItems){
				item.updateProductClass(mProductClass);
			}
		}
	}

	@Override
	public void updateProductEntity(ProductEntity mProductEntity) {
		if(mProductEntity!=null){
			for(KUpdateItem item:mItems){
				item.updateProductEntity(mProductEntity);
			}
		}
	}

	@Override
	public void updateVipBuy(VipBuy mVipBuy) {
		if(mVipBuy!=null){
			for(KUpdateItem item:mItems){
				item.updateVipBuy(mVipBuy);
			}
		}
	}

	@Override
	public void updateVipCredit(VipCredit mVipCredit) {
		if(mVipCredit!=null){
			for(KUpdateItem item:mItems){
				item.updateVipCredit(mVipCredit);
			}
		}
	}

	@Override
	public void updateVipGroup(VipGroup mVipGroup) {
		if(mVipGroup!=null){
			for(KUpdateItem item:mItems){
				item.updateVipGroup(mVipGroup);
			}
		}
	}

	@Override
	public void updateVipGroupLink(VipGroupLink mVipGroupLink) {
		if(mVipGroupLink!=null){
			for(KUpdateItem item:mItems){
				item.updateVipGroupLink(mVipGroupLink);
			}
		}
	}

	@Override
	public void updateVipPhone(VipPhone mVipPhone) {
		if(mVipPhone!=null){
			for(KUpdateItem item:mItems){
				item.updateVipPhone(mVipPhone);
			}
		}
	}

	@Override
	public void updateVipQQ(VipQQ mVipQQ) {
		if(mVipQQ!=null){
			for(KUpdateItem item:mItems){
				item.updateVipQQ(mVipQQ);
			}
		}
	}

	@Override
	public void updateVipRemark(VipRemark mVipRemark) {
		if(mVipRemark!=null){
			for(KUpdateItem item:mItems){
				item.updateVipRemark(mVipRemark);
			}
		}
	}

	@Override
	public void updateVipUser(VipUser mVipUser) {
		if(mVipUser!=null){
			for(KUpdateItem item:mItems){
				item.updateVipUser(mVipUser);
			}
		}
	}

	@Override
	public void updateVipWechat(VipWechat mVipWechat) {
		if(mVipWechat!=null){
			for(KUpdateItem item:mItems){
				item.updateVipWechat(mVipWechat);
			}
		}
	}
	
	
}
