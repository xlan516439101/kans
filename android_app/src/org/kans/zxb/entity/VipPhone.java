package org.kans.zxb.entity;

import org.json.JSONException;
import org.json.JSONObject;
import org.kans.zxb.util.KansUtils;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;
import android.os.Parcel;
import android.util.Log;

@Table(name = VipPhone.TABLE_NAME)
public class VipPhone extends DataBase {
	public static final String TABLE_NAME = "VipPhoneTable";
	public static final String _PHONE_NUM = "_phone_num";
	public static final String _VIP_ID = "_vip_id";
	public static final String _REMARK = "_remark";
	public VipPhone(Parcel in) {
		super(in);
		phoneNum = in.readString();
		vipId = in.readString();
		remark = in.readString();
	}
	
	public VipPhone() {
	}
	
	//电话号码
	@Column(name=_PHONE_NUM)
	public String phoneNum="";

	//vipID号
	@Column(name=_VIP_ID)
	public String vipId="";

	//备注
	@Column(name=_REMARK)
	public String remark="";

	public VipPhone(JSONObject json) {
		super(json);
		try {
			if(json.has(_PHONE_NUM)){
				phoneNum = json.getString(_PHONE_NUM);
			}
			if(json.has(_VIP_ID)){
				vipId = json.getString(_VIP_ID);
			}
			if(json.has(_REMARK)){
				remark = json.getString(_REMARK);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(phoneNum);
		dest.writeString(vipId);
		dest.writeString(remark);
		
	}
	

	@Override
	public JSONObject getJson() {
		JSONObject mJSONObject = super.getJson();
		try {
			mJSONObject.put(_PHONE_NUM, phoneNum);
			mJSONObject.put(_VIP_ID, vipId);
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
		if(obj instanceof VipPhone){
			VipPhone mVipPhone = (VipPhone) obj;
			if(KansUtils.equals(phoneNum, mVipPhone.phoneNum)
					&& KansUtils.equals(vipId, mVipPhone.vipId)
					&& KansUtils.equals(remark, mVipPhone.remark)){
				return true;
			}
			return false;
		}
		return super.equals(obj);
	}
	
}
