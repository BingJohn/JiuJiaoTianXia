package com.tangsoft.xkr.jiujiaotianxia;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.lidroid.xutils.util.LogUtils;
import com.tangsoft.xkr.jiujiaotianxia.config.DrawContent;
import com.tangsoft.xkr.jiujiaotianxia.dialog.ShareDialog;
import com.tangsoft.xkr.jiujiaotianxia.model.ShareInfo;

import java.util.Random;


/**
 * Created by xilinch on 17-12-16.
 */

public class DrawActivity extends Activity implements SensorEventListener {

    private TextView tv_title;
    private TextView tv_introduce;
    private LinearLayout back;
    private ImageView iv1;
    private ImageView iv2;
    private ImageView iv_agin;
    private ImageView iv_share;
    private LinearLayout ll_action;
    private LinearLayout ll_guid;
    private Handler myHandler = new Handler();
    private SensorManager mSensorManager;
    public static final String TAG_URL = "TAG_URL";
    public static final String TAG_TITLE = "TAG_TITLE";
    public static final String TAG_USERID = "TAG_USERID";
    private boolean isCQ = false;
    private SoundPool soundPool;
    private int drawIndex = 1;
    private String userId = "";
    private String url = "";
    private ShareDialog mShareDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        initView();
        initListener();
    }

    private void initView(){
        tv_title = (TextView) findViewById(R.id.tv_title);
        if(getIntent() != null && getIntent().getExtras() != null){
            String title = getIntent().getExtras().getString(TAG_TITLE);
            userId = getIntent().getExtras().getString(TAG_USERID);
            url = getIntent().getExtras().getString(TAG_URL);
            if(!TextUtils.isEmpty(title)){
                tv_title.setText(title);
            }
        }
        tv_introduce = (TextView) findViewById(R.id.tv_introduce);
        back = (LinearLayout) findViewById(R.id.back);
        iv1 = (ImageView) findViewById(R.id.iv1);
        iv2 = (ImageView) findViewById(R.id.iv2);
        iv_agin = (ImageView) findViewById(R.id.iv_agin);
        iv_share = (ImageView) findViewById(R.id.iv_share);
        ll_action = (LinearLayout) findViewById(R.id.ll_action);
        ll_guid = (LinearLayout) findViewById(R.id.ll_guid);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // 为方向传感器注册监听器
        if(mSensorManager != null){
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        }
        Glide.with(DrawActivity.this).load("file:///android_asset/bf.gif").asBitmap().dontAnimate().into(iv1);

    }

    private void initListener(){
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        if(getIntent() != null && getIntent().getExtras() != null){
            final String url = getIntent().getExtras().getString("");
            tv_introduce.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(DrawActivity.this, WebViewActivity.class);
                    intent.putExtra(TAG_URL, url);
                    DrawActivity.this.startActivity(intent);
                }
            });
        }
        iv_agin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //再来一次
                long current = System.currentTimeMillis();
                ll_action.setVisibility(View.GONE);
                if(current % 2 ==0){
                    showBf();
                } else {
                    showLf();
                }
            }
        });
        iv_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareInfo shareInfo = new ShareInfo();
                shareInfo.setImgUrl("http://wx.xtyxmall.com/static/jjtx/share_logo.png");
                shareInfo.setTitle(DrawContent.share_title);
                shareInfo.setSpreadContent(DrawContent.content[drawIndex - 1]);
                String url = DrawContent.share_url.replaceFirst("@",drawIndex + "");
                url = DrawContent.share_title.replaceFirst("@",userId);
                shareInfo.setSpreadUrl(url);
                showNativeShareDialog(shareInfo);
            }
        });
        ll_guid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_guid.setVisibility(View.GONE);
            }
        });
    }


    private void showNativeShareDialog(ShareInfo shareInfo) {
        if (mShareDialog != null && !mShareDialog.isShowing()) {
            mShareDialog.setShareInfo(shareInfo);
            mShareDialog.show();
        }

    }

    /**
     * 显示前后
     */
    private synchronized void showBf(){
        Log.e("my","showBf:" + isCQ);
        if(!isCQ){
            isCQ = true;
            try {
                Glide.with(DrawActivity.this).load("file:///android_asset/bf.gif").diskCacheStrategy(DiskCacheStrategy.SOURCE).into(new GlideDrawableImageViewTarget(iv1, 1));
                iv2.setVisibility(View.INVISIBLE);
                myHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showCQ();
                    }
                }, 4750);
                playSoundDJ();
            } catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    /**
     * 显示左右
     */
    private synchronized void showLf(){
        Log.e("my","showLf:" + isCQ);
        if(!isCQ){
            isCQ = true;
            iv2.setVisibility(View.INVISIBLE);
            Glide.with(DrawActivity.this).load("file:///android_asset/lf.gif").diskCacheStrategy(DiskCacheStrategy.SOURCE).into(new GlideDrawableImageViewTarget(iv1, 1));
            myHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showCQ();
                }
            }, 4750);
            playSoundDJ();
        }

    }


    /**
     * 显示出签
     */
    private synchronized void showCQ(){
        iv2.setVisibility(View.VISIBLE);
        soundPool.stop(cqId);
        drawIndex = (int)Math.ceil(39 * (Math.random()));
        Log.i("my","drawIndex:" + drawIndex);
        Glide.with(DrawActivity.this).load("file:///android_asset/"+ drawIndex + ".gif").into(new GlideDrawableImageViewTarget(iv2, 1));
        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isCQ = false;
                ll_action.setVisibility(View.VISIBLE);
                playSoundCQ();
            }
        },1000);


    }

    private int djId = 0;
    private void playSoundDJ(){
        if(soundPool == null){
            soundPool= new SoundPool(10, AudioManager.STREAM_SYSTEM,0);
        }
        soundPool.load(this,R.raw.dj,1);
        djId = soundPool.play(1,1, 1, 0, -1, 0.4f);
    }

    private int cqId = 0;
    private void playSoundCQ(){
        if(soundPool == null){
            soundPool= new SoundPool(10, AudioManager.STREAM_SYSTEM,0);
        }
        soundPool.stop(djId);
        soundPool.load(this,R.raw.cq,1);
        cqId = soundPool.play(1,1, 1, 0, 0, 1);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent != null){
            float[] values = sensorEvent.values;
            int type = sensorEvent.sensor.getType();
            StringBuilder sb = null;
            switch (type){
                case Sensor.TYPE_ACCELEROMETER:
                    sb = new StringBuilder();
                    sb.append("加速度传感器返回数据：");
                    sb.append("X方向的加速度：");
                    sb.append(values[0]);
                    sb.append("Y方向的加速度：");
                    sb.append(values[1]);
                    sb.append("Z方向的加速度：");
                    sb.append(values[2]);
                    if(values[0] > 30 || values[0] <= -20){
                        //视为左右
                        showLf();
//                        Toast.makeText(DrawActivity.this,"左右",Toast.LENGTH_SHORT).show();
//                        System.out.println(sb.toString());
                    } else if (values[1] > 30 || values[1] <= -20 || values[1] > 30 || values[1] <= -20){
                        showBf();
//                        Toast.makeText(DrawActivity.this,"前后",Toast.LENGTH_SHORT).show();
//                        System.out.println(sb.toString());
                    }
                    break;
                case Sensor.TYPE_ORIENTATION:
                    sb = new StringBuilder();
                    sb.append("\n方向传感器返回数据：");
                    sb.append("\n绕Z轴转过的角度：");
                    sb.append(values[0]);
                    sb.append("\n绕X轴转过的角度：");
                    sb.append(values[1]);
                    sb.append("\n绕Y轴转过的角度：");
                    sb.append(values[2]);
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    sb = new StringBuilder();
                    sb.append("\n陀螺仪传感器返回数据：");
                    sb.append("\n绕X轴旋转的角速度：");
                    sb.append(values[0]);
                    sb.append("\n绕Y轴旋转的角速度：");
                    sb.append(values[1]);
                    sb.append("\n绕Z轴旋转的角速度：");
                    sb.append(values[2]);
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    sb = new StringBuilder();
                    sb.append("\n磁场传感器返回数据：");
                    sb.append("\nX轴方向上的磁场强度：");
                    sb.append(values[0]);
                    sb.append("\nY轴方向上的磁场强度：");
                    sb.append(values[1]);
                    sb.append("\nZ轴方向上的磁场强度：");
                    sb.append(values[2]);
                    break;
                case Sensor.TYPE_GRAVITY:
                    sb = new StringBuilder();
                    sb.append("\n重力传感器返回数据：");
                    sb.append("\nX轴方向上的重力：");
                    sb.append(values[0]);
                    sb.append("\nY轴方向上的重力：");
                    sb.append(values[1]);
                    sb.append("\nZ轴方向上的重力：");
                    sb.append(values[2]);
                    break;
                case Sensor.TYPE_LINEAR_ACCELERATION:
                    sb = new StringBuilder();
                    sb.append("\n线性加速度传感器返回数据：");
                    sb.append("\nX轴方向上的线性加速度：");
                    sb.append(values[0]);
                    sb.append("\nY轴方向上的线性加速度：");
                    sb.append(values[1]);
                    sb.append("\nZ轴方向上的线性加速度：");
                    sb.append(values[2]);
                    break;
                case Sensor.TYPE_AMBIENT_TEMPERATURE:
                    sb = new StringBuilder();
                    sb.append("\n温度传感器返回数据：");
                    sb.append("\n当前温度为：");
                    sb.append(values[0]);
                    break;
                case Sensor.TYPE_LIGHT:
                    sb = new StringBuilder();
                    sb.append("\n光传感器返回数据：");
                    sb.append("\n当前光的强度为：");
                    sb.append(values[0]);
                    break;
                case Sensor.TYPE_PRESSURE:
                    sb = new StringBuilder();
                    sb.append("\n压力传感器返回数据：");
                    sb.append("\n当前压力为：");
                    sb.append(values[0]);
                    break;
            }

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mSensorManager != null){
            mSensorManager.unregisterListener(this);
        }
        if(soundPool != null){
            soundPool.release();
        }
    }
}
