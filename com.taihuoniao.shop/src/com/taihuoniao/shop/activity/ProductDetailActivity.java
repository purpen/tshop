package com.taihuoniao.shop.activity;

import java.util.LinkedList;
import java.util.List;

import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TabWidget;
import android.widget.TextView;

import com.taihuoniao.shop.R;
import com.taihuoniao.shop.ShopApp;
import com.taihuoniao.shop.ShopApp.ResultData;
import com.taihuoniao.shop.ShopApp.ShopHttpParams;
import com.taihuoniao.shop.ShopAppContentProvider;
import com.taihuoniao.shop.fragment.ProductCommentFragment;
import com.taihuoniao.shop.fragment.ProductDetailFragment;
import com.taihuoniao.shop.fragment.ProductFAQFragment;
import com.viewpagerindicator.TabPageIndicator;

public class ProductDetailActivity extends BaseStyleActivity {

	private int id;
	private String content_view_url; 
	private ViewPager pager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		id = intent.getIntExtra(ShopAppContentProvider.ProductItem._ID, 0);
		content_view_url = intent.getStringExtra(ShopAppContentProvider.ProductItem.CONTENT_VIEW_URL);
		setContentView(R.layout.activity_product_detail);
		showBackButton(true);
  		pager = (ViewPager)findViewById(R.id.pager);				
		MyPagerAdapter adapter = new MyPagerAdapter(this.getSupportFragmentManager());
		pager.setAdapter(adapter);
        TabPageIndicator indicator = (TabPageIndicator)findViewById(R.id.indicator);
        indicator.setViewPager(pager);
	}
  
    //
    private class MyPagerAdapter extends FragmentPagerAdapter{
    	public MyPagerAdapter(FragmentManager fm){
    		super(fm);
    	}  

        @Override
        public Fragment getItem(int position) {
        	switch(position){
        	case 0:
        		return ProductDetailFragment.newInstance(id,content_view_url);
        	case 1:
        		return ProductCommentFragment.newInstance(id,content_view_url);
        	case 2:
        		return ProductFAQFragment.newInstance(id,content_view_url);
        	}
            return null;
        }
    	
		@Override
		public int getCount() {
			return 3;
		}

    	@Override
    	public CharSequence getPageTitle(int position) {
        	switch(position){
        	case 0:
        		return ProductDetailActivity.this.getString(R.string.product_detail);
        	case 1:
        		return ProductDetailActivity.this.getString(R.string.product_comment);
        	case 2:
        		return ProductDetailActivity.this.getString(R.string.product_faq);
        	} 
        	return null;
    	}
    }

	@Override
	public void onUrlSuccess(ShopHttpParams hp, ResultData result) {
		ShopApp.self().showToast(this, result.message);
	}
}
