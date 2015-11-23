package com.taihuoniao.shop.fragment;

import java.text.DecimalFormat;

import com.taihuoniao.shop.R;
import com.taihuoniao.shop.ShopApp;
import com.taihuoniao.shop.ShopApp.ResultData;
import com.taihuoniao.shop.ShopApp.ShopHttpParams;
import com.taihuoniao.shop.ShopAppContentProvider;
import com.taihuoniao.shop.R.drawable;
import com.taihuoniao.shop.R.id;
import com.taihuoniao.shop.R.layout;
import com.taihuoniao.shop.R.string;
import com.taihuoniao.shop.ShopApp.PageData;
import com.taihuoniao.shop.ShopAppContentProvider.ProductItem;
import com.taihuoniao.shop.activity.ProductViewActivity;
import com.taihuoniao.shop.widget.PullToRefreshGridView;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * 
 */
public class ProductListFragment extends BaseStyleFragment {

	protected PullToRefreshGridView gridview;
	protected ProductListAdapter adapter;
	protected int mLayoutId;
	protected int mItemLayoutId;
	private static final int REFRESH_TIME = 5000;//
	public ProductListFragment() {
		mLayoutId = R.layout.fragment_product_list;
		mItemLayoutId = R.layout.product_list_item;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(mLayoutId, container, false);
		gridview = (PullToRefreshGridView) v.findViewById(R.id.gridview);
		InitGridView();
		return v;
	}
	protected String mTitle = null;
	protected int category_id = -1;
	protected int stick = -1;	
	public void setCategory(int categoryId,String title,int stick){
		mTitle = title;
		category_id = categoryId;
		this.stick = stick;
	}
	@Override
	public void onResume() {
		super.onResume();
		if(mTitle == null)
			getActivity().setTitle(R.string.title_product_list);
		else
			getActivity().setTitle(mTitle);		
		//if(adapter == null)		
			setAdapter();
		getListPage(-1);	
	}

	private int threadCount = 0;	
	private void getListPage(int pageIndexArg){
		if(lastPageTime.get(pageIndexArg)==null){
			lastPageTime.append(pageIndexArg, System.currentTimeMillis());
		}else{
			if(System.currentTimeMillis() - lastPageTime.get(pageIndexArg) < REFRESH_TIME)
				return;
			lastPageTime.put(pageIndexArg,System.currentTimeMillis());
		}
		Log.i("","getListPage pageIndexArg=" + pageIndexArg);
		threadCount++;
		final int pageIndex = pageIndexArg;
		ShopHttpParams hp = ShopApp.self().doGetProductList(pageIndex,category_id,stick);
		sendUrlRequest(hp);
	}
	public void onUrlSuccess(ShopHttpParams hp, ResultData result) {
		threadCount--;
		ShopApp.self().parseProductList(result, hp.tag);
		if(adapter == null || adapter.getCount() == 0)
			setAdapter();
		else{
			adapter.notifyDataSetChanged();
		}		
	};
	@Override
	public void onUrlFailure(ShopHttpParams hp, ResultData result) {
		threadCount--;
		super.onUrlFailure(hp, result);
	}
	
	private void setAdapter(){
		Context context = getActivity();
		Uri uri = ShopAppContentProvider.ProductItem.URI;
		String selection = null;
		if(stick != -1){
			selection = ShopAppContentProvider.ProductItem.STICK +"="+stick;
		}
		if(category_id != -1){
			String s = ShopAppContentProvider.ProductItem.CATEGORY_ID +"="+category_id;
			if(selection == null)
				selection = s;
			else
				selection += " and " + s;
		}
		Cursor cursor = context.getContentResolver().query(uri, null, selection, null, null);
		adapter = new ProductListAdapter(getActivity(),cursor,mItemLayoutId);
		gridview.setAdapter(adapter);
	}
	class ItemClickListener implements OnItemClickListener {
		public void onItemClick(AdapterView<?> arg0,// The AdapterView where the
													// click happened
				View arg1,// The view within the AdapterView that was clicked
				int arg2,// The position of the view in the adapter
				long arg3// The row id of the item that was clicked
		){
			ViewHolder holder = (ViewHolder)arg1.getTag();
			if(holder != null){
				Intent intent = new Intent();
				intent.setClass(getActivity(), ProductViewActivity.class);
				intent.putExtra(ShopAppContentProvider.ProductItem._ID, holder._id);
				getActivity().startActivity(intent);
			}
		}
	};
	ItemClickListener mItemClickListener = new ItemClickListener();
	private SparseArray<Long>lastPageTime = new SparseArray<Long>();
	private void InitGridView(){		
		gridview.setOnItemClickListener(mItemClickListener);	
		gridview.setOnRefreshListener(new PullToRefreshGridView.OnRefreshListener() {
	        public void onRefresh() {
    			if(threadCount < 2)
    				getListPage(1);//刷新第一页
	        }
        	@Override
        	public void onScroll(int firstIndex,int visibleCount,int totalCount) {
        		if(firstIndex + visibleCount < totalCount){//中间的页面
        			if(threadCount < 1)
        				getListPage(firstIndex/ShopApp.PageData.size + 1);
        		}else{
        			if(threadCount < 2)
        				getListPage(totalCount/ShopApp.PageData.size + 1);//获取下一页，如果当前页已经是最后一页，那么
        		}
        	}        
	    });
	}
	
	static final class ProductListAdapter extends CursorAdapter{
		LayoutInflater mInflater;
		int mItemLayoutId;
		ProductListAdapter(Context context, Cursor c,int itemLayoutId){
			super(context,c,false);
			mInflater = LayoutInflater.from(context);
			mItemLayoutId = itemLayoutId;
		}
		
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View v = mInflater.inflate(mItemLayoutId, parent,false);
			ViewHolder holder = new ViewHolder();
			holder.image =(ImageView) v.findViewById(R.id.ItemImage);
			holder.title = (TextView)v.findViewById(R.id.ItemTextTitle);
			holder.advantage = (TextView)v.findViewById(R.id.ItemAdvantage);
			holder.summary = (TextView)v.findViewById(R.id.ItemSummary);
			holder.price = (TextView)v.findViewById(R.id.ItemTextPrice);
			holder.like = (TextView)v.findViewById(R.id.ItemTextLike);

			v.setTag(holder);
			return v;
		}
		
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ViewHolder holder = (ViewHolder)view.getTag();
			ShopAppContentProvider.ProductItem item = new ShopAppContentProvider.ProductItem();
			item.ReadFromCursor(cursor);
			holder.title.setText(item.title);
			holder.advantage.setText(item.advantage);
			holder.summary.setText(item.summary);
			DecimalFormat df=new DecimalFormat("#.##");
			holder.price.setText("￥"+df.format(item.sale_price));
			//holder.price.setText(String.format("%.2f", item.sale_price));
			//holder.like.setText(Integer.toString(item.presale_people));
			holder.like.setVisibility(View.INVISIBLE);
			ShopApp.self().showImageAsyn(holder.image, item.cover_url);
			holder._id = item._id;
//			holder.price.setVisibility(item.can_saled == 1?View.VISIBLE:View.INVISIBLE);
//			holder.like.setVisibility(item.can_saled == 1?View.VISIBLE:View.INVISIBLE);
		}
	}
    public static final class ViewHolder{
        public ImageView image;
        public TextView title;
        public TextView advantage;
        public TextView summary;        
        public TextView price;
        public TextView like;
        public int _id;
    };	
}
