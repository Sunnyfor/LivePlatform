package com.sunny.livechat.widget.textView

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView

/**
 * Desc  图标字体的TextView
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/10/22 0022 12:02
 */
open class IconTextView : TextView {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
//        typeface = MyApplication.getInstance().iconFontType
    }

    /**
     * 字体加粗
     */
    fun boldText() {
        paint.isFakeBoldText = true
    }
}