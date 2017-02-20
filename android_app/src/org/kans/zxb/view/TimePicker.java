package org.kans.zxb.view;

import java.util.ArrayList;
import java.util.List;

import org.kans.zxb.R;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class TimePicker extends FrameLayout implements ScrollContentView.OnScrollListener{

	private ScrollContentView hourView;//显示月的小时
	private ScrollContentView minView;//显示天的分钟
	private int hour=1;//起始的小时
	private int min=1;//起始的分钟
	private Handler myHandler = new Handler();
	private SetTimeRunnable mRunnable = new SetTimeRunnable();//设置时间的进程
	private TimeChangeListener mChangeListener;//回调接口
	
	public TimePicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public TimePicker(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TimePicker(Context context) {
		super(context);
	}
	
	/**
	 * 设置小时的数组
	 */
	public void setHours(List<String> hours){
		hourView.setList(hours);
	}

	/**
	 * 设置分钟的数组
	 */
	public void setMins(List<String> mins){
		minView.setList(mins);
	}
	
	/**
	 * 初始化数据
	 */
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		inflate(getContext(), R.layout.birthday_widget_time_picker, this);
		hourView = (ScrollContentView) findViewById(R.id.birthday_widget_timePicker_hour);
		List<String> hours = new ArrayList<String>();
		for(int i=0;i<24;i++){
			if(i<10){
				hours.add("0"+i);
			}else{
				hours.add(String.valueOf(i));
			}
		}
		hourView.setList(hours);
		hourView.setOnScrollListener(this);
		
		minView = (ScrollContentView) findViewById(R.id.birthday_widget_timePicker_min);
		List<String> mins = new ArrayList<String>();
		for(int i=0;i<60;i++){
			if(i<10){
				mins.add("0"+i);
			}else{
				mins.add(String.valueOf(i));
			}
		}
		minView.setList(mins);
		minView.setOnScrollListener(this);
	}
	
	/**
	 * 子组件滑动的回调方法
	 */
	@Override
	public void onScrollChange(View view, int current,int childCount) {
		switch (view.getId()) {
		case R.id.birthday_widget_timePicker_hour:
			hour = current;
			break;

		case R.id.birthday_widget_timePicker_min:
			if(current==(childCount-1)&&min==0){
				hourView.setOnMoveItem(hourView.getCurrentItem()-1,false);
			}else if(current==0&&min==(childCount-1)){
				hourView.setOnMoveItem(hourView.getCurrentItem()+1,false);
			}
			min = current;
			break;

		default:
			break;
		}
		if(mChangeListener!=null){
			mChangeListener.onTimeChage(this,hour, min);
		}
	}
	
	/**
	 * 设置时间 初始化屏幕
	 * @param hour
	 * @param min
	 */
	public void setTime(int hour,int min){
		mRunnable.setTime(hour, min);
		myHandler.removeCallbacks(mRunnable);
		myHandler.postDelayed(mRunnable, 500);
	}
	
	/**
	 * 设置时间的running
	 * @author Administrator
	 *
	 */
	private class SetTimeRunnable implements Runnable{
		private int hour,min;
		public void setTime(int hour,int min){
			this.hour=hour;
			this.min=min;
		}
		public void run(){
			if(hourView.allChildCountHight()!=0&&minView.allChildCountHight()!=0){
				hourView.setOnMoveItem(hour,true);
				minView.setOnMoveItem(min,true);
			}else{
				myHandler.removeCallbacks(mRunnable);
				myHandler.postDelayed(mRunnable, 500);
			}
		}
	}
	/**
	 * 获取小时
	 * @return
	 */
	public int getHour(){
		return hour;
	}
	
	/**
	 * 获取分钟
	 * @return
	 */
	public int getMin(){
		return min;
	}
	
	/**
	 * 设置数据变化的监听
	 * @param mChangeListener
	 */
	public void setTimeChangeListener(TimeChangeListener mChangeListener){
		this.mChangeListener = mChangeListener;
	}
	
	/**
	 * 定义时间变化回调的接口
	 * @author Administrator
	 *
	 */
	public interface TimeChangeListener{
		void onTimeChage(TimePicker mPicker,int hour,int min);
	}
}
