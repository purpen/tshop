package com.taihuoniao.shop.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class TabViewPager extends LinearLayout {

    public TabViewPager(Context context) {
        super(context);
    }
    
    public TabViewPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabViewPager(Context context, AttributeSet attrs, int defStyle) {
    	super(context,attrs,defStyle);
    }
    //唯一接口，用于添加一个页面，注意，需要填写对应标题
    public void addPage(View page,String title){
    	
    }
    private boolean mInit = false;    
    private LinearLayout mTitleLayout;
    private ViewPager mPager;
    private void Init(Context context){
    	if(!mInit){
    		mTitleLayout = new LinearLayout(context);
    		mTitleLayout.setOrientation(LinearLayout.HORIZONTAL);
    		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
    		this.addView(mTitleLayout,params);
    		mPager = new ViewPager(context);
    		params.height = 0;
    		params.weight = 1;
    		this.addView(mPager,params);
    	}
    }
}
