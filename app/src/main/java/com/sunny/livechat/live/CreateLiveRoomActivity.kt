package com.sunny.livechat.live

import android.app.Activity
import android.content.Intent
import android.view.View
import com.sunny.livechat.live.bean.LiveListBean
import com.google.gson.Gson
import com.orhanobut.logger.Logger
import com.sunny.livechat.R
import com.sunny.livechat.base.BaseActivity
import com.sunny.livechat.constant.UrlConstant
import com.sunny.livechat.http.ApiManager
import com.sunny.livechat.util.ToastUtil
import com.sunny.livechat.util.URIUtil
import com.sunny.livechat.util.intent.IntentValue
import kotlinx.android.synthetic.main.act_live_create.*

/**
 * Desc 创建直播间
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/12/24 14:52
 */
class CreateLiveRoomActivity : BaseActivity() {

    private var liveInfoBean: LiveListBean.LiveInfoBean? = null

    override fun setLayout(): Int = R.layout.act_live_create

    override fun initTitle(): View? = titleManager.defaultTitle("创建直播间")

    override fun initView() {
        iv_live_cover.setOnClickListener(this)
        btn_create.setOnClickListener(this)
    }

    override fun onClickEvent(v: View) {
        when (v.id) {
            R.id.btn_create -> doCreate()
            R.id.iv_live_cover -> {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(intent, IntentValue.requestCode)
            }
        }
    }

    override fun loadData() {}

    override fun close() {}

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IntentValue.requestCode && resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data
            val path = URIUtil.getRealPathFromUri(this, uri)
            liveInfoBean?.liveCover = path
            Logger.i("图片的绝对地址:$path")
        }
    }

    private fun doCreate() {

        if (et_live_name.text.isEmpty()) {
            ToastUtil.show("请输入直播间名称！")
            return
        }

        liveInfoBean?.let {
            it.liveName = et_live_name.text.toString()
            it.liveNotice = et_live_notice.text.toString()
            it.liveCode = ""
            it.liveState = ""
            it.isMsg = ""
            it.liveSrc = ""
            it.liveClassId = ""
        }

        val json = Gson().toJson(liveInfoBean)

        ApiManager.postJson(null, json.toString(), UrlConstant.CREATE_LIVE_ROOM_URL, object : ApiManager.OnResult<LiveListBean>() {
            override fun onSuccess(model: LiveListBean) {
                ToastUtil.show("创建成功")
                finish()
            }

            override fun onFailed(code: String, msg: String) {
                ToastUtil.showInterfaceError(code, msg)
            }
        })
    }
}