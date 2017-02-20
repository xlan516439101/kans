package org.kans.zxb.entity;

import org.json.JSONException;
import org.json.JSONObject;
import org.kans.zxb.util.KansUtils;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;
import android.os.Parcel;

@Table(name = VipBuy.TABLE_NAME)
public class VipBuy extends DataBase {
	public static final String TABLE_NAME = "VipBuyTable";
	public static final String _PRODUCT_ID = "_product_id";
	public static final String _VIP_ID = "_vip_id";
	public static final String _BUY_DATE = "_buy_date";
	public static final String _PRICE = "_price";
	public static final String _REMARK = "_remark";

	//使用产品ID
	@Column(name=_PRODUCT_ID)
	public int product_id = -1;

	//vip ID
	@Column(name=_VIP_ID)
	public String vipId="";

	//购买时间
	@Column(name=_BUY_DATE)
	public long buy_date=0;

	//价格
	@Column(name=_PRICE)
	public double price=0;

	//备注
	@Column(name=_REMARK)
	String remark="";

	public VipBuy(JSONObject json) {
		super(json);
		try {
			if(json.has(_PRODUCT_ID)){
				product_id = json.getInt(_PRODUCT_ID);
			}
			if(json.has(_VIP_ID)){
				vipId = json.getString(_VIP_ID);
			}
			if(json.has(_BUY_DATE)){
				buy_date = json.getLong(_BUY_DATE);
			}
			if(json.has(_PRICE)){
				price = json.getDouble(_PRICE);
			}
			if(json.has(_REMARK)){
				remark = json.getString(_REMARK);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public VipBuy(Parcel in) {
		super(in);
		product_id = in.readInt();
		vipId = in.readString();
		buy_date = in.readLong();
		price = in.readDouble();
		remark = in.readString();
	}
	
	public VipBuy() {
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);

		dest.writeInt(product_id);
		dest.writeString(vipId);
		dest.writeDouble(buy_date);
		dest.writeDouble(price);
		dest.writeString(remark);
	}


	@Override
	public JSONObject getJson() {
		JSONObject mJSONObject = super.getJson();
		try {
			mJSONObject.put(_PRODUCT_ID, product_id);
			mJSONObject.put(_VIP_ID, vipId);
			mJSONObject.put(_BUY_DATE, buy_date);
			mJSONObject.put(_PRICE, price);
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
		if(obj instanceof VipBuy){
			VipBuy mVipBuy = (VipBuy) obj;
			if(product_id == mVipBuy.product_id
					&& KansUtils.equals(vipId, mVipBuy.vipId)
					&& buy_date == mVipBuy.buy_date
					&& price == mVipBuy.price
					&& KansUtils.equals(remark, mVipBuy.remark)){
				return true;
			}
			return false;
		}
		return super.equals(obj);
	}
	
}
