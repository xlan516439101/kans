package org.kans.zxb.entity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kans.zxb.util.KansUtils;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;
import android.os.Parcel;
import android.os.Parcelable;

@Table(name = VipUser.TABLE_NAME)
public class VipUser extends DataBase {
	public static final String TABLE_NAME = "VipUserTable";
	public static final String _VIP_ID = "_vip_id";
	public static final String _NAME = "_name";
	public static final String _SEXUALITY = "_sexuality";
	public static final String _FAVORITE = "_favorite";
	public static final String _ICON_PATH = "_icon_path";
	public static final String _BIRTHDAY_ISLUNAR = "_birthday_isLunar";
	public static final String _BIRTHDAY = "_birthday";
	public static final String _CITY_NAME = "_city_name";
	public static final String _EMAIL = "_email";
	public static final String _CREDIT = "_credit";
	public static final String _GROUP_LIST = "_group_List";
	public static final String _PHONE_LIST = "_phone_List";
	public static final String _WECHAT_LIST = "_wechat_list";
	public static final String _QQ_LIST = "_qq_list";
	public static final String _REMARK_LIST = "_remark_list";
	public static final String _BUY_DATA_LIST = "_buy_data_list";
	//vipID号 数字字母
	@Column(name=_VIP_ID)
	public String vipId="";

	//名字
	@Column(name=_NAME)
	public String name="";
	
	//性别 男1女0
	@Column(name=_SEXUALITY)
	public int sexuality=0;


	//是否收藏 1|0
	@Column(name=_FAVORITE)
	public int favorite=0;

	//icon路径
	@Column(name=_ICON_PATH)
	public String iconPath="";

	//是否为农历
	@Column(name=_BIRTHDAY_ISLUNAR)
	public int birthday_isLunar=0;
	
	//生日  月 日
	@Column(name=_BIRTHDAY)
	public String birthday="";

	//籍贯
	@Column(name=_CITY_NAME)
	public String cityName="";
	
	//email
	@Column(name= _EMAIL)
	public String email="";
	
	//积分
	@Column(name=_CREDIT)
	public int credit= 0;
	
	//所属组 多个
	public List<VipGroup> groupList = new ArrayList<VipGroup>();
	
	//电话号码
	public List<VipPhone> phoneList = new ArrayList<VipPhone>();
	
	//微信号码
	public List<VipWechat> wechatList = new ArrayList<VipWechat>();
	
	//qq号码
	public List<VipQQ> qqList = new ArrayList<VipQQ>();

	
	
	
	//购买记录
	public List<VipBuy> buyDataList = new ArrayList<VipBuy>();
	
	//备注
	public List<VipRemark> remarkList = new ArrayList<VipRemark>();
	

	static ClassLoader mClassLoader;
	static{
		mClassLoader = ClassLoader.getSystemClassLoader();
	}
	
	public VipUser(JSONObject json) {
		super(json);
		try {
			if(json.has(_VIP_ID)){
				vipId = json.getString(_VIP_ID);
			}
			if(json.has(_NAME)){
				name = json.getString(_NAME);
			}
			if(json.has(_SEXUALITY)){
				sexuality = json.getInt(_SEXUALITY);
			}
			if(json.has(_FAVORITE)){
				favorite = json.getInt(_FAVORITE);
			}
			if(json.has(_ICON_PATH)){
				iconPath = json.getString(_ICON_PATH);
			}
			if(json.has(_BIRTHDAY_ISLUNAR)){
				birthday_isLunar = json.getInt(_BIRTHDAY_ISLUNAR);
			}
			if(json.has(_BIRTHDAY)){
				birthday = json.getString(_BIRTHDAY);
			}
			if(json.has(_CITY_NAME)){
				cityName = json.getString(_CITY_NAME);
			}
			if(json.has(_EMAIL)){
				email = json.getString(_EMAIL);
			}
			if(json.has(_CREDIT)){
				credit = json.getInt(_CREDIT);
			}
			if(json.has(_GROUP_LIST)){
				setGroupTable(json.getJSONArray(_GROUP_LIST));
			}
			if(json.has(_PHONE_LIST)){
				setVipPhone(json.getJSONArray(_PHONE_LIST));
			}
			if(json.has(_WECHAT_LIST)){
				setVipWechat(json.getJSONArray(_WECHAT_LIST));
			}
			if(json.has(_QQ_LIST)){
				setVipQQ(json.getJSONArray(_QQ_LIST));
			}
			if(json.has(_REMARK_LIST)){
				setVipRemark(json.getJSONArray(_REMARK_LIST));
			}
			if(json.has(_BUY_DATA_LIST)){
				setVipBuy(json.getJSONArray(_BUY_DATA_LIST));
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public VipUser(Parcel in) {
		super(in);
		vipId = in.readString();
		name = in.readString();
		sexuality = in.readInt();
		favorite = in.readInt();
		iconPath = in.readString();
		birthday_isLunar = in.readInt();
		birthday = in.readString();
		cityName = in.readString();
		email = in.readString();
		credit = in.readInt();
		Parcelable[] groupParcelables =in.readParcelableArray(mClassLoader);
		for(int i=0;i<groupParcelables.length;i++){
			groupList.add((VipGroup)groupParcelables[i]);
		}
		Parcelable[] phoneParcelables =in.readParcelableArray(mClassLoader);
		for(int i=0;i<phoneParcelables.length;i++){
			phoneList.add((VipPhone)phoneParcelables[i]);
		}
		Parcelable[] wechatParcelables =in.readParcelableArray(mClassLoader);
		for(int i=0;i<wechatParcelables.length;i++){
			wechatList.add((VipWechat)wechatParcelables[i]);
		}
		Parcelable[] qqParcelables =in.readParcelableArray(mClassLoader);
		for(int i=0;i<qqParcelables.length;i++){
			qqList.add((VipQQ)qqParcelables[i]);
		}
		Parcelable[] remarkParcelables =in.readParcelableArray(mClassLoader);
		for(int i=0;i<remarkParcelables.length;i++){
			remarkList.add((VipRemark)remarkParcelables[i]);
		}
		Parcelable[] buyDataParcelables =in.readParcelableArray(mClassLoader);
		for(int i=0;i<buyDataParcelables.length;i++){
			buyDataList.add((VipBuy)buyDataParcelables[i]);
		}
	}
	
	public VipUser() {
		
	}
	
	protected void setGroupTable(JSONArray mJSONArray){
		for(int i=0;i<mJSONArray.length();i++){
			try {
				JSONObject mObj = (JSONObject) mJSONArray.get(i);
				VipGroup mVipGroup = new VipGroup(mObj);
				groupList.add(mVipGroup);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected void setVipPhone(JSONArray mJSONArray){
		for(int i=0;i<mJSONArray.length();i++){
			try {
				JSONObject mObj = (JSONObject) mJSONArray.get(i);
				VipPhone mVipPhone = new VipPhone(mObj);
				phoneList.add(mVipPhone);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	protected void setVipWechat(JSONArray mJSONArray){
		for(int i=0;i<mJSONArray.length();i++){
			try {
				JSONObject mObj = (JSONObject) mJSONArray.get(i);
				VipWechat mVipWechat = new VipWechat(mObj);
				wechatList.add(mVipWechat);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected void setVipQQ(JSONArray mJSONArray){
		for(int i=0;i<mJSONArray.length();i++){
			try {
				JSONObject mObj = (JSONObject) mJSONArray.get(i);
				VipQQ mVipQQ = new VipQQ(mObj);
				qqList.add(mVipQQ);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	protected void setVipRemark(JSONArray mJSONArray){
		for(int i=0;i<mJSONArray.length();i++){
			try {
				JSONObject mObj = (JSONObject) mJSONArray.get(i);
				VipRemark mVipRemark = new VipRemark(mObj);
				remarkList.add(mVipRemark);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	protected void setVipBuy(JSONArray mJSONArray){
		for(int i=0;i<mJSONArray.length();i++){
			try {
				JSONObject mObj = (JSONObject) mJSONArray.get(i);
				VipBuy mVipBuy = new VipBuy(mObj);
				buyDataList.add(mVipBuy);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(vipId);
		dest.writeString(name);
		dest.writeInt(sexuality);
		dest.writeInt(favorite);
		dest.writeString(iconPath);
		dest.writeInt(birthday_isLunar);
		dest.writeString(birthday);
		dest.writeString(cityName);
		dest.writeString(email);
		dest.writeInt(credit);
		
		Parcelable[] groupParcelables = new Parcelable[groupList.size()];
		for(int i=0;i<groupList.size();i++){
			groupParcelables[i] = groupList.get(i);
		}
		dest.writeParcelableArray(groupParcelables, 0);
		
		Parcelable[] phoneParcelables = new Parcelable[phoneList.size()];
		for(int i=0;i<phoneList.size();i++){
			phoneParcelables[i] = phoneList.get(i);
		}
		dest.writeParcelableArray(phoneParcelables, 0);

		Parcelable[] wechatParcelables = new Parcelable[wechatList.size()];
		for(int i=0;i<wechatList.size();i++){
			wechatParcelables[i] = wechatList.get(i);
		}
		dest.writeParcelableArray(wechatParcelables, 0);

		Parcelable[] qqParcelables = new Parcelable[qqList.size()];
		for(int i=0;i<qqList.size();i++){
			qqParcelables[i] = qqList.get(i);
		}
		dest.writeParcelableArray(qqParcelables, 0);

		Parcelable[] remarkParcelables = new Parcelable[remarkList.size()];
		for(int i=0;i<remarkList.size();i++){
			remarkParcelables[i] = remarkList.get(i);
		}
		dest.writeParcelableArray(remarkParcelables, 0);

		Parcelable[] buyDataParcelables = new Parcelable[buyDataList.size()];
		for(int i=0;i<buyDataList.size();i++){
			buyDataParcelables[i] = buyDataList.get(i);
		}
		dest.writeParcelableArray(buyDataParcelables, 0);
	}


	@Override
	public JSONObject getJson() {
		JSONObject mJSONObject = super.getJson();
		try {
			mJSONObject.put(_VIP_ID, vipId);
			mJSONObject.put(_NAME, name);
			mJSONObject.put(_SEXUALITY, sexuality);
			mJSONObject.put(_FAVORITE, favorite);
			mJSONObject.put(_ICON_PATH, iconPath);
			mJSONObject.put(_BIRTHDAY_ISLUNAR, birthday_isLunar);
			mJSONObject.put(_BIRTHDAY, birthday);
			mJSONObject.put(_CITY_NAME, cityName);
			mJSONObject.put(_EMAIL, email);
			mJSONObject.put(_CREDIT, credit);
			
			JSONArray groupListJson = new JSONArray();
			for(int i = 0 ; i < groupList.size() ; i++){
				groupListJson.put(i, groupList.get(i).getJson());
			}
			mJSONObject.put(_GROUP_LIST, groupListJson);
			
			JSONArray phoneListJson = new JSONArray();
			for(int i = 0 ; i < phoneList.size() ; i++){
				phoneListJson.put(i, phoneList.get(i).getJson());
			}
			mJSONObject.put(_PHONE_LIST, phoneListJson);

			JSONArray wechatListJson = new JSONArray();
			for(int i = 0 ; i < wechatList.size() ; i++){
				wechatListJson.put(i, wechatList.get(i).getJson());
			}
			mJSONObject.put(_WECHAT_LIST, wechatListJson);

			JSONArray qqListJson = new JSONArray();
			for(int i = 0 ; i < qqList.size() ; i++){
				qqListJson.put(i, qqList.get(i).getJson());
			}
			mJSONObject.put(_QQ_LIST, qqListJson);

			JSONArray remarkListJson = new JSONArray();
			for(int i = 0 ; i < remarkList.size() ; i++){
				remarkListJson.put(i, remarkList.get(i).getJson());
			}
			mJSONObject.put(_REMARK_LIST, remarkListJson);

			JSONArray buyDataListJson = new JSONArray();
			for(int i = 0 ; i < buyDataList.size() ; i++){
				buyDataListJson.put(i, buyDataList.get(i).getJson());
			}
			mJSONObject.put(_BUY_DATA_LIST, buyDataListJson);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return mJSONObject;
	}

	public boolean equalsVipUser(VipUser mVipUser){
		if(vipId == mVipUser.vipId
				&& KansUtils.equals(name, mVipUser.name)
				&& favorite == mVipUser.favorite
				&& KansUtils.equals(iconPath, mVipUser.iconPath)
				&& KansUtils.equals(birthday, mVipUser.birthday)
				&& KansUtils.equals(email, mVipUser.email)
				&& credit == mVipUser.credit){
			return true;
		}else{
			return false;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!super.equals(obj)){
			return false;
		}
		if(obj instanceof VipUser){
			VipUser mVipUser = (VipUser) obj;
			if(vipId == mVipUser.vipId
					&& KansUtils.equals(name, mVipUser.name)
					&& favorite == mVipUser.favorite
					&& KansUtils.equals(iconPath, mVipUser.iconPath)
					&& KansUtils.equals(birthday, mVipUser.birthday)
					&& KansUtils.equals(email, mVipUser.email)
					&& credit == mVipUser.credit){
				if(groupList.size() == mVipUser.groupList.size()){
					for(int i=0;i<groupList.size();i++){
						boolean has = false;
						for(int j=0;j<mVipUser.groupList.size();j++){
							if(groupList.get(i).equals(mVipUser.groupList.get(j))){
								has = true;
								break;
							}
						}
						if(!has){
							return false;
						}
					}
				}else{
					return false;
				}
				if(phoneList.size() == mVipUser.phoneList.size()){
					for(int i=0;i<phoneList.size();i++){
						boolean has = false;
						for(int j=0;j<mVipUser.phoneList.size();j++){
							if(phoneList.get(i).equals(mVipUser.phoneList.get(j))){
								has = true;
								break;
							}
						}
						if(!has){
							return false;
						}
					}
				}else{
					return false;
				}
				if(wechatList.size() == mVipUser.wechatList.size()){
					for(int i=0;i<wechatList.size();i++){
						boolean has = false;
						for(int j=0;j<mVipUser.wechatList.size();j++){
							if(wechatList.get(i).equals(mVipUser.wechatList.get(j))){
								has = true;
								break;
							}
						}
						if(!has){
							return false;
						}
					}
				}else{
					return false;
				}
				if(qqList.size() == mVipUser.qqList.size()){
					for(int i=0;i<qqList.size();i++){
						boolean has = false;
						for(int j=0;j<mVipUser.qqList.size();j++){
							if(qqList.get(i).equals(mVipUser.qqList.get(j))){
								has = true;
								break;
							}
						}
						if(!has){
							return false;
						}
					}
				}else{
					return false;
				}
				if(buyDataList.size() == mVipUser.buyDataList.size()){
					for(int i=0;i<buyDataList.size();i++){
						boolean has = false;
						for(int j=0;j<mVipUser.buyDataList.size();j++){
							if(buyDataList.get(i).equals(mVipUser.buyDataList.get(j))){
								has = true;
								break;
							}
						}
						if(!has){
							return false;
						}
					}
				}else{
					return false;
				}
				if(remarkList.size() == mVipUser.remarkList.size()){
					for(int i=0;i<remarkList.size();i++){
						boolean has = false;
						for(int j=0;j<mVipUser.remarkList.size();j++){
							if(remarkList.get(i).equals(mVipUser.remarkList.get(j))){
								has = true;
								break;
							}
						}
						if(!has){
							return false;
						}
					}
				}else{
					return false;
				}
				return true;
			}
			return false;
		}
		return super.equals(obj);
	}
	
}
