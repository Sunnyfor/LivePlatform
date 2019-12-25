package com.sunny.livechat.base

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.orhanobut.logger.Logger
import com.sunny.livechat.R
import com.sunny.livechat.util.ToastUtil
import com.sunny.livechat.util.factory.ViewType
import kotlinx.android.synthetic.main.act_base.*
import kotlinx.android.synthetic.main.layout_empty.view.*

/**
 * Desc Activity基类
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/11/2 00:31
 */
@Suppress("UNCHECKED_CAST")
abstract class BaseActivity : AppCompatActivity(), IBaseView, View.OnClickListener {

    val loadingView: View by lazy {
        View.inflate(this, R.layout.layout_loading, null)
    }

    val promptView: View by lazy {
        View.inflate(this, R.layout.layout_empty, null)
    }

    lateinit var titleManager: TitleManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN) //强制关闭键盘

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT //强制屏幕

        titleManager = TitleManager(this)

        setContentView(R.layout.act_base)

        // 初始化标题栏
        if (initTitle() != null) {
            fl_title.removeAllViews()
            fl_title.addView(initTitle())
        }

        // 初始化内容区
        val layoutRes = setLayout()
        if (layoutRes != 0) {
            val bodyView = LayoutInflater.from(this).inflate(layoutRes, null, false)
            fl_body.addView(bodyView)
        }

        loadingView.setOnClickListener { }
        promptView.setOnClickListener { } //空事件，防止点击穿透

        initView()
        loadData()
    }

    abstract fun setLayout(): Int

    abstract fun initTitle(): View?

    abstract fun initView()

    abstract fun onClickEvent(v: View)

    abstract fun loadData()

    abstract fun close()


    override fun showLoading() {
        hideLoading()
        loadingView.setOnClickListener { }

        fl_body.addView(loadingView)
    }

    override fun hideLoading() {
        fl_body.removeView(loadingView)
    }

    override fun showView(type: ViewType) {
        hideView()

        if (type.viewCode == 0) {
            promptView.btn_retry.visibility = View.GONE
        } else {
            promptView.btn_retry.visibility = View.VISIBLE
            promptView.setOnClickListener {
                hideView()
                loadData()
            }
        }
        if (type.viewIcon != 0) {
            promptView.iv_icon.setImageResource(type.viewIcon)
        }
        promptView.tv_desc.text = type.viewMessage

        fl_body.addView(promptView)
    }

    override fun hideView() {
        fl_body.removeView(promptView)
    }

    override fun showMessage(message: String) {
        ToastUtil.show(message)
    }

    override fun onDestroy() {
        super.onDestroy()
        close()
    }


    /**
     * 拦截按钮多次点击事件
     */
    private var lastClickId = 0
    private var lastClickTime = 0L
    override fun onClick(v: View) {
        if (v.id == lastClickId && System.currentTimeMillis() - lastClickTime < 500) {
            lastClickId = 0
            lastClickTime = 0
            Logger.i("拦截重复点击生效")
            return
        }
        lastClickId = v.id
        lastClickTime = System.currentTimeMillis()
        onClickEvent(v)
    }

}