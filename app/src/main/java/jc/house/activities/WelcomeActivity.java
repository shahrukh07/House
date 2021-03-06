package jc.house.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Scroller;

import jc.house.R;
import jc.house.global.Constants;
import jc.house.global.MApplication;
import jc.house.models.CustomerHelper;

public class WelcomeActivity extends Activity {
    Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0x123){
                if(Constants.APPINFO.USER_VERSION)
                    startActivity(new Intent(WelcomeActivity.this, HomeActivity.class));
                else if(!((MApplication)getApplicationContext()).isEmployeeLogin)
                    startActivity(new Intent(WelcomeActivity.this, CustomerHelperLoginActivity.class));
                else
                    startActivity(new Intent(WelcomeActivity.this, HomeActivity.class));
                WelcomeActivity.this.finish();
            }
        }
    } ;

    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_welcome);
        imageView = (ImageView)findViewById(R.id.id_imageview);
        if(!Constants.APPINFO.USER_VERSION)
            imageView.setImageResource(R.drawable.guide_customer_helper);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_welcome_image);
        animation.setFillAfter(true);
        imageView.startAnimation(animation);
        mHandler.sendEmptyMessageDelayed(0x123, 3 * 1000);
    }
}
