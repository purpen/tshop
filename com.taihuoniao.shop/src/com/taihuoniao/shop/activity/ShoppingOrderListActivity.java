package com.taihuoniao.shop.activity;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

import com.taihuoniao.shop.R;
import com.taihuoniao.shop.ShopApp;
import com.taihuoniao.shop.ShopApp.ShoppingItem;
import com.taihuoniao.shop.ShopApp.ShoppingOrder;
import com.taihuoniao.shop.ShopUtils;
import com.taihuoniao.shop.R.id;
import com.taihuoniao.shop.R.layout;
import com.taihuoniao.shop.R.menu;
import com.taihuoniao.shop.ShopApp.PageData;
import com.taihuoniao.shop.ShopApp.ProductComment;
import com.taihuoniao.shop.ShopApp.ResultData;
import com.taihuoniao.shop.ShopApp.ShopHttpParams;
import com.taihuoniao.shop.fragment.ProductCommentFragment;
import com.taihuoniao.shop.fragment.ProductDetailFragment;
import com.taihuoniao.shop.fragment.ProductFAQFragment;
import com.taihuoniao.shop.widget.PullToRefreshListView;
import com.viewpagerindicator.TabPageIndicator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

public class ShoppingOrderListActivity extends BaseStyleActivity {

	private ViewPager pager;
	private PullToRefreshListView listAll;
	private PullToRefreshListView listUnpayed;
	private PullToRefreshListView listUnreceive;
	private View mViews[];
	ShoppingOrderListAdapter adapterAll;
	ShoppingOrderListAdapter adapterUnpayed;
	ShoppingOrderListAdapter adapterUnreceive;
	private LayoutInflater mInflater;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shopping_order_list);
		mInflater = LayoutInflater.from(this);
		showBackButton(true);
  		pager = (ViewPager)findViewById(R.id.pager);
  		listAll = new PullToRefreshListView(this,null);
  		listUnpayed = new PullToRefreshListView(this,null);
  		listUnreceive = new PullToRefreshListView(this,null);
  		mViews = new View[3];
  		mViews[0] = listAll;
  		mViews[1] = listUnpayed;
  		mViews[2] = listUnreceive;
  		adapterAll = new ShoppingOrderListAdapter(ORDER_ALL);
  		listAll.setAdapter(adapterAll);
  		adapterUnpayed = new ShoppingOrderListAdapter(ORDER_UNPAYED);
  		listUnpayed.setAdapter(adapterUnpayed);
  		adapterUnreceive = new ShoppingOrderListAdapter(ORDER_UNRECV);
  		listUnreceive.setAdapter(adapterUnreceive);
  		
		MyPagerAdapter adapter = new MyPagerAdapter();
		pager.setAdapter(adapter);
        TabPageIndicator indicator = (TabPageIndicator)findViewById(R.id.indicator);
        indicator.setViewPager(pager);
		getOrderList(true);  
	    listAll.setOnRefreshListener(mRefreshListener);
	    listUnpayed.setOnRefreshListener(mRefreshListener);
	    listUnreceive.setOnRefreshListener(mRefreshListener);
	}
	PullToRefreshListView.OnRefreshListener mRefreshListener = new PullToRefreshListView.OnRefreshListener() {
        public void onRefreshHead() {
			if(mTaskThreadCount < 1)
				getOrderList(true);//刷新第一页
        }
        public void onRefreshTail() {
			if(mTaskThreadCount < 1)
				getOrderList(false);//刷新第一页
        }        
    	@Override
    	public void onScroll(int firstIndex,int visibleCount,int totalCount) {
//    		if(firstIndex + visibleCount < totalCount){//中间的页面
////    			if(threadCount < 1)
////    				getCommentList(firstIndex/ShopApp.PageData.size + 1);
//    		}else{
//    			if(mTaskThreadCount < 1)
//    				getOrderList(false);
//    		}
    	}        
    };	
	
    //
    private class MyPagerAdapter extends PagerAdapter{
    	public MyPagerAdapter(){
    		
    	}  
		@Override
		public int getCount() {
			return mViews.length;
		}
    	@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
    	public CharSequence getPageTitle(int position) {
        	switch(position){
        	case 0:
        		return ShoppingOrderListActivity.this.getString(R.string.orderAll);
        	case 1:
        		return ShoppingOrderListActivity.this.getString(R.string.unPayed);
        	case 2:
        		return ShoppingOrderListActivity.this.getString(R.string.unRecv);
        	} 
        	return null;
    	}
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(mViews[position]);
		}
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(mViews[position]);
			return mViews[position];
		}
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		reShowOrder();
	}
	protected void reShowOrder(){
		adapterAll.mOrderList.clear();
		adapterUnpayed.mOrderList.clear();
		adapterUnreceive.mOrderList.clear();
		adapterAll.notifyDataSetChanged();
		adapterUnpayed.notifyDataSetChanged();
		adapterUnreceive.notifyDataSetChanged();
		getOrderList(true);		
	}
	
	private PageData mPage = new PageData();
	private void getOrderList(boolean refresh){
		if(refresh)
			mPage.setPage(1);
		else
			mPage.setPage(-1);
		ShopHttpParams hp = ShopApp.self().getShoppingOrderList(-1,mPage);
		sendUrlRequest(hp);	
		mTaskThreadCount++;
	}
	
	private int mTaskThreadCount=0;
	@Override
	public void onUrlRequestReturn(ShopHttpParams hp, ResultData result) {
		super.onUrlRequestReturn(hp, result);
		if(hp.url.startsWith(ShopUtils.getShoppingOrderListUrl())){
			mTaskThreadCount--;
			listAll.onRefreshComplete();
			listUnpayed.onRefreshComplete();
			listUnreceive.onRefreshComplete();
		}else if(hp.url.startsWith(ShopUtils.getCancelOrderUrl())){
			pager.setEnabled(true);			
		}
	}

	private ShoppingOrder findOrderByRid(String rid,List<ShoppingOrder> orderList){
		for(ShoppingOrder order:orderList){
			if(order.rid.equals(rid)){
				return order;
			}
		}
		return null;
	}
	
	@Override
	public void onUrlSuccess(ShopHttpParams hp, ResultData result) {
		if(hp.url.startsWith(ShopUtils.getShoppingOrderListUrl())){
			ShopApp.self().parseShoppingOrderList(result);
			PageData page = (PageData)result.object;
			boolean addToTail = true;
			int countRefresh = 0;
			List<ShoppingOrder> remoteOrderList = (List<ShoppingOrder>)page.object;

			if(mPage.total_page != -1 && page.current_page == 1){
				addToTail = false;
			}	
			boolean allChanged=false;
			boolean unpayedChanged=false;
			boolean unrecvChanged=false;
			for(ShoppingOrder remoteOrder:remoteOrderList){
				if(remoteOrder.rid != null){
					ShoppingOrder orderAll = findOrderByRid(remoteOrder.rid,adapterAll.mOrderList);
					ShoppingOrder orderUnpayed = findOrderByRid(remoteOrder.rid,adapterUnpayed.mOrderList);
					ShoppingOrder orderUnreceive = findOrderByRid(remoteOrder.rid,adapterUnreceive.mOrderList);
					if(addToTail){
						if(orderAll==null)
							adapterAll.mOrderList.add(remoteOrder);
						else
							orderAll.status = remoteOrder.status;
						allChanged = true;
						
						if(remoteOrder.status == ORDER_UNPAYED ){
							if(orderUnpayed == null){
								adapterUnpayed.mOrderList.add(remoteOrder);
								unpayedChanged = true;
							}
						}else if(orderUnpayed != null){//该订单已经不属于
							adapterUnpayed.mOrderList.remove(orderUnpayed);	
							unpayedChanged = true;
						}					
						if(remoteOrder.status == ORDER_UNRECV ){
							if(orderUnreceive == null){
								adapterUnreceive.mOrderList.add(remoteOrder);
								unrecvChanged =true;
							}
						}else if(orderUnreceive != null){
							adapterUnreceive.mOrderList.remove(orderUnreceive);
							unrecvChanged =true;
						}
					}else{
						if(orderAll==null)
							adapterAll.mOrderList.add(0,remoteOrder);
						else
							orderAll.status = remoteOrder.status;
						allChanged = true;
						
						if(remoteOrder.status == ORDER_UNPAYED ){
							if(orderUnpayed == null){
								adapterUnpayed.mOrderList.add(0,remoteOrder);
								unpayedChanged = true;								
							}
						}else if(orderUnpayed != null){//该订单已经不属于
							adapterUnpayed.mOrderList.remove(orderUnpayed);	
							unpayedChanged = true;
						}
						
						if(remoteOrder.status == ORDER_UNRECV){
							if(orderUnreceive == null){
								adapterUnreceive.mOrderList.add(0,remoteOrder);	
								unrecvChanged =true;								
							}
						}else if(orderUnreceive != null){
							adapterUnreceive.mOrderList.remove(orderUnreceive);
							unrecvChanged =true;
						}
					}
				}
			}	
			if(allChanged)
				adapterAll.notifyDataSetChanged();
			if(unpayedChanged){
				adapterUnpayed.notifyDataSetChanged();
			}
			if(unrecvChanged)
				adapterUnreceive.notifyDataSetChanged();
		}else if(hp.url.startsWith(ShopUtils.getCancelOrderUrl())){
			ShopApp.self().showToast(this, result.message);
			String rid = hp.tag;
			ShoppingOrder orderAll = findOrderByRid(rid,adapterAll.mOrderList);
			if(orderAll != null){
				orderAll.status = order_CANCEL;
				adapterAll.notifyDataSetChanged();
			}
			ShoppingOrder orderUnpayed = findOrderByRid(rid,adapterUnpayed.mOrderList);			
			if(orderUnpayed != null){
				adapterUnpayed.mOrderList.remove(orderUnpayed);
				adapterUnpayed.notifyDataSetChanged();
			}
		}
	}
	private static final int ORDER_ALL = -2;
	private static final int ORDER_UNPAYED = 1;
	private static final int ORDER_UNRECV = 2;
	private static final int order_CANCEL = 0;
	
	private String getStatus(int status){
		//１.待付款;   2.待发货;3.已发货;4.确认收货;5.申请退款; 6.退款成功; 9,过期订单; 9,取消订单;		
//status:状态值　-１,过期订单; ０,取消订单; １.待付款; ５.等待审核; ６.支付失败; 10.待发货; 12.申请退款; 13.退款成功;  15.已发货;  20.完成;		
		switch(status){
		case -1:
			return "过期订单";
		case 0:
			return "取消订单";
		case 1:
			return "待付款";
		case 2:
			return "待发货";
		case 3:
			return "已发货";		
		case 4:
			return "确认收货";	
		case 5:
			return "等待审核";	
		case 6:
			return "支付失败";	
		case 10:
			return "待发货";
		case 12:
			return "申请退款";
		case 13:			
			return "退款成功";	
		case 15:
			return "已发货";
		case 20:
			return "完成";
		default:
			return "未知状态";
		}
	}
	
	private class ShoppingOrderListAdapter extends BaseAdapter{
		public List<ShoppingOrder> mOrderList = new LinkedList<ShoppingOrder>();
		private int status;
		ShoppingOrderListAdapter(int status){
			this.status = status;
		}
		@Override
		public int getCount() {
			return mOrderList.size();
		}

		@Override
		public Object getItem(int position) {
			return mOrderList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView==null){
				convertView = mInflater.inflate(R.layout.shopping_order_list_item, parent,false);
			}
			TextView rid = (TextView)convertView.findViewById(R.id.rid);
			TextView realname = (TextView)convertView.findViewById(R.id.realname);
			TextView payStatus = (TextView)convertView.findViewById(R.id.payStatus);
			LinearLayout shopping_item_list = (LinearLayout)convertView.findViewById(R.id.shopping_item_list);					
			Button payNow = (Button)convertView.findViewById(R.id.payNow);
			Button cancelOrder = (Button)convertView.findViewById(R.id.cancelOrder);
			ShoppingOrder order = mOrderList.get(position);
			rid.setText(order.rid);
			realname.setText(order.realname);
			payStatus.setText(getStatus(order.status));
			payNow.setVisibility(order.status == ORDER_UNPAYED?View.VISIBLE:View.GONE);
			cancelOrder.setVisibility(order.status == ORDER_UNPAYED?View.VISIBLE:View.GONE);
			shopping_item_list.removeAllViews();
			if( order.shopping_items!=null){
				for(int i=0; i<order.shopping_items.length; i++){
					View v = mInflater.inflate(R.layout.shopping_order_list_sub_item, shopping_item_list,true);
					ImageView image = (ImageView)v.findViewById(R.id.image);
					TextView title = (TextView)v.findViewById(R.id.title);
					TextView count = (TextView)v.findViewById(R.id.count);
					TextView price = (TextView)v.findViewById(R.id.price);					
					ShoppingItem item = order.shopping_items[i];
					ShopApp.self().showImageAsyn(image, item.cover_url);
					title.setText(item.name);
					count.setText(""+item.quantity);
					DecimalFormat df=new DecimalFormat("#.##");
					price.setText("￥"+df.format(item.sale_price));					
				}
			}						
			if(payNow.getTag() == null){
				payNow.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						onPayNow((ShoppingOrder)v.getTag());
					}
				});
			}
			payNow.setTag(order);
			if(cancelOrder.getTag() == null){
				cancelOrder.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						onCancelOrder(v);
					}
				});		
			}
			cancelOrder.setTag(order);
			return convertView;
		}	
	}
	public void onPayNow(ShoppingOrder order){
		Intent intent = new Intent();
		intent.putExtra("rid",order.rid);
		intent.putExtra("pay_money",order.pay_money);
		intent.setClass(ShoppingOrderListActivity.this, ShoppingConfirmActivity.class);
		ShoppingOrderListActivity.this.startActivity(intent);					
	}
	public void onCancelOrder(View v){
		ShoppingOrder order = (ShoppingOrder)v.getTag();
		ShopHttpParams hp = ShopApp.self().cancelOrder(order.rid);
		hp.tag = order.rid;
		sendUrlRequest(hp);
		pager.setEnabled(false);
	}
}
