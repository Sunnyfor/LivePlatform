package com.sunny.livechat.login

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Toast
import com.orhanobut.logger.Logger
import com.sunny.livechat.R
import com.sunny.livechat.base.BaseActivity
import com.sunny.livechat.chat.MLOC
import com.sunny.livechat.live.LiveListActivity
import com.sunny.livechat.login.bean.UserBean
import com.sunny.livechat.login.presenter.LoginPresenter
import com.sunny.livechat.login.view.ILoginView
import com.sunny.livechat.service.KeepLiveService
import com.sunny.livechat.util.sp.SpKey
import com.sunny.livechat.util.sp.SpUtil
import kotlinx.android.synthetic.main.activity_login.*
import java.util.*

/**
 * Desc 登录
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/10/21 22:41
 */
class LoginActivity : BaseActivity(), ILoginView {


    private val loginPresenter: LoginPresenter by lazy {
        LoginPresenter(this)
    }

    override fun setLayout(): Int = R.layout.activity_login

    override fun initTitle(): View? = titleManager.defaultTitle("登录")

    override fun initView() {

        btn_login.setOnClickListener(this)

        //是否记住登录
        val isRemember = SpUtil.getBoolean(SpKey.isRememberPassword, false)
        if (isRemember) { //将账号和密码都设置到文本中
            val username = SpUtil.getString(SpKey.username)
            val password = SpUtil.getString(SpKey.password)
            et_username.setText(username)
            et_password.setText(password)
            cb_remember_pwd.isChecked = true
            btn_login.performClick()
        }

        //设置密码显示与隐藏
        cb_show_password.setOnCheckedChangeListener { _, isChecked ->
            et_password.transformationMethod = if (isChecked) {
                HideReturnsTransformationMethod.getInstance()
            } else {
                PasswordTransformationMethod.getInstance()
            }
            et_password.setSelection(et_password.text.toString().length)
        }

        cb_remember_pwd.setOnCheckedChangeListener { _, isChecked ->
            SpUtil.setBoolean(SpKey.isRememberPassword, isChecked)
        }

        checkPermission()
    }

    override fun onClickEvent(v: View) {
        when (v.id) {
            btn_login.id -> {
                val username = et_username.text.toString()
                val password = et_password.text.toString()

                if (username.isEmpty()) {
                    Toast.makeText(this@LoginActivity, "请输入账号", Toast.LENGTH_SHORT).show()
                    return
                }

                if (password.isEmpty()) {
                    Toast.makeText(this@LoginActivity, "请输入密码", Toast.LENGTH_SHORT).show()
                    return
                }

                loginPresenter.login(username, password)
            }
        }
    }

    override fun loadData() {}

    override fun close() {}


    override fun loginResult(model: UserBean?) {
        // 自动登录：保存帐号密码
        SpUtil.setString(SpKey.username, et_username.text.toString())
        SpUtil.setString(SpKey.password, et_password.text.toString())

        MLOC.saveUserId(model?.userId.toString())
        startService(Intent(this, KeepLiveService::class.java))
        Logger.i("IM服务启动")

        startActivity(Intent(this, LiveListActivity::class.java))
        finish()
    }


    private var times = 0
    private val REQUEST_PHONE_PERMISSIONS = 0

    private fun checkPermission() {
        times++
        val permissionsList = ArrayList<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(
                    Manifest.permission.ACCESS_NETWORK_STATE
                )
            }
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(
                    Manifest.permission.READ_PHONE_STATE
                )
            }
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(
                    Manifest.permission.CAMERA
                )
            }
            if (checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(
                    Manifest.permission.BLUETOOTH
                )
            }
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(
                    Manifest.permission.RECORD_AUDIO
                )
            }
            if (permissionsList.size != 0) {
                if (times == 1) {
                    requestPermissions(
                        permissionsList.toTypedArray(),
                        REQUEST_PHONE_PERMISSIONS
                    )
                } else {
                    AlertDialog.Builder(this)
                        .setCancelable(true)
                        .setTitle("提示")
                        .setMessage("获取不到授权，APP将无法正常使用，请允许APP获取权限！")
                        .setPositiveButton("确定") { _, _ ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermissions(
                                    permissionsList.toTypedArray(),
                                    REQUEST_PHONE_PERMISSIONS
                                )
                            }
                        }.setNegativeButton(
                            "取消"
                        ) { _, _ -> finish() }.show()
                }
            }
        }
    }

}