package org.kans.zxb.presenter;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.kans.zxb.KApplication;
import org.kans.zxb.KUi;
import org.kans.zxb.R;
import org.kans.zxb.entity.ProductClass;
import org.kans.zxb.fragment.VipRemarkFragment;
import org.kans.zxb.fragment.VipRemarkFragment.ViewHold;
import org.kans.zxb.util.KansUtils;
import org.kans.zxb.view.KItemView;
import org.xutils.DbManager;
import org.xutils.x;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Toast;

public class VipRemarkPresenter extends KPresenter<VipRemarkPresenter.Ui> implements OnScrollListener {

	public static final int REQUEST_ICON_CODE = 0x101;

	private MediaPlayer mediaPlayer;
	private MediaRecorder mediaRecorder;

	private File file;
	public interface Ui extends KUi {
		VipRemarkFragment getUi();
		Context getContext();
		ViewHold getViewHold();
		void hidePopuView();
		void startActivityForResult(Intent intent, int requestCode);
		void showIconSelect();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		updateData();
	}

	@Override
	public void onStart() {
		super.onStart();
		x.task().postDelayed(new Runnable() {
			@Override
			public void run() {
				//KansUtils.setImeVisibility(false, getUi().getContext(), getUi().getViewHold().mKItemView);
			}
		}, 200);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void updateData() {
		x.task().run(new Runnable() {
			@Override
			public void run() {
				DbManager manager = x.getDb(KApplication.localDaoConfig);
				try {
					final List<ProductClass> mProductClasses = manager.selector(ProductClass.class).orderBy(ProductClass._ID).findAll();
				} catch (DbException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private void stopAudio(){
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
		}
	}
	
	private void playAudio() {
		if (mediaPlayer == null) {
			mediaPlayer = new MediaPlayer();
		}
		try {
			mediaPlayer.reset();
			mediaPlayer.setDataSource(file.getAbsolutePath());
			mediaPlayer.prepare();
			mediaPlayer.start();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	boolean isRecorder = false;
	
	@Override
	public void onClick(View v) {
		Object tag = v.getTag();
		switch (v.getId()) {
		case R.id.roduct_icon:
			getUi().showIconSelect();
			break;
		case R.id.camera:
			getUi().hidePopuView();
			Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			getUi().startActivityForResult(cameraIntent, REQUEST_ICON_CODE);
			break;
		case R.id.gallery:
			getUi().hidePopuView();
			Intent galleryIntent = new Intent(Intent.ACTION_PICK);
			galleryIntent.setType("image/*");
			getUi().startActivityForResult(galleryIntent, REQUEST_ICON_CODE);
			break;
		case R.id.vip_remark_video_button:
			Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
			File out = new File("video_path");
			if(!out.exists()){
				out.mkdirs();
			}
			videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(out));
			videoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
			videoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 20);
			videoIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 640*480);
			getUi().startActivityForResult(videoIntent, REQUEST_ICON_CODE);
			break;
		case R.id.cancel:
			getUi().hidePopuView();
			break;
			
			
		case R.id.add_product_class_button:
			if (mediaPlayer != null) {
				if (mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition() > 500) {
				} else {
				}
			}
			break;

		case R.id.del_item:
			if(!isRecorder){
				stopAudio();
				isRecorder = true;
				if(mediaRecorder != null){
					mediaRecorder.stop();
					mediaRecorder.release();
					mediaRecorder = null;
				}
				try {
					mediaRecorder = new MediaRecorder();
					mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
					mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
					mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
					mediaRecorder.setOutputFile(file.getAbsolutePath());
					mediaRecorder.prepare();
					mediaRecorder.start();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}else{
				mediaRecorder.stop();
				mediaRecorder.release();
				mediaRecorder = null;
				isRecorder = false;
			}
			break;

		default:
			break;

		}
	}

	public Bitmap getVideoThumbnail(String path){
		if(new File(path).exists()){
			return KansUtils.getVideoThumbnail(path);
		}
		return null;
	}
	
	public void addOrUpdateProductClass(final ProductClass mProductClass) {
		if (mProductClass != null) {
			x.task().run(new Runnable() {

				@Override
				public void run() {
					mProductClass.lastRefreshTime = System.currentTimeMillis();
					DbManager manager = x.getDb(KApplication.localDaoConfig);
					try {
						manager.saveOrUpdate(mProductClass);
						updateData();
					} catch (DbException e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	public void delProductClass(final ProductClass mProductClass) {
		if (mProductClass != null) {
			x.task().run(new Runnable() {

				@Override
				public void run() {
					DbManager manager = x.getDb(KApplication.localDaoConfig);
					try {
						if (manager.selector(ProductClass.class).findAll().size() > 1) {
							int count = manager.delete(ProductClass.class, WhereBuilder.b().and(ProductClass._NAME, "=", mProductClass.name));
							updateData();
							Log.i("xlan", "del:" + mProductClass.name + " count:" + count);
						} else {
							x.task().post(new Runnable() {

								@Override
								public void run() {
									Toast.makeText(getUi().getContext(), R.string.kans_del_keep_one, Toast.LENGTH_SHORT).show();
								}
							});
						}
					} catch (DbException e) {
						e.printStackTrace();
					}
				}
			});
		}

	}


	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (OnScrollListener.SCROLL_STATE_TOUCH_SCROLL == scrollState) {
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.i("xlan", "requestCode:" + requestCode + " resultCode:" + resultCode + " data:" + data.getData());
		if (REQUEST_ICON_CODE == requestCode && resultCode == Activity.RESULT_OK) {
			if (data != null) {
				if (data.getData() != null) {
					saveIconBitmap(data.getData());
				} else if (data.getExtras() != null) {
					Bundle mBundle = data.getExtras();
					if (mBundle != null) {
						saveIconBitmap((Bitmap) mBundle.get("data"));
					}
				}
			}
		}
	}

	private void saveIconBitmap(Uri mUri) {
		Bitmap bitmap = KansUtils.getBitmapFromUri(getUi().getContext(), mUri);
		saveIconBitmap(bitmap);
	}

	private void saveIconBitmap(Bitmap bitmap) {
		Bitmap saveBitmap = KansUtils.getRoundIconBitmap(bitmap, getUi().getContext().getResources().getDimensionPixelSize(R.dimen.kans_rouduct_icon_size));
//		mViewHold.mPriceIcon.setImageBitmap(saveBitmap);
//		KansUtils.saveProductIconPath(mProductEntity, saveBitmap);
	}

}
