package com.taihuoniao.shop.fragment;

import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import com.taihuoniao.shop.ShopApp;
import com.taihuoniao.shop.ShopApp.HttpCallBackInterface;
import com.taihuoniao.shop.ShopApp.ResultData;
import com.taihuoniao.shop.ShopApp.ShopHttpParams;

import android.support.v4.app.Fragment;

public abstract class BaseStyleFragment extends Fragment implements HttpCallBackInterface {
	public void sendUrlRequest(ShopHttpParams hp) {
		ShopApp.self().sendUrlRequest(hp, this);
	}
	@Override
	public void onUrlRequestReturn(ShopHttpParams hp, ResultData result) {
		if(result.success){
			onUrlSuccess(hp,result);
		}else{
			onUrlFailure(hp,result);
		}
	}
	abstract public void onUrlSuccess(ShopHttpParams hp,ResultData result);
	public void onUrlFailure(ShopHttpParams hp,ResultData result){
		ShopApp.self().showToast(getActivity(), result.message);
	}
}
