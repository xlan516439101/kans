package org.kans.zxb.entity;

import org.json.JSONException;
import org.json.JSONObject;
import org.kans.zxb.util.KansUtils;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;
import android.os.Parcel;

@Table(name = VipWechat.TABLE_NAME)
public class VipWechat extends DataBase {
	public static final String TABLE_NAME = "VipWechatTable";
	public static final String _VIP_ID = "_vip_id";
	public static final String _NAME = "_name";
	public static final String _REMARK = "_remark";
	
	//vipID号
	@Column(name=_VIP_ID)
	public String vipId="";
	
	//微信号码
	@Column(name=_NAME)
	public String name="";

	//备注
	@Column(name=_REMARK)
	public String remark="";
	
	public VipWechat(JSONObject json) {
		super(json);
		try {
			if(json.has(_VIP_ID)){
				vipId = json.getString(_VIP_ID);
			}
			if(json.has(_NAME)){
				name = json.getString(_NAME);
			}
			if(json.has(_REMARK)){
				remark = json.getString(_REMARK);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	
	public VipWechat(Parcel in) {
		super(in);
		vipId = in.readString();
		name = in.readString();
		remark = in.readString();
	}
	
	public VipWechat() {
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(vipId);
		dest.writeString(name);
		dest.writeString(remark);
	}

	@Override
	public JSONObject getJson() {
		JSONObject mJSONObject = super.getJson();
		try {
			mJSONObject.put(_VIP_ID, vipId);
			mJSONObject.put(_NAME, name);
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
		if(obj instanceof VipWechat){
			VipWechat mVipWechat = (VipWechat) obj;
			if(KansUtils.equals(vipId, mVipWechat.vipId)
					&& KansUtils.equals(name, mVipWechat.name)
					&& KansUtils.equals(remark, mVipWechat.remark)){
				return true;
			}
			return false;
		}
		return super.equals(obj);
	}
}
