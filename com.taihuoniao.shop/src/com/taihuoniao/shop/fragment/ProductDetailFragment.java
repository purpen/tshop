package com.taihuoniao.shop.fragment;

import com.taihuoniao.shop.R;
import com.taihuoniao.shop.R.layout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * A simple {@link Fragment} subclass. Use the
 * {@link ProductDetailFragment#newInstance} factory method to create an
 * instance of this fragment.
 * 
 */
public class ProductDetailFragment extends Fragment {

	public static ProductDetailFragment newInstance(int id, String content_view_url) {
		ProductDetailFragment fragment = new ProductDetailFragment();
		fragment.id = id;
		fragment.content_view_url = content_view_url;
		return fragment;
	}
	private int id;
	private String content_view_url; 
	public ProductDetailFragment() {
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	private WebView mWebView;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_product_detail, container,
				false);
		mWebView = (WebView)view.findViewById(R.id.webview);
		mWebView.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
	            view.loadUrl(url);  
	            return true;
			}
		});		
		mWebView.loadUrl(content_view_url);
		return view;
	}

//    public boolean onKeyDown(int keyCode, KeyEvent event) {  
//       if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {  
//           webview.goBack(); //goBack()表示返回WebView的上一页面  
//           return true;  
//       }  
//       return super.onKeyDown(keyCode, event);  
//    }
}
