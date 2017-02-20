package org.kans.zxb.service;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.IBinder;
import org.kans.zxb.IKService;

public class KServiceMain extends Service implements OnSharedPreferenceChangeListener {

	
	private KRService mKRService;
	private List<IKServiceBranch> mKServiceItems;
	private SharedPreferences mSharedPreferences;

	
	private void initServiceItems(){
		mKServiceItems = new ArrayList<IKServiceBranch>();
		for(Class<?> mClass:IKServiceBranch.itemsClasses){
			try {
				Object itemObj = mClass.newInstance();
				if(itemObj instanceof IKServiceBranch){
					IKServiceBranch item = (IKServiceBranch) itemObj;
					if(item.isLive()){
						mKServiceItems.add(item);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		for(IKServiceBranch item:mKServiceItems){
			item.init(this);
		}
	}
	
	/**
	 * 通过 key来控制服务
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
		for(Class<?> mClass:IKServiceBranch.itemsClasses){
			if(key.equals(mClass.getName())){
				if(sp.getBoolean(key, false)){
					for(IKServiceBranch item:mKServiceItems){
						if(item.getClass().getName().equals(key)){
							mKServiceItems.remove(item);
							break;
						}
					}
				}else{
					boolean hasItem = false;
					for(IKServiceBranch item:mKServiceItems){
						if(item.getClass().getName().equals(key)){
							hasItem = true;
							break;
						}
					}
					if(!hasItem){
						try {
							Object itemObj = mClass.newInstance();
							if(itemObj instanceof IKServiceBranch){
								IKServiceBranch item = (IKServiceBranch) itemObj;
								if(item.isLive()){
									mKServiceItems.add(item);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		if(!isWillLive()){
			stopSelf();
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return mKRService;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		initServiceItems();
		mKRService = new KRService();
		mSharedPreferences = getSharedPreferences(getPackageName()+"_SharedPreferences", Context.MODE_PRIVATE);
		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(!isWillLive()){
			stopSelf();
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	private boolean isWillLive(){
		for(IKServiceBranch item:mKServiceItems){
			if(item.isLive()){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
	}
	
	private class KRService extends IKService.Stub{
		
	}

}
