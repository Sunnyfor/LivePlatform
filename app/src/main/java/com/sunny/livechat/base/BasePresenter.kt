package com.sunny.livechat.base

import com.sunny.livechat.util.factory.ViewType
import io.reactivex.disposables.CompositeDisposable

/**
 * Desc Presenter 父类
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/11/5 15:29
 */
abstract class BasePresenter<T : IBaseView>(var view: T?) {

    val composites = CompositeDisposable()//用来管理订阅的observer

    abstract fun onCreate()

    abstract fun onClose()


    fun onDestroy() {
        composites.dispose()
        onClose()
        view = null
    }

    fun showError(code: Int, message: String) {
        val viewType = ViewType(code, message)
        when (code) {
            0 -> view?.showView(viewType)
        }

    }

    fun hideError() {
        view?.hideView()
    }

    fun showMessage(message: String) {
        view?.showMessage(message)
    }

    fun showLoading() {
        view?.showLoading()
    }

    fun hideLoading() {
        view?.hideLoading()
    }
}