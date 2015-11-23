package com.taihuoniao.shop.fragment;

import java.util.LinkedList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.taihuoniao.shop.R;
import com.taihuoniao.shop.ShopApp;
import com.taihuoniao.shop.ShopApp.PageData;
import com.taihuoniao.shop.ShopApp.ProductComment;
import com.taihuoniao.shop.ShopApp.ResultData;
import com.taihuoniao.shop.ShopApp.ShopHttpParams;
import com.taihuoniao.shop.ShopUtils;
import com.taihuoniao.shop.widget.PullToRefreshGridView;
import com.taihuoniao.shop.widget.PullToRefreshListView;

/**
 * A simple {@link Fragment} subclass. Use the
 * {@link ProductCommentFragment#newInstance} factory method to create an
 * instance of this fragment.
 * 
 */
public class ProductCommentFragment extends BaseStyleFragment {
	public static ProductCommentFragment newInstance(int id, String content_view_url) {
		ProductCommentFragment fragment = new ProductCommentFragment();
		fragment.id = id;
		fragment.content_view_url = content_view_url;
		return fragment;
	}
	private int id;
	private String content_view_url;
	private PullToRefreshListView list;
	private EditText edit;
	private Button send;
	private ProductCommentAdapter adapter;
	public ProductCommentFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mInflater = inflater;
		View v = inflater.inflate(R.layout.fragment_product_comment, container,
				false);
		list = (PullToRefreshListView)v.findViewById(R.id.list);
		edit = (EditText)v.findViewById(R.id.edit);
		send = (Button)v.findViewById(R.id.send);
		send.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {				
				String content = edit.getText().toString();
				if(content.isEmpty()){
					ShopApp.self().showToast(ProductCommentFragment.this.getActivity(), "评论内容不能为空");
					return;
				}
				int star = 5;
				send.setEnabled(false);
				edit.setEnabled(false);
				ShopHttpParams hp = ShopApp.self().doProductComment(id, content, star);
				sendUrlRequest(hp);
			}
		});
		list.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
	        public void onRefreshHead() {
    			if(mTaskThreadCount < 2)
    				getCommentList(1);//刷新第一页
	        }
	        public void onRefreshTail() {
    			if(mTaskThreadCount < 2)
    				getCommentList(-1);//刷新下一页
	        }	        
        	@Override
        	public void onScroll(int firstIndex,int visibleCount,int totalCount) {
        		if(firstIndex + visibleCount < totalCount){//中间的页面
//        			if(threadCount < 1)
//        				getCommentList(firstIndex/ShopApp.PageData.size + 1);
        		}else{
        			if(mTaskThreadCount < 2)
        				getCommentList(totalCount/ShopApp.PageData.size + 1);//获取下一页，如果当前页已经是最后一页，那么
        		}
        	}        
	    });		
		return v;
	}
	@Override
	public void onResume() {
		super.onResume();
		getCommentList(0);
	}
	private PageData mPage = new PageData();
	private void getCommentList(int page){
		mPage.setPage(page);
		ShopHttpParams hp = ShopApp.self().getProductCommentList(id,mPage);
		sendUrlRequest(hp);	
		mTaskThreadCount++;
	}
	private int mTaskThreadCount=0;
	private List<ProductComment> comments = new LinkedList<ProductComment>();
	@Override
	public void onUrlRequestReturn(ShopHttpParams hp, ResultData result) {
		super.onUrlRequestReturn(hp, result);
		if(hp.url.startsWith(ShopUtils.getProductCommentsUrl())){
			mTaskThreadCount--;
		}
	}
	@Override
	public void onUrlSuccess(ShopHttpParams hp, ResultData result) {
		if(hp.url.startsWith(ShopUtils.getProductCommentsUrl())){
			ShopApp.self().parseProductCommentList(result);
			PageData page = (PageData)result.object;
			ProductComment []comments = (ProductComment[])page.object;

			boolean addToTail = true;

			int countRefresh = 0;
			for(int i=0; i<comments.length; i++){
				for(ProductComment pc:this.comments){
					if(pc.strId != null && comments[i].strId!=null && pc.strId.equals(comments[i].strId)){
						comments[i].strId = null;//该评论已经显示了，所以不用再次显示
					}else{
						countRefresh++;
					}
				}
			}
			if(mPage.total_page != -1 && page.current_page == 1){
				addToTail = false;
				list.onRefreshComplete("更新了"+countRefresh+"条评论");
			}			
			for(int i=0; i<comments.length; i++){
				if(comments[i].strId == null)
					continue;
				if(addToTail){
					this.comments.add(comments[i]);//添加到末尾
				}else{
					this.comments.add(0, comments[i]);//添加到开头
				}
			}
			
			page.object = null;
			mPage = page;
			if(adapter == null){
				adapter = new ProductCommentAdapter();
				list.setAdapter(adapter);
			}else{
				adapter.notifyDataSetChanged();
			}
		}else if(hp.url.startsWith(ShopUtils.getProductCommentUrl())){
			send.setEnabled(true);
			edit.setEnabled(true);
			edit.setText("");
			getCommentList(-1);
		}
	}
	@Override
	public void onUrlFailure(ShopHttpParams hp, ResultData result) {
		super.onUrlFailure(hp, result);
		if(hp.url.startsWith(ShopUtils.getProductCommentsUrl())){
		}else if(hp.url.startsWith(ShopUtils.getProductCommentUrl())){
			send.setEnabled(true);
			edit.setEnabled(true);
		}		
	}
	LayoutInflater mInflater;
	private class ProductCommentAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return comments.size();
		}

		@Override
		public Object getItem(int position) {
			return comments.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView==null){
				convertView = mInflater.inflate(R.layout.product_comment_item, parent,false);
			}
			ImageView image = (ImageView)convertView.findViewById(R.id.avatar);
			TextView nickname = (TextView)convertView.findViewById(R.id.nickname);
			TextView created_on = (TextView)convertView.findViewById(R.id.created_on);
			TextView content = (TextView)convertView.findViewById(R.id.content);
			RatingBar star = (RatingBar)convertView.findViewById(R.id.star);
			ProductComment item = comments.get(position);
			ShopApp.self().showImageAsyn(image,item.userInfo.avatar);
			nickname.setText(item.userInfo.nickname);
			created_on.setText(item.created_on);
			content.setText(item.content);
			star.setEnabled(false);
			star.setRating(item.star);
			return convertView;
		}		
	}
}
