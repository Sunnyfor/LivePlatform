package com.sunny.livechat.live.bean

import android.widget.RelativeLayout
import com.starrtc.starrtcsdk.core.player.StarPlayer

/**
 * Desc
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/12/23 20:13
 */
class ViewPosition {
    var parentView: RelativeLayout? = null
    var videoPlayer: StarPlayer? = null
    var userId: String? = null
    var upId: Int = 0

    override fun toString(): String {
        return "ViewPosition(parentView=$parentView, videoPlayer=$videoPlayer, userId=$userId, upId=$upId)"
    }
}