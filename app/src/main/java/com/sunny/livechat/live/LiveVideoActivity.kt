package com.sunny.livechat.live

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.orhanobut.logger.Logger
import com.starrtc.starrtcsdk.api.XHClient
import com.starrtc.starrtcsdk.api.XHConstants
import com.starrtc.starrtcsdk.api.XHCustomConfig
import com.starrtc.starrtcsdk.api.XHLiveManager
import com.starrtc.starrtcsdk.apiInterface.IXHResultCallback
import com.starrtc.starrtcsdk.core.audio.StarRTCAudioManager
import com.starrtc.starrtcsdk.core.im.message.XHIMMessage
import com.starrtc.starrtcsdk.core.player.StarPlayer
import com.starrtc.starrtcsdk.core.player.StarPlayerScaleType
import com.sunny.livechat.MyApplication
import com.sunny.livechat.R
import com.sunny.livechat.base.BaseActivity
import com.sunny.livechat.chat.AEvent
import com.sunny.livechat.chat.IChatListener
import com.sunny.livechat.chat.MLOC
import com.sunny.livechat.live.adapter.LiveMsgListAdapter
import com.sunny.livechat.live.bean.LiveListBean
import com.sunny.livechat.live.bean.ViewPosition
import com.sunny.livechat.util.DensityUtils
import com.sunny.livechat.util.ToastUtil
import com.sunny.livechat.util.intent.IntentKey
import com.sunny.livechat.util.sp.SpKey
import com.sunny.livechat.util.sp.SpKey.liveInfoBean
import com.sunny.livechat.widget.CircularCoverView
import kotlinx.android.synthetic.main.activity_video_live.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * Desc 直播播放页
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/12/23 19:51
 */
class LiveVideoActivity : BaseActivity(), IChatListener {

    /**
     * true为竖屏，false为横屏
     */
    private var isPortraitScreen = true

    /**
     * true为主播，false为观众
     */
    private var isUploader = false

    /**
     * true为正在直播
     */
    private var isRunning = false

    private var borderW = 0
    private var borderH = 0

    private var creatorId: String? = null
    private var liveCode: String? = null
    private var liveName: String? = null
    private var liveType: XHConstants.XHLiveType? = null

    private var msgList = ArrayList<XHIMMessage>()
    private var playerList = ArrayList<ViewPosition>()

    private var liveManager: XHLiveManager? = null

    private var mPrivateMsgTargetId: String? = null

    private var starRTCAudioManager: StarRTCAudioManager? = null

    private val liveMsgListAdapter: LiveMsgListAdapter by lazy {
        LiveMsgListAdapter(this.msgList).apply {
            this.setOnItemClickListener { _, i ->
                val clickUserId = this@LiveVideoActivity.msgList[i].fromId
                showManagerDialog(clickUserId)
            }
        }
    }


    override fun setLayout(): Int = R.layout.activity_video_live


    override fun initTitle(): View? = null


    override fun initView() {

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        liveManager = XHClient.getInstance().getLiveManager(this)

        starRTCAudioManager = StarRTCAudioManager.create(this)
        starRTCAudioManager?.start { _, _ ->
            Logger.i("IM语音开启")
        }


        val dm = resources.displayMetrics
        isPortraitScreen = dm.heightPixels > dm.widthPixels

        val liveInfoBean = MyApplication.getInstance().getData<LiveListBean.LiveInfoBean>(SpKey.liveInfoBean)
        liveCode = liveInfoBean?.liveCode
        liveName = liveInfoBean?.liveName
        creatorId = liveInfoBean?.creator
        liveType = XHConstants.XHLiveType.XHLiveTypeGlobalPublic //后期改成接口调用：liveInfoBean.liveClassId


        if (TextUtils.isEmpty(liveCode) || TextUtils.isEmpty(liveName) || liveType == null) {
            ToastUtil.show("没有直播信息")
            stopAndFinish()
            return
        }


        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        recyclerView.layoutManager = layoutManager
        recyclerView.scrollToPosition(liveMsgListAdapter.itemCount - 1)
        recyclerView.adapter = liveMsgListAdapter


        iv_live_id.text = ("直播编号：$liveName")

        et_input.clearFocus()

        borderW = DensityUtils.screenWidth(this)
        borderH = borderW / 3 * 4


        if (creatorId != null && creatorId == MLOC.userId) {
            if (liveCode == null) {
                startActivity(Intent(this, CreateLiveActivity::class.java))
            } else {
                startLive()
            }

            iv_mic_btn.visibility = View.GONE
            iv_switch_camera.visibility = View.VISIBLE
            iv_panel_btn.visibility = View.VISIBLE
        } else {
            joinLive()

            iv_mic_btn.visibility = View.VISIBLE
            iv_switch_camera.visibility = View.GONE
            iv_panel_btn.visibility = View.GONE
        }


        addListener()

        iv_back_btn.setOnClickListener(this)
        iv_switch_camera.setOnClickListener(this)
        iv_panel_btn.setOnClickListener(this)
        iv_clean_btn.setOnClickListener(this)
        iv_send_btn.setOnClickListener(this)
        iv_mic_btn.setOnClickListener(this)
    }

    override fun onClickEvent(v: View) {
        when (v.id) {
            R.id.iv_back_btn -> onBackPressed()
            R.id.iv_switch_camera -> liveManager?.switchCamera()
            R.id.iv_clean_btn -> starWhitePanel.clean()
            R.id.iv_mic_btn -> clickMicBtn()
            R.id.iv_panel_btn -> {
                if (iv_panel_btn.isSelected) {
                    iv_panel_btn.isSelected = false
                    iv_clean_btn.visibility = View.INVISIBLE
                    starWhitePanel.pause()
                } else {
                    iv_panel_btn.isSelected = true
                    iv_clean_btn.visibility = View.VISIBLE
                    starWhitePanel.publish(liveManager)
                }
            }
            R.id.iv_send_btn -> {
                val txt = et_input.text.toString()
                if (!TextUtils.isEmpty(txt)) {
                    sendChatMsg(txt)
                    et_input.setText("")
                }

                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(et_input.windowToken, 0)
            }
        }
    }

    override fun loadData() {}


    override fun close() {}


    override fun dispatchEvent(aEventID: String?, success: Boolean, eventObj: Any?) {
        Logger.e("IM dispatchEvent  $aEventID$eventObj")
        when (aEventID) {
            AEvent.AEVENT_LIVE_ADD_UPLOADER -> {
                try {
                    val data = eventObj as JSONObject
                    val addId = data.getString("actorID")
                    addPlayer(addId)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            AEvent.AEVENT_LIVE_REMOVE_UPLOADER -> {
                try {
                    val data = eventObj as JSONObject
                    val removeUserId = data.getString("actorID")
                    deletePlayer(removeUserId)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            AEvent.AEVENT_LIVE_APPLY_LINK -> {
                AlertDialog.Builder(this).setCancelable(true)
                    .setTitle(eventObj.toString() + "申请上麦")
                    .setNegativeButton("拒绝") { _, _ -> liveManager?.refuseApplyToBroadcaster(eventObj as String) }
                    .setPositiveButton("同意") { _, _ -> liveManager?.agreeApplyToBroadcaster(eventObj as String) }
                    .show()
            }
            AEvent.AEVENT_LIVE_APPLY_LINK_RESULT -> {
                if (eventObj as XHConstants.XHLiveJoinResult == XHConstants.XHLiveJoinResult.XHLiveJoinResult_accept) {
                    AlertDialog.Builder(this).setCancelable(true)
                        .setTitle("房主同意连麦，是否现在开始上麦？")
                        .setNegativeButton("取消") { _, _ -> }
                        .setPositiveButton("开始") { _, _ ->
                            isUploader = true
                            liveManager?.changeToBroadcaster(object : IXHResultCallback {
                                override fun success(data: Any) {}
                                override fun failed(errMsg: String) {}
                            })
                            iv_mic_btn.isSelected = true
                            iv_switch_camera.visibility = View.VISIBLE
                            iv_panel_btn.visibility = View.VISIBLE
                        }.show()
                }
            }
            AEvent.AEVENT_LIVE_INVITE_LINK -> {
                AlertDialog.Builder(this).setCancelable(true)
                    .setTitle(eventObj.toString() + "邀请您上麦")
                    .setNegativeButton("拒绝") { _, _ -> liveManager?.refuseInviteToBroadcaster(eventObj as String) }
                    .setPositiveButton("同意") { _, _ ->
                        iv_mic_btn.isSelected = true
                        iv_switch_camera.visibility = View.VISIBLE
                        isUploader = true
                        liveManager?.agreeInviteToBroadcaster(eventObj as String)
                    }.show()
            }
            AEvent.AEVENT_LIVE_INVITE_LINK_RESULT -> {
                when (eventObj as XHConstants.XHLiveJoinResult) {
                    XHConstants.XHLiveJoinResult.XHLiveJoinResult_accept -> sendChatMsg("欢迎新的小伙伴上麦！！！")
                    XHConstants.XHLiveJoinResult.XHLiveJoinResult_refuse -> Logger.e("IM上麦被拒")
                    XHConstants.XHLiveJoinResult.XHLiveJoinResult_outtime -> Logger.e("IM上麦超时")
                }
            }
            AEvent.AEVENT_LIVE_GET_ONLINE_NUMBER -> Logger.e("IM获取在线直播号")
            AEvent.AEVENT_LIVE_SELF_KICKED -> {
                ToastUtil.show("你已被踢出")
                stopAndFinish()
            }
            AEvent.AEVENT_LIVE_SELF_BANNED -> {
                val banTime = eventObj.toString()
                ToastUtil.show("你已被禁言, $banTime 秒后自动解除")
            }
            AEvent.AEVENT_LIVE_REV_MSG -> {
                val revMsg = eventObj as XHIMMessage
                msgList.add(revMsg)
                liveMsgListAdapter.notifyDataSetChanged()
            }
            AEvent.AEVENT_LIVE_REV_PRIVATE_MSG -> {
                val revMsgPrivate = eventObj as XHIMMessage
                msgList.add(revMsgPrivate)
                liveMsgListAdapter.notifyDataSetChanged()
            }
            AEvent.AEVENT_LIVE_ERROR -> {
                var errStr = eventObj as String
                if (errStr == "30016") {
                    errStr = "直播关闭"
                }
                ToastUtil.show(errStr)
                stopAndFinish()
            }
            AEvent.AEVENT_LIVE_SELF_COMMANDED_TO_STOP -> if (isUploader) {
                isUploader = false
                iv_mic_btn.isSelected = false
                iv_switch_camera.visibility = View.GONE
                iv_panel_btn.visibility = View.GONE
                ToastUtil.show("您的表演被叫停")
            }
            AEvent.AEVENT_LIVE_REV_REALTIME_DATA -> {
                if (success) {
                    try {
                        val jsonObject = eventObj as JSONObject
                        val tData = jsonObject.get("data") as ByteArray
                        val tUpid = jsonObject.getString("upId")
                        starWhitePanel.setPaintData(tData, tUpid)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }


    override fun onBackPressed() {
        AlertDialog.Builder(this@LiveVideoActivity).setCancelable(true)
            .setTitle("是否要退出?")
            .setNegativeButton("取消") { _, _ -> }
            .setPositiveButton("确定") { _, _ -> stopLive() }
            .show()
    }


    /**
     * 开始直播
     */
    private fun startLive() {
        isUploader = true
        liveManager?.startLive(liveCode, object : IXHResultCallback {
            override fun success(data: Any) {
                Logger.e("IM直播开始 $data")
            }

            override fun failed(errMsg: String) {
                Logger.e("IM直播失败 $errMsg")
                ToastUtil.show(errMsg)
                stopAndFinish()
            }
        })
    }


    /**
     * 停止直播
     */
    private fun stopLive() {
        liveManager?.leaveLive(object : IXHResultCallback {
            override fun success(data: Any?) {
                stopAndFinish()
            }

            override fun failed(errMsg: String?) {
                ToastUtil.show(errMsg)
                stopAndFinish()
            }
        })
    }

    private fun stopAndFinish() {
        if (starRTCAudioManager != null) {
            starRTCAudioManager?.stop()
        }
        removeListener()
        finish()
    }


    /**
     * 观众加入直播
     */
    private fun joinLive() {
        isUploader = false
        liveManager?.watchLive(liveCode, object : IXHResultCallback {
            override fun success(data: Any) {
                Logger.e("IM观众加入直播 $data")
            }

            override fun failed(errMsg: String) {
                Logger.e("IM观众加入失败 $errMsg")
                ToastUtil.show(errMsg)
                stopAndFinish()
            }
        })
    }


    /**
     * 发送消息
     */
    private fun sendChatMsg(msg: String) {
        Logger.d("IM发送消息 $msg")
        if (TextUtils.isEmpty(mPrivateMsgTargetId)) {
            val imMessage = liveManager?.sendMessage(msg, null)
            imMessage?.let {
                msgList.add(it)
            }

        } else {
            val imMessage = liveManager?.sendPrivateMessage(msg, mPrivateMsgTargetId, null)
            imMessage?.let {
                msgList.add(it)
            }
        }
        liveMsgListAdapter.notifyDataSetChanged()
        mPrivateMsgTargetId = ""

    }


    /**
     * 踢人
     */
    private fun kickUser(userId: String) {
        liveManager?.kickMember(userId, object : IXHResultCallback {
            override fun success(data: Any) {
                Logger.i("踢人成功")
            }

            override fun failed(errMsg: String) {
                Logger.i("踢人失败")
            }
        })
    }


    /**
     * 禁言
     */
    private fun muteUser(userId: String, times: Int) {
        liveManager?.muteMember(userId, times, object : IXHResultCallback {
            override fun success(data: Any) {
                Logger.i("禁言成功")
            }

            override fun failed(errMsg: String) {
                Logger.i("禁言失败")
            }
        })
    }


    /**
     * 上麦
     */
    private fun clickMicBtn() {
        if (isUploader) {
            AlertDialog.Builder(this).setCancelable(true)
                .setTitle("是否结束上麦?")
                .setNegativeButton("取消") { _, _ -> }
                .setPositiveButton("确定") { _, _ ->
                    isUploader = false
                    liveManager?.changeToAudience(object : IXHResultCallback {
                        override fun success(data: Any) {}

                        override fun failed(errMsg: String) {}
                    })
                    iv_mic_btn.isSelected = false
                    iv_switch_camera.visibility = View.GONE
                    iv_panel_btn.visibility = View.GONE
                    //vCarBtn.setVisibility(View.GONE);
                }.show()
        } else {
            AlertDialog.Builder(this).setCancelable(true)
                .setTitle("是否申请上麦?")
                .setNegativeButton("取消") { _, _ -> }
                .setPositiveButton("确定") { _, _ -> liveManager?.applyToBroadcaster(creatorId) }.show()
        }
    }


    private fun addPlayer(addUserID: String) {
        val newOne = ViewPosition()
        newOne.userId = addUserID
        val player = StarPlayer(this)
        newOne.videoPlayer = player
        playerList.add(newOne)
        view1.addView(player)
        val coverView = CircularCoverView(this)
        coverView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        coverView.setCoverColor(Color.BLACK)
        coverView.setRadians(35, 35, 35, 35, 10)
        player.addView(coverView)
        player.setOnClickListener { v -> changeLayout(v) }
        resetLayout()
        player.setZOrderMediaOverlay(true)
        player.setScalType(StarPlayerScaleType.DRAW_TYPE_CENTER)

        if (playerList.size == 1) {
            liveManager?.attachPlayerView(addUserID, player, true)
        } else {
            liveManager?.attachPlayerView(addUserID, player, false)
        }
    }


    private fun deletePlayer(removeUserId: String) {
        if (playerList.size > 0) {
            for (i in playerList.indices) {
                val temp = playerList[i]
                if (temp.userId.equals(removeUserId)) {
                    val remove = playerList.removeAt(i)
                    view1.removeView(remove.videoPlayer)
                    resetLayout()
                    liveManager?.changeToBig(playerList[0].userId)
                    break
                }
            }
        }
    }


    private fun changeLayout(v: View) {
        if (isRunning) return
        if (v === playerList[0].videoPlayer) return
        var clickPlayer: ViewPosition? = null
        var clickIndex = 0
        for (i in playerList.indices) {
            if (playerList[i].videoPlayer === v) {
                clickIndex = i
                clickPlayer = playerList.removeAt(i)
                liveManager?.changeToBig(clickPlayer.userId)
                break
            }
        }
        val mainPlayer = playerList.removeAt(0)
        liveManager?.changeToSmall(mainPlayer.userId)
        playerList.remove(clickPlayer)
        clickPlayer?.let {
            playerList.add(0, it)
        }
        playerList.add(clickIndex, mainPlayer)

        val finalClickPlayer = clickPlayer
        startAnimation(finalClickPlayer?.videoPlayer, mainPlayer.videoPlayer)
    }


    private fun resetLayout() {
        if (isPortraitScreen) {
            showPortraitScreen()
        } else {
            showLandscapeScreen()
        }
    }


    private fun showPortraitScreen() {
        when (playerList.size) {
            1 -> {
                val player = playerList[0].videoPlayer
                val lp = RelativeLayout.LayoutParams(borderW, borderH)
                player?.layoutParams = lp
                player?.y = 0f
                player?.x = 0f
            }
            2, 3, 4 -> {
                for (i in playerList.indices) {
                    if (i == 0) {
                        val player = playerList[i].videoPlayer
                        val lp = RelativeLayout.LayoutParams(borderW / 3 * 2, borderH)
                        player?.layoutParams = lp
                        player?.y = 0f
                        player?.x = 0f
                    } else {
                        val player = playerList[i].videoPlayer
                        val lp = RelativeLayout.LayoutParams(borderW / 3, borderH / 3)
                        player?.layoutParams = lp
                        player?.y = ((i - 1) * borderH / 3).toFloat()
                        player?.x = (borderW / 3 * 2).toFloat()
                    }
                }
            }
            5, 6, 7 -> {
                for (i in playerList.indices) {
                    when (i) {
                        0 -> {
                            val player = playerList[i].videoPlayer
                            val lp = RelativeLayout.LayoutParams(
                                borderW - borderW / 3,
                                borderH - borderH / 4
                            )
                            player?.layoutParams = lp
                        }
                        in 1..2 -> {
                            val player = playerList[i].videoPlayer
                            val lp = RelativeLayout.LayoutParams(borderW / 3, borderH / 4)
                            player?.layoutParams = lp
                            player?.x = (borderW - borderW / 3).toFloat()
                            player?.y = ((i - 1) * borderH / 4).toFloat()
                        }
                        else -> {
                            val player = playerList[i].videoPlayer
                            val lp = RelativeLayout.LayoutParams(borderW / 3, borderH / 4)
                            player?.layoutParams = lp
                            player?.x = ((i - 3) * borderW / 3).toFloat()
                            player?.y = (borderH - borderH / 4).toFloat()
                        }
                    }
                }
            }
        }
    }


    private fun showLandscapeScreen() {
        when (playerList.size) {
            1 -> {
                val player = playerList[0].videoPlayer
                val lp = RelativeLayout.LayoutParams(borderW, borderH)
                player?.layoutParams = lp
                player?.y = 0f
                player?.x = 0f
            }
            2, 3, 4 -> {
                for (i in playerList.indices) {
                    if (i == 0) {
                        val player = playerList[i].videoPlayer
                        val lp = RelativeLayout.LayoutParams(borderW / 4 * 3, borderH)
                        player?.layoutParams = lp
                        player?.y = 0f
                        player?.x = 0f
                    } else {
                        val player = playerList[i].videoPlayer
                        val lp = RelativeLayout.LayoutParams(borderW / 4, borderH / 3)
                        player?.layoutParams = lp
                        player?.y = ((i - 1) * borderH / 3).toFloat()
                        player?.x = (borderW / 4 * 3).toFloat()
                        player?.setScalType(StarPlayerScaleType.DRAW_TYPE_TOTAL_GRAPH)
                    }
                }
            }
            5, 6, 7 -> {
                for (i in playerList.indices) {
                    when (i) {
                        0 -> {
                            val player = playerList[i].videoPlayer
                            val lp = RelativeLayout.LayoutParams(borderW / 4 * 2, borderH)
                            player?.layoutParams = lp
                            player?.y = 0f
                            player?.x = 0f
                        }
                        in 1..2 -> {
                            val player = playerList[i].videoPlayer
                            val lp = RelativeLayout.LayoutParams(borderW / 4, borderH / 3)
                            player?.layoutParams = lp
                            player?.y = ((i - 1) * borderH / 3).toFloat()
                            player?.x = (borderW / 4 * 2).toFloat()
                        }
                        else -> {
                            val player = playerList[i].videoPlayer
                            val lp = RelativeLayout.LayoutParams(borderW / 4, borderH / 3)
                            player?.layoutParams = lp
                            player?.y = ((i - 3) * borderH / 3).toFloat()
                            player?.x = (borderW / 4 * 3).toFloat()
                        }
                    }
                }
            }
        }
    }


    private fun showManagerDialog(userId: String) {
        if (userId != MLOC.userId) {
            val builder = AlertDialog.Builder(this)
            if (creatorId == MLOC.userId) {
                var ac = false

                playerList.forEach {
                    if (userId == it.userId) {
                        ac = true
                        return@forEach
                    }
                }

                if (ac) {
                    val items = arrayOf("踢出房间", "禁止发言", "私信", "下麦")
                    builder.setItems(items) { _, i ->
                        when (i) {
                            0 -> kickUser(userId)
                            1 -> muteUser(userId, 30)
                            2 -> {
                                mPrivateMsgTargetId = userId
                                et_input.setText(("[私$userId]"))
                            }
                            3 -> liveManager?.commandToAudience(userId)
                        }
                    }
                } else {
                    val items = arrayOf("踢出房间", "禁止发言", "私信", "邀请上麦")
                    builder.setItems(items) { _, i ->
                        when (i) {
                            0 -> kickUser(userId)
                            1 -> muteUser(userId, 60)
                            2 -> {
                                mPrivateMsgTargetId = userId
                                et_input.setText(("[私$userId]"))
                            }
                            3 -> liveManager?.inviteToBroadcaster(userId)
                        }
                    }
                }
            } else {
                val items = arrayOf("私信")
                builder.setItems(items) { _, i ->
                    if (i == 0) {
                        mPrivateMsgTargetId = userId
                        et_input.setText(("[私$userId]"))
                    }
                }
            }
            builder.setCancelable(true)
            val dialog = builder.create()
            dialog.show()
        }

    }


    private fun startAnimation(clickPlayer: StarPlayer?, mainPlayer: StarPlayer?) {
        if (clickPlayer == null || mainPlayer == null) {
            return
        }

        val clickStartW = clickPlayer.width.toFloat()
        val clickStartH = clickPlayer.height.toFloat()
        val clickEndW = mainPlayer.width.toFloat()
        val clickEndH = mainPlayer.height.toFloat()
        val mainStartW = mainPlayer.width.toFloat()
        val mainStartH = mainPlayer.height.toFloat()
        val mainEndW = clickPlayer.width.toFloat()
        val mainEndH = clickPlayer.height.toFloat()

        val clickStartX = clickPlayer.x
        val clickStartY = clickPlayer.y
        val clickEndX = mainPlayer.x
        val clickEndY = mainPlayer.y
        val mainStartX = mainPlayer.x
        val mainStartY = mainPlayer.y
        val mainEndX = clickPlayer.x
        val mainEndY = clickPlayer.y

        if (XHCustomConfig.getInstance(this).openGLESEnable) {
            clickPlayer.x = clickEndX
            clickPlayer.y = clickEndY
            clickPlayer.layoutParams.width = clickEndW.toInt()
            clickPlayer.layoutParams.height = clickEndH.toInt()
            clickPlayer.requestLayout()

            mainPlayer.x = mainEndX
            mainPlayer.y = mainEndY
            mainPlayer.layoutParams.width = mainEndW.toInt()
            mainPlayer.layoutParams.height = mainEndH.toInt()
            mainPlayer.requestLayout()
        } else {

            val valTotal = ValueAnimator.ofFloat(0f, 1f)
            valTotal.addUpdateListener { animation ->
                clickPlayer.x = clickStartX + animation.animatedValue as Float * (clickEndX - clickStartX)
                clickPlayer.y = clickStartY + animation.animatedValue as Float * (clickEndY - clickStartY)
                clickPlayer.layoutParams.width = (clickStartW + animation.animatedValue as Float * (clickEndW - clickStartW)).toInt()
                clickPlayer.layoutParams.height = (clickStartH + animation.animatedValue as Float * (clickEndH - clickStartH)).toInt()
                clickPlayer.requestLayout()

                mainPlayer.x = mainStartX + animation.animatedValue as Float * (mainEndX - mainStartX)
                mainPlayer.y = mainStartY + animation.animatedValue as Float * (mainEndY - mainStartY)
                mainPlayer.layoutParams.width = (mainStartW + animation.animatedValue as Float * (mainEndW - mainStartW)).toInt()
                mainPlayer.layoutParams.height = (mainStartH + animation.animatedValue as Float * (mainEndH - mainStartH)).toInt()
                mainPlayer.requestLayout()
            }

            valTotal.duration = 300
            valTotal.interpolator = LinearInterpolator()
            valTotal.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    isRunning = true
                }

                override fun onAnimationEnd(animation: Animator) {
                    isRunning = false
                    clickPlayer.setScalType(StarPlayerScaleType.DRAW_TYPE_CENTER)
                    mainPlayer.setScalType(StarPlayerScaleType.DRAW_TYPE_CENTER)
                }

                override fun onAnimationCancel(animation: Animator) {
                    isRunning = false
                }

                override fun onAnimationRepeat(animation: Animator) {

                }
            })
            valTotal.start()
        }
    }


    private fun removeListener() {
        AEvent.removeListener(AEvent.AEVENT_LIVE_ERROR, this)
        AEvent.removeListener(AEvent.AEVENT_LIVE_ADD_UPLOADER, this)
        AEvent.removeListener(AEvent.AEVENT_LIVE_REMOVE_UPLOADER, this)
        AEvent.removeListener(AEvent.AEVENT_LIVE_APPLY_LINK, this)
        AEvent.removeListener(AEvent.AEVENT_LIVE_APPLY_LINK_RESULT, this)
        AEvent.removeListener(AEvent.AEVENT_LIVE_INVITE_LINK, this)
        AEvent.removeListener(AEvent.AEVENT_LIVE_INVITE_LINK_RESULT, this)
        AEvent.removeListener(AEvent.AEVENT_LIVE_GET_ONLINE_NUMBER, this)
        AEvent.removeListener(AEvent.AEVENT_LIVE_SELF_KICKED, this)
        AEvent.removeListener(AEvent.AEVENT_LIVE_SELF_BANNED, this)
        AEvent.removeListener(AEvent.AEVENT_LIVE_REV_MSG, this)
        AEvent.removeListener(AEvent.AEVENT_LIVE_REV_PRIVATE_MSG, this)
        AEvent.removeListener(AEvent.AEVENT_LIVE_SELF_COMMANDED_TO_STOP, this)
        AEvent.removeListener(AEvent.AEVENT_LIVE_REV_REALTIME_DATA, this)
    }


    private fun addListener() {
        AEvent.addListener(AEvent.AEVENT_LIVE_ERROR, this)
        AEvent.addListener(AEvent.AEVENT_LIVE_ADD_UPLOADER, this)
        AEvent.addListener(AEvent.AEVENT_LIVE_REMOVE_UPLOADER, this)
        AEvent.addListener(AEvent.AEVENT_LIVE_APPLY_LINK, this)
        AEvent.addListener(AEvent.AEVENT_LIVE_APPLY_LINK_RESULT, this)
        AEvent.addListener(AEvent.AEVENT_LIVE_INVITE_LINK, this)
        AEvent.addListener(AEvent.AEVENT_LIVE_INVITE_LINK_RESULT, this)
        AEvent.addListener(AEvent.AEVENT_LIVE_GET_ONLINE_NUMBER, this)
        AEvent.addListener(AEvent.AEVENT_LIVE_SELF_KICKED, this)
        AEvent.addListener(AEvent.AEVENT_LIVE_SELF_BANNED, this)
        AEvent.addListener(AEvent.AEVENT_LIVE_REV_MSG, this)
        AEvent.addListener(AEvent.AEVENT_LIVE_REV_PRIVATE_MSG, this)
        AEvent.addListener(AEvent.AEVENT_LIVE_SELF_COMMANDED_TO_STOP, this)
        AEvent.addListener(AEvent.AEVENT_LIVE_REV_REALTIME_DATA, this)
    }


    override fun onResume() {
        super.onResume()
        MLOC.canPickupVoip = false
    }


    override fun onPause() {
        super.onPause()
        MLOC.canPickupVoip = true
    }


    override fun onRestart() {
        super.onRestart()
        addListener()
    }

    override fun onStop() {
        removeListener()
        super.onStop()
    }

}