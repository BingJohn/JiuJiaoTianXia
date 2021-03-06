package com.tangsoft.xkr.jiujiaotianxia;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.tangsoft.xkr.jiujiaotianxia.base.BaseActivity;
import com.tangsoft.xkr.jiujiaotianxia.config.DrawContent;
import com.tangsoft.xkr.jiujiaotianxia.dialog.ShareDialog;
import com.tangsoft.xkr.jiujiaotianxia.model.ShareInfo;
import com.tangsoft.xkr.jiujiaotianxia.util.ToastUtils;


/**
 * Created by xilinch on 17-12-16.
 */

public class DrawActivity extends BaseActivity implements SensorEventListener {

    /**
     * 标题
     */
    private TextView tv_title;
    /**
     * 介绍
     */
    private TextView tv_introduce;
    /**
     * 返回
     */
    private LinearLayout back;
    /**
     * 摇动的签筒
     */
    private ImageView iv1;
    /**
     * 求签结果
     */
    private ImageView iv2;
    /**
     * 再来一次
     */
    private ImageView iv_agin;
    /**
     * 分享
     */
    private ImageView iv_share;
    /**
     * 分享和再来一次的父节点
     */
    private LinearLayout ll_action;
    /**
     * 引导页
     */
    private LinearLayout ll_guid;
    private Handler myHandler = new Handler();
    /**
     * 传感器
     */
    private SensorManager mSensorManager;
    public static final String TAG_URL = "TAG_URL";
    public static final String TAG_TITLE = "TAG_TITLE";
    public static final String TAG_USERID = "TAG_USERID";
    /**
     * 出签
     */
    private boolean isCQ = false;
    /**
     * 轻量级的声音播放
     */
    private SoundPool soundPool;
    /**
     * 出签结果
     */
    private int drawIndex = 1;
    /**
     * 用户id
     */
    private String userId = "";
    /**
     * 分享链接
     */
    private String url = "http://app.jiujtx.com/Member/game-tips2.html";
    /**
     * 分享框
     */
    private ShareDialog mShareDialog;
    /**
     * 打签播放的流id
     */
    private int djId = 0;
    /**
     * 出签播放的流id
     */
    private int cqId = 0;

    /**
     * 震动
     */
    private Vibrator vibrator;
    /**
     * 是否正在显示
     */
    private boolean isShow = false;

    /**
     * 是否点击了再来一次按钮，分享以后需要点击再来一次按钮才能继续抽签
     */
    private boolean isAgian = true;

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
        preLoadSound();
        iv1.setVisibility(View.VISIBLE);
        SharedPreferences sp = getSharedPreferences("draw", Activity.MODE_PRIVATE);
        String guide = sp.getString("guide","");
        if(TextUtils.isEmpty(guide)){
            ll_guid.setVisibility(View.VISIBLE);
        } else {
            ll_guid.setVisibility(View.GONE);
        }
//        try {
//            Glide.with(DrawActivity.this.getApplicationContext()).load(R.mipmap.first).into(iv1);
//        } catch (Exception exception){
//            exception.printStackTrace();
//        }
    }

    /**
     * 预加载声音文件
     */
    private void preLoadSound(){
        if(soundPool == null){
            soundPool= new SoundPool(10, AudioManager.STREAM_SYSTEM,0);
        }
        soundPool.load(this,R.raw.dq,1);
        soundPool.load(this,R.raw.cq,1);

    }

    private void initListener(){
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        if(getIntent() != null && getIntent().getExtras() != null){

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
//                showRandom();
                if(isAgian){
                    ToastUtils.showShort(DrawActivity.this,"请摇晃手机抽签");
                }
                isAgian = true;
                iv2.setVisibility(View.GONE);
                iv1.setImageResource(R.mipmap.first);
                iv_share.setClickable(false);
            }
        });
        iv_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareInfo shareInfo = new ShareInfo();
                shareInfo.setImgUrl("http://wx.xtyxmall.com/static/jjtx/share_logo.png");
                shareInfo.setTitle(DrawContent.share_title);
                shareInfo.setSpreadContent(DrawContent.content[drawIndex - 1]);
                String url = DrawContent.share_url.replaceFirst("@",drawIndex - 1 + "");
                url = url.replaceFirst("@",userId);
                shareInfo.setSpreadUrl(url);
                showNativeShareDialog(shareInfo);
                isAgian = false;
            }
        });
        ll_guid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_guid.setVisibility(View.GONE);
                SharedPreferences sp = getSharedPreferences("draw", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("guide","1");
                editor.commit();
                iv1.setVisibility(View.VISIBLE);
//                showRandom();
            }
        });
    }

    /**
     * 再来一次 显示一个随机的动画
     */
    private synchronized void showRandom(){
        //再来一次
        long current = System.currentTimeMillis();
        ll_action.setVisibility(View.GONE);
        if(current % 2 ==0){
            showBf();
        } else {
            showLf();
        }
    }

    /**
     * 显示分享
     * @param shareInfo
     */
    private void showNativeShareDialog(ShareInfo shareInfo) {
        if(mShareDialog == null){
            mShareDialog = new ShareDialog(DrawActivity.this);
        }
        if (mShareDialog != null && !mShareDialog.isShowing()) {
            mShareDialog.setShareInfo(shareInfo);
            mShareDialog.show();
        }

    }

    /**
     * 显示前后
     */
    private synchronized void showBf(){
//        Log.e("my","showBf:" + isCQ + "  isAgian:" + isAgian);
        if(!isCQ && isAgian){
            isCQ = true;
            iv_share.setClickable(false);
            iv_agin.setVisibility(View.GONE);
            iv_share.setVisibility(View.GONE);
            try {
                Glide.with(DrawActivity.this.getApplicationContext()).load("file:///android_asset/bf1.gif").diskCacheStrategy(DiskCacheStrategy.SOURCE).into(new GlideDrawableImageViewTarget(iv1, 1));
                iv2.setVisibility(View.INVISIBLE);
                myHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showCQ();
                    }
                }, 3250);
                myHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        soundPool.stop(djId);
                    }
                },1500);
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
//        Log.e("my","showLf:" + isCQ + "  isAgian:" + isAgian);
        if(!isCQ  && isAgian){
            isCQ = true;
            iv_share.setClickable(false);
            iv2.setVisibility(View.INVISIBLE);
            iv_agin.setVisibility(View.GONE);
            iv_share.setVisibility(View.GONE);
            try {
                Glide.with(DrawActivity.this.getApplicationContext()).load("file:///android_asset/lf1.gif").diskCacheStrategy(DiskCacheStrategy.SOURCE).into(new GlideDrawableImageViewTarget(iv1, 1));
                myHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showCQ();
                    }
                }, 3250);
                myHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        soundPool.stop(djId);
                    }
                },1500);
            } catch (Exception excaption){
                excaption.printStackTrace();
            }
            playSoundDJ();
        }

    }


    /**
     * 显示出签
     */
    private synchronized void showCQ(){
        iv2.setVisibility(View.VISIBLE);
        drawIndex = (int)Math.ceil(39 * (Math.random()));
        Log.i("my","drawIndex:" + drawIndex);
        Glide.with(DrawActivity.this.getApplicationContext()).load("file:///android_asset/"+ drawIndex + ".gif").into(new GlideDrawableImageViewTarget(iv2, 1));
        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                playSoundCQ();
            }
        },500);
        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isCQ = false;
                ll_action.setVisibility(View.VISIBLE);
                iv_agin.setVisibility(View.VISIBLE);
                iv_share.setVisibility(View.VISIBLE);
                iv_share.setClickable(true);
            }
        },1000);


    }

    /**
     * 播放打签的声音
     */
    private void playSoundDJ(){
        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                djId = soundPool.play(1,0.5f, 0.5f, 0, -1, 1f);

            }
        }, 500);

    }

    /**
     * 播放出签的声音
     */
    private void playSoundCQ(){
        if(vibrator == null){
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        }
        cqId = soundPool.play(2,1, 1, 0, 0, 1);
        vibrator.vibrate(50);
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
                    if(values[0] > 15 || values[0] <= -15 && isShow){
                        //视为左右
                        showLf();
//                        Toast.makeText(DrawActivity.this,"左右",Toast.LENGTH_SHORT).show();
//                        System.out.println(sb.toString());
                    } else if (values[1] > 15 || values[1] <= -15 && isShow){
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
    protected void onResume() {
        super.onResume();
        isShow = true;
//        Log.e("my","onResume");
    }

    @Override
    protected void onStop() {
        super.onStop();
//        Log.e("my","onStop");
        isShow = false;
        if(soundPool != null){
            soundPool.stop(cqId);
            soundPool.stop(djId);
        }
//        if(vibrator != null){
//            vibrator.cancel();
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Log.e("my","onDestroy");
        if(mSensorManager != null){
            mSensorManager.unregisterListener(this);
        }
        if(soundPool != null){
            soundPool.release();
        }

    }
}
