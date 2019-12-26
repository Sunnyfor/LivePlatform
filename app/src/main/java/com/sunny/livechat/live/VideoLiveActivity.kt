package com.sunny.livechat.live

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.ListView
import android.widget.RelativeLayout
import com.sunny.livechat.live.adapter.LiveMsgListAdapter
import com.sunny.livechat.live.bean.ViewPosition
import com.starrtc.starrtcsdk.api.*
import com.starrtc.starrtcsdk.apiInterface.IXHResultCallback
import com.starrtc.starrtcsdk.core.audio.StarRTCAudioManager
import com.starrtc.starrtcsdk.core.im.message.XHIMMessage
import com.starrtc.starrtcsdk.core.player.StarPlayer
import com.starrtc.starrtcsdk.core.player.StarPlayerScaleType
import com.starrtc.starrtcsdk.core.pusher.XHCameraRecorder
import com.sunny.livechat.R
import com.sunny.livechat.base.BaseActivity
import com.sunny.livechat.chat.AEvent
import com.sunny.livechat.chat.IChatListener
import com.sunny.livechat.chat.MLOC
import com.sunny.livechat.chat.listener.XHLiveManagerListener
import com.sunny.livechat.util.DensityUtils
import com.sunny.livechat.util.ToastUtil
import com.sunny.livechat.widget.CircularCoverView
import kotlinx.android.synthetic.main.activity_video_live.*
import org.json.JSONException
import org.json.JSONObject

/**
 * Desc 直播播放页
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/12/23 19:51
 */
class VideoLiveActivity : BaseActivity(), IChatListener {

    companion object {
        var CREATOR_ID = "CREATOR_ID"          //创建者ID
        var LIVE_TYPE = "LIVE_TYPE"           //创建信息
        var LIVE_ID = "LIVE_ID"            //直播ID
        var LIVE_NAME = "LIVE_NAME"          //直播名称
    }

    private var isUploader = false
    private var createrId: String? = null
    private var liveId: String? = null
    private var liveName: String? = null
    private var liveType: XHConstants.XHLiveType? = null
    private var liveManager: XHLiveManager? = null
    private var mPrivateMsgTargetId: String? = null
    private var starRTCAudioManager: StarRTCAudioManager? = null
    private var isPortrait = true
    private var msgList = ArrayList<XHIMMessage>()
    private val mAdapter: LiveMsgListAdapter by lazy {
        LiveMsgListAdapter(this, msgList)
    }

    private var mPlayerList = ArrayList<ViewPosition>()
    private var borderW = 0
    private var borderH = 0

    override fun setLayout(): Int = R.layout.activity_video_live

    override fun initTitle(): View? = null

    override fun initView() {
        starRTCAudioManager = StarRTCAudioManager.create(this)
        starRTCAudioManager?.start { _, _ -> }

        val dm = resources.displayMetrics
        isPortrait = dm.heightPixels > dm.widthPixels


        createrId = intent.getStringExtra(CREATOR_ID)
        liveName = intent.getStringExtra(LIVE_NAME)
        liveId = intent.getStringExtra(LIVE_ID)
        intent.getSerializableExtra(LIVE_TYPE)?.let {
            liveType = it as XHConstants.XHLiveType
        }

        if (TextUtils.isEmpty(liveId)) {
            if (createrId == MLOC.userId) {
                if (TextUtils.isEmpty(liveName) || liveType == null) {
                    ToastUtil.show("没有直播信息")
                    stopAndFinish()
                    return
                }
            } else {
                if (TextUtils.isEmpty(liveName) || liveType == null) {
                    ToastUtil.show("没有直播信息")
                    stopAndFinish()
                    return
                }
            }
        }

        liveManager = XHClient.getInstance().getLiveManager(this)
        liveManager?.setRtcMediaType(XHConstants.XHRtcMediaTypeEnum.STAR_RTC_MEDIA_TYPE_VIDEO_AND_AUDIO)
        liveManager?.setRecorder(XHCameraRecorder())
        liveManager?.addListener(XHLiveManagerListener())

        live_id_text.text = ("直播编号：$liveName")
        back_btn.setOnClickListener { onBackPressed() }

        id_input.clearFocus()

        msg_list.transcriptMode = ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL
        msg_list.isStackFromBottom = true
        msg_list.adapter = mAdapter
        msg_list.setOnItemClickListener { _, _, position, _ ->
            val clickUserId = msgList[position].fromId
            val msgText = msgList[position].contentData
            showManagerDialog(clickUserId, msgText)
        }

        send_btn.setOnClickListener(this)


        if (createrId != null && createrId == MLOC.userId) {
            mic_btn.visibility = View.GONE
            switch_camera.visibility = View.VISIBLE
            panel_btn.visibility = View.VISIBLE
            //            vCarBtn.setVisibility(View.VISIBLE);
        } else {
            mic_btn.visibility = View.VISIBLE
            switch_camera.visibility = View.GONE
            panel_btn.visibility = View.GONE
            //            vCarBtn.setVisibility(View.GONE);
        }


        mic_btn.setOnClickListener {
            if (isUploader) {
                AlertDialog.Builder(this@VideoLiveActivity).setCancelable(true)
                    .setTitle("是否结束上麦?")
                    .setNegativeButton("取消") { _, _ -> }.setPositiveButton(
                        "确定"
                    ) { _, _ ->
                        isUploader = false
                        liveManager?.changeToAudience(object : IXHResultCallback {
                            override fun success(data: Any) {

                            }

                            override fun failed(errMsg: String) {

                            }
                        })
                        mic_btn.isSelected = false
                        switch_camera.visibility = View.GONE
                        panel_btn.visibility = View.GONE
                        //                                            vCarBtn.setVisibility(View.GONE);
                    }.show()
            } else {
                AlertDialog.Builder(this@VideoLiveActivity).setCancelable(true)
                    .setTitle("是否申请上麦?")
                    .setNegativeButton("取消") { _, _ -> }.setPositiveButton(
                        "确定"
                    ) { _, _ -> liveManager?.applyToBroadcaster(createrId) }.show()
            }
        }

        switch_camera.setOnClickListener {
            liveManager?.switchCamera()
        }

        borderW = DensityUtils.screenWidth(this)
        borderH = borderW / 3 * 4

        panel_btn.setOnClickListener {
            if (panel_btn.isSelected) {
                panel_btn.isSelected = false
                clean_btn.visibility = View.INVISIBLE
                painter.pause()
            } else {
                panel_btn.isSelected = true
                clean_btn.visibility = View.VISIBLE
                painter.publish(liveManager)
            }
        }

        clean_btn.setOnClickListener {
            painter.clean()
        }

        if (createrId == MLOC.userId) {
            if (liveId == null) {
                startActivity(Intent(this, CreateLiveRoomActivity::class.java))
            } else {
                startLive()
            }
        } else {
            joinLive()
        }
    }


    override fun onClickEvent(v: View) {
        when (v.id) {
            send_btn.id -> {
                val txt = id_input.text.toString()
                if (!TextUtils.isEmpty(txt)) {
                    sendChatMsg(txt)
                    id_input.setText("")
                }
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(id_input.windowToken, 0)
            }
        }
    }

    override fun loadData() {

    }

    override fun close() {

    }


    private fun showManagerDialog(userId: String, msgText: String) {
        if (userId != MLOC.userId) {
            val builder = AlertDialog.Builder(this)
            if (createrId == MLOC.userId) {
                var ac = false

                mPlayerList.forEach {
                    if (userId == it.userId) {
                        ac = true
                        return@forEach
                    }
                }

                if (ac) {
                    val items = arrayOf("踢出房间", "禁止发言", "私信", "下麦")
                    builder.setItems(items) { _, i ->
                        if (i == 0) {
                            kickUser(userId)
                        } else if (i == 1) {
                            muteUser(userId, 60)
                        } else if (i == 2) {
                            mPrivateMsgTargetId = userId
                            id_input.setText(("[私$userId]"))
                        } else if (i == 3) {
                            liveManager?.commandToAudience(userId)
                        }
                    }
                } else {
                    val items = arrayOf("踢出房间", "禁止发言", "私信", "邀请上麦")
                    builder.setItems(items) { _, i ->
                        if (i == 0) {
                            kickUser(userId)
                        } else if (i == 1) {
                            muteUser(userId, 60)
                        } else if (i == 2) {
                            mPrivateMsgTargetId = userId
                            id_input.setText(("[私$userId]"))
                        } else if (i == 3) {
                            liveManager?.inviteToBroadcaster(userId)
                        }
                    }
                }


            } else {
                val items = arrayOf("私信")
                builder.setItems(items) { _, i ->
                    if (i == 0) {
                        mPrivateMsgTargetId = userId
                        id_input.setText(("[私$userId]"))
                    }
                }
            }
            builder.setCancelable(true)
            val dialog = builder.create()
            dialog.show()
        }

    }

    private fun kickUser(userId: String) {
        liveManager?.kickMember(userId, object : IXHResultCallback {
            override fun success(data: Any) {
                //踢人成功
            }

            override fun failed(errMsg: String) {
                //踢人失败
            }
        })
    }

    private fun muteUser(userId: String, times: Int) {
        liveManager?.muteMember(userId, times, object : IXHResultCallback {
            override fun success(data: Any) {
                //禁言成功
            }

            override fun failed(errMsg: String) {
                //禁言失败
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


    private fun sendChatMsg(msg: String) {
        Log.d("XHLiveManager", "sendChatMsg $msg")
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
        mAdapter.notifyDataSetChanged()
        mPrivateMsgTargetId = ""

    }


    private fun startLive() {
        //开始直播
        isUploader = true
        liveManager?.startLive(liveId, object : IXHResultCallback {
            override fun success(data: Any) {
                MLOC.d("XHLiveManager", "startLive success $data")
            }

            override fun failed(errMsg: String) {
                MLOC.d("XHLiveManager", "startLive failed $errMsg")
                MLOC.showMsg(this@VideoLiveActivity, errMsg)
                stopAndFinish()
            }
        })
    }

    private fun joinLive() {
        //观众加入直播
        isUploader = false
        liveManager?.watchLive(liveId, object : IXHResultCallback {
            override fun success(data: Any) {
                MLOC.d("XHLiveManager", "watchLive success $data")
            }

            override fun failed(errMsg: String) {
                MLOC.d("XHLiveManager", "watchLive failed $errMsg")
                MLOC.showMsg(this@VideoLiveActivity, errMsg)
                stopAndFinish()
            }
        })
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


    fun addListener() {
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

    override fun dispatchEvent(aEventID: String?, success: Boolean, eventObj: Any?) {
        MLOC.d("XHLiveManager", "dispatchEvent  $aEventID$eventObj")
        when (aEventID) {
            AEvent.AEVENT_LIVE_ADD_UPLOADER -> try {
                val data = eventObj as JSONObject
                val addId = data.getString("actorID")
                addPlayer(addId)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            AEvent.AEVENT_LIVE_REMOVE_UPLOADER -> try {
                val data = eventObj as JSONObject
                val removeUserId = data.getString("actorID")
                deletePlayer(removeUserId)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            AEvent.AEVENT_LIVE_APPLY_LINK -> AlertDialog.Builder(this@VideoLiveActivity).setCancelable(
                true
            )
                .setTitle(eventObj.toString() + "申请上麦")
                .setNegativeButton("拒绝") { _, _ -> liveManager?.refuseApplyToBroadcaster(eventObj as String) }.setPositiveButton(
                    "同意"
                ) { _, _ -> liveManager?.agreeApplyToBroadcaster(eventObj as String) }.show()
            AEvent.AEVENT_LIVE_APPLY_LINK_RESULT -> if (eventObj as XHConstants.XHLiveJoinResult == XHConstants.XHLiveJoinResult.XHLiveJoinResult_accept) {
                AlertDialog.Builder(this@VideoLiveActivity).setCancelable(true)
                    .setTitle("房主同意连麦，是否现在开始上麦？")
                    .setNegativeButton("取消") { _, _ -> }.setPositiveButton(
                        "开始"
                    ) { _, _ ->
                        isUploader = true
                        liveManager?.changeToBroadcaster(object : IXHResultCallback {
                            override fun success(data: Any) {

                            }

                            override fun failed(errMsg: String) {

                            }
                        })
                        mic_btn.isSelected = true
                        switch_camera.visibility = View.VISIBLE
                        panel_btn.visibility = View.VISIBLE
                    }.show()
            }
            AEvent.AEVENT_LIVE_INVITE_LINK -> AlertDialog.Builder(this@VideoLiveActivity).setCancelable(
                true
            )
                .setTitle(eventObj.toString() + "邀请您上麦")
                .setNegativeButton("拒绝") { _, _ -> liveManager?.refuseInviteToBroadcaster(eventObj as String) }.setPositiveButton(
                    "同意"
                ) { _, _ ->
                    mic_btn.isSelected = true
                    switch_camera.visibility = View.VISIBLE
                    isUploader = true
                    liveManager?.agreeInviteToBroadcaster(eventObj as String)
                }.show()
            AEvent.AEVENT_LIVE_INVITE_LINK_RESULT -> {
                when (eventObj as XHConstants.XHLiveJoinResult) {
                    XHConstants.XHLiveJoinResult.XHLiveJoinResult_accept -> sendChatMsg("欢迎新的小伙伴上麦！！！")
                    XHConstants.XHLiveJoinResult.XHLiveJoinResult_refuse -> {
                    }
                    XHConstants.XHLiveJoinResult.XHLiveJoinResult_outtime -> {
                    }
                }
            }
            AEvent.AEVENT_LIVE_GET_ONLINE_NUMBER -> {
            }
            AEvent.AEVENT_LIVE_SELF_KICKED -> {
                MLOC.showMsg(this@VideoLiveActivity, "你已被踢出")
                stopAndFinish()
            }
            AEvent.AEVENT_LIVE_SELF_BANNED -> {
                val banTime = eventObj.toString()
                MLOC.showMsg(this@VideoLiveActivity, "你已被禁言," + banTime + "秒后自动解除")
            }
            AEvent.AEVENT_LIVE_REV_MSG -> {
                val revMsg = eventObj as XHIMMessage
                msgList.add(revMsg)
                mAdapter.notifyDataSetChanged()
            }
            AEvent.AEVENT_LIVE_REV_PRIVATE_MSG -> {
                val revMsgPrivate = eventObj as XHIMMessage
                msgList.add(revMsgPrivate)
                mAdapter.notifyDataSetChanged()
            }
            AEvent.AEVENT_LIVE_ERROR -> {
                var errStr = eventObj as String
                if (errStr == "30016") {
                    errStr = "直播关闭"
                }
                MLOC.showMsg(applicationContext, errStr)
                stopAndFinish()
            }
            AEvent.AEVENT_LIVE_SELF_COMMANDED_TO_STOP -> if (isUploader) {
                isUploader = false
                mic_btn.isSelected = false
                switch_camera.visibility = View.GONE
                panel_btn.visibility = View.GONE
                //                            vCarBtn.setVisibility(View.GONE);
                MLOC.showMsg(this@VideoLiveActivity, "您的表演被叫停")
            }
            AEvent.AEVENT_LIVE_REV_REALTIME_DATA -> if (success) {
                try {
                    val jsonObject = eventObj as JSONObject
                    val tData = jsonObject.get("data") as ByteArray
                    val tUpid = jsonObject.getString("upId")
                    painter.setPaintData(tData, tUpid)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }//                onLineUserNumber = (int) eventObj;
    }


    private fun addPlayer(addUserID: String) {
        val newOne = ViewPosition()
        newOne.userId = addUserID
        val player = StarPlayer(this)
        newOne.videoPlayer = player
        mPlayerList.add(newOne)
        view1.addView(player)
        val coverView = CircularCoverView(this)
        coverView.layoutParams = RelativeLayout.LayoutParams(
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

        if (mPlayerList.size == 1) {
            liveManager?.attachPlayerView(addUserID, player, true)
        } else {
            liveManager?.attachPlayerView(addUserID, player, false)
        }
    }


    private var isRuning = false
    private fun changeLayout(v: View) {
        if (isRuning) return
        if (v === mPlayerList[0].videoPlayer) return
        var clickPlayer: ViewPosition? = null
        var clickIndex = 0
        for (i in mPlayerList.indices) {
            if (mPlayerList[i].videoPlayer === v) {
                clickIndex = i
                clickPlayer = mPlayerList.removeAt(i)
                liveManager?.changeToBig(clickPlayer.userId)
                break
            }
        }
        val mainPlayer = mPlayerList.removeAt(0)
        liveManager?.changeToSmall(mainPlayer.userId)
        mPlayerList.remove(clickPlayer)
        clickPlayer?.let {
            mPlayerList.add(0, it)
        }
        mPlayerList.add(clickIndex, mainPlayer)

        val finalClickPlayer = clickPlayer
        startAnimation(finalClickPlayer?.videoPlayer, mainPlayer.videoPlayer)
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
                clickPlayer.x =
                    clickStartX + animation.animatedValue as Float * (clickEndX - clickStartX)
                clickPlayer.y =
                    clickStartY + animation.animatedValue as Float * (clickEndY - clickStartY)
                clickPlayer.layoutParams.width =
                    (clickStartW + animation.animatedValue as Float * (clickEndW - clickStartW)).toInt()
                clickPlayer.layoutParams.height =
                    (clickStartH + animation.animatedValue as Float * (clickEndH - clickStartH)).toInt()
                clickPlayer.requestLayout()

                mainPlayer.x =
                    mainStartX + animation.animatedValue as Float * (mainEndX - mainStartX)
                mainPlayer.y =
                    mainStartY + animation.animatedValue as Float * (mainEndY - mainStartY)
                mainPlayer.layoutParams.width =
                    (mainStartW + animation.animatedValue as Float * (mainEndW - mainStartW)).toInt()
                mainPlayer.layoutParams.height =
                    (mainStartH + animation.animatedValue as Float * (mainEndH - mainStartH)).toInt()
                mainPlayer.requestLayout()
            }

            valTotal.duration = 300
            valTotal.interpolator = LinearInterpolator()
            valTotal.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    isRuning = true
                }

                override fun onAnimationEnd(animation: Animator) {
                    isRuning = false
                    clickPlayer.setScalType(StarPlayerScaleType.DRAW_TYPE_CENTER)
                    mainPlayer.setScalType(StarPlayerScaleType.DRAW_TYPE_CENTER)
                }

                override fun onAnimationCancel(animation: Animator) {
                    isRuning = false
                }

                override fun onAnimationRepeat(animation: Animator) {

                }
            })
            valTotal.start()
        }
    }

    private fun deletePlayer(removeUserId: String) {
        if (mPlayerList.size > 0) {
            for (i in mPlayerList.indices) {
                val temp = mPlayerList[i]
                if (temp.userId.equals(removeUserId)) {
                    val remove = mPlayerList.removeAt(i)
                    view1.removeView(remove.videoPlayer)
                    resetLayout()
                    liveManager?.changeToBig(mPlayerList[0].userId)
                    break
                }
            }
        }
    }


    private fun resetLayout() {
        if (isPortrait) {
            when (mPlayerList.size) {
                1 -> {
                    val player = mPlayerList[0].videoPlayer
                    val lp = RelativeLayout.LayoutParams(borderW, borderH)
                    player?.layoutParams = lp
                    player?.y = 0f
                    player?.x = 0f
                }
                2, 3, 4 -> {
                    for (i in mPlayerList.indices) {
                        if (i == 0) {
                            val player = mPlayerList[i].videoPlayer
                            val lp = RelativeLayout.LayoutParams(borderW / 3 * 2, borderH)
                            player?.layoutParams = lp
                            player?.y = 0f
                            player?.x = 0f
                        } else {
                            val player = mPlayerList[i].videoPlayer
                            val lp = RelativeLayout.LayoutParams(borderW / 3, borderH / 3)
                            player?.layoutParams = lp
                            player?.y = ((i - 1) * borderH / 3).toFloat()
                            player?.x = (borderW / 3 * 2).toFloat()
                        }
                    }
                }
                5, 6, 7 -> {
                    for (i in mPlayerList.indices) {
                        if (i == 0) {
                            val player = mPlayerList[i].videoPlayer
                            val lp = RelativeLayout.LayoutParams(
                                borderW - borderW / 3,
                                borderH - borderH / 4
                            )
                            player?.layoutParams = lp
                        } else if (i in 1..2) {
                            val player = mPlayerList[i].videoPlayer
                            val lp = RelativeLayout.LayoutParams(borderW / 3, borderH / 4)
                            player?.layoutParams = lp
                            player?.x = (borderW - borderW / 3).toFloat()
                            player?.y = ((i - 1) * borderH / 4).toFloat()
                        } else {
                            val player = mPlayerList[i].videoPlayer
                            val lp = RelativeLayout.LayoutParams(borderW / 3, borderH / 4)
                            player?.layoutParams = lp
                            player?.x = ((i - 3) * borderW / 3).toFloat()
                            player?.y = (borderH - borderH / 4).toFloat()
                        }
                    }
                }
            }
        } else {
            when (mPlayerList.size) {
                1 -> {
                    val player = mPlayerList[0].videoPlayer
                    val lp = RelativeLayout.LayoutParams(borderW, borderH)
                    player?.layoutParams = lp
                    player?.y = 0f
                    player?.x = 0f
                }
                2, 3, 4 -> {
                    for (i in mPlayerList.indices) {
                        if (i == 0) {
                            val player = mPlayerList[i].videoPlayer
                            val lp = RelativeLayout.LayoutParams(borderW / 4 * 3, borderH)
                            player?.layoutParams = lp
                            player?.y = 0f
                            player?.x = 0f
                        } else {
                            val player = mPlayerList[i].videoPlayer
                            val lp = RelativeLayout.LayoutParams(borderW / 4, borderH / 3)
                            player?.layoutParams = lp
                            player?.y = ((i - 1) * borderH / 3).toFloat()
                            player?.x = (borderW / 4 * 3).toFloat()
                            player?.setScalType(StarPlayerScaleType.DRAW_TYPE_TOTAL_GRAPH)
                        }
                    }
                }
                5, 6, 7 -> {
                    for (i in mPlayerList.indices) {
                        if (i == 0) {
                            val player = mPlayerList[i].videoPlayer
                            val lp = RelativeLayout.LayoutParams(borderW / 4 * 2, borderH)
                            player?.layoutParams = lp
                            player?.y = 0f
                            player?.x = 0f
                        } else if (i in 1..2) {
                            val player = mPlayerList[i].videoPlayer
                            val lp = RelativeLayout.LayoutParams(borderW / 4, borderH / 3)
                            player?.layoutParams = lp
                            player?.y = ((i - 1) * borderH / 3).toFloat()
                            player?.x = (borderW / 4 * 2).toFloat()
                        } else {
                            val player = mPlayerList[i].videoPlayer
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

}