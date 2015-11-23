package com.taihuoniao.shop;

public class ShopUtils {
	public static final String DEV_BASE_URL = "http://dev.taihuoniao.com/app/api";
	public static final String DEV_BASE_URL_T = "http://t.taihuoniao.com/app/api";
	public static final String PAGE_BASE_URL = "http://t.taihuoniao.com/app/wap";
	public static final String ENV_BASE_URL = "http://api.taihuoniao.com";
	public static final String BASE_URL = DEV_BASE_URL;
	public static final String LOGIN = "/auth/login";
	public static final String SEND_VERIFY_CODE = "/auth/verify_code";
	public static final String REGISTER = "/auth/register";
	public static final String PRODUCT_CATEGORY = "/product/category";
	public static final String PRODUCT_LIST = "/product/getlist";
	public static final String USER_INFO = "/auth/user";
	public static final String LOGOUT = "/auth/logout";
	public static final String RESET_PASS = "/auth/find_pwd";
	public static final String UPDATE_AVATAR = "/my/update_avatar";
	public static final String UPLOAD_TOKEN = "/my/upload_token";
	public static final String UPDATE_USERINFO = "/my/update_profile";	
	public static final String USER_ADDRESS = "/shopping/address";
	public static final String EDIT_USER_ADDRESS = "/shopping/ajax_address";
	public static final String DEL_USER_ADDRESS = "/shopping/remove_address";
	public static final String GET_PROVINCES = "/shopping/ajax_provinces";
	public static final String GET_DISTRICTS = "/shopping/ajax_districts";
	public static final String PRODUCT_VIEW = "/product/view";
	public static final String PRODUCT_FAVORITE = "/product/ajax_favorite";
	public static final String PRODUCT_LOVE = "/product/ajax_love";
	public static final String PRODUCT_CANCEL_FAVORITE ="/product/ajax_cancel_favorite";
	public static final String PRODUCT_CANCEL_LOVE = "/shopping/ajax_cancel_love";
	public static final String PRODUCT_COMMENTS = "/product/comments";
	public static final String SHOPPING_CART = "/shopping/cart";
	public static final String SHOPPING_NOW_BUY = "/shopping/now_buy";
	public static final String SHOPPING_CONFIRM = "/shopping/confirm";
	public static final String SHOPPING_PAYED = "/shopping/payed";
	public static final String SHOPPING_CHECKOUT = "/shopping/checkout";
	public static final String PRODUCT_COMMENT = "/product/ajax_comment";
	public static final String SHOPPING_ORDER_LIST = "/shopping/orders";
	public static final String SHOPPING_SET_DEFAULT_ADDRESS = "/shopping/set_default_address";
	public static final String MY_CANCEL_ORDER = "/my/cancel_order";
	
	public static final String TOPIC_COMMENT = "/topic/ajax_comment";
	//
	
	public static final String TOPIC_CATEGORY = "/topic/category";
	public static final String TOPIC_GET_LIST = "/topic/getlist";	
	public static final String TOPIC_VIEW = "/topic/view";
	
	public static final String TOPIC_FAVORITE = "/topic/ajax_favorite";
	
	private static String makeUrl(String url,String path){
		String strRet = url + path;
		return strRet;
	}
	public static String getBaseUrl(){
		return BASE_URL;
	}
	public static String getLoginUrl(){
		return makeUrl(getBaseUrl(),LOGIN);
	}
	public static String getLogoutUrl(){
		return makeUrl(getBaseUrl(),LOGOUT);
	}
	public static String getSendVerifyCodeUrl(){
		return makeUrl(getBaseUrl(),SEND_VERIFY_CODE);
	}	
	public static String getRegisterUrl(){
		return makeUrl(getBaseUrl(),REGISTER);
	}	
	public static String getProductCategoryUrl(){
		return makeUrl(getBaseUrl(),PRODUCT_CATEGORY);
	}
	public static String getProductListUrl(){
		return makeUrl(getBaseUrl(),PRODUCT_LIST);
	}	
	public static String getUserInfoUrl(){
		return makeUrl(getBaseUrl(),USER_INFO);
	}	
	public static String getResetPassUrl(){
		return makeUrl(getBaseUrl(),RESET_PASS);
	}
	public static String getUploadUrl(){
		return makeUrl(getBaseUrl(),UPLOAD_TOKEN);		
	}
	public static String getUpdateUserInfoUrl(){
		return makeUrl(getBaseUrl(),UPDATE_USERINFO);		
	}	
	public static String getUserAddressUrl(){
		return makeUrl(getBaseUrl(),USER_ADDRESS);
	}
	public static String getEditUserAddressUrl(){
		return makeUrl(getBaseUrl(),EDIT_USER_ADDRESS);
	}	
	public static String getDelUserAddressUrl(){
		return makeUrl(getBaseUrl(),DEL_USER_ADDRESS);
	}	
	public static String getProvincesUrl(){
		return makeUrl(getBaseUrl(),GET_PROVINCES);
	}
	public static String getDistrictsUrl(){
		return makeUrl(getBaseUrl(),GET_DISTRICTS);
	}
	public static String getProductViewUrl(){
		return makeUrl(getBaseUrl(),PRODUCT_VIEW);
	}
	public static String getProductFavoriteUrl(){
		return makeUrl(getBaseUrl(),PRODUCT_FAVORITE);
	}
	public static String getProductLoveUrl(){
		return makeUrl(getBaseUrl(),PRODUCT_LOVE);
	}
	public static String getProductCancelFavoriteUrl(){
		return makeUrl(getBaseUrl(),PRODUCT_CANCEL_FAVORITE);
	}
	public static String getProductCancelLoveUrl(){
		return makeUrl(getBaseUrl(),PRODUCT_CANCEL_LOVE);
	}	
	public static String getShoppingCartUrl(){
		return makeUrl(getBaseUrl(),SHOPPING_CART);
	}
	public static String getShoppingNowBuyUrl(){
		return makeUrl(getBaseUrl(),SHOPPING_NOW_BUY);
	}	
	public static String getShoppingConfirmUrl(){
		return makeUrl(getBaseUrl(),SHOPPING_CONFIRM);
	}
	public static String getShoppingPayedUrl(){
		return makeUrl(getBaseUrl(),SHOPPING_PAYED);
	}
	public static String getShoppingCheckoutUrl(){
		return makeUrl(getBaseUrl(),SHOPPING_CHECKOUT);
	}
	
	public static String getProductCommentsUrl(){
		return makeUrl(getBaseUrl(),PRODUCT_COMMENTS);
	}	
	
	public static String getProductCommentUrl(){
		return makeUrl(getBaseUrl(),PRODUCT_COMMENT);
	}	
	public static String getShoppingOrderListUrl(){
		return makeUrl(getBaseUrl(),SHOPPING_ORDER_LIST);
	}
	public static String getCancelOrderUrl(){
		return makeUrl(getBaseUrl(),MY_CANCEL_ORDER);
	}
	public static String getSetDefaultAddressUrl(){
		return makeUrl(getBaseUrl(),SHOPPING_SET_DEFAULT_ADDRESS);
	}
}
