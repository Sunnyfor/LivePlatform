package com.sunny.livechat.live

import android.view.View
import android.widget.ArrayAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sunny.livechat.R
import com.sunny.livechat.base.BaseActivity
import com.sunny.livechat.constant.UrlConstant
import com.sunny.livechat.util.ToastUtil
import com.sunny.livechat.util.sp.SpKey
import com.sunny.livechat.util.sp.SpUtil
import kotlinx.android.synthetic.main.activity_host_set.*

/**
 * Desc 域名配置，后期需要改成暗藏彩蛋模式
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/12/30 09:49
 */
class HostSetActivity : BaseActivity() {

    private var list = ArrayList<String>()
    private var mAdapter: ArrayAdapter<String>? = null

    override fun setLayout(): Int = R.layout.activity_host_set

    override fun initTitle(): View? = titleManager.defaultTitle("域名配置")

    override fun initView() {

        et_input_host.clearFocus()
        et_input_host.setText(UrlConstant.host)

        mAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, list)
        lv_host.adapter = mAdapter

        getHostList()

        // 点击选择host
        lv_host.setOnItemClickListener { _, _, position, _ ->
            et_input_host.setText(list[position])
        }

        // 长按删除子条目
        lv_host.setOnItemLongClickListener { _, _, position, _ ->
            delHostItem(position)
            ToastUtil.show("删除成功！")
            true
        }

        btn_save.setOnClickListener(this)
    }

    override fun onClickEvent(v: View) {
        when (v.id) {
            R.id.btn_save -> {
                val hostStr = et_input_host.text.toString().trim()
                if (hostStr.isNotEmpty()) {

                    if (!list.contains(hostStr)) {
                        setHostList(hostStr)
                    }
                    UrlConstant.host = hostStr
                    finish()
                }

            }
        }
    }

    override fun loadData() {}

    override fun close() {}


    private fun getHostList(): ArrayList<String> {

        val json = SpUtil.getString(SpKey.hostList, "")
        if (json != "") {

            list = Gson().fromJson(json, object : TypeToken<List<String>>() {}.type)

            return list
        } else {

            list.add("http://10.0.0.158:1081")

            SpUtil.setString(SpKey.hostList, Gson().toJson(list))
        }

        return list
    }


    /**
     * 添加host
     */
    private fun setHostList(host: String) {
        list.add(0, host)
        mAdapter?.notifyDataSetChanged()

        SpUtil.setString(SpKey.hostList, Gson().toJson(list))
    }

    /**
     * 删除host
     * 先将sp中的字符串转为list，删除相对应的子条目，在存入sp中
     */
    private fun delHostItem(position: Int) {
        list.removeAt(position)
        mAdapter?.notifyDataSetChanged()

        val spList = Gson().fromJson<ArrayList<String>>(
            SpUtil.getString(SpKey.hostList, ""), object : TypeToken<List<String>>() {}.type
        )

        spList.removeAt(position)
        SpUtil.setString(SpKey.hostList, Gson().toJson(spList))
    }
}