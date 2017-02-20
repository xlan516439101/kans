package org.kans.zxb.entity;

import org.json.JSONException;
import org.json.JSONObject;
import org.kans.zxb.util.KansUtils;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;
import android.os.Parcel;

@Table(name = VipCredit.TABLE_NAME)
public class VipCredit extends DataBase {
	public static final String TABLE_NAME = "VipCreditTable";
	public static final String _BUY_ID = "_buy_id";
	public static final String _VIP_ID = "_vip_id";
	public static final String _CREDIT = "_credit";
	public static final String _REMARK = "_remark";

	//购买记录的ID号
	@Column(isId=true, name=_BUY_ID)
	public String buy_id="";

	//vipID号
	@Column(name=_VIP_ID)
	public String vipId="";

	//积分
	@Column(name=_CREDIT)
	public int credit=0;

	//备注
	@Column(name=_REMARK)
	public String remark="";
	
	public VipCredit(JSONObject json) {
		super(json);
		try {
			if(json.has(_BUY_ID)){
				buy_id = json.getString(_BUY_ID);
			}
			if(json.has(_VIP_ID)){
				vipId = json.getString(_VIP_ID);
			}
			if(json.has(_CREDIT)){
				credit = json.getInt(_CREDIT);
			}
			if(json.has(_REMARK)){
				remark = json.getString(_REMARK);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public VipCredit(Parcel in) {
		super(in);
		buy_id = in.readString();
		vipId = in.readString();
		credit = in.readInt();
		remark = in.readString();
	}
	
	public VipCredit() {
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);

		dest.writeString(buy_id);
		dest.writeString(vipId);
		dest.writeInt(credit);
		dest.writeString(remark);
	}


	@Override
	public JSONObject getJson() {
		JSONObject mJSONObject = super.getJson();
		try {
			mJSONObject.put(_BUY_ID, buy_id);
			mJSONObject.put(_VIP_ID, vipId);
			mJSONObject.put(_CREDIT, credit);
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
		if(obj instanceof VipCredit){
			VipCredit mVipCredit = (VipCredit) obj;
			if(KansUtils.equals(vipId, mVipCredit.buy_id)
					&& KansUtils.equals(vipId, mVipCredit.vipId)
					&& credit == mVipCredit.credit
					&& KansUtils.equals(remark, mVipCredit.remark)){
				return true;
			}
			return false;
		}
		return super.equals(obj);
	}
}
