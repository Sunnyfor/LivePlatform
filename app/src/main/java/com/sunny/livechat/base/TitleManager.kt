package com.sunny.livechat.base

import android.view.View
import com.sunny.livechat.R
import kotlinx.android.synthetic.main.act_base.*
import kotlinx.android.synthetic.main.layout_title.view.*

/**
 * Desc 标题管理类
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/11/1 23:56
 */
class TitleManager(private var baseActivity: BaseActivity) {

    /**
     * 默认标题栏
     */
    fun defaultTitle(title: String): View {
        baseActivity.fl_title.visibility = View.VISIBLE
        baseActivity.v_line.visibility = View.VISIBLE

        val view = View.inflate(baseActivity, R.layout.layout_title, null)
        view.tv_title.text = title
        view.tv_left.setOnClickListener(getBackClickListener())
        return view
    }


    /**
     * 标题栏右侧有文字/按钮
     * fontSizeResId如果为0，使用布局中的默认值
     */
    fun rightTitle(title: String, rightTitle: String, fontSizeResId: Int = 0, onRightTextClickListener: () -> Unit): View {
        val view = defaultTitle(title)
        view.tv_right.text = rightTitle
        view.tv_right.visibility = View.VISIBLE
        view.tv_right.setOnClickListener {
            onRightTextClickListener()
        }
        if (fontSizeResId > 0) view.tv_right.textSize = baseActivity.resources.getDimension(fontSizeResId)
        return view
    }

    fun rightTitle(title: String, rightTitle: String, onRightTextClickListener: () -> Unit): View {
        return rightTitle(title, rightTitle, 0, onRightTextClickListener)
    }

    private fun getBackClickListener(): View.OnClickListener = View.OnClickListener {
        baseActivity.finish()
    }

}