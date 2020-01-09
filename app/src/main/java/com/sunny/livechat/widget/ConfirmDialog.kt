package com.sunny.livechat.widget

import android.app.Activity
import com.sunny.livechat.base.BaseDialog
import kotlinx.android.synthetic.main.dialog_confirm.*

/**
 * Desc 确认对话框
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/10/31 0031 14:53
 */
class ConfirmDialog(activity: Activity) : BaseDialog(activity) {

    var prompt: String? = null

    init {
        prompt?.let {
            tv_content.text = prompt
        }
        btn_confirm.setOnClickListener {
            dismiss()
            onConfirmListener?.invoke()
        }
        btn_cancel.setOnClickListener {
            dismiss()
            onCancelListener?.invoke()
        }

        setCanceledOnTouchOutside(true)
        setCancelable(true)
    }
}