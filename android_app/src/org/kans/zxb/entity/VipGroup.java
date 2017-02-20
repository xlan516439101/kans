package org.kans.zxb.entity;

import org.json.JSONException;
import org.json.JSONObject;
import org.kans.zxb.util.KansUtils;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;
import android.os.Parcel;

@Table(name = VipGroup.TABLE_NAME)
public class VipGroup extends DataBase {
	public static final String TABLE_NAME = "GroupTable";
	public static final String _NAME = "_name";
	public static final String _REMARK = "_remark";

	//产品组名 vip分组的组名
	@Column(name=_NAME)
	public String name="";

	//备注
	@Column(name=_REMARK)
	public String remark="";
	
	public VipGroup(JSONObject json) {
		super(json);
		try {
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
	
	public VipGroup(Parcel in) {
		super(in);
		name = in.readString();
		remark = in.readString();
		
	}
	
	public VipGroup() {
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(name);
		dest.writeString(remark);
	}
	
	@Override
	public JSONObject getJson() {
		JSONObject mJSONObject = super.getJson();
		try {
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
		if(obj instanceof VipGroup){
			VipGroup mVipGroup = (VipGroup) obj;
			if(KansUtils.equals(remark, mVipGroup.remark)
					&& KansUtils.equals(remark, mVipGroup.remark)){
				return true;
			}
			return false;
		}
		return super.equals(obj);
	}

}
