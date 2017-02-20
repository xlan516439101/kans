package org.kans.zxb.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class SquarFrameLayout extends FrameLayout {

	public SquarFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public SquarFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SquarFrameLayout(Context context) {
		super(context);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, widthMeasureSpec);
	}
}
