package com.taihuoniao.shop.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.taihuoniao.shop.R;

public class SimpleNumberPicker extends FrameLayout{
	private Button decrease;
	private Button increase;
	private TextView text;
	private int mMin;
	private int mMax;
	private int mValue;
	
	public SimpleNumberPicker(Context context) {
		super(context);
		Init(context);
	}

	public SimpleNumberPicker(Context context, AttributeSet attrs) {
		super(context, attrs, 0);
		Init(context);
	}

	public SimpleNumberPicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Init(context);
	}
	
	public void setMinValue(int min){
		mMin = min;
	}
	public void setMaxValue(int max){
		mMax = max;
	}	
	public void setValue(int v){
		mValue = v;
		if(mValue < mMin)
			mValue = mMin;		
		if(mValue > mMax)
			mValue = mMax;
		text.setText(""+mValue);
	}
	public int getValue(){
		return mValue;
	}
	
	private void Init(Context context){
		this.inflate(context, R.layout.simple_number_picker, this);
		decrease = (Button)findViewById(R.id.decrease);
		increase = (Button)findViewById(R.id.increase);
		text = (TextView)findViewById(R.id.num);
		decrease.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				mValue--;
				if(mValue < mMin)
					mValue = mMin;
				text.setText(""+mValue);
			}
		});
		increase.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				mValue++;
				if(mValue > mMax)
					mValue = mMax;
				text.setText(""+mValue);
			}
		});		
	}
	
}
