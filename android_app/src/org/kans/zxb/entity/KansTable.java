package org.kans.zxb.entity;

import org.json.JSONException;
import org.json.JSONObject;
import org.kans.zxb.KApplication;
import org.kans.zxb.util.KansUtils;
import org.xutils.DbManager;
import org.xutils.x;
import org.xutils.common.util.KeyValue;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;

import android.os.Parcel;

@Table(name = KansTable.TABLE_NAME)
public class KansTable extends DataBase {
	public static final String TABLE_NAME = "KansTable";
	public static final String _NAME = "_name";
	public static final String _ICON_PATH = "_icon_path";
	public static final String _PHONE = "_phone";
	public static final String _EMAIL = "_email";
	public static final String _REMARK = "_remark";

	
	//名字
	@Column(name=_NAME)
	public String name="kans";
	
	//phone
	@Column(name=_PHONE)
	public String phone="88888888";
	
	//email
	@Column(name=_EMAIL)
	public String email="666666@qq.com";

	//icon
	@Column(name=_ICON_PATH)
	public String iconPath="";

	//json 方式保存key-value值
	@Column(name=_REMARK)
	public String remark="";
	
	public String get(String key,String defaultValue){
		try {
			JSONObject json = new JSONObject(remark);
			return json.getString(key);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return defaultValue;
	}
	
	public void put(String key,String value){
		JSONObject json;
		try {
			json = new JSONObject(remark);
		} catch (JSONException e) {
			json = new JSONObject();
			e.printStackTrace();
		}
		try {
			json.put(value, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if(json != null){
			
			try {
				DbManager mDbManager = x.getDb(KApplication.localDaoConfig);
				if(id>0){
					mDbManager.update(KansTable.class, WhereBuilder.b("id", "=", id), new KeyValue(_REMARK, json.toString()));
				}else{
					mDbManager.saveBindingId(this);
				}
			} catch (DbException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public KansTable(JSONObject json) {
		super(json);
		try {
			if(json.has(_NAME)){
				name = json.getString(_NAME);
			}
			if(json.has(_PHONE)){
				phone = json.getString(_PHONE);
			}
			if(json.has(_EMAIL)){
				email = json.getString(_EMAIL);
			}
			if(json.has(_ICON_PATH)){
				iconPath = json.getString(_ICON_PATH);
			}
			if(json.has(_REMARK)){
				remark = json.getString(_REMARK);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	
	public KansTable(Parcel in) {
		super(in);
		name = in.readString();
		phone = in.readString();
		email = in.readString();
		iconPath = in.readString();
		remark = in.readString();
	}
	
	public KansTable() {
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(name);
		dest.writeString(phone);
		dest.writeString(email);
		dest.writeString(iconPath);
		dest.writeString(remark);
	}

	@Override
	public JSONObject getJson() {
		JSONObject mJSONObject = super.getJson();
		try {
			mJSONObject.put(_NAME, name);
			mJSONObject.put(_PHONE, phone);
			mJSONObject.put(_EMAIL, email);
			mJSONObject.put(_ICON_PATH, iconPath);
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
		if(obj instanceof KansTable){
			KansTable mKansTable = (KansTable) obj;
			if(KansUtils.equals(name, mKansTable.name)
					&& KansUtils.equals(phone, mKansTable.phone)
					&& KansUtils.equals(email, mKansTable.email)
					&& KansUtils.equals(iconPath, mKansTable.iconPath)
					&& KansUtils.equals(remark, mKansTable.remark)){
				return true;
			}
			return false;
		}
		return super.equals(obj);
	}
}
