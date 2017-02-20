package org.kans.zxb.ui;

import org.kans.zxb.R;
import org.kans.zxb.view.TimePicker;
import org.kans.zxb.view.TimePicker.TimeChangeListener;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class TimePickerUI implements TimePicker.TimeChangeListener, OnClickListener, OnCheckedChangeListener{
	private BirthdayTimeCallBack mBirthdayTimeCallBack;
	private int hour;
	private int min;
	private Context context;
	private Dialog mDialog;
	private View rootView;
	private TimePicker timePicker;
	private Button okButton,cancelButton;
	private int type;
	private View checkLayout;
	private final CheckBox[] checkBox = new CheckBox[6];
	public TimePickerUI(int hour, int min,
			Context context) {
		super();
		this.hour = hour;
		this.min = min;
		this.context = context;
		initUi(false);
	}
	public TimePickerUI(int hour, int min,int type,Context context){
		super();
		this.hour = hour;
		this.min = min;
		this.type = type;
		this.context = context;
		initUi(true);
	}
	public void setTime(int hour, int min){
		timePicker.setTime(hour, min);
	}
	public void setTime(int hour, int min,int type){
		timePicker.setTime(hour, min);
		this.type = type;
		if(checkBox.length>0){
			for(int i=0;i<checkBox.length;i++){
				CheckBox mCheckBox = checkBox[i];
				mCheckBox.setChecked(((type>>(checkBox.length-1-i))&0x1)==1);
			}
		}
	}
	
	
	public void initUi(boolean showCheckBox){
		
		if(rootView==null){
			rootView = View.inflate(context, R.layout.birthday_time_picker_dialog, null) ;
			timePicker = (TimePicker) rootView.findViewById(R.id.birthday_time_pick_dialog_time_picker);
			timePicker.setTimeChangeListener(this);
			timePicker.setTime(hour, min);
			okButton = (Button) rootView.findViewById(R.id.birthday_time_pick_dialog_button_ok);
			okButton.setOnClickListener(this);
			cancelButton = (Button) rootView.findViewById(R.id.birthday_time_pick_dialog_button_cancel);
			cancelButton.setOnClickListener(this);
			checkLayout = rootView.findViewById(R.id.birthday_time_pick_dialog_checkboxs);
			if(showCheckBox){
				checkLayout.setVisibility(View.VISIBLE);
				checkBox[0] = (CheckBox) rootView.findViewById(R.id.birthday_time_pick_dialog_checkbox_current_day);
				checkBox[1] = (CheckBox) rootView.findViewById(R.id.birthday_time_pick_dialog_checkbox_one_day);
				checkBox[2] = (CheckBox) rootView.findViewById(R.id.birthday_time_pick_dialog_checkbox_two_day);
				checkBox[3] = (CheckBox) rootView.findViewById(R.id.birthday_time_pick_dialog_checkbox_one_week);
				checkBox[4] = (CheckBox) rootView.findViewById(R.id.birthday_time_pick_dialog_checkbox_two_week);
				checkBox[5] = (CheckBox) rootView.findViewById(R.id.birthday_time_pick_dialog_checkbox_one_month);
				for(int i=0;i<checkBox.length;i++){
					CheckBox mCheckBox = checkBox[i];
					mCheckBox.setOnCheckedChangeListener(this);
					mCheckBox.setChecked(((type>>(checkBox.length-1-i))&0x1)==1);
				}
			}else{//����checkbox����
				checkLayout.setVisibility(View.GONE);
			}
		}			
		
		mDialog = new Dialog(context, R.style.Dialog_NoActionBar);
		mDialog.setContentView(rootView);
		mDialog.setCancelable(false);
	}

	public void show(){
		if(mDialog!=null&&!mDialog.isShowing()){
			mDialog.show();
		}
	}
	
	public void dismiss(){
		if(mDialog!=null&&mDialog.isShowing()){
			mDialog.dismiss();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.birthday_time_pick_dialog_button_ok:
			if(mDialog.isShowing()){
				mDialog.dismiss();
			}
			if(mBirthdayTimeCallBack!=null){
				mBirthdayTimeCallBack.onTimeChage(hour, min ,type);
			}
			break;

		case R.id.birthday_time_pick_dialog_button_cancel:
			if(mDialog.isShowing()){
				mDialog.dismiss();
			}
			break;
		default:
			break;
		}
		
	}
	
	public void setBirthdayTimeCallBack(BirthdayTimeCallBack mBirthdayTimeCallBack){
		this.mBirthdayTimeCallBack = mBirthdayTimeCallBack;
	}
	public interface BirthdayTimeCallBack{
		public void onTimeChage(int hour, int min ,int type);
	}
	@Override
	public void onTimeChage(TimePicker mPicker, int hour, int min) {
		this.hour = hour;
		this.min = min;
	}
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		int id = buttonView.getId();
		for(int i=0;i<checkBox.length;i++){
			if(id == checkBox[i].getId()){
				int change = (1<<(checkBox.length-1-i));
				if(isChecked){
					type = (type|change);
				}else{
					type = ((type|change)-change);
				}
			}
		}
	}
}
