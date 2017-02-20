package org.kans.zxb.ui;

import org.kans.zxb.R;
import org.kans.zxb.view.DatePicker;
import org.kans.zxb.view.DatePicker.DateChangeListener;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

public class DatePickerUI implements OnCheckedChangeListener, DateChangeListener, OnClickListener, OnDismissListener{
	
	private BirthdayDateCallBack mBirthdayDateCallBack;
	private boolean isLunar;
	private int year;
	private int month;
	private int day;
	private Context context;
	private Dialog mDialog;
	private View rootView;
	private TextView showTextView;
	private Switch switch_;
	private DatePicker datePicker;
	private Button okButton,cancelButton;
	
	public DatePickerUI(boolean isLunar, int year, int month, int day,
			Context context) {
		super();
		this.isLunar = isLunar;
		this.year = year;
		this.month = month;
		this.day = day;
		this.context = context;
		initUi();
	}
	
	public void setDate(boolean isLunar,int year, int month, int day){
		this.isLunar = isLunar;
		datePicker.setDate(year, month, day);
	}
	
	public void initUi(){
		if(rootView==null){
			rootView = View.inflate(context, R.layout.birthday_data_picker_dialog, null) ;
			showTextView = (TextView) rootView.findViewById(R.id.birthday_data_pick_dialog_show_textview);
			switch_ = (Switch) rootView.findViewById(R.id.birthday_data_pick_dialog_show_switch);
			switch_.setOnCheckedChangeListener(this);
			datePicker = (DatePicker) rootView.findViewById(R.id.birthday_data_pick_dialog_date_picker);
			datePicker.setDate(year, month, day);
			datePicker.setDateChangeListener(this);
			okButton = (Button) rootView.findViewById(R.id.birthday_data_pick_dialog_button_ok);
			okButton.setOnClickListener(this);
			cancelButton = (Button) rootView.findViewById(R.id.birthday_data_pick_dialog_button_cancel);
			cancelButton.setOnClickListener(this);
		}			
		
		mDialog = new Dialog(context, R.style.Dialog_NoActionBar);
		mDialog.setContentView(rootView);
		mDialog.setOnDismissListener(this);
		mDialog.setCancelable(false);
	}

	public void show(){
		if(mDialog!=null&&!mDialog.isShowing()){
			mDialog.show();
			datePicker.setLunar(isLunar);
			switch_.setChecked(!isLunar);
			if(!isLunar){
				showTextView.setText(R.string.birthday_widget_solar);
			}else{
				showTextView.setText(R.string.birthday_widget_lundar);
			}
		}
	}
	
	public void dismiss(){
		if(mDialog!=null&&mDialog.isShowing()){
			datePicker.restore();
			mDialog.dismiss();
		}
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(rootView==null){
			return;
		}
		if(isChecked){
			showTextView.setText(R.string.birthday_widget_solar);
			datePicker.setLunar(false);
			isLunar = false;
		}else{
			showTextView.setText(R.string.birthday_widget_lundar);
			datePicker.setLunar(true);
			isLunar = true;
		}
	}

	@Override
	public void onDateChage(DatePicker mPicker, int year, int month,
			int day) {
		this.year = year;
		this.month = month;
		this.day = day;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.birthday_data_pick_dialog_button_ok:
			if(mDialog.isShowing()){
				mDialog.dismiss();
			}
			if(mBirthdayDateCallBack!=null){
				mBirthdayDateCallBack.onDateChage(isLunar, year, month, day);
			}
			break;

		case R.id.birthday_data_pick_dialog_button_cancel:
			if(mDialog.isShowing()){
				mDialog.dismiss();
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onDismiss(DialogInterface dialog) {

		if(mBirthdayDateCallBack!=null){
			mBirthdayDateCallBack.onDismiss();
		}
		
	}
	public void setBirthdayDateCallBack(BirthdayDateCallBack mBirthdayDateCallBack){
		this.mBirthdayDateCallBack = mBirthdayDateCallBack;
	}
	
	public interface BirthdayDateCallBack{
		public void onDateChage(boolean isLunar, int year, int month,int day);
		public void onDismiss();
	}

}
