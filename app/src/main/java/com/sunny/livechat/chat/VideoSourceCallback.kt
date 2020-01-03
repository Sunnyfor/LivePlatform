package com.sunny.livechat.chat

import com.starrtc.starrtcsdk.core.videosrc.StarAudioData
import com.starrtc.starrtcsdk.core.videosrc.StarVideoData
import com.starrtc.starrtcsdk.core.videosrc.XHVideoSourceCallback
import com.sunny.livechat.util.LogUtil

class VideoSourceCallback : XHVideoSourceCallback() {
    override fun onVideoFrame(videoData: StarVideoData): StarVideoData {
        LogUtil.d("视频源数据已经接到了，不做处理，直接再丢回去" + videoData.dataLength)
        //可直接对videoData里的数据进行处理，处理后将videoData对象返回即可。
        return videoData
    }

    override fun onAudioFrame(audioData: StarAudioData): StarAudioData {
        LogUtil.d("音频源数据已经接到了，不做处理，直接再丢回去" + audioData.dataLength)
        //可直接对audioData里的数据进行处理，处理后将audioData对象返回即可。
        return audioData
    }
}