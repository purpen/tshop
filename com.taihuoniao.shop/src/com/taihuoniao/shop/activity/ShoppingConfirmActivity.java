package com.taihuoniao.shop.activity;

import java.text.DecimalFormat;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.taihuoniao.shop.PayResult;
import com.taihuoniao.shop.R;
import com.taihuoniao.shop.ShopApp;
import com.taihuoniao.shop.ShopApp.ResultData;
import com.taihuoniao.shop.ShopApp.ShopHttpParams;

public class ShoppingConfirmActivity extends BaseStyleActivity {
	private static final String TAG = "ShoppingConfirmActivity";
	private String mResult;
	private String rid;
	private double pay_money;
	private String payaway = "alipay";
	
	private TextView ridText;
	private TextView priceText;
	private TextView paywayText;
	private Button buttonPay;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shopping_confirm);
		ridText = (TextView)findViewById(R.id.textViewOrderRid);
		priceText = (TextView)findViewById(R.id.textViewOrderTotalPay);
		paywayText = (TextView)findViewById(R.id.textViewOrderPayWay);
		buttonPay = (Button)findViewById(R.id.buttonPay);	
		showBackButton(true);
		Intent intent = getIntent();
		rid = intent.getStringExtra("rid");
		pay_money = intent.getDoubleExtra("pay_money", 0f);
		buttonPay.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				buttonPay.setEnabled(false);
				ShoppingPayedTask task = new ShoppingPayedTask();
				task.execute();
			}
		});
		ridText.setText(rid);
		DecimalFormat df=new DecimalFormat("#.##");
		priceText.setText("￥"+df.format(pay_money));
	}
	
	@Override
	public void onUrlSuccess(ShopHttpParams hp, ResultData result) {
		super.onUrlFailure(hp, result);
	}	
//{"success":true,"is_error":false,"status":"0","message":"OK",
//	"data":{
//		"str":"_input_charset=utf-8&body=%E5%A4%AA%E7%81%AB%E9%B8%9F%E5%95%86%E5%9F%8E115110302877%E8%AE%A2%E5%8D%95&it_b_pay=30m¬ify_url=http%3A%2F%2Fdev.taihuoniao.com%2Fapp%2Fapi%2Falipay%2Fsecrete_notify&out_trade_no=115110302877&partner=2088411237666512&payment_type=1&seller_id=admin%40taihuoniao.com&service=mobile.securitypay.pay&show_url=http%3A%2F%2Fdev.taihuoniao.com%2Fshop&subject=%E5%A4%AA%E7%81%AB%E9%B8%9F%E5%95%86%E5%9F%8E115110302877%E8%AE%A2%E5%8D%95&total_fee=799&sign=KCCUG3N8kahpS7xDM9ChoZJAWGVUdIWrmV9zGRZYl0GEHPnRyCnlJUN9200X2Syvv7F1XSbKXTdMjOPijHj9u2mefU3haDNX7aVKYer3zG%2Bvk9w27yeV1yUhBbenbolIIrMEdAj1M22pu5OyqKGbcvn1ynUpXskV63W21cp%2F3m4%3D&sign_type=RSA"
//		,"current_user_id":0
//			},"current_user_id":0}	
	private class ShoppingPayedTask extends AsyncTask<Void,Void,ResultData>{
		@Override
		protected ResultData doInBackground(Void... params) {
			ShopHttpParams hp = ShopApp.self().doShoppingPayed(rid,payaway);
			ResultData result = ShopApp.self().doCommonAction(hp);
			if(result.success){				
				JSONTokener jsonParser = new JSONTokener((String)result.object);
				result.object = null;
				try{
					JSONObject retobj = (JSONObject) jsonParser.nextValue();	
					JSONObject data = retobj.getJSONObject("data");
					String payInfo = data.getString("str");				
					PayTask alipay = new PayTask(ShoppingConfirmActivity.this);
					// 调用支付接口，获取支付结果
					Log.i("ShoppingConfirmActivity","PayTask.pay:"+payInfo);
					String strPayResult = alipay.pay(payInfo);					
					Log.i("ShoppingConfirmActivity","PayTask.pay result:"+strPayResult);	
					result.object = strPayResult;					
				}catch(JSONException e){
					e.printStackTrace();
				}			
			}
			return result;
		}
		@Override
		protected void onPostExecute(ResultData result) {		
			super.onPostExecute(result);
			buttonPay.setEnabled(true);
			if(!result.success || result.object == null){
				ShopApp.showToast(ShoppingConfirmActivity.this, result.message);
				return;
			}
			String strPayResult = (String)result.object;
			PayResult payResult = new PayResult(strPayResult);
			// 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
			String resultInfo = payResult.getResult();

			String resultStatus = payResult.getResultStatus();
			
			String mmo = payResult.getMemo();

			// 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
			if (TextUtils.equals(resultStatus, "9000")) {
				Toast.makeText(ShoppingConfirmActivity.this, "支付成功",
						Toast.LENGTH_SHORT).show();
				ShoppingConfirmActivity.this.finish();
			} else {
				// 判断resultStatus 为非“9000”则代表可能支付失败
				// “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
				if (TextUtils.equals(resultStatus, "8000")) {
					Toast.makeText(ShoppingConfirmActivity.this, "支付结果确认中",
							Toast.LENGTH_SHORT).show();

				} else {
					// 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
					Toast.makeText(ShoppingConfirmActivity.this, "支付失败: " + resultStatus + " " + mmo,
							Toast.LENGTH_SHORT).show();

				}
			}			
//			Intent intent = new Intent();
//			intent.putExtra("result", (String)result.object);
//			intent.setClass(ShoppingConfirmActivity.this, ShoppingConfirmActivity.class);
//			ShoppingConfirmActivity.this.startActivity(intent);
//			ShoppingConfirmActivity.this.finish();
		}
	}	
}
