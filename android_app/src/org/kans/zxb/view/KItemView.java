package org.kans.zxb.view;

import org.kans.zxb.R;
import org.kans.zxb.view.KItemView.STATE;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;

interface IListInterface {

	abstract void setState(STATE mState);

	abstract STATE getState();

	abstract void editStart();

	abstract void editReset();

	abstract void removeItem();

	abstract void showDelete();

	abstract void setIcon(Drawable ico);

	abstract void setWidget(View view);

	abstract void setEditView(View view);

	abstract void setContent(View view);

	abstract void setEndIconVisibility(boolean isVisibility);
}

public class KItemView extends FrameLayout implements IListInterface, View.OnClickListener, View.OnLongClickListener {

	public enum STATE {
		NORMAL, // 正常状态
		RESETING, // 恢复状态
		EDIT, // 编辑状态
		EDITING, // 正在转换到编辑状态
		SHOWDELETE, // 显示删除状态
		SHOWDELETEING, // 正在显示删除状态
		REMOVEING// 正在删除状态
	}
	private float mLastX;
	private int oldHight = -1;
	private Scroller scroller;
	private Button btn_delete;
	private View editView;
	private ImageView left_btn_delete;
	private ImageView end_icon;
	private int leftDiff, rightDiff;
	private final int TOUCHSLOP = 5;
	private VelocityTracker velocity;
	private LinearLayout content_area;
	private KListItemCallback callback;
	private KListItemStateCallback mStateCallback;
	private STATE currentState = STATE.NORMAL;
	private Handler mHandler = new Handler();

	public void setKListItemCallback(KListItemCallback callback) {
		this.callback = callback;
	}

	public void setKListItemStateCallback(KListItemStateCallback mStateCallback) {
		this.mStateCallback = mStateCallback;
	}

	private void callbackState() {
		if (mStateCallback != null) {
			mStateCallback.onStateChange(this, currentState);
		}
	}

	public interface KListItemCallback {
		abstract void delItem(KItemView itemView);

		abstract void onItemClick(KItemView itemView);

		abstract void onItemLongClick(KItemView itemView);

		abstract void endIconClick(KItemView itemView);
	}

	public interface KListItemStateCallback {
		abstract void onStateChange(KItemView itemView, STATE mState);
	}

	public KItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public KItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public KItemView(Context context) {
		super(context);
	}

	@Override
	public void setEditView(View view) {
		FrameLayout mFrameLayout = (FrameLayout) findViewById(android.R.id.edit);
		mFrameLayout.removeAllViews();
		FrameLayout.LayoutParams mParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT);
		mFrameLayout.addView(view, mParams);
		btn_delete.setVisibility(View.GONE);
	}

	public static KItemView onCreateKItemView(Context context, int resource) {
		KItemView mKItemView = (KItemView) View.inflate(context, R.layout.k_item_view, null);
		if (resource > 0) {
			View.inflate(context, resource, (ViewGroup) mKItemView.findViewById(android.R.id.content));
		}
		return mKItemView;
	}

	@Override
	public void setIcon(Drawable drawable) {
		((ImageView) findViewById(android.R.id.icon)).setImageDrawable(drawable);
	}

	@Override
	public void setWidget(View view) {
		FrameLayout mFrameLayout = (FrameLayout) findViewById(android.R.id.widget_frame);
		mFrameLayout.removeAllViews();
		FrameLayout.LayoutParams mParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT);
		mFrameLayout.addView(view, mParams);
	}

	@Override
	public void setContent(View view) {
		FrameLayout mFrameLayout = (FrameLayout) findViewById(android.R.id.content);
		mFrameLayout.removeAllViews();
		FrameLayout.LayoutParams mParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT);
		mFrameLayout.addView(view, mParams);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		// Log.e("xlan",
		// "onLayout(boolean changed:"+changed+", int left:"+left+", int top:"+top+", int right"+right+",int bottom:"+bottom);
		if (currentState != STATE.REMOVEING) {
			super.onLayout(changed, left, top, right, bottom);
			LayoutParams lp = (LayoutParams) editView.getLayoutParams();
			rightDiff = editView.getWidth() + lp.leftMargin + lp.rightMargin;
			lp = (LayoutParams) left_btn_delete.getLayoutParams();
			leftDiff = left_btn_delete.getWidth() + lp.leftMargin + lp.rightMargin;
			if (oldHight < 0) {
				oldHight = bottom - top;
			}
			if (currentState == STATE.NORMAL) {
				lp = (LayoutParams) left_btn_delete.getLayoutParams();
				left_btn_delete.layout(-(left_btn_delete.getWidth() + lp.leftMargin), left_btn_delete.getTop(), -lp.rightMargin, left_btn_delete.getBottom());

				lp = (LayoutParams) editView.getLayoutParams();
				editView.layout(getWidth() - rightDiff, editView.getTop(), getWidth(), lp.topMargin + editView.getHeight());

				lp = (LayoutParams) content_area.getLayoutParams();
				content_area.layout(lp.leftMargin, content_area.getTop(), getWidth() - lp.leftMargin, lp.topMargin + content_area.getHeight());

			} else if (currentState == STATE.EDIT) {
				lp = (LayoutParams) left_btn_delete.getLayoutParams();
				left_btn_delete.layout(lp.leftMargin, left_btn_delete.getTop(), lp.leftMargin + left_btn_delete.getWidth(), left_btn_delete.getBottom());

				lp = (LayoutParams) editView.getLayoutParams();
				editView.layout(getWidth() - rightDiff, editView.getTop(), getWidth(), lp.topMargin + editView.getHeight());

				lp = (LayoutParams) content_area.getLayoutParams();
				content_area.layout(leftDiff + lp.leftMargin, content_area.getTop(), leftDiff + lp.leftMargin + content_area.getWidth(), lp.topMargin + content_area.getHeight());

			} else if (currentState == STATE.SHOWDELETE) {
				lp = (LayoutParams) left_btn_delete.getLayoutParams();
				left_btn_delete.layout(-leftDiff - rightDiff, left_btn_delete.getTop(), -rightDiff, left_btn_delete.getBottom());

				lp = (LayoutParams) editView.getLayoutParams();
				editView.layout(getWidth() - rightDiff, editView.getTop(), getWidth(), lp.topMargin + editView.getHeight());

				lp = (LayoutParams) content_area.getLayoutParams();
				content_area.layout(-rightDiff, content_area.getTop(), getWidth() - rightDiff, lp.topMargin + content_area.getHeight());
			}
		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		scroller = new Scroller(getContext());
		left_btn_delete = (ImageView) findViewById(R.id.left_btn_delete);
		left_btn_delete.setOnClickListener(this);
		left_btn_delete.setVisibility(View.VISIBLE);
		end_icon = (ImageView) findViewById(android.R.id.icon1);
		end_icon.setOnClickListener(this);
		end_icon.setVisibility(View.VISIBLE);
		editView = findViewById(android.R.id.edit);
		btn_delete = (Button) findViewById(R.id.btn_delete);
		btn_delete.setVisibility(View.VISIBLE);
		btn_delete.setOnClickListener(this);
		content_area = (LinearLayout) findViewById(R.id.content_area);
		content_area.setOnClickListener(this);
		content_area.setOnLongClickListener(this);
		content_area.setVisibility(View.VISIBLE);
	}

	private void checkStartScroll(float x) {
		if (currentState == STATE.NORMAL) {
			final int xDiff = (int) (mLastX - x);
			boolean xMoved = xDiff > TOUCHSLOP;
			if (xMoved) {
				setState(STATE.SHOWDELETEING);
				setChildrenDrawingCacheEnabled(true);
				setChildrenDrawnWithCacheEnabled(true);
			}
		}
	}

	public int getDeleteButtonLeft() {
		return getRight() - rightDiff;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (getTag() == null) {
			return true;
		}
		final int action = ev.getAction();
		// Log.i("xlan", "onInterceptTouchEvent currentState:"+currentState);
		if ((action == MotionEvent.ACTION_MOVE) && (currentState == STATE.RESETING)) {
			return true;
		}
		final float x = ev.getX();

		switch (action) {
		case MotionEvent.ACTION_MOVE:
			checkStartScroll(x);
			break;
		case MotionEvent.ACTION_DOWN:
			if (currentState == STATE.EDIT && x > leftDiff) {
				mLastX = x;
				return true;
			}
			if (currentState == STATE.SHOWDELETE && x < (getWidth() - rightDiff)) {
				editReset();
				mLastX = -1000;
			} else {
				mLastX = x;
			}
			break;

		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			setChildrenDrawingCacheEnabled(false);
			setChildrenDrawnWithCacheEnabled(false);
			break;
		}
		// return mTouchState != TouchState.TOUCH_STATE_REST;
		return currentState == STATE.SHOWDELETEING;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (getTag() == null || !isEnabled()) {
			return true;
		}
		float curX = event.getX();
		int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (velocity == null) {
				velocity = VelocityTracker.obtain();
				velocity.addMovement(event);
			}
			if (!scroller.isFinished()) {
				scroller.abortAnimation();
			}
			// mLastX = curX;
			break;

		case MotionEvent.ACTION_MOVE:
			checkStartScroll(curX);
			int distance_x = (int) (mLastX - curX);
			if (IsCanMove() && currentState == STATE.SHOWDELETEING) {

				if (velocity != null) {
					velocity.addMovement(event);
				} else {
					velocity = VelocityTracker.obtain();
					velocity.addMovement(event);
				}
				mLastX = curX;
				int left = content_area.getLeft() - distance_x;
				int top = content_area.getTop();
				int right = content_area.getRight() - distance_x;
				int bottom = content_area.getBottom();
				if (left < (getLeft() - rightDiff)) {
					int diff = getLeft() - rightDiff - left;
					left += diff;
					right += diff;
				}
				content_area.layout(left, top, right, bottom);

			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			int velocityX = 0;
			if (velocity != null) {
				velocity.addMovement(event);
				velocity.computeCurrentVelocity(1000);
				velocityX = (int) velocity.getXVelocity();
			}

			if (velocity != null) {
				velocity.recycle();
				velocity = null;
			}
			if (currentState == STATE.SHOWDELETEING) {
				// Log.e("xlan", "  velocityX:"+velocityX);
				if (velocityX < -200 || (getRight() - editView.getRight()) > (rightDiff / 2)) {
					showDelete();
				} else {
					editReset();
				}
				setState(STATE.NORMAL);
			}
			break;

		}

		return true;
	}

	public boolean IsCanMove() {
		int right = content_area.getRight();
		if (right <= getRight() && right >= (getRight() - rightDiff) && (currentState == STATE.NORMAL || currentState == STATE.SHOWDELETEING)) {
			return true;
		}
		return false;
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}

	@Override
	public void computeScroll() {
		if (scroller.computeScrollOffset()) {
			scrollTo(scroller.getCurrX(), scroller.getCurrY());
			postInvalidate();
		}
	}

	public class XhlObjectAnimation implements AnimatorUpdateListener, AnimatorListener, Runnable {
		private ObjectAnimator anim;
		private int id, currentValue, end, oldValue;
		private final int MIN_MOVE = 10;
		private STATE startState, endState;
		private boolean isStart = false;

		public void creatAnimation(STATE startState, STATE endState, int id, int start, int end, int duration) {
			// Log.e("xlan",
			// id+"  creatAnimation  startState:"+startState+"  endState:" +
			// "  start:"+start+"  end:"+end+"   duration:"+duration);
			this.startState = startState;
			this.endState = endState;
			this.end = end;
			this.id = id;
			oldValue = start;
			anim = ObjectAnimator.ofInt(this, "currentValue", start, end);
			anim.setDuration(duration / 2);
			anim.setInterpolator(new AccelerateInterpolator());
			anim.addUpdateListener(this);
			anim.addListener(this);
		}

		public void run() {
			anim.start();
		}

		public int getCurrentValue() {
			return currentValue;
		}

		public void setCurrentValue(int currentValue) {
			this.currentValue = currentValue;
		}

		public void onAnimationUpdate(ValueAnimator animation) {
			if (isStart && (Math.abs(getCurrentValue() - oldValue) > MIN_MOVE)) {
				oldValue = (Integer) animation.getAnimatedValue();
				int left = 0, right = 0;
				switch (id) {
				case R.id.left_btn_delete:
					right = getCurrentValue();
					left = right - left_btn_delete.getWidth();
					left_btn_delete.layout(left, left_btn_delete.getTop(), right, left_btn_delete.getBottom());
					break;
				case R.id.btn_delete:
					right = getCurrentValue();
					left = right - editView.getWidth();
					editView.layout(left, editView.getTop(), right, editView.getBottom());
					break;
				case R.id.content_area:
					right = getCurrentValue();
					left = right - content_area.getWidth();
					content_area.layout(left, content_area.getTop(), right, content_area.getBottom());
					break;
				case -1:
					// 显示高低变化的动画，目前手机显示有些卡顿
					ViewGroup.LayoutParams lp = getLayoutParams();
					boolean isShowAnimation = (lp.height - getCurrentValue()) > 540;
					if (isShowAnimation) {
						lp.height = getCurrentValue();
						setLayoutParams(lp);
					}
					break;
				default:
					break;
				}
			}
		}

		public void onAnimationStart(Animator animation) {
			isStart = true;
			setState(startState);
		}

		public void onAnimationEnd(Animator animation) {
			int left = 0, right = end;
			switch (id) {
			case R.id.left_btn_delete:
				left = right - left_btn_delete.getWidth();
				left_btn_delete.layout(left, left_btn_delete.getTop(), right, left_btn_delete.getBottom());
				break;
			case R.id.btn_delete:
				left = right - editView.getWidth();
				editView.layout(left, editView.getTop(), right, editView.getBottom());
				break;
			case R.id.content_area:
				left = right - content_area.getWidth();
				content_area.layout(left, content_area.getTop(), right, content_area.getBottom());
				break;
			case -1:
				ViewGroup.LayoutParams lp = getLayoutParams();
				lp.height = end;
				setLayoutParams(lp);
				if (callback != null) {
					callback.delItem(KItemView.this);
				}
				mHandler.postDelayed(new Runnable() {

					@Override
					public void run() {
						ViewGroup.LayoutParams lp = getLayoutParams();
						lp.height = oldHight;
						setLayoutParams(lp);
						requestLayout();
					}
				}, 20);
				break;

			default:
				break;
			}
			isStart = false;
			setState(endState);
			// Log.e("xlan", "currentState:"+currentState);
		}

		public void onAnimationCancel(Animator animation) {
		}

		public void onAnimationRepeat(Animator animation) {
		}
	}

	/**
	 * 点击编辑按钮向右移动
	 */
	@Override
	public void editStart() {

		if (getTag() == null) {
			return;
		}
		// private ImageView btn_edit;//左边的红圆圈
		int right = left_btn_delete.getRight();
		LayoutParams lp = (LayoutParams) left_btn_delete.getLayoutParams();
		if (right - (lp.leftMargin + left_btn_delete.getWidth()) != 0) {
			int start = right;
			int end = lp.leftMargin + left_btn_delete.getWidth();
			XhlObjectAnimation animation = new XhlObjectAnimation();
			animation.creatAnimation(STATE.EDITING, STATE.EDIT, R.id.left_btn_delete, start, end, Math.abs(start - end));
			mHandler.post(animation);

		}

		// private LinearLayout content_area;
		right = content_area.getRight();
		lp = (LayoutParams) content_area.getLayoutParams();
		if (right != (getRight() + leftDiff - lp.rightMargin)) {
			int start = right;
			int end = getRight() + leftDiff - lp.rightMargin;
			XhlObjectAnimation animation = new XhlObjectAnimation();
			animation.creatAnimation(STATE.RESETING, STATE.EDIT, R.id.content_area, start, end, Math.abs(start - end));
			mHandler.post(animation);
		}

		// private Button btn_delete;//右边的删除按
		right = editView.getRight();
		lp = (LayoutParams) editView.getLayoutParams();
		if (right != (getRight() - lp.rightMargin)) {
			int start = right;
			int end = getRight() - lp.rightMargin;
			XhlObjectAnimation animation = new XhlObjectAnimation();
			animation.creatAnimation(STATE.RESETING, STATE.EDIT, R.id.btn_delete, start, end, Math.abs(start - end));
			mHandler.post(animation);
		}

	}

	/**
	 * 点击完成按钮 布局还原
	 */
	@Override
	public void editReset() {
		if (getTag() == null) {
			return;
		}
		// private ImageView btn_edit;//左边的红圆圈
		int right = left_btn_delete.getRight();
		LayoutParams lp = (LayoutParams) left_btn_delete.getLayoutParams();
		if (right + lp.rightMargin != 0) {
			int start = right;
			int end = -lp.rightMargin;
			XhlObjectAnimation animation = new XhlObjectAnimation();
			animation.creatAnimation(STATE.RESETING, STATE.NORMAL, R.id.left_btn_delete, start, end, Math.abs(start - end));
			mHandler.post(animation);

		}

		// private LinearLayout content_area;
		right = content_area.getRight();
		lp = (LayoutParams) content_area.getLayoutParams();
		if (right != (getRight() - lp.rightMargin)) {
			int start = right;
			int end = getRight() - lp.rightMargin;
			XhlObjectAnimation animation = new XhlObjectAnimation();
			animation.creatAnimation(STATE.RESETING, STATE.NORMAL, R.id.content_area, start, end, Math.abs(start - end));
			mHandler.post(animation);
		}

		// private Button btn_delete;//右边的删除按�?
		right = editView.getRight();
		lp = (LayoutParams) editView.getLayoutParams();
		if (right != (getRight() - lp.rightMargin)) {
			int start = right;
			int end = getRight() - lp.rightMargin;
			XhlObjectAnimation animation = new XhlObjectAnimation();
			animation.creatAnimation(STATE.RESETING, STATE.NORMAL, R.id.btn_delete, start, end, Math.abs(start - end));
			mHandler.post(animation);
		}

	}

	/**
	 * 按左边的删除 显示右边删除
	 */
	@Override
	public void showDelete() {
		if (getTag() == null) {
			return;
		}
		// private ImageView btn_edit;//左边的红圆圈
		int right = left_btn_delete.getRight();
		LayoutParams lp = (LayoutParams) editView.getLayoutParams();
		if (right + rightDiff != 0) {
			int start = right;
			int end = -rightDiff;
			XhlObjectAnimation animation = new XhlObjectAnimation();
			animation.creatAnimation(STATE.EDIT, STATE.SHOWDELETE, R.id.left_btn_delete, start, end, Math.abs(start - end));
			mHandler.post(animation);

		}

		// private LinearLayout content_area;
		right = content_area.getRight();
		if (right != (getRight() - rightDiff)) {
			int start = right;
			int end = getRight() - rightDiff;
			XhlObjectAnimation animation = new XhlObjectAnimation();
			animation.creatAnimation(STATE.EDIT, STATE.SHOWDELETE, R.id.content_area, start, end, Math.abs(start - end));
			mHandler.post(animation);
		}

		// private Button btn_delete;//右边的删除按
		right = editView.getRight();
		lp = (LayoutParams) editView.getLayoutParams();
		if (right != (getRight() - lp.rightMargin)) {
			int start = right;
			int end = getRight() - lp.rightMargin;
			XhlObjectAnimation animation = new XhlObjectAnimation();
			animation.creatAnimation(STATE.EDIT, STATE.SHOWDELETE, R.id.btn_delete, start, end, Math.abs(start - end));
			mHandler.post(animation);
		}
	}

	/**
	 * 按删�?高度不断变化动画 动画结束后知数据库删除
	 */
	@Override
	public void removeItem() {
		if (getTag() == null) {
			return;
		}
		int duration = 0;
		// all
		if (getHeight() > 0) {
			int start = getHeight();
			int end = 0;
			duration = Math.abs(start - end) * 2;
			XhlObjectAnimation animation = new XhlObjectAnimation();
			animation.creatAnimation(STATE.REMOVEING, STATE.REMOVEING, -1, start, end, duration);
			mHandler.post(animation);
		}

		// private ImageView btn_edit;//左边的红圆圈
		int right = left_btn_delete.getRight();
		if (right - getWidth() != 0) {
			int start = right;
			int end = -getWidth();
			XhlObjectAnimation animation = new XhlObjectAnimation();
			animation.creatAnimation(STATE.REMOVEING, STATE.REMOVEING, R.id.left_btn_delete, start, end, duration);
			mHandler.post(animation);

		}

		// private LinearLayout content_area;
		right = content_area.getRight();
		if (right != 0) {
			int start = right;
			int end = 0;
			XhlObjectAnimation animation = new XhlObjectAnimation();
			animation.creatAnimation(STATE.REMOVEING, STATE.REMOVEING, R.id.content_area, start, end, duration);
			mHandler.post(animation);
		}

		// private Button btn_delete;//右边的删除按
		right = editView.getRight();
		if (currentState == STATE.SHOWDELETE || currentState == STATE.EDIT) {
			if (right != (getRight() + rightDiff)) {
				int start = right;
				int end = getRight() + rightDiff;// - lp.rightMargin;//
				XhlObjectAnimation animation = new XhlObjectAnimation();
				animation.creatAnimation(STATE.REMOVEING, STATE.REMOVEING, R.id.btn_delete, start, end, duration);
				mHandler.post(animation);
			}
		} else {
			if (right != 0) {
				int start = right;
				int end = 0;
				XhlObjectAnimation animation = new XhlObjectAnimation();
				animation.creatAnimation(STATE.REMOVEING, STATE.REMOVEING, R.id.btn_delete, start, end, duration);
				mHandler.post(animation);
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.left_btn_delete:
			showDelete();
			break;
		case R.id.btn_delete:
			removeItem();
			break;
		case R.id.content_area:
			if (callback != null) {
				callback.onItemClick(this);
			}
			break;
		case android.R.id.icon1:
			if (callback != null) {
				callback.endIconClick(this);
			}
			break;

		default:
			break;
		}
	}

	@Override
	public boolean onLongClick(View v) {
		if (callback != null) {
			callback.onItemLongClick(this);
		}
		return true;
	}

	@Override
	public STATE getState() {
		return currentState;
	}

	@Override
	public void setState(STATE mState) {
		if (currentState != mState) {
			currentState = mState;
			callbackState();
		}
	}

	@Override
	public void setEndIconVisibility(boolean visibility) {
		findViewById(android.R.id.icon1).setVisibility(visibility ? View.VISIBLE : View.GONE);
	}

}
