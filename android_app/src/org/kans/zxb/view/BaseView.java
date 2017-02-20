package org.kans.zxb.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;

public abstract class BaseView extends View implements OnPreDrawListener {

	public BaseView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public BaseView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public BaseView(Context context) {
		super(context, null);
	}

	private boolean isPreDraw = false;
	private boolean isInit = false;
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		onCreate();
		getViewTreeObserver().addOnPreDrawListener(this);
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		onDestroy();
	}

	@Override
	public boolean onPreDraw() {
		if(!isInit){
			isInit = true;
			init();
		}
		if(!isPreDraw){
			isPreDraw = true;
			getViewTreeObserver().removeOnPreDrawListener(this);
			return true;
		}
		return false;
	}
	

	protected abstract void onCreate();
	protected abstract void onDestroy();
	protected abstract void init();
}
