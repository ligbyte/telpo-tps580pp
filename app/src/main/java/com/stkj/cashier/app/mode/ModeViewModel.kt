package com.stkj.cashier.app.mode

import android.app.Application
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.alibaba.fastjson.JSON
import com.stkj.cashier.utils.util.LogUtils
import com.google.gson.Gson
import com.king.base.util.SystemUtils
import com.stkj.cashier.App
import com.stkj.cashier.R
import com.stkj.cashier.app.base.BaseModel
import com.stkj.cashier.app.base.BaseViewModel
import com.stkj.cashier.bean.*
import com.stkj.cashier.config.MessageEventType
import com.stkj.cashier.utils.util.GsonUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.inject.Inject


/**
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 */
@HiltViewModel
class ModeViewModel @Inject constructor(application: Application, model: BaseModel?) :
    BaseViewModel(application, model) {

        val TAG = "ModeViewModel"
//    var beforeparams = ""
    val liveDataBanner by lazy { MutableLiveData<List<BannerBean>>() }

    val liveData by lazy { MutableLiveData<MutableList<Bean>>() }
    val modifyBalance by lazy { MutableLiveData<Result<ModifyBalanceBean>>() }
    val queryBalance by lazy { MutableLiveData<Result<QueryBalanceBean>>() }
    val consumeRecord by lazy { MutableLiveData<ConsumeRecordListBean>() }

    val sendPhoneMsg by lazy { MutableLiveData<Result<SendPhoneMsgBean>>() }
    val restaurantManager by lazy { MutableLiveData<Result<RestaurantManagerBean>>() }
    val checkPhoneCode by lazy{ MutableLiveData<Result<CheckPhoneCodeBean>>() }

    val takeMealsList by lazy { MutableLiveData<TakeMealsListResult>() } //订餐信息列表
    val takeMeals by lazy { MutableLiveData<Result<TakeMealsBean>>() } //已出餐的订单信息
    val currentTimeInfo by lazy { MutableLiveData<Result<CurrentTimeInfoBean>>() }

    val consumeRefund by lazy { MutableLiveData<ConsumeRefundListBean>() }//查询退款订单列表

    val consumeRefundList by lazy { MutableLiveData<Result<ConsumeRefundListBean>>() }//查询退款订单列表

    val consumeRefundResult by lazy { MutableLiveData<Result<Any>>() }//处理退款

    fun getRequestBanner() {
        launch(false) {
            // TODO 模拟请求
            val data = arrayOf(
                "https://jenly1314.gitee.io/medias/banner/1.jpg",
                "https://jenly1314.gitee.io/medias/banner/2.jpg",
                "https://jenly1314.gitee.io/medias/banner/3.jpg",
                "https://jenly1314.gitee.io/medias/banner/4.jpg"
            )
            delay(1000)
            liveDataBanner.value = data.map { BannerBean(it) }
        }
    }

    fun getRequestData(curPage: Int, pageSize: Int) {
        // TODO 模拟请求
        launch(false) {
            var start = (curPage - 1) * pageSize + 1
            var end = (curPage) * pageSize
            if (curPage > 1) {
                end -= pageSize / 2
            }
            var data = ArrayList<Bean>()
            for (index in start..end) {
                var bean = Bean()
                with(bean) {
                    title = "列表模板标题示例$index"
                    content = "列表模板内容示例$index"
                    imageUrl = "http://jenly1314.gitee.io/medias/banner/${index % 7}.jpg"
                }
                data.add(bean)
            }
            delay(1000)
            liveData.value = data
        }
    }

    fun modifyBalance(params: Map<String, Any>) {
        if (SystemUtils.isNetWorkActive(getApp())) {
            launch(false, block = {
                // TODO Http请求
                val result = apiService.modifyBalance(params)
                // TODO 只需处理成功的场景，失败的场景都已统一处理
                /* ToastUtils.showShort("失败")
                 if(!isSuccess(result)){
                     ToastUtils.showShort("断网"+Gson().toJson(result))
                 }*/

//            if(isSuccess(result)){
                modifyBalance.value = result
//            }
            }, error = {
                Timber.w(it)
                if (SystemUtils.isNetWorkActive(getApp())) {
                    when (it) {
                        is SocketTimeoutException -> ttsSpeak("网络连接超时")
                        is ConnectException -> ttsSpeak("网络连接失败")
                        else -> ttsSpeak("系统异常,请重试")
                    }
                } else {
                    ttsSpeak("网络已断开，请检查网络。")
                }
                //金额模式
                EventBus.getDefault().post(MessageEventBean(MessageEventType.AmountCancel))
                EventBus.getDefault().post(MessageEventBean(MessageEventType.ModifyBalanceError))
            })
        } else {
            ttsSpeak(getString(R.string.result_network_unavailable_error))
            //金额模式
            EventBus.getDefault().post(MessageEventBean(MessageEventType.AmountCancel))
        }


    }

    fun queryBalance(params: Map<String, Any>) {
        if (SystemUtils.isNetWorkActive(getApp())) {
            launch(false, block = {
                val result = apiService.queryBalance(params)
                queryBalance.value = result
            }, error = {
                Timber.w(it)
                if (SystemUtils.isNetWorkActive(getApp())) {
                    when (it) {
                        is SocketTimeoutException -> ttsSpeak("网络连接超时")
                        is ConnectException -> ttsSpeak("网络连接失败")
                        else -> ttsSpeak("系统异常,请重试")
                    }
                } else {
                    ttsSpeak("网络已断开，请检查网络。")
                }
                //金额模式
                EventBus.getDefault().post(MessageEventBean(MessageEventType.AmountCancel))
            })
        } else {
            ttsSpeak(getString(R.string.result_network_unavailable_error))
            //金额模式
            EventBus.getDefault().post(MessageEventBean(MessageEventType.AmountCancel))
        }


    }

    fun sendPhoneMsg(params: Map<String, Any>) {
        var result: Result<SendPhoneMsgBean>? = null;
        if (SystemUtils.isNetWorkActive(getApp())) {
            launch(false, block = {
                 result = apiService.sendPhoneMsg(params)
                sendPhoneMsg.value = result
                if (result?.code != 10000){
                    EventBus.getDefault()
                        .post(
                            MessageEventBean(
                                MessageEventType.DismissLoadingDialog
                            )
                        )
                }
//                if (result?.message!!.contains("验证码已发送")){
//                    ttsSpeak("验证码已发送")
//                    return@launch
//                }
            }, error = {
                Timber.w(it)
                EventBus.getDefault()
                    .post(
                        MessageEventBean(
                            MessageEventType.DismissLoadingDialog
                        )
                    )
                if (SystemUtils.isNetWorkActive(getApp())) {
                    if (result?.message!!.contains("验证码已发送")){
                        return@launch
                    }
                    when (it) {
                        is SocketTimeoutException -> ttsSpeak("网络连接超时")
                        is ConnectException -> ttsSpeak("网络连接失败")
                        else -> ttsSpeak("网络已断开,请重试")
                    }
                } else {
                    ttsSpeak("网络已断开，请检查网络。")
                }
            })
        } else {
            ttsSpeak(getString(R.string.result_network_unavailable_error))
        }


    }

    fun restaurantManager(params: Map<String, Any>) {
        if (SystemUtils.isNetWorkActive(getApp())) {
            launch(false, block = {
                val result = apiService.restaurantManager(params)
                restaurantManager.value = result

            }, error = {
                Timber.w(it)
                if (SystemUtils.isNetWorkActive(getApp())) {
                    when (it) {
                        is SocketTimeoutException -> ttsSpeak("网络连接超时")
                        is ConnectException -> ttsSpeak("网络连接失败")
                        else -> ttsSpeak("网络已断开,请重试")
                    }
                } else {
                    ttsSpeak("网络已断开，请检查网络。")
                }
            })
        } else {
            ttsSpeak(getString(R.string.result_network_unavailable_error))
        }

    }


    fun checkPhoneCode(params: Map<String, Any>) {
        if (SystemUtils.isNetWorkActive(getApp())) {
            launch(false, block = {
                val result = apiService.checkPhoneCode(params)
                checkPhoneCode.value = result

            }, error = {
                Timber.w(it)
                if (SystemUtils.isNetWorkActive(getApp())) {
                    EventBus.getDefault()
                        .post(
                            MessageEventBean(
                                MessageEventType.DismissLoadingDialog,
                                "提交失败",
                                "FAIL"
                            )
                        )
                    when (it) {
                        is SocketTimeoutException -> ttsSpeak("网络连接超时")
                        is ConnectException -> ttsSpeak("网络连接失败")
                        else -> ttsSpeak("网络已断开,请重试")
                    }
                } else {
                    ttsSpeak("网络已断开，请检查网络。")
                }
            })
        } else {
            ttsSpeak(getString(R.string.result_network_unavailable_error))
        }

    }

    /**
     * 设备查询消费记录接口
     */
    fun consumeRecordList(params: Map<String, Any>) {
        launch(false) {
            // TODO Http请求
            val result = apiService.consumeRecordList(params)
            // TODO 只需处理成功的场景，失败的场景都已统一处理
            if (isSuccess(result)) {
                consumeRecord.value = result.data
            }
        }
    }

    /**
     * 设备查询消费记录接口
     */
    fun consumeRefundList(params: Map<String, Any>) {
        launch(false, block =  {
            // TODO Http请求
            val result = apiService.consumeRefundList(params)
            // TODO 只需处理成功的场景，失败的场景都已统一处理
            consumeRefundList.value = result
            Log.d(TAG, "limeconsumeRefundList 287: " + JSON.toJSONString(result))
        }, error = {
            Log.e(TAG, "limeconsumeRefundList 289: ")
            Timber.w(it)
            if (SystemUtils.isNetWorkActive(getApp())) {
                when (it) {
                    is SocketTimeoutException -> ttsSpeak("网络连接超时")
                    is ConnectException -> ttsSpeak("网络连接失败")
                    else -> ttsSpeak("系统异常,请重试")
                }
            } else {
                ttsSpeak("网络已断开，请检查网络。")
            }
            EventBus.getDefault()
                .post(MessageEventBean(MessageEventType.AmountRefund))
        })
    }

    /**
     * 订单退款
     */
    fun consumeRefundResult(params: Map<String, Any>) {

        launch(false, block =  {
            // TODO Http请求
            val result = apiService.downFaceSyn(params)
            // TODO 只需处理成功的场景，失败的场景都已统一处理
            LogUtils.e("订单退款"+Gson().toJson(result))
            consumeRefundResult.value = result
            Log.d(TAG, "limeconsumeRefundList 316: " + JSON.toJSONString(result))
        }, error = {
            Log.e(TAG, "limeconsumeRefundList 318: ")
            Timber.w(it)
            if (SystemUtils.isNetWorkActive(getApp())) {
                when (it) {
                    is SocketTimeoutException -> ttsSpeak("网络连接超时")
                    is ConnectException -> ttsSpeak("网络连接失败")
                    else -> ttsSpeak("系统异常,请重试")
                }
            } else {
                ttsSpeak("网络已断开，请检查网络。")
            }
            EventBus.getDefault()
                .post(MessageEventBean(MessageEventType.AmountRefund))
        })
    }

    /**
     * 设备查询订餐信息接口
     */
    fun takeMealsList(params: Map<String, Any>) {
        launch(false) {
            // TODO Http请求
            val result = apiService.takeMealsList(params)
            // TODO 只需处理成功的场景，失败的场景都已统一处理
//            if(isSuccess(result)){
            takeMealsList.value = result
//            }
        }
    }

    /**
     * 设备订单取餐接口
     */
    fun takeMeals(params: Map<String, Any>) {
        launch(false) {
            // TODO Http请求
            val result = apiService.takeMeals(params)
            // TODO 只需处理成功的场景，失败的场景都已统一处理
            //  if(isSuccess(result)){
            takeMeals.value = result
            //  }
        }
    }

    /**
     * 2.设备订单取餐接口
     */
    fun takeMeal(params: Map<String, Any>) {
        launch(false) {
            // TODO Http请求
            val result = apiService.takeMeals(params)
            // TODO 只需处理成功的场景，失败的场景都已统一处理
            LogUtils.e("默认出餐" + Gson().toJson(result))
            //  if(isSuccess(result)){
            //takeMeals.value = result
            //  }
        }
    }

    /**
     * 当前时段信息查询接口
     */
    fun currentTimeInfo(params: Map<String, Any>) {
        launch(false) {
            // TODO Http请求
            val result = apiService.currentTimeInfo(params)
            // TODO 只需处理成功的场景，失败的场景都已统一处理
//            if(isSuccess(result)){
            currentTimeInfo.value = result
//            }
        }
    }

    /**
     * 订单支付中,状态查询
     */
    fun payStatus(params: Map<String, Any>) {
        launch(false) {
            // TODO Http请求
            Log.i(TAG,"limecardparams 241: " + GsonUtils.toJson(params))
            val result = apiService.modifyBalance(params)
            // TODO 只需处理成功的场景，失败的场景都已统一处理
            LogUtils.e("支付中" + Gson().toJson(result))
            modifyBalance.value = result
        }
    }

    fun ttsSpeak(value: String) {
        try{
        App.TTS.setSpeechRate(1f)
        App.TTS.speak(
            value,
            TextToSpeech.QUEUE_FLUSH, null
        )
        }catch (e:Exception){

        }
    }
}