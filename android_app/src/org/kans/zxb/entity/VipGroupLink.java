package org.kans.zxb.entity;

import org.json.JSONException;
import org.json.JSONObject;
import org.kans.zxb.util.KansUtils;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;
import android.os.Parcel;

//vip与组   多对多的中间表
@Table(name = VipGroupLink.TABLE_NAME)
public class VipGroupLink extends DataBase {
	public static final String TABLE_NAME = "VipGroupTable";
	public static final String _VIP_ID = "_vip_id";
	public static final String _GROUP_ID = "_group_id";
	public static final String _REMARK = "_remark";

	//vipID号
	@Column(name=_VIP_ID)
	public String vipId="";
	
	//组的ID号
	@Column(name=_GROUP_ID)
	public int groupId=0;

	//备注
	@Column(name=_REMARK)
	public String remark;
	
	public VipGroupLink(JSONObject json) {
		super(json);
		try {
			if(json.has(_VIP_ID)){
				vipId = json.getString(_VIP_ID);
			}
			if(json.has(_GROUP_ID)){
				groupId = json.getInt(_GROUP_ID);
			}
			if(json.has(_REMARK)){
				remark = json.getString(_REMARK);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public VipGroupLink(Parcel in) {
		super(in);
		vipId = in.readString();
		groupId = in.readInt();
		remark = in.readString();
		
	}
	
	public VipGroupLink() {
	}
	
	
	
	public VipGroupLink(String vipId, int groupId, String remark, long lastRefreshTime) {
		super();
		this.vipId = vipId;
		this.groupId = groupId;
		this.remark = remark;
		this.lastRefreshTime = lastRefreshTime;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(vipId);
		dest.writeInt(groupId);
		dest.writeString(remark);
	}
	
	@Override
	public JSONObject getJson() {
		JSONObject mJSONObject = super.getJson();
		try {
			mJSONObject.put(_VIP_ID, vipId);
			mJSONObject.put(_GROUP_ID, groupId);
			mJSONObject.put(_REMARK, remark);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return mJSONObject;
	}


	@Override
	public boolean equals(Object obj) {
		if(!super.equals(obj)){
			return false;
		}
		if(obj instanceof VipGroupLink){
			VipGroupLink mVipGroupLink = (VipGroupLink) obj;
			if(KansUtils.equals(vipId, mVipGroupLink.vipId)
					&& groupId == mVipGroupLink.groupId
					&& KansUtils.equals(remark, mVipGroupLink.remark)){
				return true;
			}
			return false;
		}
		return super.equals(obj);
	}
}
