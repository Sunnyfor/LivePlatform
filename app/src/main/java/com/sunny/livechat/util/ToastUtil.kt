package com.sunny.livechat.util

import android.widget.Toast
import com.orhanobut.logger.Logger
import com.sunny.livechat.MyApplication

/**
 * Desc 单例Toast
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/10/22 17:38
 */
object ToastUtil {

    private var toast = Toast.makeText(MyApplication.getInstance(), "", Toast.LENGTH_SHORT)

    /**
     * 显示Toast
     * @param content Toast信息
     */
    fun show(content: String?, type: Int) {
        content?.let {
            toast.setText(content)
            toast.duration = type
            toast.show()
        }
    }

    fun show(content: String?) {
        show(content, Toast.LENGTH_SHORT)
    }

    fun show() {
        show("阿猿正在玩命开发，敬请期待...", Toast.LENGTH_LONG)
    }

    fun showInterfaceError(code: String, msg: String) {
        Logger.e("error:  $msg : $code")
        show("数据请求失败，请联系相关开发人员... \n $msg : $code", Toast.LENGTH_LONG)
    }
}