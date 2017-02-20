package org.kans.zxb.view;

import java.util.ArrayList;
import java.util.List;

import org.kans.zxb.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;

public class SunView extends BaseView implements Runnable{

	public SunView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public SunView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SunView(Context context) {
		super(context);
	}

	private int width = 0;
	private int height = 0;
	private final int LIGHT_COUNT = 12;
	private Handler mHandler = new Handler();
	private int radius = 0;
	private Drawable centerDrawable;
	private Rect ctenterRect;
	
	@Override
	protected void onCreate() {
		centerDrawable = getBackground();
		if(centerDrawable == null){
			centerDrawable = getContext().getResources().getDrawable(R.drawable.ic_launcher);
		}else{
			setBackground(null);
		}
		
	}

	@Override
	protected void init() {
		width = getWidth();
		height = getHeight();
		int centerX = width/2;
		int centerY = height/2;
		int radius = (int) (width*3.5f/10f);
		ctenterRect = new Rect(centerX-radius, centerY-radius, centerX+radius, centerY+radius);
		centerDrawable.setBounds(ctenterRect);
		int span = 360/LIGHT_COUNT;
		for(int i=0;i<LIGHT_COUNT;i++){
			SunLight mSunLight = new SunLight(centerX, centerY, radius, i*span, span);
			mLightDraw.add(mSunLight);
		}

		for(int i=0;i<mLightDraw.size();i++){
			mLightDraw.get(i).onCreate(mHandler);
		}
	}

	@Override
	protected void onDestroy() {
		for(int i=0;i<mLightDraw.size();i++){
			mLightDraw.get(i).onDestory();
		}
		mLightDraw.clear();
		mHandler.removeCallbacks(this);
	}
	
	@SuppressLint("WrongCall") 
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		centerDrawable.draw(canvas);
		radius = ++radius%360;
		for(int i=0;i<mLightDraw.size();i++){
			mLightDraw.get(i).update(radius);
			mLightDraw.get(i).onDraw(canvas);
		}
		mHandler.removeCallbacks(this);
		mHandler.postDelayed(this, 50);
	}

	@Override
	public void run() {
		invalidate();
	}
	
	private List<ISunDraw> mLightDraw = new ArrayList<ISunDraw>();
	private interface ISunDraw{
		abstract void onCreate(Handler mHandler);
		abstract void onDraw(Canvas canvas);
		abstract void update(int num);
		abstract void onDestory();
	}
	
	public class SunLight implements Runnable, ISunDraw{
		private final String drawTxt = "I";
		private final int centerX, centerY, radius;
		private final float start, span;
		private Handler mHandler;
		boolean isRun = false;
		private long delayMillis;
		private Paint mPaint;
		private float textSize,maxTextSize,minTextSize;
		private int alpha,maxAlpha,minAlpha,updateNum;
		private RectF oval;
		private Path mPath = new Path();
		public SunLight(int centerX, int centerY, int radius, float start, float span) {
			super();
			this.centerX = centerX;
			this.centerY = centerY;
			this.radius = radius;
			this.start = start;
			this.span = span;
			oval = new RectF(this.centerX - this.radius, this.centerY - this.radius, this.centerX + this.radius, this.centerY + this.radius);
		}

		@Override
		public void onCreate(Handler mHandler) {
			this.mHandler = mHandler;
			minTextSize = radius*0.6f;
			maxTextSize = (float) (radius*0.7f);
			textSize = (float) (Math.random()*(maxTextSize-minTextSize)+minTextSize);
			minAlpha = (int) (125+Math.random()*125);
			maxAlpha = (int) (155+Math.random()*100);
			alpha = (int) (Math.random()*(maxAlpha-minAlpha)+minAlpha);
			delayMillis = (long) (Math.random()*500+300);
			mPaint = new Paint();
			mPaint.setFlags(Paint.FAKE_BOLD_TEXT_FLAG);
			mPaint.setAntiAlias(true);
			mPaint.setColor(Color.YELLOW);
			mPaint.setTextSize(textSize);
			mPaint.setAlpha(alpha);
		}
		
		@Override
		public void onDraw(Canvas canvas) {
			if(!isRun){
				isRun = true;
				mHandler.postDelayed(this, delayMillis);
			}
			mPath.reset();
			mPath.addArc(oval, start+updateNum, span);
			canvas.drawTextOnPath(drawTxt, mPath, 1, 0, mPaint);
			mHandler.removeCallbacks(this);
			mHandler.postDelayed(this, 50);
		}

		@Override
		public void onDestory() {
			if(mHandler != null){
				mHandler.removeCallbacks(this);
			}
		}
		
		private boolean isplus = true;
		private final float PLUS_VLAUE = 0.8f;
		@Override
		public void run() {
			if(mHandler != null){
				mHandler.removeCallbacks(this);
			}
			alpha = (alpha+10)%255;
			textSize= textSize+(isplus?PLUS_VLAUE:-PLUS_VLAUE);
			if(textSize>=maxTextSize){
				isplus = false;
				textSize=maxTextSize;
			}else if(textSize<=minTextSize){
				isplus = true;
				textSize=minTextSize;
			}
			mPaint.setTextSize(textSize);
			if(mHandler != null){
				mHandler.postDelayed(this, delayMillis);
			}
		}

		@Override
		public void update(int num) {
			updateNum = num;
		}
	}


}
