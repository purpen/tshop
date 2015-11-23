package com.taihuoniao.shop.widget;

import com.taihuoniao.shop.R;
import com.taihuoniao.shop.ShopApp;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class CustomRecyleView extends FrameLayout {

	Gallery gallery;
	LinearLayout line;
    public CustomRecyleView(Context context) {
        super(context);
    }
    
    public CustomRecyleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomRecyleView(Context context, AttributeSet attrs, int defStyle) {
    	super(context,attrs,defStyle);
    }
    boolean mInit = false;
    private void Init(Context context){
    	if(!mInit){
	    	gallery = new Gallery(context);
	    	this.addView(gallery,new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
	    	line = new LinearLayout(context);
	    	line.setOrientation(LinearLayout.HORIZONTAL);
	    	LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	    	params.gravity = Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL;
	    	params.bottomMargin = (int)(context.getResources().getDisplayMetrics().density * 5);
	    	this.addView(line,params); 
	    	//首尾必须是循环，当移动到最后一张图片，调整到第二张图片，当移动到第一张图片，调整到倒数第2张图片
	    	//所以，图片至少3张，最后一张图片要和第二张一样，第一张图片要和倒数第二张图片一样
	        gallery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
	        	@Override
	        	public void onItemSelected(AdapterView<?> parent, View view,
	        			int position, long id) {
	        		if(mImages == null)
	        			return;
	        		if(position == mImages.length - 1){
	        			gallery.setSelection(1);
	        			setPageIndicateFocus(0);
	        		}else if(position == 0){
	        			gallery.setSelection(mImages.length - 2);
	        			setPageIndicateFocus(mImages.length - 1);
	        		}else{
	        			setPageIndicateFocus(position - 1);
	        		}
	        	}
	        	@Override
	        	public void onNothingSelected(AdapterView<?> parent) {
	        		gallery.setSelection(1);//从第二张图片开始显示
	        	}
			});	   
	    	mInit = true;	    		        
    	}
    }

    private ImageAdapter adapter;
	private ImageView[] mImages;
	private ImageView[] mPageIndicates;
	     
    public void SetCircleImageViews(ImageView[] images){
    	//首尾必须是循环的，当移动到最后一张图片，调整到第二张图片，当移动到第一张图片，调整到倒数第2张图片
    	//所以，图片至少3张，最后一张图片要和第二张一样，第一张图片要和倒数第二张图片一样    	
        Init(getContext());
        mImages = images;
    	adapter = new ImageAdapter();
    	gallery.setAdapter(adapter);
    }
    public void showNetworkUrl(String []urls){
    	if(urls == null || urls.length == 0)
    		return;
    	Init(getContext());
    	mImages = new ImageView[urls.length + 2];
    	for(int i=0; i<mImages.length; i++){
    		mImages[i] = new ImageView(getContext());
    		//mImages[i].setBackgroundColor(0xFF000000 + i * 256 * 64);
    		if(i==0){
    			ShopApp.self().showImageAsyn(mImages[i], urls[urls.length-1]);
    		}else if(i == mImages.length -1){
    			ShopApp.self().showImageAsyn(mImages[i], urls[0]);
    		}else{
    			ShopApp.self().showImageAsyn(mImages[i], urls[i-1]);
    		}
    	}
    	adapter = new ImageAdapter();
    	gallery.setAdapter(adapter);	
    }
    
    private void setPageIndicateFocus(int position){
    	if(mPageIndicates == null || mImages == null || mImages.length != mPageIndicates.length + 2){  
    		if(mPageIndicates != null){
    			line.removeAllViews();
    			mPageIndicates = null;
    		}
    		if(mImages == null)
    			return;
        	int indicateLength = mImages.length - 2;
        	if(indicateLength > 0){
        		mPageIndicates = new ImageView[indicateLength];
        		for(int i=0; i<mPageIndicates.length; i++){
        			mPageIndicates[i] = new ImageView(this.getContext());
        			mPageIndicates[i].setImageResource(R.drawable.page_indicator_unfocused);
        			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        	    	int margin = (int)(getContext().getResources().getDisplayMetrics().density * 5);
        	    	params.setMargins(margin, margin, margin, margin);
        	    	params.leftMargin = margin;
        	    	params.rightMargin = margin;
        			line.addView(mPageIndicates[i],params);
        		}
        	}    		
    	}
    	for(int i=0; i<mPageIndicates.length; i++){
    		if(mPageIndicates[i] != null){
	    		mPageIndicates[i].setImageResource(i == position?R.drawable.page_indicator_focused:
	    			R.drawable.page_indicator_unfocused);	    			
    		}
    	}
    }
    
    @Override
    protected void onAttachedToWindow() {    	
    	super.onAttachedToWindow();    	
    }
    
    public class ImageAdapter extends BaseAdapter {
       public ImageAdapter() {
        }
        public int getCount() {
            return mImages.length;
        }
        public Object getItem(int position) {
            return position;
        }
        public long getItemId(int position) {
            return position;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                convertView = mImages[position];
                imageView = (ImageView) convertView;
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                imageView.setLayoutParams(new Gallery.LayoutParams(Gallery.LayoutParams.WRAP_CONTENT,Gallery.LayoutParams.MATCH_PARENT));
            } else {
                imageView = (ImageView) convertView;
            }
            return imageView;
        }
    }   
    
}
