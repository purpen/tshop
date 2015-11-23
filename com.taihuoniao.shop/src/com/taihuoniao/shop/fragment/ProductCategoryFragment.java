package com.taihuoniao.shop.fragment;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.taihuoniao.shop.R;
import com.taihuoniao.shop.ShopApp;
import com.taihuoniao.shop.ShopApp.ShopHttpParams;
import com.taihuoniao.shop.ShopAppContentProvider;
import com.taihuoniao.shop.ShopAppContentProvider.ProductCategory;
import com.taihuoniao.shop.activity.BaseStyleActivity;
import com.taihuoniao.shop.widget.PullToRefreshGridView;

/**
 * A simple {@link Fragment} subclass.
 * 
 */
public class ProductCategoryFragment extends BaseStyleFragment {
	PullToRefreshGridView gridview;
	ProductCategoryAdapter adapter;
	OnItemClickListener mItemClick;
	private static final int REFRESH_TIME = 50000;//
	public ProductCategoryFragment(OnItemClickListener itemClick) {
		// Required empty public constructor
		mItemClick = itemClick;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_product_category, container,
				false);
		gridview = (PullToRefreshGridView) v.findViewById(R.id.gridview);
		InitGridView();
		setAdapter();
		return v;
	}
	public void setAdapter(){
		Cursor cursor = this.getActivity().getContentResolver().query(ShopAppContentProvider.PRODUCTCATEGORY_CONTENT_URI,null,null,null, null);				
		adapter = new ProductCategoryAdapter(ProductCategoryFragment.this.getActivity(),cursor);
		gridview.setAdapter(adapter);		
	}
	@Override
	public void onResume() {
		super.onResume();
		getActivity().setTitle(R.string.title_shop);
		final BaseStyleActivity bsa = (BaseStyleActivity)getActivity();
		bsa.setBackAction(null);
		getCategoryPage(-1);
	}
	private int threadCount = 0;	
	private void getCategoryPage(int pageIndexArg){
		if(lastPageTime.get(pageIndexArg)==null){
			lastPageTime.append(pageIndexArg, System.currentTimeMillis());
		}else{
			if(System.currentTimeMillis() - lastPageTime.get(pageIndexArg) < REFRESH_TIME)
				return;
			lastPageTime.put(pageIndexArg,System.currentTimeMillis());
		}
		Log.i("","getCategoryPage pageIndexArg=" + pageIndexArg);
		threadCount++;
		final int pageIndex = pageIndexArg;		
		ShopHttpParams hp = ShopApp.self().doGetProductCategory(pageIndex);
		sendUrlRequest(hp);
	}
	public void onUrlSuccess(ShopHttpParams hp, com.taihuoniao.shop.ShopApp.ResultData result){
		threadCount--;
		ShopApp.self().parseProductCategory(result);
		adapter.notifyDataSetChanged();
	};
	public void onUrlFailure(ShopHttpParams hp, com.taihuoniao.shop.ShopApp.ResultData result) {
		threadCount--;
		super.onUrlFailure(hp, result);
	};
	private SparseArray<Long>lastPageTime = new SparseArray<Long>();
	private void InitGridView(){		
		gridview.setOnItemClickListener(mItemClick);	
		gridview.setOnRefreshListener(new PullToRefreshGridView.OnRefreshListener() {
	        public void onRefresh() {
    			if(threadCount < 2)
    				getCategoryPage(1);//刷新第一页
	        }
        	@Override
        	public void onScroll(int firstIndex,int visibleCount,int totalCount) {
        		if(firstIndex + visibleCount < totalCount){//中间的页面
        			if(threadCount < 1)
        				getCategoryPage(firstIndex/ShopApp.PageData.size + 1);
        		}else{
        			if(threadCount < 2)
        				getCategoryPage(totalCount/ShopApp.PageData.size + 1);//获取下一页，如果当前页已经是最后一页，那么
        		}
        	}        
	    });
	}
													  
	static final class ProductCategoryAdapter extends CursorAdapter{
		LayoutInflater mInflater;
		ProductCategoryAdapter(Context context,Cursor cursor){
			super(context,cursor,true);
			mInflater = LayoutInflater.from(context);
		}
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = mInflater.inflate(R.layout.product_category_item, parent, false); 
			ViewHolder holder = new ViewHolder();
			holder.name = holder.title = (TextView)view.findViewById(R.id.ItemText);
			holder.image =(ImageView) view.findViewById(R.id.ItemImage);
			view.setTag(holder);			
			return view;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ViewHolder holder = (ViewHolder)view.getTag();
			ProductCategory d = new ProductCategory();
			d.ReadFromCursor(cursor);
			holder.title.setText(d.title);
			ShopApp.self().showImageAsyn(holder.image, d.app_cover_url);
			holder._id = d._id;
		}		
	};
    public static final class ViewHolder{
        public TextView name;
        public TextView title;
        public ImageView image;
        public int _id;
    }
}
