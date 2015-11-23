package com.taihuoniao.shop.activity;

import java.text.DecimalFormat;

import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.taihuoniao.shop.R;
import com.taihuoniao.shop.ShopApp;
import com.taihuoniao.shop.ShopApp.ResultData;
import com.taihuoniao.shop.ShopApp.ShopHttpParams;
import com.taihuoniao.shop.ShopApp.ShoppingOrder;
import com.taihuoniao.shop.ShopAppContentProvider;
import com.taihuoniao.shop.ShopAppContentProvider.CartItem;
import com.taihuoniao.shop.ShopAppContentProvider.ProductItem.SKU;
import com.taihuoniao.shop.ShopUtils;
import com.taihuoniao.shop.widget.CustomRecyleView;
import com.taihuoniao.shop.widget.SimpleNumberPicker;

public class ProductViewActivity extends BaseStyleActivity {

	private int id;
	private String content_view_url;
	private CustomRecyleView custom;
	private SimpleNumberPicker numberPicker;
	private TextView title;
	private TextView price;
	private TextView designer;
	private ImageView desiger_pic;
	private ToggleButton favorite;
	private ToggleButton love;
	private Button add_to_cart;
	private Button buy;
	private View skuLayout;
	private RadioGroup skuGroup;
	private int sku_id = 0;
	private String sku_mode;
	private int sku_stage;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_product_view);
		showBackButton(true);
		Intent intent = getIntent();
		id = intent.getIntExtra(ShopAppContentProvider.ProductItem._ID, 0);
		TextView textView = (TextView)findViewById(R.id.textView1);
		textView.setText(""+id);
		custom = (CustomRecyleView)findViewById(R.id.customRecyleView);
		numberPicker = (SimpleNumberPicker)findViewById(R.id.numberPicker);
		numberPicker.setMinValue(1);
		numberPicker.setMaxValue(99);
		numberPicker.setValue(1);
		numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		title = (TextView)findViewById(R.id.title);
		price = (TextView)findViewById(R.id.price);
		designer = (TextView)findViewById(R.id.designer);
		desiger_pic = (ImageView)findViewById(R.id.designer_pic);
		favorite  = (ToggleButton)findViewById(R.id.favorite);
		love = (ToggleButton)findViewById(R.id.love);
		favorite.setChecked(false);
		favorite.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(!ShopApp.self().testLogin(ProductViewActivity.this))
					return;
				// TODO Auto-generated method stub
				boolean isChecked = favorite.isChecked();
				favorite.setEnabled(false);
				ShopHttpParams hp  = ShopApp.self().doProductCommonAction(id,!isChecked?ShopUtils.getProductCancelFavoriteUrl():ShopUtils.getProductFavoriteUrl());
				sendUrlRequest(hp);
			}
		});
		love.setChecked(false);
		love.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(!ShopApp.self().testLogin(ProductViewActivity.this))
					return;
				//加入到喜欢列表的代码
				boolean isChecked = love.isChecked();
				love.setEnabled(false);
				ShopHttpParams hp  = ShopApp.self().doProductCommonAction(id,!isChecked?ShopUtils.getProductCancelLoveUrl():ShopUtils.getProductLoveUrl());
				sendUrlRequest(hp);
			}
		});		
		add_to_cart = (Button)findViewById(R.id.add_to_cart);
		buy = (Button)findViewById(R.id.buy);
		skuLayout = findViewById(R.id.skusLayout);
		skuGroup = (RadioGroup)findViewById(R.id.skuGroup);
	}
	ShopAppContentProvider.ProductItem item;
	@Override
	public void onUrlSuccess(ShopHttpParams hp, ResultData result) {
		if(hp.url.startsWith(ShopUtils.getProductCancelFavoriteUrl())){
			favorite.setEnabled(true);
			favorite.setChecked(false);
		}else if(hp.url.startsWith(ShopUtils.getProductFavoriteUrl())){
			favorite.setEnabled(true);
			favorite.setChecked(true);			
		}else if(hp.url.startsWith(ShopUtils.getProductCancelLoveUrl())){
			love.setEnabled(true);
			love.setChecked(false);
		}else if(hp.url.startsWith(ShopUtils.getProductLoveUrl())){
			love.setEnabled(true);
			love.setChecked(true);			
		}else if(hp.url.startsWith(ShopUtils.getShoppingCartUrl())){
			add_to_cart.setEnabled(true);
			ShopApp.showToast(ProductViewActivity.this, result.message);
			if(item != null){
				CartItem cart_item = new ShopAppContentProvider.CartItem();
				cart_item._id = sku_id;
				cart_item.product_id = id;
				cart_item.title = cart_item.name = item.title;
				cart_item.cover = item.cover_url;
				cart_item.price = cart_item.sale_price= item.sale_price;
				cart_item.quantity = numberPicker.getValue();
				for(SKU sku:item.skus){
					if(sku._id == sku_id){			
						cart_item.mode = sku.mode;
						cart_item.price = item.sale_price;
					}
				}
				cart_item.UpdateToDataBase(this);			
			}
			
		}else if(hp.url.startsWith(ShopUtils.getShoppingNowBuyUrl())){
			JSONObject data = result.data;
			ShoppingOrder order = new ShoppingOrder();
			order.readJsonData(data);
			if(order.shopping_items != null && order.shopping_items.length > 0)
				order.shopping_items[0].mode = sku_mode;			
			Intent intent = new Intent();
			intent.putExtra(ShopAppContentProvider.ProductItem._ID, id);
			intent.putExtra(ShoppingOrderActivity.ORDER_OBJECT, order);
			order.is_nowbuy = 1;
			order.is_presaled = item.stage==5?1:0;//5为预售类型
			intent.setClass(ProductViewActivity.this, ShoppingOrderActivity.class);
			ProductViewActivity.this.startActivity(intent);	
			buy.setEnabled(true);
		}else if(hp.url.startsWith(ShopUtils.getProductViewUrl())){
			if(ShopApp.self().parseProductView(result)){
				item = (ShopAppContentProvider.ProductItem)result.object;
				String text = "title:" + item.title + "\r\nadvantage:" + item.advantage + "\r\ncontent:" + item.content + " \r\ntags:" + ShopAppContentProvider.Array2String(item.tags)
						+"\r\nasset:"+ShopAppContentProvider.Array2String(item.asset) + "\r\nvideo:" + item.video + 
						"\r\nis_favorite:"+item.is_favorite+"\r\nis_love:"+item.is_love + 
						"\r\ncover_url:" + item.cover_url + "\r\n small_avatar_url:" + item.small_avatar_url +
						"\r\nprice:"+item.sale_price + "\r\ncan_saled:" + item.can_saled;
				TextView textView = (TextView)findViewById(R.id.textView1);
				textView.setText(text);	
				custom.showNetworkUrl(item.asset);
				title.setText(item.title);
				DecimalFormat df=new DecimalFormat("#.##");
				price.setText("￥"+df.format(item.sale_price));
				favorite.setChecked(item.is_favorite == 1 ? true:false);
				love.setChecked(item.is_love == 1 ? true:false);	
				add_to_cart.setVisibility(item.can_saled==1?View.VISIBLE:View.INVISIBLE);
				buy.setVisibility(item.can_saled==1?View.VISIBLE:View.INVISIBLE);
				content_view_url = item.content_view_url;	
				if(item.skus.size()==0){
					skuLayout.setVisibility(View.GONE);
					sku_id = id;
				}else{
					skuLayout.setVisibility(View.VISIBLE);
					skuGroup.removeAllViews();
					for(SKU sku:item.skus){
						RadioButton rb = new RadioButton(this);
						rb.setLayoutParams(new RadioGroup.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
						rb.setText(sku.mode);
						rb.setId(sku._id);
						skuGroup.addView(rb);
					}
					skuGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {			
						@Override
						public void onCheckedChanged(RadioGroup group, int checkedId) {
							sku_id = checkedId;
							sku_mode = ((RadioButton)group.findViewById(sku_id)).getText().toString();
						}
					});
				}
				if(item.designer!= null){
					designer.setText(item.designer.screen_name);
					ShopApp.self().showImageAsyn(desiger_pic,item.designer.big_avatar_url);
				}
			}
		}
	}


	
	@Override
	public void onUrlFailure(ShopHttpParams hp, ResultData result) {
		super.onUrlFailure(hp, result);
		if(hp.url.startsWith(ShopUtils.getProductCancelFavoriteUrl())){
			favorite.setEnabled(true);
		}else if(hp.url.startsWith(ShopUtils.getProductFavoriteUrl())){
			favorite.setEnabled(true);	
		}else if(hp.url.startsWith(ShopUtils.getProductCancelLoveUrl())){
			love.setEnabled(true);
		}else if(hp.url.startsWith(ShopUtils.getProductLoveUrl())){
			love.setEnabled(true);		
		}else if(hp.url.startsWith(ShopUtils.getShoppingCartUrl())){
			add_to_cart.setEnabled(true);
		}else if(hp.url.startsWith(ShopUtils.getShoppingNowBuyUrl())){
			buy.setEnabled(true);
		}else if(hp.url.startsWith(ShopUtils.getProductViewUrl())){
			
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		ShopHttpParams hp = ShopApp.self().doProductView(id);
		sendUrlRequest(hp);
	}
	public void onAdd(View v){
		if(!ShopApp.self().testLogin(ProductViewActivity.this))
			return;		
		//添加到购物车的代码，还没有实现
		if(sku_id == 0){
			ShopApp.self().showToast(this, "请选择类型");
			return;
		}
		if(item != null && item.is_try == 1){
			ShopApp.self().showToast(this, "该商品为试用商品!");
			return;			
		}
		if(item != null && item.snatched == 1){//snatched == 1
			ShopApp.self().showToast(this, "请到太火鸟官网参与抢购!");
			return;			
		}		

		add_to_cart.setEnabled(false);
		ShopHttpParams hp = ShopApp.self().doShoppingCartAction(id);
		sendUrlRequest(hp);
	}

	public void onDetail(View v){
		Intent intent = new Intent();
		intent.putExtra(ShopAppContentProvider.ProductItem._ID, id);
		intent.putExtra(ShopAppContentProvider.ProductItem.CONTENT_VIEW_URL, content_view_url);
		intent.setClass(this, ProductDetailActivity.class);
		startActivity(intent);		
	}
	public void onBuy(View v){
		if(!ShopApp.self().testLogin(ProductViewActivity.this))
			return;
		if(sku_id == 0){
			ShopApp.self().showToast(this, "请选择类型");
			return;
		}
		buy.setEnabled(false);
		ShopHttpParams hp = ShopApp.self().doShoppingNowBuyAction(sku_id,numberPicker.getValue());
		sendUrlRequest(hp);
	}
}
