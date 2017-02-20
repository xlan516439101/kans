package org.kans.zxb.view;


import java.util.List;

import org.kans.zxb.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;
public class ScrollContentView extends ViewGroup{

	private Scroller scroller;//滑动计算器
	private VelocityTracker velocitY;//速度计算器
	private int mTouchSlop = 8;//滑动溢出
	private float mLastY;//Y的位置
	private boolean mAllowLongPress = true;//是否应许子组件长按
	private int childHeight;//一个子组件的高度
	private int moveCurrentItem = 0;//当前移动的位置
	private int onChangeItem = 1;//当前改变的位置
	private OnScrollListener listener;//回调接口
	private ScrollState mTouchState = ScrollState.TOUCH_STATE_REST;//默认当前的状态
	/**
	 * 定义的几种状态
	 * @author Administrator
	 *
	 */
	public enum ScrollState{
		TOUCH_STATE_REST,TOUCH_STATE_SCROLLING
	}
	
	public ScrollContentView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public ScrollContentView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ScrollContentView(Context context) {
		super(context);
	}
	
	/**
	 * 给每个子组件布局
	 */
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (changed) {
			int childCount = getChildCount();
			int childTop = 0;
			for (int i = 0; i < childCount; i++) {
				View childView = getChildAt(i);
				int height = childHeight;
				childView.layout(l,childTop,r, childTop + height);
				childTop += height;
			}
		}
	}
	
	/**
	 * 每个子组件大小
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View childView = getChildAt(i);
			childView.measure(getWidth(),childHeight);
		}
	}

	/**
	 * 初始化数据
	 */
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		scroller = new Scroller(getContext());
		childHeight = getResources().getDimensionPixelSize(R.dimen.birthday_month_day_picker_text_hight);
	}

	/**
	 * 当滑动时候给每个子组件布局
	 */
	private void layoutChild(int childTop){
		int scrollY = getScrollY();
		int childCount = getChildCount();
		for (int i = 0; i < childCount*2; i++) {
			if((childTop-scrollY)>(getHeight()+childHeight)){
				continue;
			}
			int childAt = i%childCount;
			View childView = getChildAt(childAt);
			int width = getMeasuredWidth();
			int height = childHeight;
			childView.layout(0,childTop,width, childTop + height);
			childTop += height;
		}
		computScrollItemChange();
	}

	/**
	 * 手指松开后滑动计算
	 */
	@Override
	public void computeScroll() {
		int scrollY = getScrollY();
		int scrllViewHight = childHeight*getChildCount();
		int childTop = 0;
		int scrollSmail = scrollY%scrllViewHight;
		while(scrollSmail<0){
			scrollSmail += scrllViewHight;
		}
		childTop = scrollY - scrollSmail;
		layoutChild(childTop);
		
		if (scroller.computeScrollOffset()) {
			scrollTo(0, scroller.getCurrY());
			postInvalidate();
		}
	}
	
	/**
	 * 计算当前状态
	 */
	private void checkStartScroll(float x, float y) {
		final int yDiff = (int) Math.abs(y - mLastY);
		boolean yMoved = yDiff > mTouchSlop;

		if(yMoved){
			if(yMoved){
				mTouchState = ScrollState.TOUCH_STATE_SCROLLING;
				setChildrenDrawingCacheEnabled(true);
				setChildrenDrawnWithCacheEnabled(true);
			}
			if(mAllowLongPress){
				mAllowLongPress = false;
				int num = getChildCount();
				for(int i = 0;i<num;i++){
					 getChildAt(0).cancelLongPress();
				}
			}
		}
		
	}

	/**
	 * 拦截屏幕事件
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		if ((action == MotionEvent.ACTION_MOVE)
				&& (mTouchState != ScrollState.TOUCH_STATE_REST)) {
			return true;
		}
		final float x = ev.getX();
		final float y = ev.getY();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mLastY = y;
			mAllowLongPress = true;
			mTouchState = scroller.isFinished() ?ScrollState.TOUCH_STATE_REST : ScrollState.TOUCH_STATE_SCROLLING; 
			if (velocitY == null) {
				velocitY = VelocityTracker.obtain();
				velocitY.addMovement(ev);
			}
			if (!scroller.isFinished()) {
				scroller.abortAnimation();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (mTouchState == ScrollState.TOUCH_STATE_REST) {
				checkStartScroll(x, y);
			}
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			setChildrenDrawnWithCacheEnabled(false);
			mTouchState = ScrollState.TOUCH_STATE_REST;
			break;
		}
		boolean returnBoolean = mTouchState != ScrollState.TOUCH_STATE_REST;
		return returnBoolean;

	}
	
	/**
	 * 拦截屏幕事件
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float curY = event.getY();
		float curX = event.getX();
		int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_MOVE:
			if(mTouchState == ScrollState.TOUCH_STATE_REST){
				checkStartScroll(curX, curY);
			}else{
				int distance_Y = (int) (mLastY - curY);
				if (velocitY != null) {
					velocitY.addMovement(event);
				}
				mLastY = curY;
				scrollBy(0,distance_Y);
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			int velocit_Y = 0;
			if (velocitY != null) {
				velocitY.addMovement(event);
				velocitY.computeCurrentVelocity(1000);
				velocit_Y = (int) velocitY.getYVelocity();
			}
			int snap = (int) ((int)-velocit_Y/5f);
			snapToScreen(snap);

			if (velocitY != null) {
				velocitY.recycle();
				velocitY = null;
			}
			mTouchState = ScrollState.TOUCH_STATE_REST;
		}

		return true;
	}
	
	/**
	 * 根据Y方向速度计算出滑到的位置并滑动
	 */
	public void snapToScreen(int velocitY) {
		int itemHight = childHeight;
		double itmCount = ((velocitY/(itemHight*1d))%1)>0.5?((velocitY/itemHight)+1):(velocitY/itemHight);
		int moveY = (int)(itmCount*itemHight)-(getScrollY()%itemHight);
		scroller.startScroll(0,getScrollY(),0,moveY, Math.abs(moveY * 2));
		invalidate();
	}
	
	/**
	 * 获取一个Textview
	 */
	private View getItemView(){
		return View.inflate(getContext(), R.layout.birthday_widget_picker_textview, null);
	}

	/**
	 * 获取一个Textview的LayoutParams
	 */
	private ViewGroup.LayoutParams getTextViewLayoutParams(){
		ViewGroup.LayoutParams mLayoutParams = new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,childHeight);
		return mLayoutParams;
	}

	/**
	 * 添加每个子组件
	 */
	public void setList(List<String> values) {
		int scrollY = getScrollY();
		scrollTo(0, 0);
		removeAllViews();
		for(int i=0;i<values.size();i++){
			String names = values.get(i);
			View itemView = getItemView();
			TextView textView = (TextView) itemView.findViewById(R.id.birthday_widget_picker_textview);
			textView.setTag(new Integer(i));
			textView.setText(names);
			addView(itemView, getTextViewLayoutParams());
		}
		scrollTo(0, scrollY);
	}

	/**
	 * 计算出滑动到第一个组件位置
	 */
	private void computScrollItemChange(){
		int scrollY = getScrollY();
		int childCount = getChildCount();
		for(int i = 0;i<childCount;i++){
			View childView = getChildAt(i);
			float diff = childHeight/3f;
			if(Math.abs(childView.getTop()-scrollY)<diff){
				if(moveCurrentItem!=i){
					moveCurrentItem = i;
					onChangeItem = (i+1)%getChildCount();
					if(listener!=null){
						listener.onScrollChange(this,onChangeItem,getChildCount());
					}
					if(onChangeItem==1){
						restoreItem();
					}
				}
			}
		}
	}

	/**
	 * 复原
	 */
	public void restoreItem(){
		onChangeItem = 1;
		scrollBy(0, 0);
		int childCount = getChildCount();
		int childTop = 0;
		for (int i = 0; i < childCount; i++) {
			View childView = getChildAt(i);
			int width = getMeasuredWidth();
			int height = childHeight;
			childView.layout(0,childTop,width, childTop + height);
			childTop += height;
		}
	}

	public int allChildCountHight(){
		return childHeight*getChildCount();
	}
	
	/**
	 * 滑动到指定的位置
	 */
	public void setOnMoveItem(int number,boolean duration){
		if(onChangeItem==number|| childHeight*getChildCount()==0){
			return;
		}
		if(!scroller.isFinished()){
			scroller.abortAnimation();
		}
		//restoreItem();
		int changeNum = number-onChangeItem;
		int moveY = (int)((changeNum/(getChildCount()*1d))*( childHeight*getChildCount()));
		int time = duration?200:Math.abs(moveY * 2);
		scroller.startScroll(0,getScrollY(),0,moveY, time);
		computeScroll();
	}

	/**
	 * 添加callback的方法
	 */
	public void setOnScrollListener(OnScrollListener listener){
		this.listener = listener;
	}
	
	/**
	 * 定义callback
	 */
	public interface OnScrollListener{
		void onScrollChange(View view,int current,int childCount);
	}
	/**
	 * 获取当前显示的位置
	 */
	public int getCurrentItem() {
		return onChangeItem;
	}
	
}
