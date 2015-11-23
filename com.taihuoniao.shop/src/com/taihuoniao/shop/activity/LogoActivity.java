package com.taihuoniao.shop.activity;

import com.taihuoniao.shop.R;
import com.taihuoniao.shop.R.anim;
import com.taihuoniao.shop.R.layout;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN) public class LogoActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);	
		final View view = View.inflate(this, R.layout.activity_logo, null);
		setContentView(view);
        
		//渐变展示启动屏
		AlphaAnimation aa = new AlphaAnimation(0.3f,1.0f);
		aa.setDuration(2000);
		view.startAnimation(aa);
		aa.setAnimationListener(new AnimationListener()
		{
			@Override
			public void onAnimationEnd(Animation arg0) {
				redirectTo();
			}
			@Override
			public void onAnimationRepeat(Animation animation) {}
			@Override
			public void onAnimationStart(Animation animation) {}
			
		});
		
    }
    private void redirectTo(){        
        Intent intent = new Intent(this,  MainActivity.class);
        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.ICE_CREAM_SANDWICH){
            ActivityOptions opts = ActivityOptions.makeCustomAnimation(LogoActivity.this,
                    R.anim.fade, R.anim.hold);      
            startActivity(intent,opts.toBundle());        	
        }else{
            startActivity(intent);        	        	
        }

        finish();
    }
}
