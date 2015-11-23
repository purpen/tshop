package com.taihuoniao.shop.fragment;

import com.taihuoniao.shop.R;
import com.taihuoniao.shop.R.layout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass. Use the
 * {@link ProductFAQFragment#newInstance} factory method to create an instance
 * of this fragment.
 * 
 */
public class ProductFAQFragment extends Fragment {
	public static ProductFAQFragment newInstance(int id, String content_view_url) {
		ProductFAQFragment fragment = new ProductFAQFragment();
		fragment.id = id;
		fragment.content_view_url = content_view_url;
		return fragment;
	}
	private int id;
	private String content_view_url; 

	public ProductFAQFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater
				.inflate(R.layout.fragment_product_faq, container, false);
	}

}
