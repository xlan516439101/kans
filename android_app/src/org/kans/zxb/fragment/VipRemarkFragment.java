package org.kans.zxb.fragment;


import org.kans.zxb.R;
import org.kans.zxb.entity.ProductClass;
import org.kans.zxb.presenter.VipRemarkPresenter;
import org.kans.zxb.view.KItemView;
import org.xutils.x;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

@ContentView(value = R.layout.vip_remark_fragment)
public class VipRemarkFragment extends KFragment<VipRemarkPresenter, VipRemarkPresenter.Ui> implements VipRemarkPresenter.Ui {

	public class ViewHold {
		public KItemView mKItemView;
		public TextView mNameView;
		public TextView mRemarkView;
		public Button delButton;
		public Button editButton;
		public ProductClass mProductClass;
	}
	
	@ViewInject(R.id.add_product_class_button)
	View button;

	@ViewInject(R.id.add_product_class_list)
	ListView mListView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		getPresenter().updateData();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public VipRemarkPresenter createPresenter() {
		return new VipRemarkPresenter();
	}

	@Override
	public VipRemarkFragment getUi() {
		return this;
	}

	@Override
	public Context getContext() {
		return getActivity();
	}

	@Override
	public boolean onBackButtonClick() {
		return false;
	}

	@Event(value = R.id.add_product_class_button, type = View.OnClickListener.class)
	public void onViewClick(View view) {
		Log.i("xlan", "onViewClick" + view);
		super.onClick(view);
	}

	@Override
	public ViewHold getViewHold() {
		return null;
	}
	
	public class AudioView extends View implements Runnable{
		private int width, height;
		private Bitmap bg = null;
		private Paint paint = null;
		private float pathTop = 0;
		private float pathBottom = 0;
		private Path path;
		private boolean isStart;
		private MediaRecorder mMediaRecorder;
		public void setMediaRecorder(MediaRecorder mMediaRecorder){
			this.mMediaRecorder = mMediaRecorder;
		}
		
		public void setIsStart(boolean isStart){
			this.isStart = isStart;
		}
		
		public AudioView(Context context) {
			super(context);
			width = getResources().getDisplayMetrics().widthPixels * 4 / 5;
			ratio = max/width;
			height = width / 2;
			pathTop = height / 5f;
			pathBottom = pathTop + height * 2 / 3f - 5;
			bg = getAudioBackground();
			paint = new Paint();
			paint.setColor(Color.RED);
			paint.setStrokeWidth(6);
			paint.setAntiAlias(true);
			paint.setStyle(Style.FILL_AND_STROKE);
			paint.setStrokeCap(Paint.Cap.SQUARE);
		}

		private Bitmap getAudioBackground() {
			Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			Drawable drawable = getResources().getDrawable(R.drawable.k_audio_bg);
			drawable.setBounds(0, 0, width, height);
			Canvas canvas = new Canvas(bitmap);
			drawable.draw(canvas);
			return bitmap;
		}
		
		private float current = 0;
		private int max = 25000;
		float ratio =0;
		@Override
		protected void onDraw(Canvas canvas) {
			x.task().removeCallbacks(this);
			super.onDraw(canvas);
			canvas.drawBitmap(bg, 0, 0, paint);
			path = new Path();
			if(mMediaRecorder!=null){
				float temp = (mMediaRecorder.getMaxAmplitude()+1000)/ratio;
				current = temp>5?temp:5;
			}else{
				current = 0;
			}
			path.moveTo(current, pathTop);
			path.lineTo(current, pathBottom);
			canvas.drawPath(path, paint);
			if(isStart){
				x.task().postDelayed(this, 100);
			}
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			setMeasuredDimension(width, height);
		}

		@Override
		public void run() {
			invalidate();
		}

	}

	@Override
	public void hidePopuView() {
		Activity().hidePopuView();
	}

	@Override
	public void showIconSelect() {
		View view = View.inflate(getContext(), R.layout.k_icon_choice_view, null);
		view.findViewById(R.id.gallery).setOnClickListener(getPresenter());
		view.findViewById(R.id.camera).setOnClickListener(getPresenter());
		view.findViewById(R.id.cancel).setOnClickListener(getPresenter());
		Activity().showPopuView(view);
	}
}
