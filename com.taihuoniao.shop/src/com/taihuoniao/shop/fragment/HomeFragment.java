package com.taihuoniao.shop.fragment;

import com.taihuoniao.shop.R;
import com.taihuoniao.shop.R.layout;
import com.taihuoniao.shop.R.string;
import com.taihuoniao.shop.activity.BaseStyleActivity;
import com.taihuoniao.shop.fragment.ProductListFragment.ItemClickListener;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

/**
 * A simple {@link Fragment} subclass.
 * 
 */
public class HomeFragment extends ProductListFragment {

	public HomeFragment() {
		mLayoutId = R.layout.fragment_home;
		mItemLayoutId = R.layout.product_list_item_big;
		stick = 1;//获取推荐的
	}
	@Override
	public void onResume() {
		super.onResume();
		getActivity().setTitle(R.string.title_home);	
		final BaseStyleActivity bsa = (BaseStyleActivity)getActivity();
		bsa.setBackAction(null);
	}
}
