package com.sunny.livechat.http

import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.google.gson.Gson
import com.orhanobut.logger.Logger
import com.sunny.livechat.R
import com.sunny.livechat.base.BaseModel
import com.sunny.livechat.MyApplication
import com.sunny.livechat.constant.UrlConstant
import com.sunny.livechat.util.NetworkUtil
import com.sunny.livechat.util.ToastUtil
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Desc Api管理类
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/10/22 0022 12:02
 */
object ApiManager {
    private lateinit var apiService: ApiService
    private lateinit var retrofit: Retrofit
    private lateinit var okHttpClient: OkHttpClient
    private val gSon = Gson()

    const val STRING = 0X1
    const val OTHER = 0x2

    init {
        init()
    }


    private fun init() {

        val cookieJar = PersistentCookieJar(
            SetCookieCache(),
            SharedPrefsCookiePersistor(MyApplication.getInstance())
        )

        /*
        * 初始化OkHttpClient
        */
        okHttpClient = OkHttpClient.Builder()
//            .addInterceptor(HeaderInterceptor())
            .addInterceptor(LoggerInterceptor(true))
            .connectTimeout(10000L, TimeUnit.MILLISECONDS)
            .readTimeout(10000L, TimeUnit.MILLISECONDS)
            .cookieJar(cookieJar)
            //其他配置
            .build()

        /*
         * 初始化Retrofit
         */
        retrofit = Retrofit.Builder()
            .baseUrl(UrlConstant.host)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(okHttpClient)
            .build()
        apiService = retrofit.create(ApiService::class.java)
    }


    /**
     * 发起一个网络请求并解析成实体类
     */
    private fun <T> request(
        composites: CompositeDisposable?,
        observable: Observable<ResponseBody>,
        onResult: OnResult<T>
    ) {
        NetworkUtil.isNetworkAvailable(object : NetworkUtil.NetworkCallback {
            override fun isNetworkAvailable(isAvailable: Boolean) {
                if (!isAvailable) {
                    onResult.onFailed(
                        "0",
                        MyApplication.getInstance().getString(R.string.networkError)
                    )
                } else {
                    observable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : Observer<ResponseBody> {

                            override fun onNext(data: ResponseBody) {
                                val body = data.string()
                                Logger.json(body)
                                parserJson(body, onResult)
                            }

                            override fun onError(e: Throwable) {
                                e.printStackTrace()

                                var code = "0"
                                var message = ""

                                if (e is SocketTimeoutException || e is TimeoutException) {
                                    message =
                                        MyApplication.getInstance().getString(R.string.timeoutError)
                                    onResult.onFailed(code, message)
                                }

                                if (e is ConnectException) {
                                    message =
                                        MyApplication.getInstance().getString(R.string.connectError)
                                    onResult.onFailed(code, message)
                                }

                                if (e is HttpException) {
                                    code = e.code().toString()
                                    message = e.message()
                                    onResult.onFailed(code, message)
                                }
                                ToastUtil.show(message)
                            }

                            override fun onComplete() {

                            }

                            override fun onSubscribe(disposable: Disposable) {
                                composites?.add(disposable)
                            }

                        })
                }
            }

        })

    }


    /**
     * GET请求
     */
    fun <T> get(
        composites: CompositeDisposable?,
        params: Map<String, String>?,
        url: String,
        onResult: OnResult<T>
    ) {
        if (params != null) {
            val sb = StringBuilder("?")
            params.forEach {
                sb.append("${it.key}=${it.value}&")
            }
            sb.deleteCharAt(sb.length - 1)
            request(composites, apiService.get(url + sb.toString()), onResult)
        } else {
            request(composites, apiService.get(url), onResult)
        }
    }


    /**
     *  Post请求
     */
    fun <T> post(
        composites: CompositeDisposable?,
        params: Map<String, String>,
        url: String,
        onResult: OnResult<T>
    ) {
        request(composites, apiService.post(params, url), onResult)
    }


    /**
     * Post一个JSON
     */
    fun <T> postJson(
        composites: CompositeDisposable?,
        params: String,
        url: String,
        onResult: OnResult<T>
    ) {
        val requestBody = RequestBody.create(MediaType.parse("application/json"), params)
        request(composites, apiService.post(requestBody, url), onResult)
    }


    /**
     * Post一张图片
     */
//    fun <T> postImage(composites: CompositeDisposable?, path: String, onResult: OnResult<T>) {
//        val requestBodyBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
//        val file = File(path)
//        requestBodyBuilder.addFormDataPart(
//            "file", path,
//            RequestBody.create(MediaType.parse("image/jpeg"), file)
//        )
//        requestBodyBuilder.addFormDataPart("picturetype", "jpg")
//        request(
//            composites,
//            apiService.post(requestBodyBuilder.build(), Constant.COMMON_UPLOADS),
//            onResult
//        )
//    }


    /**
     * 结果回调
     */
    abstract class OnResult<in T> {
        lateinit var typeToken: Type
        var tag: Int

        init {
            val t = javaClass.genericSuperclass
            val args = (t as ParameterizedType).actualTypeArguments
            val type = "class java.lang.String"
            if (args[0].toString() == type) {
                tag = STRING
            } else {
                typeToken = args[0]
                tag = OTHER
            }
        }

        abstract fun onSuccess(model: T)
        abstract fun onFailed(code: String, msg: String)
    }


    /**
     * 动态解析JSON
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T> parserJson(json: String, onResult: OnResult<T>) {
        if (onResult.tag == STRING) {
            onResult.onSuccess(json as T)
        } else {
            if (onResult.typeToken.toString().contains(BaseModel::class.java.name)) {
                try {
                    val baseModel = gSon.fromJson<T>(json, onResult.typeToken)
                    onResult.onSuccess(baseModel)
                } catch (e: Exception) {
                    e.printStackTrace()
                    onResult.onFailed("0", e.message.toString())
                }
            } else {
                onResult.onSuccess(gSon.fromJson(json, onResult.typeToken))
            }
        }
    }
}