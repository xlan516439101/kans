package org.kans.zxb;

import java.io.File;
import java.util.List;

import org.kans.zxb.entity.VipGroup;
import org.kans.zxb.entity.ProductClass;
import org.xutils.DbManager;
import org.xutils.DbManager.DbOpenListener;
import org.xutils.DbManager.TableCreateListener;
import org.xutils.db.table.TableEntity;
import org.xutils.ex.DbException;
import org.xutils.x;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

public class KApplication extends Application {
	public static DbManager.DaoConfig localDaoConfig;
	public static final String SHARED_PREFERENCE_NAME = "kans_SharedPreferences";
	public static final String IS_FIRST_START_APP = "is_first_start_app";
	public static File rootFilePath;
	public static File dbFilePath;
	public static final boolean DROP_DB = false;
	static {
		rootFilePath = new File(Environment.getExternalStorageDirectory(), "kans");
		if (!rootFilePath.exists()) {
			rootFilePath.mkdirs();
		}
		dbFilePath = new File(rootFilePath, "db");
		if (!dbFilePath.exists()) {
			dbFilePath.mkdirs();
		}
	}

	public void onCreate() {
		super.onCreate();
		x.Ext.init(this);
		localDaoConfig = new DbManager.DaoConfig().setDbVersion(1).setDbName("kans.db")
		// .setDbDir(dbFilePath)
				.setAllowTransaction(true).setDbOpenListener(new DbOpenListener() {

					@Override
					public void onDbOpened(DbManager db) {
						db.getDatabase().enableWriteAheadLogging();
					}
				}).setTableCreateListener(new TableCreateListener() {

					@Override
					public void onTableCreated(DbManager db, TableEntity<?> table) {
						initTable(db, table);
					}
				}).setDbUpgradeListener(new DbManager.DbUpgradeListener() {
					@Override
					public void onUpgrade(DbManager db, int oldVersion, int newVersion) {
						// TODO: ...
						// db.addColumn(...);
						// db.dropTable(...);
						// ...
						// or
						// db.dropDb();
					}
				});
		try {
			initDb(this);
		} catch (DbException e) {
			e.printStackTrace();
		}
	}

	private void initTable(DbManager db, TableEntity<?> table) {
		Log.i("xlan", table.getName());
	}

	public static void initDb(Context context) throws DbException {
		DbManager mDbManager = x.getDb(KApplication.localDaoConfig);
		if(DROP_DB){
			mDbManager.dropDb();
		}
		List<ProductClass> mProductClasses = mDbManager.selector(ProductClass.class).findAll();
		// 添加默认产品类别
		if(mProductClasses == null || mProductClasses.size() == 0){
			String[] productClassArray = context.getResources().getStringArray(R.array.ProductClassArray);
			for (String name : productClassArray) {
				ProductClass mItem = new ProductClass();
				mItem.lastRefreshTime = System.currentTimeMillis();
				mItem.name = name;
				mItem.remark = name;
				mDbManager.saveBindingId(mItem);
			}
		}

		List<VipGroup> mVipGroups = mDbManager.selector(VipGroup.class).findAll();
		// 添加默认组名
		if(mVipGroups == null || mVipGroups.size() == 0){
			String[] groupTableArray = context.getResources().getStringArray(R.array.GroupTableArray);
			for (String name : groupTableArray) {
				VipGroup mItem = new VipGroup();
				mItem.lastRefreshTime = System.currentTimeMillis();
				mItem.name = name;
				mItem.remark = name;
				mDbManager.saveBindingId(mItem);
			}
		}
	}
}
