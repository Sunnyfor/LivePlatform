package com.sunny.livechat.live;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.orhanobut.logger.Logger;
import com.starrtc.starrtcsdk.api.XHClient;
import com.starrtc.starrtcsdk.api.XHConstants;
import com.starrtc.starrtcsdk.api.XHCustomConfig;
import com.starrtc.starrtcsdk.api.XHLiveManager;
import com.starrtc.starrtcsdk.apiInterface.IXHResultCallback;
import com.starrtc.starrtcsdk.core.audio.StarRTCAudioManager;
import com.starrtc.starrtcsdk.core.im.message.XHIMMessage;
import com.starrtc.starrtcsdk.core.player.StarPlayer;
import com.starrtc.starrtcsdk.core.player.StarPlayerScaleType;
import com.starrtc.starrtcsdk.core.player.StarWhitePanel;
import com.sunny.livechat.MyApplication;
import com.sunny.livechat.R;
import com.sunny.livechat.base.BaseActivity;
import com.sunny.livechat.chat.AEvent;
import com.sunny.livechat.chat.IChatListener;
import com.sunny.livechat.chat.MLOC;
import com.sunny.livechat.live.adapter.LiveMsgListAdapter;
import com.sunny.livechat.live.bean.LiveListBean;
import com.sunny.livechat.live.bean.ViewPosition;
import com.sunny.livechat.util.DensityUtils;
import com.sunny.livechat.util.ToastUtil;
import com.sunny.livechat.util.sp.SpKey;
import com.sunny.livechat.widget.CircularCoverView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Set;

public class VideoLiveActivity extends BaseActivity implements IChatListener {

    /**
     * true为竖屏，false为横屏
     */
    private Boolean isPortraitScreen = true;

    /**
     * true为主播，false为观众
     */
    private Boolean isUploader = false;

    /**
     * true为正在直播
     */
    private Boolean isRunning = false;

    private int borderW = 0;
    private int borderH = 0;

    private String creatorId;
    private String liveCode;

    private ArrayList<XHIMMessage> msgList;
    private ArrayList<ViewPosition> playerList;

    private XHLiveManager liveManager;

    private StarRTCAudioManager starRTCAudioManager;

    private LiveMsgListAdapter liveMsgListAdapter;

    private String mPrivateMsgTargetId;

    private StarWhitePanel vPaintPlayer;
    private RelativeLayout vPlayerView;
    private EditText vEditText;
    private View vMicBtn;
    private View vCameraBtn;
    private View vPanelBtn;
    private View vCleanBtn;


    @Override
    public int setLayout() {
        return R.layout.activity_video_live;
    }

    @Nullable
    @Override
    public View initTitle() {
        return null;
    }

    @Override
    public void initView() {

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_live);

        starRTCAudioManager = StarRTCAudioManager.create(this);
        starRTCAudioManager.start(new StarRTCAudioManager.AudioManagerEvents() {
            @Override
            public void onAudioDeviceChanged(StarRTCAudioManager.AudioDevice selectedAudioDevice, Set availableAudioDevices) {
                Logger.i("IM语音开启");
            }
        });

        DisplayMetrics dm = getResources().getDisplayMetrics();
        isPortraitScreen = dm.heightPixels > dm.widthPixels;

        LiveListBean.LiveInfoBean liveInfoBean = MyApplication.Companion.getInstance().getData(SpKey.liveInfoBean);
        String liveName = liveInfoBean.getLiveName();
        liveCode = liveInfoBean.getLiveCode();
        creatorId = liveInfoBean.getCreator();
        XHConstants.XHLiveType liveType = XHConstants.XHLiveType.XHLiveTypeGlobalPublic; //后期改成接口调用：liveInfoBean.liveClassId

        if (TextUtils.isEmpty(liveCode) || TextUtils.isEmpty(liveName)) {
            ToastUtil.INSTANCE.show("没有直播信息");
            stopAndFinish();
            return;
        }

        liveManager = XHClient.getInstance().getLiveManager(this);

        addListener();
        TextView vRoomId = findViewById(R.id.iv_live_id);
        vRoomId.setText(("直播编号：" + liveName));

        vPaintPlayer = findViewById(R.id.starWhitePanel);
        vPanelBtn = findViewById(R.id.iv_panel_btn);
        vCleanBtn = findViewById(R.id.iv_clean_btn);

        findViewById(R.id.iv_back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        vEditText = findViewById(R.id.et_input);
        vEditText.clearFocus();

        msgList = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        liveMsgListAdapter = new LiveMsgListAdapter(msgList);
        recyclerView.setAdapter(liveMsgListAdapter);

        View vSendBtn = findViewById(R.id.iv_send_btn);
        vSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt = vEditText.getText().toString();
                if (!TextUtils.isEmpty(txt)) {
                    sendChatMsg(txt);
                    vEditText.setText("");
                }
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(vEditText.getWindowToken(), 0);
            }
        });

        vCameraBtn = findViewById(R.id.iv_switch_camera);
        vMicBtn = findViewById(R.id.iv_mic_btn);
        if (creatorId != null && creatorId.equals(MLOC.userId)) {
            vMicBtn.setVisibility(View.GONE);
            vCameraBtn.setVisibility(View.VISIBLE);
            vPanelBtn.setVisibility(View.VISIBLE);
        } else {
            vMicBtn.setVisibility(View.VISIBLE);
            vCameraBtn.setVisibility(View.GONE);
            vPanelBtn.setVisibility(View.GONE);
        }
        vMicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUploader) {
                    new AlertDialog.Builder(VideoLiveActivity.this).setCancelable(true)
                            .setTitle("是否结束上麦?")
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {

                                }
                            }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    isUploader = false;
                                    liveManager.changeToAudience(new IXHResultCallback() {
                                        @Override
                                        public void success(Object data) {

                                        }

                                        @Override
                                        public void failed(String errMsg) {

                                        }
                                    });
                                    vMicBtn.setSelected(false);
                                    vCameraBtn.setVisibility(View.GONE);
                                    vPanelBtn.setVisibility(View.GONE);
                                }
                            }
                    ).show();
                } else {
                    new AlertDialog.Builder(VideoLiveActivity.this).setCancelable(true)
                            .setTitle("是否申请上麦?")
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {

                                }
                            }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    liveManager.applyToBroadcaster(creatorId);
                                }
                            }
                    ).show();
                }
            }
        });

        findViewById(R.id.iv_switch_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                liveManager.switchCamera();
            }
        });

        vPlayerView = findViewById(R.id.view1);
        borderW = DensityUtils.screenWidth(this);
        borderH = DensityUtils.screenHeight(this);

        playerList = new ArrayList<>();

        vPanelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (vPanelBtn.isSelected()) {
                    vPanelBtn.setSelected(false);
                    vCleanBtn.setVisibility(View.INVISIBLE);
                    vPaintPlayer.pause();
                } else {
                    vPanelBtn.setSelected(true);
                    vCleanBtn.setVisibility(View.VISIBLE);
                    vPaintPlayer.publish(liveManager);
                }
            }
        });
        vCleanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vPaintPlayer.clean();
            }
        });

        init();
    }

    @Override
    public void onClickEvent(@NotNull View v) {
    }

    @Override
    public void loadData() {

    }

    @Override
    public void close() {

    }


    private void init() {
        if (creatorId.equals(MLOC.userId)) {
            starLive();
        } else {
            joinLive();
        }
    }


    private void starLive() {
        //开始直播
        isUploader = true;
        liveManager.startLive(liveCode, new IXHResultCallback() {
            @Override
            public void success(Object data) {
            }

            @Override
            public void failed(final String errMsg) {
                stopAndFinish();
            }
        });
    }

    private void joinLive() {
        //观众加入直播
        isUploader = false;
        liveManager.watchLive(liveCode, new IXHResultCallback() {
            @Override
            public void success(Object data) {
            }

            @Override
            public void failed(final String errMsg) {
                stopAndFinish();
            }
        });
    }

    private void sendChatMsg(String msg) {
        if (TextUtils.isEmpty(mPrivateMsgTargetId)) {
            XHIMMessage imMessage = liveManager.sendMessage(msg, null);
            msgList.add(imMessage);
        } else {
            XHIMMessage imMessage = liveManager.sendPrivateMessage(msg, mPrivateMsgTargetId, null);
            msgList.add(imMessage);
        }
        liveMsgListAdapter.notifyDataSetChanged();
        mPrivateMsgTargetId = "";

    }


    public void addListener() {
        AEvent.addListener(AEvent.AEVENT_LIVE_ERROR, this);
        AEvent.addListener(AEvent.AEVENT_LIVE_ADD_UPLOADER, this);
        AEvent.addListener(AEvent.AEVENT_LIVE_REMOVE_UPLOADER, this);
        AEvent.addListener(AEvent.AEVENT_LIVE_APPLY_LINK, this);
        AEvent.addListener(AEvent.AEVENT_LIVE_APPLY_LINK_RESULT, this);
        AEvent.addListener(AEvent.AEVENT_LIVE_INVITE_LINK, this);
        AEvent.addListener(AEvent.AEVENT_LIVE_INVITE_LINK_RESULT, this);
        AEvent.addListener(AEvent.AEVENT_LIVE_GET_ONLINE_NUMBER, this);
        AEvent.addListener(AEvent.AEVENT_LIVE_SELF_KICKED, this);
        AEvent.addListener(AEvent.AEVENT_LIVE_SELF_BANNED, this);
        AEvent.addListener(AEvent.AEVENT_LIVE_REV_MSG, this);
        AEvent.addListener(AEvent.AEVENT_LIVE_REV_PRIVATE_MSG, this);
        AEvent.addListener(AEvent.AEVENT_LIVE_SELF_COMMANDED_TO_STOP, this);
        AEvent.addListener(AEvent.AEVENT_LIVE_REV_REALTIME_DATA, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        MLOC.canPickupVoip = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        MLOC.canPickupVoip = true;
    }

    @Override
    public void onRestart() {
        super.onRestart();
        addListener();
    }

    @Override
    public void onStop() {
        removeListener();
        super.onStop();
    }

    private void removeListener() {
        AEvent.removeListener(AEvent.AEVENT_LIVE_ERROR, this);
        AEvent.removeListener(AEvent.AEVENT_LIVE_ADD_UPLOADER, this);
        AEvent.removeListener(AEvent.AEVENT_LIVE_REMOVE_UPLOADER, this);
        AEvent.removeListener(AEvent.AEVENT_LIVE_APPLY_LINK, this);
        AEvent.removeListener(AEvent.AEVENT_LIVE_APPLY_LINK_RESULT, this);
        AEvent.removeListener(AEvent.AEVENT_LIVE_INVITE_LINK, this);
        AEvent.removeListener(AEvent.AEVENT_LIVE_INVITE_LINK_RESULT, this);
        AEvent.removeListener(AEvent.AEVENT_LIVE_GET_ONLINE_NUMBER, this);
        AEvent.removeListener(AEvent.AEVENT_LIVE_SELF_KICKED, this);
        AEvent.removeListener(AEvent.AEVENT_LIVE_SELF_BANNED, this);
        AEvent.removeListener(AEvent.AEVENT_LIVE_REV_MSG, this);
        AEvent.removeListener(AEvent.AEVENT_LIVE_REV_PRIVATE_MSG, this);
        AEvent.removeListener(AEvent.AEVENT_LIVE_SELF_COMMANDED_TO_STOP, this);
        AEvent.removeListener(AEvent.AEVENT_LIVE_REV_REALTIME_DATA, this);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(VideoLiveActivity.this).setCancelable(true)
                .setTitle("是否要退出?")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        stop();
                    }
                }
        ).show();
    }

    private void stop() {
        liveManager.leaveLive(new IXHResultCallback() {
            @Override
            public void success(Object data) {
                stopAndFinish();
            }

            @Override
            public void failed(final String errMsg) {
                stopAndFinish();
            }
        });
    }

    private void addPlayer(String addUserID) {
        ViewPosition newOne = new ViewPosition();
        newOne.setUserId(addUserID);
        StarPlayer player = new StarPlayer(this);
        newOne.setVideoPlayer(player);
        playerList.add(newOne);
        vPlayerView.addView(player);
        CircularCoverView coverView = new CircularCoverView(this);
        coverView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        coverView.setCoverColor(Color.BLACK);
        coverView.setRadians(35, 35, 35, 35, 10);
        player.addView(coverView);
        player.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLayout(v);
            }
        });
        resetLayout();
        player.setZOrderMediaOverlay(true);
        player.setScalType(StarPlayerScaleType.DRAW_TYPE_CENTER);

        if (playerList.size() == 1) {
            liveManager.attachPlayerView(addUserID, player, true);
        } else {
            liveManager.attachPlayerView(addUserID, player, false);
        }
    }

    private void changeLayout(View v) {
        if (isRunning) return;
        if (v == playerList.get(0).getVideoPlayer()) return;
        ViewPosition clickPlayer = null;
        int clickIndex = 0;
        for (int i = 0; i < playerList.size(); i++) {
            if (playerList.get(i).getVideoPlayer() == v) {
                clickIndex = i;
                clickPlayer = playerList.remove(i);
                liveManager.changeToBig(clickPlayer.getUserId());
                break;
            }
        }
        final ViewPosition mainPlayer = playerList.remove(0);
        liveManager.changeToSmall(mainPlayer.getUserId());
        playerList.remove(clickPlayer);
        playerList.add(0, clickPlayer);
        playerList.add(clickIndex, mainPlayer);

        final ViewPosition finalClickPlayer = clickPlayer;
        startAnimation(finalClickPlayer.getVideoPlayer(), mainPlayer.getVideoPlayer());
    }

    private void startAnimation(final StarPlayer clickPlayer, final StarPlayer mainPlayer) {
        final float clickStartW = clickPlayer.getWidth();
        final float clickStartH = clickPlayer.getHeight();
        final float clickEndW = mainPlayer.getWidth();
        final float clickEndH = mainPlayer.getHeight();
        final float mainStartW = mainPlayer.getWidth();
        final float mainStartH = mainPlayer.getHeight();
        final float mainEndW = clickPlayer.getWidth();
        final float mainEndH = clickPlayer.getHeight();

        final float clickStartX = clickPlayer.getX();
        final float clickStartY = clickPlayer.getY();
        final float clickEndX = mainPlayer.getX();
        final float clickEndY = mainPlayer.getY();
        final float mainStartX = mainPlayer.getX();
        final float mainStartY = mainPlayer.getY();
        final float mainEndX = clickPlayer.getX();
        final float mainEndY = clickPlayer.getY();

        if (XHCustomConfig.getInstance(this).getOpenGLESEnable()) {
            clickPlayer.setX(clickEndX);
            clickPlayer.setY(clickEndY);
            clickPlayer.getLayoutParams().width = (int) clickEndW;
            clickPlayer.getLayoutParams().height = (int) clickEndH;
            clickPlayer.requestLayout();

            mainPlayer.setX(mainEndX);
            mainPlayer.setY(mainEndY);
            mainPlayer.getLayoutParams().width = (int) mainEndW;
            mainPlayer.getLayoutParams().height = (int) mainEndH;
            mainPlayer.requestLayout();
        } else {

            final ValueAnimator valTotal = ValueAnimator.ofFloat(0f, 1f);
            valTotal.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    clickPlayer.setX(clickStartX + (Float) animation.getAnimatedValue() * (clickEndX - clickStartX));
                    clickPlayer.setY(clickStartY + (Float) animation.getAnimatedValue() * (clickEndY - clickStartY));
                    clickPlayer.getLayoutParams().width = (int) (clickStartW + (Float) animation.getAnimatedValue() * (clickEndW - clickStartW));
                    clickPlayer.getLayoutParams().height = (int) (clickStartH + (Float) animation.getAnimatedValue() * (clickEndH - clickStartH));
                    clickPlayer.requestLayout();

                    mainPlayer.setX(mainStartX + (Float) animation.getAnimatedValue() * (mainEndX - mainStartX));
                    mainPlayer.setY(mainStartY + (Float) animation.getAnimatedValue() * (mainEndY - mainStartY));
                    mainPlayer.getLayoutParams().width = (int) (mainStartW + (Float) animation.getAnimatedValue() * (mainEndW - mainStartW));
                    mainPlayer.getLayoutParams().height = (int) (mainStartH + (Float) animation.getAnimatedValue() * (mainEndH - mainStartH));
                    mainPlayer.requestLayout();
                }
            });

            valTotal.setDuration(300);
            valTotal.setInterpolator(new LinearInterpolator());
            valTotal.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    isRunning = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    isRunning = false;
                    clickPlayer.setScalType(StarPlayerScaleType.DRAW_TYPE_CENTER);
                    mainPlayer.setScalType(StarPlayerScaleType.DRAW_TYPE_CENTER);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    isRunning = false;
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            valTotal.start();
        }
    }


    private void deletePlayer(String removeUserId) {
        if (playerList != null && playerList.size() > 0) {
            for (int i = 0; i < playerList.size(); i++) {
                ViewPosition temp = playerList.get(i);
                if (removeUserId.equals(temp.getUserId())) {
                    ViewPosition remove = playerList.remove(i);
                    vPlayerView.removeView(remove.getVideoPlayer());
                    resetLayout();
                    if (playerList.size() > 0) {
                        liveManager.changeToBig(playerList.get(0).getUserId());
                    }
                    break;
                }
            }
        }
    }

    private void resetLayout() {
        if (isPortraitScreen) {
            switch (playerList.size()) {
                case 1: {
                    StarPlayer player = playerList.get(0).getVideoPlayer();
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(borderW, borderH);
                    player.setLayoutParams(lp);
                    player.setY(0);
                    player.setX(0);
                    break;
                }
                case 2:
                case 3:
                case 4: {
                    for (int i = 0; i < playerList.size(); i++) {
                        if (i == 0) {
                            StarPlayer player = playerList.get(i).getVideoPlayer();
                            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(borderW / 3 * 2, borderH);
                            player.setLayoutParams(lp);
                            player.setY(0);
                            player.setX(0);
                        } else {
                            StarPlayer player = playerList.get(i).getVideoPlayer();
                            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(borderW / 3, borderH / 3);
                            player.setLayoutParams(lp);
                            player.setY((i - 1) * borderH / 3);
                            player.setX(borderW / 3 * 2);
                        }
                    }
                    break;
                }
                case 5:
                case 6:
                case 7: {
                    for (int i = 0; i < playerList.size(); i++) {
                        if (i == 0) {
                            StarPlayer player = playerList.get(i).getVideoPlayer();
                            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(borderW - borderW / 3, borderH - borderH / 4);
                            player.setLayoutParams(lp);
                        } else if (i > 0 && i < 3) {
                            StarPlayer player = playerList.get(i).getVideoPlayer();
                            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(borderW / 3, borderH / 4);
                            player.setLayoutParams(lp);
                            player.setX(borderW - borderW / 3);
                            player.setY((i - 1) * borderH / 4);
                        } else {
                            StarPlayer player = playerList.get(i).getVideoPlayer();
                            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(borderW / 3, borderH / 4);
                            player.setLayoutParams(lp);
                            player.setX((i - 3) * borderW / 3);
                            player.setY(borderH - borderH / 4);
                        }
                    }
                    break;
                }
            }
        } else {
            switch (playerList.size()) {
                case 1: {
                    StarPlayer player = playerList.get(0).getVideoPlayer();
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(borderW, borderH);
                    player.setLayoutParams(lp);
                    player.setY(0);
                    player.setX(0);
                    break;
                }
                case 2:
                case 3:
                case 4: {
                    for (int i = 0; i < playerList.size(); i++) {
                        if (i == 0) {
                            StarPlayer player = playerList.get(i).getVideoPlayer();
                            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(borderW / 4 * 3, borderH);
                            player.setLayoutParams(lp);
                            player.setY(0);
                            player.setX(0);
                        } else {
                            StarPlayer player = playerList.get(i).getVideoPlayer();
                            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(borderW / 4, borderH / 3);
                            player.setLayoutParams(lp);
                            player.setY((i - 1) * borderH / 3);
                            player.setX(borderW / 4 * 3);
                            player.setScalType(StarPlayerScaleType.DRAW_TYPE_TOTAL_GRAPH);
                        }
                    }
                    break;
                }
                case 5:
                case 6:
                case 7: {
                    for (int i = 0; i < playerList.size(); i++) {
                        if (i == 0) {
                            StarPlayer player = playerList.get(i).getVideoPlayer();
                            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(borderW / 4 * 2, borderH);
                            player.setLayoutParams(lp);
                            player.setY(0);
                            player.setX(0);
                        } else if (i > 0 && i < 3) {
                            StarPlayer player = playerList.get(i).getVideoPlayer();
                            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(borderW / 4, borderH / 3);
                            player.setLayoutParams(lp);
                            player.setY((i - 1) * borderH / 3);
                            player.setX(borderW / 4 * 2);
                        } else {
                            StarPlayer player = playerList.get(i).getVideoPlayer();
                            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(borderW / 4, borderH / 3);
                            player.setLayoutParams(lp);
                            player.setY((i - 3) * borderH / 3);
                            player.setX(borderW / 4 * 3);
                        }
                    }
                    break;
                }
            }
        }


    }

    @Override
    public void dispatchEvent(String aEventID, boolean success, final Object eventObj) {
        switch (aEventID) {
            case AEvent.AEVENT_LIVE_ADD_UPLOADER:
                try {
                    JSONObject data = (JSONObject) eventObj;
                    String addId = data.getString("actorID");
                    addPlayer(addId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case AEvent.AEVENT_LIVE_REMOVE_UPLOADER:
                try {
                    JSONObject data = (JSONObject) eventObj;
                    String removeUserId = data.getString("actorID");
                    deletePlayer(removeUserId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case AEvent.AEVENT_LIVE_APPLY_LINK:
                new AlertDialog.Builder(VideoLiveActivity.this).setCancelable(true)
                        .setTitle(eventObj + "申请上麦")
                        .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                liveManager.refuseApplyToBroadcaster((String) eventObj);
                            }
                        }).setPositiveButton("同意", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                liveManager.agreeApplyToBroadcaster((String) eventObj);
                            }
                        }
                ).show();
                break;
            case AEvent.AEVENT_LIVE_APPLY_LINK_RESULT:
                if (eventObj == XHConstants.XHLiveJoinResult.XHLiveJoinResult_accept) {
                    new AlertDialog.Builder(VideoLiveActivity.this).setCancelable(true)
                            .setTitle("房主同意连麦，是否现在开始上麦？")
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                }
                            }).setPositiveButton("开始", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    isUploader = true;
                                    liveManager.changeToBroadcaster(new IXHResultCallback() {
                                        @Override
                                        public void success(Object data) {

                                        }

                                        @Override
                                        public void failed(String errMsg) {

                                        }
                                    });
                                    vMicBtn.setSelected(true);
                                    vCameraBtn.setVisibility(View.VISIBLE);
                                    vPanelBtn.setVisibility(View.VISIBLE);
                                }
                            }
                    ).show();
                }
                break;
            case AEvent.AEVENT_LIVE_INVITE_LINK:
                new AlertDialog.Builder(VideoLiveActivity.this).setCancelable(true)
                        .setTitle(eventObj + "邀请您上麦")
                        .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                liveManager.refuseInviteToBroadcaster((String) eventObj);
                            }
                        }).setPositiveButton("同意", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                vMicBtn.setSelected(true);
                                vCameraBtn.setVisibility(View.VISIBLE);
                                isUploader = true;
                                liveManager.agreeInviteToBroadcaster((String) eventObj);
                            }
                        }
                ).show();
                break;
            case AEvent.AEVENT_LIVE_INVITE_LINK_RESULT:
                XHConstants.XHLiveJoinResult result = (XHConstants.XHLiveJoinResult) eventObj;
                switch (result) {
                    case XHLiveJoinResult_accept:
                        sendChatMsg("欢迎新的小伙伴上麦！！！");
                        break;
                    case XHLiveJoinResult_refuse:
                        break;
                    case XHLiveJoinResult_outtime:
                        break;
                }
                break;
            case AEvent.AEVENT_LIVE_GET_ONLINE_NUMBER:
//                onLineUserNumber = (int) eventObj;
                break;
            case AEvent.AEVENT_LIVE_SELF_KICKED:
                stopAndFinish();
                break;
            case AEvent.AEVENT_LIVE_SELF_BANNED:
                final String banTime = eventObj.toString();
                break;
            case AEvent.AEVENT_LIVE_REV_MSG:
            case AEvent.AEVENT_LIVE_REV_PRIVATE_MSG:
                XHIMMessage revMsg = (XHIMMessage) eventObj;
                msgList.add(revMsg);
                liveMsgListAdapter.notifyDataSetChanged();
                break;
            case AEvent.AEVENT_LIVE_ERROR:
                String errStr = (String) eventObj;
                if (errStr.equals("30016")) {
                    errStr = "直播关闭";
                }
                stopAndFinish();
                break;
            case AEvent.AEVENT_LIVE_SELF_COMMANDED_TO_STOP:
                if (isUploader) {
                    isUploader = false;
                    vMicBtn.setSelected(false);
                    vCameraBtn.setVisibility(View.GONE);
                    vPanelBtn.setVisibility(View.GONE);
//                            vCarBtn.setVisibility(View.GONE);
                }
                break;
            case AEvent.AEVENT_LIVE_REV_REALTIME_DATA:
                if (success) {
                    try {
                        JSONObject jsonObject = (JSONObject) eventObj;
                        byte[] tData = (byte[]) jsonObject.get("data");
                        String tUpid = jsonObject.getString("upId");
                        vPaintPlayer.setPaintData(tData, tUpid);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private void showManagerDialog(final String userId, final String msgText) {
        if (!userId.equals(MLOC.userId)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            if (creatorId.equals(MLOC.userId)) {
                Boolean ac = false;
                for (int i = 0; i < playerList.size(); i++) {
                    if (userId.equals(playerList.get(i).getUserId())) {
                        ac = true;
                        break;
                    }
                }
                if (ac) {
                    final String[] Items = {"踢出房间", "禁止发言", "私信", "下麦"};
                    builder.setItems(Items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (i == 0) {
                                kickUser(userId);
                            } else if (i == 1) {
                                muteUser(userId, 60);
                            } else if (i == 2) {
                                mPrivateMsgTargetId = userId;
                                vEditText.setText("[私" + userId + "]");
                            } else if (i == 3) {
                                liveManager.commandToAudience(userId);
                            }
                        }
                    });
                } else {
                    final String[] Items = {"踢出房间", "禁止发言", "私信", "邀请上麦"};
                    builder.setItems(Items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (i == 0) {
                                kickUser(userId);
                            } else if (i == 1) {
                                muteUser(userId, 60);
                            } else if (i == 2) {
                                mPrivateMsgTargetId = userId;
                                vEditText.setText("[私" + userId + "]");
                            } else if (i == 3) {
                                liveManager.inviteToBroadcaster(userId);
                            }
                        }
                    });
                }


            } else {
                final String[] Items = {"私信"};
                builder.setItems(Items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            mPrivateMsgTargetId = userId;
                            vEditText.setText("[私" + userId + "]");
                        }
                    }
                });
            }
            builder.setCancelable(true);
            AlertDialog dialog = builder.create();
            dialog.show();
        }

    }


    private void kickUser(String userId) {
        liveManager.kickMember(userId, new IXHResultCallback() {
            @Override
            public void success(Object data) {
                //踢人成功
            }

            @Override
            public void failed(String errMsg) {
                //踢人失败
            }
        });
    }

    private void muteUser(String userId, int times) {
        liveManager.muteMember(userId, times, new IXHResultCallback() {
            @Override
            public void success(Object data) {
                //禁言成功
            }

            @Override
            public void failed(String errMsg) {
                //禁言失败
            }
        });
    }

    private void stopAndFinish() {
        if (starRTCAudioManager != null) {
            starRTCAudioManager.stop();
        }
        removeListener();
        finish();
    }
}
