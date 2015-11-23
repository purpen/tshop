package com.taihuoniao.shop.fragment;

import com.taihuoniao.shop.R;
import com.taihuoniao.shop.R.id;
import com.taihuoniao.shop.R.layout;
import com.taihuoniao.shop.activity.BaseStyleActivity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * A simple {@link Fragment} subclass.
 * 
 */
public class ShopFragment extends Fragment {
	public ShopFragment() {
		
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_shop, container, false);		
		return v;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FragmentManager fm = ShopFragment.this.getChildFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ProductCategoryFragment frag = new ProductCategoryFragment(mCategoryClick);
		ft.add(R.id.childFragment, frag);
		ft.commit();
	}
	@Override
	public void onResume() {
		super.onResume();
		showBackAction();
	}
	
	class CategoryClickListener implements OnItemClickListener {
		public void onItemClick(AdapterView<?> arg0,// The AdapterView where the
													// click happened
				View arg1,// The view within the AdapterView that was clicked
				int arg2,// The position of the view in the adapter
				long arg3// The row id of the item that was clicked
		){
			ProductCategoryFragment.ViewHolder holder = (ProductCategoryFragment.ViewHolder)arg1.getTag();
			ShowProductListFragment(holder.title.getText().toString(),holder._id);
		}
	};
	CategoryClickListener mCategoryClick = new CategoryClickListener();
	
	ProductListFragment mProductListFragment;
	private void showBackAction(){
		final BaseStyleActivity bsa = (BaseStyleActivity)getActivity();
		if(bsa != null){
			if(mProductListFragment == null || mProductListFragment.isDetached()){
				bsa.setBackAction(null);
			}else{
				bsa.setBackAction(new Runnable() {	
					@Override
					public void run() {		
						FragmentManager fm = ShopFragment.this.getChildFragmentManager();
						fm.popBackStack();
					}
				});				
			}
		}
	}
	public void ShowProductListFragment(String title,int category_id){
		FragmentManager fm = ShopFragment.this.getChildFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		if(mProductListFragment == null ){
			mProductListFragment = new ProductListFragment();
			ft.replace(R.id.childFragment, mProductListFragment);			
		}else {
			ft.replace(R.id.childFragment,mProductListFragment);
		}
		mProductListFragment.setCategory(category_id, title, -1);
		ft.addToBackStack(null);
		ft.commit();
		showBackAction();
	}
}
	
