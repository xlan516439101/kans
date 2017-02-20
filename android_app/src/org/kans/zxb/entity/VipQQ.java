package org.kans.zxb.entity;

import org.json.JSONException;
import org.json.JSONObject;
import org.kans.zxb.util.KansUtils;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;
import android.os.Parcel;

@Table(name = VipQQ.TABLE_NAME)
public class VipQQ extends DataBase {
	public static final String TABLE_NAME = "VipQQTable";
	public static final String _VIP_ID = "_vip_id";
	public static final String _NAME = "_name";
	public static final String _REMARK = "_remark";

	//vipID号
	@Column(name=_VIP_ID)
	public String vipId="";
	
	//qq号码
	@Column(name=_NAME)
	public String name="";

	//备注
	@Column(name=_REMARK)
	public String remark="";
	
	public VipQQ(JSONObject json) {
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
	
	
	public VipQQ(Parcel in) {
		super(in);
		vipId = in.readString();
		name = in.readString();
		remark = in.readString();
	}
	
	public VipQQ() {
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
		if(obj instanceof VipQQ){
			VipQQ mVipQQ = (VipQQ) obj;
			if(KansUtils.equals(vipId, mVipQQ.vipId)
					&& KansUtils.equals(name, mVipQQ.name)
					&& KansUtils.equals(remark, mVipQQ.remark)){
				return true;
			}
			return false;
		}
		return super.equals(obj);
	}
}
