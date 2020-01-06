package com.sunny.livechat.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.core.content.ContextCompat;

import com.starrtc.starrtcsdk.core.player.StarWhitePanel;
import com.sunny.livechat.R;
import com.sunny.livechat.live.VideoLiveActivity;


/**
 * Desc
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2020/1/3 17:21
 */
public class LiveUtils {

    private LiveUtils() {

    }

    private static LiveUtils utils;

    //布局参数.
    private WindowManager.LayoutParams params;
    //实例化的WindowManager.
    private WindowManager windowManager;
    private FrameLayout toucherLayout;

    private float start_X = 0;
    private float start_Y = 0;


    // 记录上次移动的位置
    private float lastX = 0;
    private float lastY = 0;
    private int offset;
    // 是否是移动事件
    private boolean isMoved = false;

    public static LiveUtils getInstance() {
        if (utils == null) {
            utils = new LiveUtils();
        }

        return utils;
    }


    @SuppressLint("ClickableViewAccessibility")
    public void initLive(final Context context, final FrameLayout frameLayout, final RelativeLayout vPlayerView) {
        try {

            windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            //赋值WindowManager&LayoutParam.
            params = new WindowManager.LayoutParams();
            //设置type.系统提示型窗口，一般都在应用程序窗口之上.
            if (Build.VERSION.SDK_INT >= 26) {//8.0新特性
                params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            }
            //设置效果为背景透明.
            params.format = PixelFormat.RGBA_8888;
            //设置flags.不可聚焦及不可使用按钮对悬浮窗进行操控.
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            //设置窗口坐标参考系
            params.gravity = Gravity.START | Gravity.TOP;
            offset = (int) DensityUtils.dp2px(context, 2);//移动偏移量
            //设置原点
            params.x = (int) (DensityUtils.screenWidth(context) - DensityUtils.dp2px(context, 170));
            params.y = (int) (DensityUtils.screenHeight(context) - DensityUtils.dp2px(context, 100 + 72));
            //设置悬浮窗口长宽数据.
            params.width = (int) DensityUtils.dp2px(context, 180);
            params.height = (int) DensityUtils.dp2px(context, 100);

            //获取浮动窗口视图所在布局.
            toucherLayout = new FrameLayout(context);
            toucherLayout.addView(vPlayerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

            ImageView imageViewClose = new ImageView(context);
            imageViewClose.setImageResource(R.drawable.ic_svg_close_live);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    (int) DensityUtils.dp2px(context, 16), (int) DensityUtils.dp2px(context, 16));
            layoutParams.gravity = Gravity.TOP | Gravity.END;
            layoutParams.rightMargin = (int) DensityUtils.dp2px(context, 3);
            layoutParams.topMargin = (int) DensityUtils.dp2px(context, 3);
            imageViewClose.setLayoutParams(layoutParams);

            toucherLayout.addView(imageViewClose, layoutParams);


            //添加toucherlayout
            windowManager.addView(toucherLayout, params);


            //主动计算出当前View的宽高信息.
            toucherLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

            //处理touch
            toucherLayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            isMoved = false;
                            // 记录按下位置
                            lastX = event.getRawX();
                            lastY = event.getRawY();

                            start_X = event.getRawX();
                            start_Y = event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            isMoved = true;
                            // 记录移动后的位置
                            float moveX = event.getRawX();
                            float moveY = event.getRawY();
                            // 获取当前窗口的布局属性, 添加偏移量, 并更新界面, 实现移动
                            params.x += (int) (moveX - lastX);
                            params.y += (int) (moveY - lastY);
                            if (toucherLayout != null) {
                                windowManager.updateViewLayout(toucherLayout, params);
                            }
                            lastX = moveX;
                            lastY = moveY;
                            break;
                        case MotionEvent.ACTION_UP:

                            float fmoveX = event.getRawX();
                            float fmoveY = event.getRawY();

                            if (Math.abs(fmoveX - start_X) < offset && Math.abs(fmoveY - start_Y) < offset) {
                                isMoved = false;
                                remove(frameLayout,vPlayerView);
                                Intent intent = new Intent(context, VideoLiveActivity.class);
                                context.startActivity(intent);


                            } else {
                                isMoved = true;
                            }
                            break;
                    }
                    // 如果是移动事件, 则消费掉; 如果不是, 则由其他处理, 比如点击
                    return isMoved;
                }

            });

            //删除
            imageViewClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    remove(frameLayout, vPlayerView);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void remove(FrameLayout frameLayout, RelativeLayout vPlayerView) {
        if (windowManager != null && toucherLayout != null) {
            toucherLayout.removeAllViews();
            windowManager.removeView(toucherLayout);
        }
        frameLayout.addView(vPlayerView);
    }

}
