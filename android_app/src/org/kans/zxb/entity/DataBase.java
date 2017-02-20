package org.kans.zxb.entity;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.db.annotation.Column;

import android.os.Parcel;
import android.os.Parcelable;

public class DataBase implements Parcelable {

	public static final int PRODUCT_CLASS_TYPE = -1;
	public static final int PRODUCT_ENTITY_TYPE = -2;
	public static final int VIP_BUY_TYPE = -3;
	public static final int VIP_CREDIT_TYPE = -4;
	public static final int VIP_GROUP_LINK_TYPE = -5;
	public static final int VIP_GROUP_TYPE = -6;
	public static final int VIP_PHONE_TYPE = -7;
	public static final int VIP_QQ_TYPE = -8;
	public static final int VIP_REMARK_TYPE = -9;
	public static final int VIP_USER_TYPE = -10;
	public static final int VIP_WECHAT_TYPE = -11;
	public static final int KANS_TABLE_TYPE = -12;
	public static final int DATA_BASE_TYPE = -13;

	public static final String _ID = "_id";
	public static final String _LAST_REFRESH_TIME = "_last_refresh_time";

	// 数据库 id
	@Column(isId = true, name = _ID)
	public int id = -1;

	// 最后更改时间
	@Column(name = _LAST_REFRESH_TIME)
	public long lastRefreshTime = 0;
	
	public DataBase(JSONObject json) {
		try {
			if (json.has(_ID)) {
				id = json.getInt(_ID);
			}
			if (json.has(_LAST_REFRESH_TIME)) {
				lastRefreshTime = json.getInt(_LAST_REFRESH_TIME);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public DataBase(Parcel in) {
		id = in.readInt();
		if (id < 0) {
			id = in.readInt();
		}
		lastRefreshTime = in.readLong();
	}

	public DataBase() {
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(getType());
		dest.writeInt(id);
		dest.writeLong(lastRefreshTime);
	}

	@Override
	public final int describeContents() {
		return getType();
	}

	public JSONObject getJson() {
		JSONObject mJSONObject = new JSONObject();
		try {
			mJSONObject.put(_ID, id);
			mJSONObject.put(_LAST_REFRESH_TIME, lastRefreshTime);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return mJSONObject;
	}

	@Override
	public final String toString() {
		return getJson().toString();
	}

	public static DataBase createFromParcel(Parcel source) {
		return CREATOR.createFromParcel(source);
	}

	public final int getType() {
		if (this instanceof VipGroupLink) {
			return VIP_GROUP_LINK_TYPE;
		} else if (this instanceof ProductClass) {
			return PRODUCT_CLASS_TYPE;
		} else if (this instanceof ProductEntity) {
			return PRODUCT_ENTITY_TYPE;
		} else if (this instanceof VipBuy) {
			return VIP_BUY_TYPE;
		} else if (this instanceof VipBuy) {
			return VIP_CREDIT_TYPE;
		} else if (this instanceof VipGroup) {
			return VIP_GROUP_TYPE;
		} else if (this instanceof VipPhone) {
			return VIP_PHONE_TYPE;
		} else if (this instanceof VipQQ) {
			return VIP_QQ_TYPE;
		} else if (this instanceof VipRemark) {
			return VIP_REMARK_TYPE;
		} else if (this instanceof VipUser) {
			return VIP_USER_TYPE;
		} else if (this instanceof VipWechat) {
			return VIP_WECHAT_TYPE;
		} else if(this instanceof KansTable){
			return KANS_TABLE_TYPE;
		}else {
			return DATA_BASE_TYPE;
		}
	}

	public static final Parcelable.Creator<DataBase> CREATOR = new Parcelable.Creator<DataBase>() {
		public DataBase createFromParcel(Parcel in) {
			switch (in.readInt()) {
			case VIP_GROUP_LINK_TYPE:

				return new VipGroup(in);
			case PRODUCT_CLASS_TYPE:

				return new ProductClass(in);
			case PRODUCT_ENTITY_TYPE:

				return new ProductEntity(in);
			case VIP_BUY_TYPE:

				return new VipBuy(in);
			case VIP_CREDIT_TYPE:

				return new VipCredit(in);
			case VIP_GROUP_TYPE:

				return new VipGroup(in);
			case VIP_PHONE_TYPE:
				
				return new VipPhone(in);
			case VIP_QQ_TYPE:
				
				return new VipQQ(in);
			case VIP_REMARK_TYPE:

				return new VipRemark(in);
			case VIP_USER_TYPE:

				return new VipUser(in);
			case VIP_WECHAT_TYPE:

				return new VipWechat(in);
			case KANS_TABLE_TYPE:

				return new KansTable(in);

			default:
				return new DataBase(in);
			}
		}

		public DataBase[] newArray(int size) {
			return new DataBase[size];
		}
	};

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof DataBase){
			DataBase mDataBase = (DataBase) obj;
			if(mDataBase.id == id){
				return true;
			}else{
				return false;
			}
		}
		return super.equals(obj);
	}
}
