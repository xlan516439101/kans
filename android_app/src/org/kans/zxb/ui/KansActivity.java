package org.kans.zxb.ui;

import java.util.ArrayList;
import java.util.List;

import org.kans.zxb.R;
import org.kans.zxb.R.id;
import org.kans.zxb.R.layout;
import org.kans.zxb.R.menu;
import org.kans.zxb.fragment.KFragment;
import org.kans.zxb.util.KansUtils;
import org.xutils.x;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;

@ContentView(R.layout.activity_main)
public class KansActivity extends FragmentActivity {

	public interface IActivityCallBack {
		abstract boolean onBackButtonClick();

		abstract boolean onMenuButtonClick();
	}

	public static final String EXTRA_SHOW_FRAGMENT_BACK_TITLE = ":kans:show_fragment_back_title";
	public static final String EXTRA_SHOW_FRAGMENT_TITLE = ":kans:show_fragment_title";
	public static final String EXTRA_SHOW_FRAGMENT_TITLE_RES_PACKAGE_NAME = ":kans:show_fragment_title_res_package_name";
	public static final String EXTRA_SHOW_FRAGMENT_TITLE_RESID = ":kans:show_fragment_title_resid";
	public static final String EXTRA_SHOW_FRAGMENT = ":kans:show_fragment";
	public static final String EXTRA_SHOW_FRAGMENT_ARGUMENTS = ":kans:show_fragment_args";
	public static final String META_DATA_KEY_FRAGMENT_CLASS = "org.kans.zxb.FRAGMENT_CLASS";
	public static final String EXTRA_UI_OPTIONS = "kans:ui_options";
	public static final String BACK_STACK_PREFS = ":kans:prefs";

	public static final String MAIN_FRAGMENT_CLASS_NAME = "org.kans.zxb.fragment.MainFragment";

	private static final String LOG_TAG = "xlan";
	private CharSequence mInitialTitle;
	private int mInitialTitleResId;
	private String mFragmentClass = null;
	private String backString = null;

	private PopupMenu mPopupMenu;
	private List<IActivityCallBack> mIActivityCallBacks;

	@ViewInject(R.id.header_view)
	View headerView;

	@ViewInject(R.id.back_button_view)
	View backButton;

	@ViewInject(R.id.back_text_view)
	TextView backTextView;

	@ViewInject(R.id.title_text_view)
	TextView titleView;

	@ViewInject(R.id.header_menu)
	ImageView menu;

	@ViewInject(R.id.popup_view)
	FrameLayout popupView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		x.view().inject(this);
		initMetaData();
		final Intent intent = getIntent();
		if (intent.hasExtra(EXTRA_UI_OPTIONS)) {
			getWindow().setUiOptions(intent.getIntExtra(EXTRA_UI_OPTIONS, 0));
		}
		final String initialFragmentName = intent.getStringExtra(EXTRA_SHOW_FRAGMENT);
		if (initialFragmentName != null) {
			Bundle initialArguments = intent.getBundleExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS);
			setTitleFromIntent(intent);
			switchToFragment(initialFragmentName, initialArguments, mInitialTitleResId, mInitialTitle);
		} else {
			setTitleFromIntent(intent);
			switchToFragment(MAIN_FRAGMENT_CLASS_NAME, null, mInitialTitleResId, mInitialTitle);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	public PopupMenu getPopuMenu(){
		if(mPopupMenu == null){
			mPopupMenu = new PopupMenu(this, menu);
		}
		return mPopupMenu;
	}
	
    public void finish() {
        super.finish();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if(keyCode == KeyEvent.KEYCODE_BACK||keyCode == KeyEvent.KEYCODE_MENU){
    		//return true;
    	}
    	return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
    	switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			onButtonClick(backButton);
			return true;
		case KeyEvent.KEYCODE_MENU:
			onButtonClick(menu);
			return true;
		default:
			break;
		}
    	return super.onKeyUp(keyCode, event);
    }
    
	@Event({ R.id.back_button_view, R.id.header_menu, R.id.popup_view })
	private void onButtonClick(View view) {
		boolean hasIntercept = false;
		switch (view.getId()) {
		case R.id.back_button_view:
			for(IActivityCallBack i:mIActivityCallBacks){
				boolean b = i.onBackButtonClick();
				if(!hasIntercept){
					hasIntercept = b;
					break;
				}
			}
			if (!hasIntercept) {
				finish();
			}
			break;
		case R.id.header_menu:
			for(IActivityCallBack i:mIActivityCallBacks){
				boolean b = i.onMenuButtonClick();
				if(!hasIntercept){
					hasIntercept = b;
					break;
				}
			}
			if (!hasIntercept) {
				onMenu();
			}
			break;

		case R.id.popup_view:
			if (popupView.getChildCount() == 0) {
				hidePopuView();
			}
			break;

		default:
			break;
		}
	}

	public void showPopuView(View view) {
		Log.i("xlan", "showPopuView");
		KansUtils.setImeVisibility(false, this, popupView);
		if (view != null) {
			FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM);
			popupView.setVisibility(View.VISIBLE);
			popupView.addView(view, lp);
			TranslateAnimation mAnimation = new TranslateAnimation(0, 0, view.getHeight(), 0);
			mAnimation.setDuration(200);
			mAnimation.setRepeatMode(Animation.RESTART);
			mAnimation.setRepeatCount(0);
			mAnimation.setFillAfter(true);
			view.clearAnimation();
			view.setAnimation(mAnimation);
			view.getAnimation().setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					popupView.clearAnimation();
				}
			});
			mAnimation.startNow();
		}
	}

	public void hidePopuView() {
		Log.i("xlan", "hidePopuView");
		if (popupView.getVisibility() == View.VISIBLE) {
			if (popupView.getChildCount() > 0) {
				View view = popupView.getChildAt(0);
				popupView.setVisibility(View.VISIBLE);
				TranslateAnimation mAnimation = new TranslateAnimation(0, 0, 0, view.getHeight());
				mAnimation.setDuration(200);
				mAnimation.setRepeatMode(Animation.RESTART);
				mAnimation.setRepeatCount(0);
				mAnimation.setFillAfter(true);
				view.clearAnimation();
				view.setAnimation(mAnimation);
				view.getAnimation().setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						popupView.removeAllViews();
						popupView.clearAnimation();
						popupView.setVisibility(View.GONE);
					}
				});
				mAnimation.startNow();
			} else {
				popupView.clearAnimation();
				popupView.setVisibility(View.GONE);
			}
		}
	}

	private void onMenu() {

	}

	public void setMenuDrawable(int resId) {
		if(menu!=null){
			menu.setVisibility(resId > 0 ? View.VISIBLE : View.GONE);
			menu.setImageResource(resId);
		}
	}

	public void setMenuDrawable(Drawable drawable) {
		if(menu!=null){
			menu.setVisibility(drawable != null ? View.VISIBLE : View.GONE);
			menu.setImageDrawable(drawable);
		}
	}

	@Override
	public void startActivity(Intent intent) {
		Log.e("xlan", "mInitialTitle:" + mInitialTitle);
		super.startActivity(getBackButtonIntent(intent));
	}

	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		super.startActivityForResult(getBackButtonIntent(intent), requestCode);
	}

	public Intent getBackButtonIntent(Intent intent) {
		intent.putExtra(EXTRA_SHOW_FRAGMENT_BACK_TITLE, mInitialTitle);
		return intent;
	}

	@Override
	public void setTitle(CharSequence title) {
		mInitialTitle = title;
		if (titleView != null && title != null) {
			titleView.setText(title);
		}
		if(backButton != null && backTextView != null){
			if (backString != null && backString.length() > 0) {
				backButton.setVisibility(View.VISIBLE);
				backTextView.setVisibility(View.VISIBLE);
				backTextView.setText(backString);
			} else {
				backButton.setVisibility(View.GONE);
				backTextView.setVisibility(View.GONE);
				backTextView.setText("");
			}
		}

	}

	@Override
	public void setTitle(int titleId) {
		setTitle(getResources().getString(titleId));
	}

	@Override
	@Deprecated
	public void setTitleColor(int textColor) {
		if(backTextView != null){
			backTextView.setTextColor(textColor);
		}
		if(titleView != null){
			titleView.setTextColor(textColor);
		}
	}

	private void setTitleFromIntent(Intent intent) {
		final int initialTitleResId = intent.getIntExtra(EXTRA_SHOW_FRAGMENT_TITLE_RESID, -1);
		backString = intent.getStringExtra(EXTRA_SHOW_FRAGMENT_BACK_TITLE);
		if (initialTitleResId > 0) {
			mInitialTitle = null;
			mInitialTitleResId = initialTitleResId;

			final String initialTitleResPackageName = intent.getStringExtra(EXTRA_SHOW_FRAGMENT_TITLE_RES_PACKAGE_NAME);
			if (initialTitleResPackageName != null) {
				try {
					Context authContext = createPackageContext(initialTitleResPackageName, Context.CONTEXT_IGNORE_SECURITY);
					mInitialTitle = authContext.getResources().getText(mInitialTitleResId);
					setTitle(mInitialTitle);
					mInitialTitleResId = -1;
					return;
				} catch (NameNotFoundException e) {
					Log.w(LOG_TAG, "Could not find package" + initialTitleResPackageName);
				}
			} else {
				setTitle(mInitialTitleResId);
			}
		} else {
			mInitialTitleResId = -1;
			final String initialTitle = intent.getStringExtra(EXTRA_SHOW_FRAGMENT_TITLE);
			mInitialTitle = (initialTitle != null) ? initialTitle : getTitle();
			setTitle(mInitialTitle);
		}
	}

	public boolean isTopActivity() {
		return backString == null || backString.length() == 0 || backString.equals("");
	}
	
	private Fragment switchToFragment(String fragmentName, Bundle args, int titleResId, CharSequence title) {

		Fragment f = Fragment.instantiate(this, fragmentName, args);
		FragmentManager mFragmentManager = getSupportFragmentManager();
		FragmentTransaction transaction = mFragmentManager.beginTransaction();
		transaction.replace(R.id.main_content, f);

		if (titleResId > 0) {
			transaction.setBreadCrumbTitle(titleResId);
		} else if (title != null) {
			transaction.setBreadCrumbTitle(title);
		}
		transaction.commitAllowingStateLoss();
		mFragmentManager.executePendingTransactions();
		return f;
	}

	public void addIActivityCallBack(IActivityCallBack mIActivityCallBack) {
		if(mIActivityCallBacks == null){
			mIActivityCallBacks = new ArrayList<IActivityCallBack>();
		}
		if(!mIActivityCallBacks.contains(mIActivityCallBack)){
			mIActivityCallBacks.add(mIActivityCallBack);
		}
	}

	public void removeIActivityCallBack(IActivityCallBack mIActivityCallBack) {

		if(mIActivityCallBacks.contains(mIActivityCallBack)){
			mIActivityCallBacks.remove(mIActivityCallBack);
		}
	}

	private void initMetaData() {

		try {
			ActivityInfo ai = getPackageManager().getActivityInfo(getComponentName(), PackageManager.GET_META_DATA);
			if (ai != null && ai.metaData != null) {
				mFragmentClass = ai.metaData.getString(META_DATA_KEY_FRAGMENT_CLASS);
			}
		} catch (NameNotFoundException nnfe) {
			Log.d(LOG_TAG, "Cannot get Metadata for: " + getComponentName().toString());
		}
	}

	private String getStartingFragmentClass(Intent intent) {
		if (mFragmentClass != null)
			return mFragmentClass;

		String intentClass = intent.getComponent().getClassName();
		if (intentClass.equals(getClass().getName())) {
			return null;
		} else {
			return intentClass;
		}

	}

	@Override
	public Intent getIntent() {
		Intent superIntent = super.getIntent();
		String startingFragment = getStartingFragmentClass(superIntent);
		if (startingFragment != null) {
			Intent modIntent = new Intent(superIntent);
			modIntent.putExtra(EXTRA_SHOW_FRAGMENT, startingFragment);
			Bundle args = superIntent.getExtras();
			if (args != null) {
				args = new Bundle(args);
			} else {
				args = new Bundle();
			}
			args.putParcelable("intent", superIntent);
			modIntent.putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, args);
			return modIntent;
		}
		return superIntent;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void onFragmentAttached(KFragment<?, ?> mBaseFragment) {

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}
}
