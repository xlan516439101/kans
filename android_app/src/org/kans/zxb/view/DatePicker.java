package org.kans.zxb.view;

import java.util.ArrayList;
import java.util.List;

import org.kans.zxb.R;
import org.kans.zxb.util.KansUtils;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class DatePicker extends FrameLayout implements ScrollContentView.OnScrollListener{

	private ScrollContentView yearsView;//显示年的组件
	private ScrollContentView monthView;//显示月的组件
	private ScrollContentView dayView;//显示天的组件
	private int year=1950;//起始的年份
	private int month=1;//起始的月份
	private int day=1;//起始的天
	private static final int STARTYEAR = 1950;//年份空隙
	private Handler myHandler = new Handler();
	private SetTimeRunnable mRunnable = new SetTimeRunnable();//设置时间的进程
	private ChangeArrayRun mChangeArrayRun = new ChangeArrayRun();//滑动年和月时候每月天数改变延迟线程
	private DateChangeListener mChangeListener;//回调接口
	private boolean isLunar = false;//是否显示为农历
	
	public DatePicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public DatePicker(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DatePicker(Context context) {
		super(context);
	}
	
	public void restore(){
		yearsView.restoreItem();
		monthView.restoreItem();
		dayView.restoreItem();
	}
	
	/**
	 * 设置月份的数组
	 */
	public void setMonths(List<String> months){
		monthView.setList(months);
	}

	/**
	 * 设置天的数组
	 */
	public void setDays(List<String> days){
		dayView.setList(days);
	}
	
	/**
	 * 初始化数据
	 */
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		inflate(getContext(), R.layout.birthday_widget_date_picker, this);
		monthView = (ScrollContentView) findViewById(R.id.birthday_widget_timePicker_month);
		List<String> months = new ArrayList<String>();
		for(int i=1;i<13;i++){
			if(i<10){
				months.add("0"+i);
			}else{
				months.add(String.valueOf(i));
			}
		}
		monthView.setList(months);
		monthView.setOnScrollListener(this);
		
		dayView = (ScrollContentView) findViewById(R.id.birthday_widget_timePicker_day);
		List<String> days = new ArrayList<String>();
		for(int i=1;i<32;i++){
			if(i<10){
				days.add("0"+i);
			}else{
				days.add(String.valueOf(i));
			}
		}
		dayView.setList(days);
		dayView.setOnScrollListener(this);
		
		yearsView = (ScrollContentView) findViewById(R.id.birthday_widget_timePicker_years);
		int newYears = KansUtils.getDate()[0];
		List<String> years = new ArrayList<String>();
		for(int i=0;i<(newYears-STARTYEAR+1);i++){
			years.add(String.valueOf(i+STARTYEAR));
		}
		yearsView.setList(years);
		yearsView.setOnScrollListener(this);
	}
	
	
	/**
	 * 子组件滑动的回调方法
	 */
	@Override
	public void onScrollChange(View view, int current,int childCount) {
		switch (view.getId()) {
		case R.id.birthday_widget_timePicker_years:
			year = current+STARTYEAR;
			break;
		case R.id.birthday_widget_timePicker_month:
			/*年自动+1
			if(current==(childCount-1)&&month==0){
				yearsView.setOnMoveItem(yearsView.getCurrentItem()-1,false);
			}else if(current==0&&month==(childCount-1)){
				yearsView.setOnMoveItem(yearsView.getCurrentItem()+1,false);
			}
			*/
			month = current;
			break;

		case R.id.birthday_widget_timePicker_day:
			/*月自动+1
			if(current==(childCount-1)&&day==0){
				monthView.setOnMoveItem(monthView.getCurrentItem()-1,false);
			}else if(current==0&&day==(childCount-1)){
				monthView.setOnMoveItem(monthView.getCurrentItem()+1,false);
			}
			*/
			day = current;
			break;

		default:
			break;
		}
		if(mChangeListener!=null){
			mChangeListener.onDateChage(this,year,month+1, day+1);
		}
		
		if(view.getId()==R.id.birthday_widget_timePicker_years || view.getId()==R.id.birthday_widget_timePicker_month){
			myHandler.removeCallbacks(mChangeArrayRun);
			myHandler.postDelayed(mChangeArrayRun, 400);
		}
	}
	
	private class ChangeArrayRun implements Runnable{

		@Override
		public void run() {
			int dayCount = KansUtils.getDayCounts(year,month+1,isLunar);
			if(isLunar){
				List<String> days = KansUtils.getDayLunarStrings(getContext(),dayCount);
				setDays(days);
			}else{
				List<String> days = KansUtils.getDayStrings(dayCount);
				setDays(days);
			}
		}
		
	}
	
	public boolean isLunar(){
		return isLunar;
	}
	
	/**
	 * 设置是否用农历显示
	 * @param isLunar
	 */
	public void setLunar(boolean isLunar){
		this.isLunar = isLunar;
		int dayCount = KansUtils.getDayCounts(year,month+1,this.isLunar);
		List<String> days;
		List<String> months;
		if(this.isLunar){
			days = KansUtils.getDayLunarStrings(getContext(),dayCount);
			months = KansUtils.getMonthLunarStrings(getContext());
		}else{
			days = KansUtils.getDayStrings(dayCount);
			months = KansUtils.getMonthStrings();
		}
		setDays(days);
		setMonths(months);
	}
	
	/**
	 * 重置，初始化
	 */
	public void reset(){
		int[] date = KansUtils.getDate();
		setDate(date[0], date[1], date[2]);
		setLunar(false);
	}
	
	/**
	 * 设置时间 初始化屏幕
	 * @param year
	 * @param month
	 * @param day
	 */
	public void setDate(int year,int month,int day){
		mRunnable.setTime(year-STARTYEAR,month-1, day-1);
		myHandler.removeCallbacks(mRunnable);
		myHandler.postDelayed(mRunnable, 800);
	}
	
	/**
	 * 设置时间的running
	 * @author Administrator
	 *
	 */
	private class SetTimeRunnable implements Runnable{
		private int year,month,day;
		public void setTime(int year,int month,int day){
			this.year=year;
			this.month=month;
			this.day=day;
		}
		public void run(){
			if(monthView.allChildCountHight()!=0&&dayView.allChildCountHight()!=0){
				yearsView.setOnMoveItem(year,true);
				monthView.setOnMoveItem(month,true);
				dayView.setOnMoveItem(day,true);
			}else{
				myHandler.removeCallbacks(mRunnable);
				myHandler.postDelayed(mRunnable, 500);
			}
			
		}
	}

	/**
	 * 获取年份
	 * @return
	 */
	public int getYear(){
		return year;
	}
	
	/**
	 * 获取月份
	 * @return
	 */
	public int getMonth(){
		return month;
	}
	
	/**
	 * 获取天
	 * @return
	 */
	public int getDay(){
		return day;
	}
	
	/**
	 * 设置数据变化的坚挺
	 * @param mChangeListener
	 */
	public void setDateChangeListener(DateChangeListener mChangeListener){
		this.mChangeListener = mChangeListener;
	}
	
	/**
	 * 定义时间变化回调的接口
	 * @author Administrator
	 *
	 */
	public interface DateChangeListener{
		void onDateChage(DatePicker mPicker,int year,int month,int day);
	}
}
