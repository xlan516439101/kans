package org.kans.zxb.entity;

import org.json.JSONException;
import org.json.JSONObject;
import org.kans.zxb.util.KansUtils;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;
import android.os.Parcel;

@Table(name = VipRemark.TABLE_NAME)
public class VipRemark extends DataBase {
	public static final String TABLE_NAME = "VipRemarkTable";
	public static final String _VIP_ID = "_vip_id";
	public static final String _ICON_PATH = "_icon_path";
	public static final String _SOUND_PATH = "_sound_path";
	public static final String _CONTENT = "_content";
	public static final String _ALARM_CLOCK = "_alarm_clock";
	
	//vipID号
	@Column(name=_VIP_ID)
	public String vipId="";
	
	//备注内容
	@Column(name=_CONTENT)
	public String content="";

	//备注图片
	@Column(name=_ICON_PATH)
	public String iconPath="";
	
	//备注声音
	@Column(name=_SOUND_PATH)
	public String soundPath="";
	
	//闹钟时间
	@Column(name=_ALARM_CLOCK)
	public long alarmClock=0;
	
	public VipRemark(JSONObject json) {
		super(json);
		try {
			if(json.has(_VIP_ID)){
				vipId = json.getString(_VIP_ID);
			}
			if(json.has(_CONTENT)){
				content = json.getString(_CONTENT);
			}
			if(json.has(_ICON_PATH)){
				iconPath = json.getString(_ICON_PATH);
			}
			if(json.has(_SOUND_PATH)){
				soundPath = json.getString(_SOUND_PATH);
			}
			if(json.has(_ALARM_CLOCK)){
				alarmClock = json.getLong(_ALARM_CLOCK);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public VipRemark(Parcel in) {
		super(in);
		vipId = in.readString();
		content = in.readString();
		iconPath = in.readString();
		soundPath = in.readString();
		alarmClock = in.readLong();
	}
	
	public VipRemark() {
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(vipId);
		dest.writeString(content);
		dest.writeString(iconPath);
		dest.writeString(soundPath);
		dest.writeLong(alarmClock);
	}
	
	@Override
	public JSONObject getJson() {
		JSONObject mJSONObject = super.getJson();
		try {
			mJSONObject.put(_VIP_ID, vipId);
			mJSONObject.put(_CONTENT, content);
			mJSONObject.put(_ICON_PATH, iconPath);
			mJSONObject.put(_SOUND_PATH, soundPath);
			mJSONObject.put(_ALARM_CLOCK, alarmClock);
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
		if(obj instanceof VipRemark){
			VipRemark mVipRemark = (VipRemark) obj;
			if(KansUtils.equals(vipId, mVipRemark.vipId)
					&& KansUtils.equals(content, mVipRemark.content)
					&& KansUtils.equals(iconPath, mVipRemark.iconPath)
					&& KansUtils.equals(soundPath, mVipRemark.soundPath)
					&& alarmClock == mVipRemark.alarmClock){
				return true;
			}
			return false;
		}
		return super.equals(obj);
	}
}
