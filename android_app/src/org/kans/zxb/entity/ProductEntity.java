package org.kans.zxb.entity;

import org.json.JSONException;
import org.json.JSONObject;
import org.kans.zxb.util.KansUtils;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;
import android.os.Parcel;
import android.util.Log;

@Table(name = ProductEntity.TABLE_NAME)
public class ProductEntity extends DataBase {
	public static final String TABLE_NAME = "ProductEntityTable";
	public static final String _PRICE = "_price";
	public static final String _NAME = "_name";
	public static final String _ICON_PATH = "_icon_path";
	public static final String _CLASS_ID = "_class_id";
	public static final String _PLANAR_CODE = "_planar_code";
	public static final String _REMARK = "_remark";

	//产品名称
	@Column(name=_NAME)
	public String name="";
	
	//产品价格
	@Column(name=_PRICE)
	public double price=0;

	//产品图片路径
	@Column(name=_ICON_PATH)
	public String iconPath="";
	
	//产品类别
	@Column(name=_CLASS_ID)
	public int class_id=-1;

	//条形码
	@Column(name=_PLANAR_CODE)
	public String planarCode="";
	
	//备注
	@Column(name=_REMARK)
	public String remark="";
	
	public ProductEntity(Parcel in) {
		super(in);
		price = in.readDouble();
		name = in.readString();
		iconPath = in.readString();
		class_id = in.readInt();
		planarCode = in.readString();
		remark = in.readString();
	}

	public ProductEntity(JSONObject json) {
		super(json);

		try {
			if(json.has(_PRICE)){
				price = json.getDouble(_PRICE);
			}
			if(json.has(_NAME)){
				name = json.getString(_NAME);
			}
			if(json.has(_ICON_PATH)){
				iconPath = json.getString(_ICON_PATH);
			}
			if(json.has(_CLASS_ID)){
				class_id = json.getInt(_CLASS_ID);
			}
			if(json.has(_PLANAR_CODE)){
				planarCode = json.getString(_PLANAR_CODE);
			}
			if(json.has(_REMARK)){
				remark = json.getString(_REMARK);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public ProductEntity() {
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeDouble(price);
		dest.writeString(name);
		dest.writeString(iconPath);
		dest.writeInt(class_id);
		dest.writeString(planarCode);
		dest.writeString(remark);
	}

	public JSONObject getJson(){
		JSONObject mJSONObject = super.getJson();
		try {
			mJSONObject.put(_PRICE, price);
			mJSONObject.put(_NAME, name);
			mJSONObject.put(_ICON_PATH, iconPath);
			mJSONObject.put(_CLASS_ID, class_id);
			mJSONObject.put(_PLANAR_CODE, planarCode);
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
		if(obj instanceof ProductEntity){
			ProductEntity mProductEntity = (ProductEntity) obj;
			if(KansUtils.equals(name, mProductEntity.name) 
					&& price == mProductEntity.price
					&& KansUtils.equals(iconPath, mProductEntity.iconPath)
					&& class_id == mProductEntity.class_id
					&& KansUtils.equals(planarCode, mProductEntity.planarCode)
					&& KansUtils.equals(remark, mProductEntity.remark)){
				return true;
			}
			return false;
		}
		return super.equals(obj);
	}
}
