package com.taihuoniao.shop.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.params.HttpParams;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.taihuoniao.shop.R;
import com.taihuoniao.shop.ShopApp;
import com.taihuoniao.shop.ShopApp.ResultData;
import com.taihuoniao.shop.ShopApp.ShopHttpParams;
import com.taihuoniao.shop.ShopApp.UserAddress;
import com.taihuoniao.shop.ShopUtils;
import com.taihuoniao.shop.utils.ImageUtils;
import com.taihuoniao.shop.utils.StringUtils;

public class UserInfoActivity extends BaseStyleActivity {
	View user_pic;
	View user_name;
	View user_sex;
	View user_city;
	View user_address;
	ImageView avatar;
	TextView nickname;
	TextView sex;
	TextView city;
	TextView address;
	private static final int DLG_SEX = 1;
	private String[] sex_items;
	private ShopApp.UserInfo mUserInfo;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_info);
		showBackButton(true);
		setTitle(R.string.title_activity_user_info);
		user_pic = findViewById(R.id.user_pic);
		user_name = findViewById(R.id.user_name);
		user_sex = findViewById(R.id.user_sex);
		user_city = findViewById(R.id.user_city);
		user_address = findViewById(R.id.user_address);
		avatar = (ImageView) findViewById(R.id.avatar);
		nickname = (TextView) findViewById(R.id.nickname);
		sex = (TextView) findViewById(R.id.sex);
		city = (TextView) findViewById(R.id.city);
		address = (TextView) findViewById(R.id.address);
		sex_items = getResources().getStringArray(R.array.select_sex);
		
		user_pic.setOnClickListener(mClickListener);
		user_name.setOnClickListener(mClickListener);
		user_sex.setOnClickListener(mClickListener);
		user_city.setOnClickListener(mClickListener);
		user_address.setOnClickListener(mClickListener);
		
	}

	protected void onResume() {
		super.onResume();
		mUserInfo = ShopApp.self().getUserInfo();
		showPic();
		showInfo();
		ShowAddress();
		getUserInfo(null);
		getAddress();
	}
	private void showPic(){
		mUserInfo.avatar = ShopApp.self().getUserInfo().avatar;
		ShopApp.self().showImageAsyn(avatar, mUserInfo.avatar,
				R.drawable.image_loading_background);
	}
	private void showInfo(){
		nickname.setText(mUserInfo.nickname);
		if (mUserInfo.sex >= sex_items.length || mUserInfo.sex < 0) {
			mUserInfo.sex = 0;
		}
		sex.setText(sex_items[mUserInfo.sex]);
		if (mUserInfo.city != null)
			city.setText(mUserInfo.city);				
	}
	private void ShowAddress(){
		UserAddress [] addresses = ShopApp.self().getUserAddresses();
		int addr = (addresses == null)?0:addresses.length;
		address.setText(""+addr);		
	}
	
	View.OnClickListener mClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v == user_pic) {
				UserInfoActivity.this.showAlertDialog(v.hashCode());
			} else if (v == user_name) {
				UserInfoActivity.this.showAlertDialog(v.hashCode());
			} else if (v == user_sex) {
				UserInfoActivity.this.showAlertDialog(v.hashCode());
			} else if (v == user_city) {
				UserInfoActivity.this.showAlertDialog(v.hashCode());
			} else if (v == user_address) {
				UserInfoActivity.this.showAlertDialog(v.hashCode());
			}
		}
	};
	AlertDialog mCurrentDlg;
	
	private void hideDialog() {
		if (mCurrentDlg != null){
			mCurrentDlg.dismiss();
			mCurrentDlg = null;
		}
	}

	protected void showAlertDialog(int id) {
		if(id == user_pic.hashCode()){
			mCurrentDlg = new AlertDialog.Builder(UserInfoActivity.this)
			.setItems(R.array.select_pic,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {							
							UserInfoActivity.this.choicePic(whichButton);
						}
					}).create();
			Window window = mCurrentDlg.getWindow();     
			window.setGravity(Gravity.BOTTOM);
			window.setLayout(android.view.WindowManager.LayoutParams.MATCH_PARENT, 
					android.view.WindowManager.LayoutParams.WRAP_CONTENT);
		} else if (id == user_name.hashCode()) {
            LayoutInflater factory = LayoutInflater.from(this);
            final View textEntryView = factory.inflate(R.layout.dlg_text_entry, null);
            final EditText username_edit = (EditText)textEntryView.findViewById(R.id.username_edit);
            username_edit.setText(mUserInfo.nickname);					
            username_edit.setSelection(username_edit.getText().length());
           	mCurrentDlg = new AlertDialog.Builder(UserInfoActivity.this)
			.setTitle(R.string.dialog_input_nickname)
			.setView(textEntryView)
	        .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	String nickname = username_edit.getText().toString();
                    	if(nickname.length() < 1){
                    		Toast.makeText(UserInfoActivity.this,R.string.warning_null_string , 5000);
                    		return;
                    	}
                    	mUserInfo.nickname = nickname;
                    	UserInfoActivity.this.hideDialog();
                    	uploadUserInfo();
                    }
                })
            .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
			.create();
		} else if (id == user_sex.hashCode()){
			mCurrentDlg = new AlertDialog.Builder(UserInfoActivity.this)
			.setSingleChoiceItems(R.array.select_sex, mUserInfo.sex,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							mUserInfo.sex = whichButton;
							sex.setText(sex_items[mUserInfo.sex]);
							UserInfoActivity.this.hideDialog();
							uploadUserInfo();
						}
					}).create();			
		} else if (id == user_city.hashCode()){
            LayoutInflater factory = LayoutInflater.from(this);
            final View textEntryView = factory.inflate(R.layout.dlg_text_entry, null);
            final EditText username_edit = (EditText)textEntryView.findViewById(R.id.username_edit);
            username_edit.setText(mUserInfo.city);					
            username_edit.setSelection(username_edit.getText().length());
			mCurrentDlg = new AlertDialog.Builder(UserInfoActivity.this)
			.setTitle(R.string.dialog_input_city)
			.setView(textEntryView)
	        .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	String city = username_edit.getText().toString();
                    	if(city.length() < 1){
                    		Toast.makeText(UserInfoActivity.this,R.string.warning_null_string , 5000);
                    		return;
                    	}                    	
                    	mUserInfo.city = city;
                    	UserInfoActivity.this.hideDialog();
                    	uploadUserInfo();
                    }
                })
            .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
			.create();			
		}else if(id == user_address.hashCode()){
			Intent intent = new Intent();
			intent.setClass(this, UserAddressActivity.class);
			startActivity(intent);
			return;
		}
		
		if (mCurrentDlg != null)
			mCurrentDlg.show();
	}
	private final static int CROP = 200;
	private final static String FILE_SAVEPATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Portrait/";
	private Uri origUri;
	private Uri cropUri;
	private File protraitFile;
	private Bitmap protraitBitmap;
	private String protraitPath;
	
	
	private void choicePic(int way){
		hideDialog();
		//判断是否挂载了SD卡
		String storageState = Environment.getExternalStorageState();		
		if(storageState.equals(Environment.MEDIA_MOUNTED)){
			File savedir = new File(FILE_SAVEPATH);
			if (!savedir.exists()) {
				savedir.mkdirs();
			}
		}					
		else{
			Toast.makeText(UserInfoActivity.this, "无法保存上传的头像，请检查SD卡是否挂载",5000);
			return;
		}
		
		File savedir = new File(FILE_SAVEPATH);
		if (!savedir.exists()) {
			savedir.mkdirs();
		}
		//输出裁剪的临时文件
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		//照片命名
		String origFileName = "thn_" + timeStamp + ".jpg";
		String cropFileName = "thn_crop_" + timeStamp + ".jpg";
		
		//裁剪头像的绝对路径
		protraitPath = FILE_SAVEPATH + cropFileName;
		protraitFile = new File(protraitPath);
		
		origUri = Uri.fromFile(new File(FILE_SAVEPATH, origFileName));
		cropUri = Uri.fromFile(protraitFile);		
		Log.i("origUri",origUri.toString());
		Log.i("cropUri",cropUri.toString());		
		if(way == 0){//拍照
			startActionCamera(origUri);	
		}else if(way == 1){//从相册选择
			startActionPickCrop(cropUri);					
		}		
	}
	
	/**
	 * 选择图片裁剪
	 * @param output
	 */
	private void startActionPickCrop(Uri output) {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		intent.putExtra("output", output);
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);// 裁剪框比例
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", CROP);// 输出图片大小
		intent.putExtra("outputY", CROP);
		startActivityForResult(Intent.createChooser(intent, "选择图片"),ImageUtils.REQUEST_CODE_GETIMAGE_BYSDCARD);
	}
	/**
	 * 相机拍照
	 * @param output
	 */
	private void startActionCamera(Uri output) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, output);
		startActivityForResult(intent, ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA);
	}
	/**
	 * 拍照后裁剪
	 * @param data 原始图片
	 * @param output 裁剪后图片
	 */
	private void startActionCrop(Uri data, Uri output) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(data, "image/*");
		intent.putExtra("output", output);
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);// 裁剪框比例
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", CROP);// 输出图片大小
		intent.putExtra("outputY", CROP);
		startActivityForResult(intent, ImageUtils.REQUEST_CODE_GETIMAGE_BYCROP);
	}
	/**
	 * 上传新照片
	 */
	private void uploadNewPhoto() {
    	//获取头像缩略图
    	if(!StringUtils.isEmpty(protraitPath) && protraitFile.exists())
    	{
    		protraitBitmap = ImageUtils.loadImgThumbnail(protraitPath, 200, 200);
    	}
        
		if(protraitBitmap != null)
		{						
			ShopHttpParams hp = ShopApp.self().doUpdateUserAvatar(protraitFile);
			sendUrlRequest(hp);
		}
    }
	
	@Override 
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data)
	{ 
    	if(resultCode != RESULT_OK) return;    	
    	switch(requestCode){
    		case ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA:
    			startActionCrop(origUri, cropUri);//拍照后裁剪
    			break;
    		case ImageUtils.REQUEST_CODE_GETIMAGE_BYSDCARD:
    		case ImageUtils.REQUEST_CODE_GETIMAGE_BYCROP:
    			uploadNewPhoto();//上传新照片
    			break;
    	}
	}
	void uploadUserInfo(){
		ShopHttpParams hp = ShopApp.self().doUpdateUserInfo(mUserInfo.nickname,""+mUserInfo.sex,mUserInfo.city,mUserInfo.job,mUserInfo.phone);
		sendUrlRequest(hp);		
	}
	void getUserInfo(String tag){
		ShopHttpParams hp = ShopApp.self().doGetUserInfo();
		hp.tag = tag;
		sendUrlRequest(hp);	
	}
	void getAddress(){
		ShopHttpParams hp = ShopApp.self().doUserAddress();
		sendUrlRequest(hp);	
	}	
	@Override
	public void onUrlSuccess(ShopHttpParams hp, ResultData result) {
		if(hp.url.startsWith(ShopUtils.getUploadUrl())){
			getUserInfo(hp.url);
		}else if(hp.url.startsWith(ShopUtils.getUpdateUserInfoUrl())){
			getUserInfo(hp.url);
		}else if(hp.url.startsWith(ShopUtils.getUserAddressUrl())){
			ShopApp.self().parseUserAddress(result);
			ShowAddress();
		}else if(hp.url.startsWith(ShopUtils.getUserInfoUrl())){
			ShopApp.self().parseUserInfo(result);
			if(hp.tag == null){
				showPic();
				showInfo();
			}else if(hp.tag.equalsIgnoreCase(ShopUtils.getUploadUrl())){
				showPic();	
			}else if(hp.tag.equalsIgnoreCase(ShopUtils.getUpdateUserInfoUrl())){
				showInfo();
			}						
		}
	}

}
