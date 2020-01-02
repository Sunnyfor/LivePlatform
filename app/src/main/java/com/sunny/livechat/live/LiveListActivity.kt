package com.sunny.livechat.live

import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.sunny.livechat.MyApplication
import com.sunny.livechat.R
import com.sunny.livechat.base.BaseActivity
import com.sunny.livechat.base.BaseModel
import com.sunny.livechat.constant.RefreshLiveListEvent
import com.sunny.livechat.constant.UrlConstant
import com.sunny.livechat.http.ApiManager
import com.sunny.livechat.live.adapter.LiveListAdapter
import com.sunny.livechat.live.bean.LiveListBean
import com.sunny.livechat.util.ToastUtil
import com.sunny.livechat.util.sp.SpKey
import kotlinx.android.synthetic.main.activity_live_list.*
import kotlinx.android.synthetic.main.layout_list.*
import kotlinx.android.synthetic.main.layout_title.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Desc 直播室列表
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/12/23 19:17
 */
class LiveListActivity : BaseActivity() {

    private var list: ArrayList<LiveListBean.LiveInfoBean> = arrayListOf()

    private val liveListAdapter: LiveListAdapter by lazy {
        LiveListAdapter(list).apply {
            this.setOnItemClickListener { _, position ->
                MyApplication.getInstance().putData(SpKey.liveInfoBean, liveListAdapter.getData(position))
                startActivity(Intent(this@LiveListActivity, VideoLiveActivity::class.java))
            }
        }
    }

    override fun setLayout(): Int = R.layout.activity_live_list

    override fun initTitle(): View = titleManager.defaultTitle("直播大厅").apply {
        this.tv_left.visibility = View.INVISIBLE
    }

    override fun initView() {

        EventBus.getDefault().register(this)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = liveListAdapter

        btn_create.setOnClickListener(this)
    }

    override fun onClickEvent(v: View) {
        when (v.id) {
            R.id.btn_create -> startActivity(Intent(this, CreateLiveActivity::class.java))
        }
    }

    override fun loadData() {
        getLiveList()
    }

    override fun close() {
        EventBus.getDefault().unregister(this)
    }

    private fun getLiveList() {

        ApiManager.post(null, hashMapOf(), UrlConstant.GET_LIVE_ROOM_LIST_URL,
            object : ApiManager.OnResult<BaseModel<LiveListBean>>() {
                override fun onSuccess(model: BaseModel<LiveListBean>) {
                    model.requestResult({
                        list.clear()
                        list.addAll(model.page?.list ?: arrayListOf())
                        liveListAdapter.notifyDataSetChanged()
                    }, {})
                }

                override fun onFailed(code: String, msg: String) {
                    ToastUtil.showInterfaceError(code, msg)
                }
            })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refreshLiveListEvent(event: RefreshLiveListEvent) {
        loadData()
    }
}