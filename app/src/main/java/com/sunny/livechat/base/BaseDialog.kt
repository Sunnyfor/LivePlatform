package com.sunny.livechat.base

import android.app.Activity
import android.app.Dialog
import android.view.InflateException
import com.sunny.livechat.R

/**
 * Desc 对话框基类
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/10/31 0031 14:55
 */
open class BaseDialog(activity: Activity, layout: Int = R.layout.dialog_confirm) : Dialog(activity, R.style.style_common_dialog) {

    var onConfirmListener: (() -> Unit)? = null
    var onCancelListener: (() -> Unit)? = null

    private var isShow = false

    init {
        initView(layout)
    }


    fun initView(layoutRes: Int) {
        try {
            setContentView(layoutRes)
        } catch (e: InflateException) {
            e.printStackTrace()
        }
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }

    override fun onBackPressed() {
        if (!isShow) {
            super.onBackPressed()
        }
    }

}