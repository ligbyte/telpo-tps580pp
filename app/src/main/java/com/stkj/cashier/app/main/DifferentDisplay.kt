package com.stkj.cashier.app.main

import android.annotation.SuppressLint
import android.app.Presentation
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Display
import android.view.KeyEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.stkj.cashier.App
import com.stkj.cashier.R
import com.stkj.cashier.app.adapter.ConsumeRefundListAdapter
import com.stkj.cashier.app.base.helper.CommonTipsHelper
import com.stkj.cashier.app.base.helper.SystemEventHelper
import com.stkj.cashier.app.main.callback.ConsumerController
import com.stkj.cashier.app.main.callback.ConsumerListener
import com.stkj.cashier.app.main.helper.CBGCameraHelper
import com.stkj.cashier.app.weigh.commontips.CommonTipsView
import com.stkj.cashier.bean.FaceChooseItemEntity
import com.stkj.cashier.bean.MessageEventBean
import com.stkj.cashier.bean.db.CompanyMemberdbEntity
import com.stkj.cashier.cbgfacepass.CBGFacePassHandlerHelper
import com.stkj.cashier.cbgfacepass.FacePassHelper
import com.stkj.cashier.cbgfacepass.model.CBGFacePassRecognizeResult
import com.stkj.cashier.cbgfacepass.model.FacePassPeopleInfo
import com.stkj.cashier.config.MessageEventType
import com.stkj.cashier.constants.Constants
import com.stkj.cashier.greendao.biz.CompanyMemberBiz
import com.stkj.cashier.ui.widget.FacePassCameraLayout
import com.stkj.cashier.utils.SettingVar
import com.stkj.cashier.utils.camera.CameraManager
import com.stkj.cashier.utils.camera.CameraPreviewData
import com.stkj.cashier.utils.camera.RecognizeData
import com.stkj.cashier.utils.rxjava.DefaultDisposeObserver
import com.stkj.cashier.utils.util.LogUtils
import com.stkj.cashier.utils.util.SPUtils
import com.stkj.cashier.utils.util.SpanUtils
import com.stkj.cashier.utils.util.ThreadUtils.runOnUiThread
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.observers.DisposableObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import com.stkj.cashier.bean.ConsumeRefundListBean as ConsumeRefundListBean1

class DifferentDisplay : Presentation, CameraManager.CameraListener, View.OnClickListener ,
    ConsumerController {

    val TAG = "DifferentDisplay"
    private var companyMember: CompanyMemberdbEntity? = null
    private lateinit var tvTime: TextView
    private lateinit var tvCompanyName: TextView
    private lateinit var tvFaceTips: TextView
    private lateinit var tvFaceTips2: TextView
    private lateinit var ivWifiState: ImageView
    private lateinit var pbBattery:ProgressBar
    private lateinit var ivBatteryBg:ImageView
//    private lateinit var tvWifiState: TextView

    private lateinit var tvRefundName: TextView
    private lateinit var tvPaySuccess: TextView
    private lateinit var tvNumber: TextView
    private lateinit var ivHeader2: ImageView
    private lateinit var rvRefund: RecyclerView
    private lateinit var tvRefundName2: TextView
    private lateinit var tvWindow: TextView

    private lateinit var llRefundList: LinearLayout
    private lateinit var llDefault: LinearLayout
    private lateinit var rlRefundConfirm: RelativeLayout

    private lateinit var flCameraAvatar:FrameLayout
    private lateinit var llBalance:LinearLayout
    private lateinit var tvBalance:TextView
    private lateinit var tvPayMoney:TextView

    //是否正在人脸身份校验
    private var isRunningFacePassAuth = false
    private lateinit var llPayError:LinearLayout
    private lateinit var tvPayError:TextView
    private lateinit var outerContext: Context;
    private var isConsumerAuthTips = false
    private var layoutManager: LinearLayoutManager? = null
    private var mAdapter: ConsumeRefundListAdapter? = null
    private var fpcFace: FacePassCameraLayout? = null
    private var consumerListener: ConsumerListener? = null
    private var beforeFaceToken = "";
    private var beforeTime:Long = 0L;
    private var recognizeCount:Int = 0
    private var cbgCameraHelper: CBGCameraHelper? = null;
    var faceLists: MutableList<CBGFacePassRecognizeResult?>? = mutableListOf()
    var faceTokens: List<CBGFacePassRecognizeResult?>? = arrayListOf()

    var facePassHelper: FacePassHelper? = null

//    private lateinit var cameraPreview: CameraPreview
//    private var ivCameraOverLayer:ImageView? = null
//    private var ivSuccessHeader:ImageView? = null

    /* 相机实例 */
//    private var cameraManager: CameraManager? = null

    private val cameraRotation = SettingVar.cameraPreviewRotation

    private var realAmount = "0.0"

    companion object {
        var isStartFaceScan:AtomicBoolean = AtomicBoolean(false)

    }

    var mRecognizeDataQueue: ArrayBlockingQueue<RecognizeData> = ArrayBlockingQueue(5)
    var mFeedFrameQueue: ArrayBlockingQueue<CameraPreviewData> = ArrayBlockingQueue(1)


    constructor(outerContext: Context, display: Display) : super(outerContext, display){
        this.outerContext = outerContext
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("HardwareIds", "SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        try{
            if (facePassHelper == null) {
                try{

                    facePassHelper = FacePassHelper(outerContext as MainActivity)

                } catch (e:Exception ) {
                    Log.e(TAG, "limestartRecognizeFrameTask facePassHelper 1178: " + e.message);
                }
            }
        LogUtils.e("副屏onCreate")
        setCancelable(false)
        setContentView(R.layout.layout_different_display_2)
            fpcFace = findViewById(R.id.fpc_face) as FacePassCameraLayout


            consumerListener?.onCreateFacePreviewView(
                fpcFace!!.facePreviewFace,
                fpcFace!!.irPreviewFace
            )

//        ivCameraOverLayer = findViewById(R.id.iv_camera_over_layer);
//        ivSuccessHeader = findViewById(R.id.ivSuccessHeader);
//        cameraPreview = findViewById(R.id.cameraPreview)
//        cameraPreview.setAutoFitSurfaceListener { width, height ->
//            if (height > 0 && width > height) {
//                val offset = -(width - height) / 2
//                cameraPreview.translationX = offset.toFloat()
//                LogUtils.e("setAutoFitSurfaceListener---offset: $offset")
//            }
//        }
//        cameraPreview.setAspectRatio(600, 600)
        tvTime = findViewById<TextView>(R.id.tvTime)
        tvCompanyName = findViewById<TextView>(R.id.tvCompanyName)
        tvFaceTips = findViewById<TextView>(R.id.tvFaceTips)
        tvFaceTips2 = findViewById<TextView>(R.id.tvFaceTips2)
        ivWifiState = findViewById<ImageView>(R.id.ivWifiState)
        pbBattery = findViewById<ProgressBar>(R.id.pb_battery)
        ivBatteryBg = findViewById<ImageView>(R.id.iv_battery_bg)
//        tvWifiState = findViewById<TextView>(R.id.tvWifiState)

        llRefundList = findViewById<LinearLayout>(R.id.llRefundList)
        llDefault = findViewById<LinearLayout>(R.id.llDefault)
        rlRefundConfirm = findViewById<RelativeLayout>(R.id.rlRefundConfirm)

        flCameraAvatar = findViewById(R.id.fl_camera_avatar);
        llBalance = findViewById(R.id.ll_balance);
        tvBalance = findViewById(R.id.tvBalance);
        tvPayMoney = findViewById(R.id.tv_pay_money);

        llPayError = findViewById(R.id.ll_pay_error);
        tvPayError = findViewById(R.id.tv_pay_error);

        tvNumber = findViewById<TextView>(R.id.tvNumber)
            tvPaySuccess = findViewById<TextView>(R.id.tvPaySuccess)
        tvWindow = findViewById<TextView>(R.id.tvWindow)
        tvRefundName = findViewById<TextView>(R.id.tvRefundName)
        tvRefundName2 = findViewById<TextView>(R.id.tvRefundName2)
        ivHeader2 = findViewById<ImageView>(R.id.ivHeader2)
        rvRefund = findViewById<RecyclerView>(R.id.rvRefund)


            tvWindow.postDelayed({
                if (tvWindow.text.equals("第--窗口")){
                    EventBus.getDefault().post(MessageEventBean(MessageEventType.RquestAgain))
                }
            },30 * 1000)

            tvWindow.postDelayed({
                if (tvWindow.text.equals("第--窗口")){
                    EventBus.getDefault().post(MessageEventBean(MessageEventType.RquestAgain))
                }
            },60 * 1000)

            tvWindow.postDelayed({
                if (tvWindow.text.equals("第--窗口")){
                    EventBus.getDefault().post(MessageEventBean(MessageEventType.RquestAgain))
                }
            },2 * 60 * 1000)

            tvWindow.postDelayed({
                if (tvWindow.text.equals("第--窗口")){
                    EventBus.getDefault().post(MessageEventBean(MessageEventType.RquestAgain))
                }
            },3 * 60 * 1000)

            tvWindow.postDelayed({
                if (tvWindow.text.equals("第--窗口")){
                    EventBus.getDefault().post(MessageEventBean(MessageEventType.RquestAgain))
                }
            },5 * 60 * 1000)

        val ctvConsumer = findViewById<CommonTipsView>(R.id.ctv_consumer);
        CommonTipsHelper.INSTANCE.setConsumerTipsView(ctvConsumer)

        val switchFacePassPay = SPUtils.getInstance().getBoolean(Constants.SWITCH_FACE_PASS_PAY, false)

        if (switchFacePassPay) {
//            goFacePassAuth()
//            openAndInitCamera()
//            startFacePassDetect()
//            ivCameraOverLayer?.visibility = View.GONE
        } else {
            stopFacePassAuth()
//            closeAndReleaseCamera()
//            stopFacePassDetect()
//            ivCameraOverLayer?.visibility = View.VISIBLE
        }

        //系统事件监听
        SystemEventHelper.INSTANCE.addSystemEventListener(systemEventListener)
        //event事件
        EventBus.getDefault().register(this)

    } catch (e: Throwable) {
            Log.e(TAG,"limecompany onCreate error:   " + e.message)
    }
    }





    /**
     * 清理人脸识别队列缓存
     */
    private fun clearFacePassQueueCache() {
        mFeedFrameQueue.clear()
        mRecognizeDataQueue.clear();
    }

    override fun show() {
        try {
            super.show()
        } catch (e: Throwable) {
            Log.e(TAG, "limeException 267: " + e.message)
            Log.d(TAG,"limescreen 259 副屏初始化失败" + e.message)
        }
    }

    override fun getResources(): Resources? {
        Log.d(TAG,"limescreen 265 副屏getResources")
        val res = super.getResources()
        LogUtils.e("副屏getResources" + res.configuration.fontScale)
        //非默认值
        val newConfig = Configuration()
        newConfig.setToDefaults() //设置默认
        res.updateConfiguration(newConfig, res.displayMetrics)
        Log.d(TAG,"limescreen 271 副屏getResources-res")
        return res
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG,"limescreen 277 副屏初始化onStart")
    }

    override fun onDisplayRemoved() {
        super.onDisplayRemoved()
        if (consumerListener != null) {
            consumerListener!!.onConsumerDismiss()
        }
        Log.d(TAG,"limescreen 277 副屏初始化onDisplayRemoved")
    }

    override fun onDisplayChanged() {
        super.onDisplayChanged()
        if (consumerListener != null) {
            consumerListener!!.onConsumerChanged()
        }
        Log.d(TAG,"limescreen 277 副屏初始化onDisplayChanged")
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (App.mMainActivity!=null){
            return App.mMainActivity!!.dispatchKeyEvent(event)
        }
        return super.dispatchKeyEvent(event)
    }

    inner class FeedFrameThread : Thread() {
        var isInterrupt = false
        override fun run() {

            while (!isInterrupt) {

                /* 将每一帧FacePassImage 送入SDK算法， 并得到返回结果 */
//                var detectionResult: FacePassTrackResult? = null
//                try {
//                    if (App.cameraType == FacePassCameraType.FACEPASS_DUALCAM) {
//                        var framePair: Pair<CameraPreviewData, CameraPreviewData> = try {
//                            ComplexFrameHelper.takeComplexFrame()
//                        } catch (e: InterruptedException) {
//                            e.printStackTrace()
//                            continue
//                        }
//                        val imageRGB = FacePassImage(
//                            framePair.first.nv21Data,
//                            framePair.first.width,
//                            framePair.first.height,
//                            cameraRotation,
//                            FacePassImageType.NV21
//                        )
//                        val imageIR = FacePassImage(
//                            framePair.second.nv21Data,
//                            framePair.second.width,
//                            framePair.second.height,
//                            cameraRotation,
//                            FacePassImageType.NV21
//                        )
//                        detectionResult = App.mFacePassHandler?.feedFrameRGBIR(imageRGB, imageIR)
//                    } else {
//                        var cameraPreviewData: CameraPreviewData? = null
//                        try {
//                            cameraPreviewData = mFeedFrameQueue.take()
//                            val imageRGB = FacePassImage(
//                                cameraPreviewData.nv21Data,
//                                cameraPreviewData.width,
//                                cameraPreviewData.height,
//                                cameraRotation,
//                                FacePassImageType.NV21
//                            )
//                            detectionResult = App.mFacePassHandler?.feedFrame(imageRGB)
//                            //LogUtils.e("识别到人脸1 "+detectionResult?.message)
//                        } catch (e: Exception) {
//                            e.printStackTrace()
//                            // LogUtils.e("识别到人脸2 "+e.message)
//                            continue
//                        }
//
//                    }
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                    LogUtils.d("识别到人脸3 " + e.message)
//                }
//
//                /*离线模式，将识别到人脸的，message不为空的result添加到处理队列中*/
//                if (detectionResult != null && detectionResult.message.isNotEmpty()) {
//                    LogUtils.d("识别到人脸" + detectionResult.images.size)
//                    /*所有检测到的人脸框的属性信息*/
//
//                    /*送识别的人脸框的属性信息*/
//                    val trackOpts = arrayOfNulls<FacePassTrackOptions>(detectionResult.images.size)
//                    for (i in detectionResult.images.indices) {
//                        if (detectionResult.images[i].rcAttr.respiratorType != FacePassRCAttribute.FacePassRespiratorType.NO_RESPIRATOR) {
//                            val searchThreshold = 75f
//                            val livenessThreshold = 80f // -1.0f will not change the liveness threshold
//                            trackOpts[i] = FacePassTrackOptions(
//                                detectionResult.images[i].trackId,
//                                searchThreshold,
//                                livenessThreshold
//                            )
//                        } else {
//                            trackOpts[i] =
//                                FacePassTrackOptions(detectionResult.images[i].trackId, -1f, -1f)
//                        }
//                    }
//                    val mRecData = RecognizeData(detectionResult.message, trackOpts)
//                    mRecognizeDataQueue.offer(mRecData)
//                }

            }
        }

        override fun interrupt() {
            isInterrupt = true
            super.interrupt()
        }
    }



    private var canSpeakFacePassFail = true
    private var canSpeakFacePassFailObserver: DisposableObserver<Long>? = null

    /**
     * 重置人脸识别自动重试
     */
    private fun resetFacePassRetryDelay(){
        canSpeakFacePassFail = true
        canSpeakFacePassFailObserver?.dispose()
        canSpeakFacePassFailObserver = null
    }

    /**
     * 处理人脸识别失败自动重试
     */
    private fun handleFacePassFailRetryDelay() {
        if (canSpeakFacePassFail) {
            canSpeakFacePassFail = false
            canSpeakFacePassFailObserver = object : DefaultDisposeObserver<Long>() {
                override fun onSuccess(t: Long) {
                    canSpeakFacePassFail = true
                    canSpeakFacePassFailObserver = null
                }
            }
            //3秒之后重置识别失败语音提醒
            Observable.timer(3, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(canSpeakFacePassFailObserver)
            tvFaceTips2.visibility = View.VISIBLE
            tvFaceTips2.text = "识别失败，正在重试"
        }
    }

    override fun onPictureTaken(cameraPreviewData: CameraPreviewData?) {
//        if (mFeedFrameThread != null && !mFeedFrameThread!!.isInterrupt) {
//            if (App.cameraType == FacePassCameraType.FACEPASS_DUALCAM) {
//                ComplexFrameHelper.addRgbFrame(cameraPreviewData)
//            } else {
//                mFeedFrameQueue.offer(cameraPreviewData)
//            }
//        }
    }

    private fun showSuccessFace(imageUrl:String){
//        if (ivSuccessHeader != null) {
//            ivSuccessHeader!!.visibility = View.VISIBLE
//            GlideApp.with(App.applicationContext)
//                .load(imageUrl)
//                .placeholder(R.mipmap.icon_camera_over_layer)
//                .into(ivSuccessHeader!!)
//        }
    }


    private fun hideBalance() {
        flCameraAvatar.visibility = View.VISIBLE
        tvFaceTips.visibility = View.VISIBLE
        llBalance.visibility = View.GONE
    }

    private fun showBalance(payMoney: String, balance: String) {
        flCameraAvatar.visibility = View.GONE
        tvFaceTips2.visibility = View.GONE
        tvFaceTips.visibility = View.GONE
        llBalance.visibility = View.VISIBLE
        tvPayMoney.text = "¥ $payMoney"
        tvPaySuccess.text = "支付成功"
        SpanUtils.with(tvBalance)
            .append("账户余额：")
            .append("¥ $balance")
            .setFontSize(50, true)
            .create()
    }

    private fun showQueryBalance(payMoney: String, balance: String,fullName :String) {
        flCameraAvatar.visibility = View.GONE
        tvFaceTips2.visibility = View.GONE
        tvFaceTips.visibility = View.GONE
        llBalance.visibility = View.VISIBLE
        tvPayMoney.text = fullName
        tvPaySuccess.text = "查询成功"
        SpanUtils.with(tvBalance)
            .append("账户余额：")
            .append("¥ $balance")
            .setFontSize(50, true)
            .create()
    }

    private fun hidePayError() {
        flCameraAvatar.visibility = View.VISIBLE
        tvFaceTips.visibility = View.VISIBLE
        llPayError.visibility = View.GONE
    }

    @SuppressLint("CheckResult")
    private fun showPayError(errorMsg: String) {
        flCameraAvatar.visibility = View.GONE
        tvFaceTips2.visibility = View.GONE
        tvFaceTips.visibility = View.GONE
        llPayError.visibility = View.VISIBLE;
        if (errorMsg.contains("余额不足")) {
            val split = errorMsg.split("|")
            if (split.size > 1) {
                SpanUtils.with(tvPayError)
                    .append("账户余额不足（可用 ¥ ")
                    .append(split[1])
                    .setForegroundColor(Color.parseColor("#FA5151"))
                    .append("）请充值")
                    .create()
            } else {
                tvPayError.text = errorMsg
            }
        } else {
            tvPayError.text = errorMsg
        }


        Observable.timer(1500, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io()) // 在IO调度器上订阅
            .observeOn(AndroidSchedulers.mainThread()) // 在主线程上观察
            .subscribe(
                { aLong: Long? ->
                    // 这里的代码会在3秒后执行一次
                    hidePayError()
                    hideBalance()
                    tvFaceTips2.visibility = View.GONE

                    tvFaceTips.visibility = View.VISIBLE
                    tvFaceTips.text = "欢迎就餐"
                    recognizeCount = 0;
                    setFacePreview(false)
                    Log.d(TAG, "limesetPreviewFace   欢迎就餐 false: " + 525)
                }
            ) { throwable: Throwable? ->
                // 当发生错误时，这里的代码会被执行
                Log.e("RxJava", "Error", throwable)
            }

    }

    //接收事件
    @SuppressLint("CheckResult")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true, priority = 1)
    open fun onEventReceiveMsg(message: MessageEventBean) {
        try{
            Log.d(TAG,"limecompany onEventReceiveMsg message.type " + message.type  + "  message.ext " + message.ext)
        when (message.type) {
            MessageEventType.OpenFacePassPay -> {
//                openAndInitCamera()
//                //重新识别
//                App.mFacePassHandler?.reset()
//                startFacePassDetect()
//                ivCameraOverLayer?.visibility = View.GONE
                goFacePassAuth()
            }
            MessageEventType.CloseFacePassPay -> {
//                stopFacePassDetect()
//                ivCameraOverLayer?.visibility  = View.VISIBLE
                stopFacePassAuth()
//                closeAndReleaseCamera()
            }
            MessageEventType.AmountNotice -> {
                hidePayError()
                hideBalance()
                //重新识别
                App.mFacePassHandler?.reset()

                //清除人脸缓存
                clearFacePassQueueCache()
                isStartFaceScan.set(true)
                message.content?.let {
                    realAmount = it
                    //tvAmount.visibility = View.VISIBLE
                    //tvAmount.text = "¥ $realAmount"

                    tvFaceTips.visibility = View.VISIBLE
                    tvFaceTips.text = "金额：$realAmount" + "元"
                    tvFaceTips2.visibility = View.VISIBLE
                    tvFaceTips2.text = "请支付"
                    if (SPUtils.getInstance().getBoolean(Constants.SWITCH_FACE_PASS_PAY, false)){
                        setFacePreview(true)
                        Log.d(TAG, "limesetPreviewFace   请支付 true: " + 525)
                    }
                }
                val switchFacePassPay = SPUtils.getInstance().getBoolean(Constants.SWITCH_FACE_PASS_PAY, false)
                if (switchFacePassPay) {
                    goFacePassAuth()
                } else {
                    stopFacePassAuth()
                }
                Log.d(TAG, "limeonEventReceiveMsg: " + 541)
                LogUtils.e("DiffAmountNotice取消操作" + isStartFaceScan)
            }
            MessageEventType.AmountNotice2 -> {
                hidePayError()
                hideBalance()
                realAmount = "0.0"
                isStartFaceScan.set(false)
                LogUtils.e("DiffAmountNotice2取消操作" + isStartFaceScan)

                tvFaceTips.visibility = View.VISIBLE
                tvFaceTips.text = "欢迎就餐"
                recognizeCount = 0;
                setFacePreview(false)
                Log.d(TAG, "limesetPreviewFace   欢迎就餐 false : " + 597)
                tvFaceTips2.visibility = View.GONE
                Log.d(TAG, "limeonEventReceiveMsg: " + 556)
            }
            MessageEventType.AmountNotice3 -> {
                hidePayError()
                hideBalance()
                //重新识别
                App.mFacePassHandler?.reset()

                //清除人脸缓存
                clearFacePassQueueCache()
                isStartFaceScan.set(true)
                message.content?.let {
                    realAmount = it
                    //tvAmount.visibility = View.VISIBLE
                    //tvAmount.text = "¥ $realAmount"

                    tvFaceTips.visibility = View.VISIBLE
                    tvFaceTips.text = realAmount
                    tvFaceTips2.visibility = View.VISIBLE
                    tvFaceTips2.text = "请支付"
                    if (SPUtils.getInstance().getBoolean(Constants.SWITCH_FACE_PASS_PAY, false)){
                        setFacePreview(true)
                        Log.d(TAG, "limesetPreviewFace   请支付 true: " + 621)
                    }
                }
                val switchFacePassPay = SPUtils.getInstance().getBoolean(Constants.SWITCH_FACE_PASS_PAY, false)
                if (switchFacePassPay) {
                    goFacePassAuth()
                } else {
                    stopFacePassAuth()
                }
                setFacePreview(true)
                Log.d(TAG, "limesetPreviewFace   true : " + 631)
                LogUtils.e("DiffAmountNotice3取消操作" + isStartFaceScan)
                Log.d(TAG, "limeonEventReceiveMsg: " + 579)
            }
            MessageEventType.AmountPayingNotice -> {
                tvFaceTips2.visibility = View.VISIBLE
                tvFaceTips2.text = "支付中,请稍等"
            }
            MessageEventType.AmountSuccess -> {
                message.content?.let {
                    companyMember = CompanyMemberBiz.getCompanyMemberByCard(message.content)
                }

                if (message.realPayMoney != null) {
                    message.ext?.let { showBalance(message.realPayMoney!!, it) }
                } else {
                    message.ext?.let { showBalance(realAmount, it) }
                }

                realAmount = "0.0"

                Observable.timer(1500, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io()) // 在IO调度器上订阅
                    .observeOn(AndroidSchedulers.mainThread()) // 在主线程上观察
                    .subscribe(
                        { aLong: Long? ->
                            // 这里的代码会在3秒后执行一次
                            hidePayError()
                            hideBalance()
                            tvFaceTips2.visibility = View.GONE

                            tvFaceTips.visibility = View.VISIBLE
                            tvFaceTips.text = "欢迎就餐"
                            recognizeCount = 0;
                            setFacePreview(false)

                            Log.d(TAG, "limesetPreviewFace   欢迎就餐 false: " + 664)
                        }
                    ) { throwable: Throwable? ->
                        // 当发生错误时，这里的代码会被执行
                        Log.e("RxJava", "Error", throwable)
                    }

            }

            MessageEventType.AmountQuerySuccess -> {
                message.content?.let {
                    companyMember = CompanyMemberBiz.getCompanyMemberByCard(message.content)
                }

                if (message.realPayMoney != null) {
                    message.ext?.let { showQueryBalance(message.realPayMoney!!, it ,message.obj.toString()) }
                } else {
                    message.ext?.let { showQueryBalance(realAmount, it ,message.obj.toString()) }
                }

                realAmount = "0.0"

                Observable.timer(1500, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io()) // 在IO调度器上订阅
                    .observeOn(AndroidSchedulers.mainThread()) // 在主线程上观察
                    .subscribe(
                        { aLong: Long? ->
                            // 这里的代码会在3秒后执行一次
                            hidePayError()
                            hideBalance()
                            tvFaceTips2.visibility = View.GONE
                            tvFaceTips.visibility = View.VISIBLE
                            tvFaceTips.text = "欢迎就餐"
                            recognizeCount = 0;
                            setFacePreview(false)
                            Log.d(TAG, "limesetPreviewFace   欢迎就餐 false: " + 698)
                        }
                    ) { throwable: Throwable? ->
                        // 当发生错误时，这里的代码会被执行
                        Log.e("RxJava", "Error", throwable)
                    }

            }

            MessageEventType.AmountError -> {
                message.content?.let {
                    showPayError(it)
                }
            }

            MessageEventType.CompanyName -> {
                message.content?.let {
//                    tvCompanyName.text = it
                    tvWindow.text = message.ext
                }
            }

            MessageEventType.AmountCard -> {
                //根据卡号刷新人脸
                message.content?.let {
                    companyMember = CompanyMemberBiz.getCompanyMemberByCard(it)
                    if (companyMember != null) {
                       showSuccessFace(companyMember!!.imgData)
                    }
                }
            }

            MessageEventType.AmountRefund -> {
                //点击退单按钮按键
                val switchFacePassPay =
                    SPUtils.getInstance().getBoolean(Constants.SWITCH_FACE_PASS_PAY)
                if (switchFacePassPay) {
                    tvFaceTips.visibility = View.VISIBLE
                    tvFaceTips.text = "请刷脸或刷卡"
                    goFacePassAuth()
                } else {
                    tvFaceTips.text = "请刷卡或扫码"
                }
                tvFaceTips2.visibility = View.GONE
                //清除人脸缓存
                clearFacePassQueueCache()
                isStartFaceScan.set(true)
            }
            MessageEventType.AmountRefundCancel -> {
                try {
                    val switchFacePassPay =
                        SPUtils.getInstance().getBoolean(Constants.SWITCH_FACE_PASS_PAY)
                    if (switchFacePassPay) {
                        stopFacePassAuth()
                    }
                }catch (e: Exception){

                }
                //点击退单按钮按键
                tvFaceTips.text = "欢迎就餐"
                recognizeCount = 0;
                setFacePreview(false)
                Log.d(TAG, "limesetPreviewFace   欢迎就餐 false: " + 750)
                tvFaceTips2.visibility = View.GONE
                llDefault.visibility = View.VISIBLE
                llRefundList.visibility = View.GONE
                rlRefundConfirm.visibility = View.GONE
                isStartFaceScan.set(false)
                hidePayError()
                hideBalance()
            }
            MessageEventType.AmountRefundList -> {
                //退款列表
                message.content?.let {
                    val fromJson = Gson().fromJson(
                        it,
                        ConsumeRefundListBean1::class.java
                    )
                    if (fromJson.results != null && fromJson.results!!.size > 0) {
                        llRefundList.visibility = View.VISIBLE
                        rlRefundConfirm.visibility = View.GONE
                        llDefault.visibility = View.GONE
                        tvRefundName.text = "姓名：" + fromJson.customerName
                        tvNumber.text = "账号/卡号：" + fromJson.customerNo

                        Glide.with(App.applicationContext).load(fromJson.customerImg)
                            .placeholder(R.mipmap.icon_camerapreview_person_3) // 设置占位图
                            .into(ivHeader2)
                        layoutManager = LinearLayoutManager(App.applicationContext);//添加布局管理器
                        rvRefund.layoutManager = layoutManager//设置布局管理器

                        mAdapter = ConsumeRefundListAdapter(fromJson.results)
                        rvRefund.adapter = mAdapter
                        mAdapter?.setList(fromJson.results)
                        mAdapter?.setSelectedPosition(0)
                    }

                }

            }
            MessageEventType.AmountRefundSuccess -> {
                LogUtils.e("退款成功")
                //退款成功
                message.content?.let {
                    LogUtils.e("退款成功" + it)
                    tvRefundName2.text = it + ""
                    rlRefundConfirm.visibility = View.VISIBLE
                }
            }
            MessageEventType.AmountRefundListSelect -> {
                //退款成功
                message.obj?.let {

                    if (mAdapter != null && layoutManager != null) {
                        mAdapter?.setSelectedPosition(it as Int)
                        layoutManager?.scrollToPosition(it as Int)
                    }
                }
            }

            MessageEventType.FaceChooseListSelect -> {
                //选择人脸
                message.content?.let {

                    faceLists?.clear()

                    if (faceTokens != null) {

                        for (faceToken in faceTokens!!) {
                             if (faceToken!!.faceToken == it){
                                 faceLists?.add(faceToken)
                                 Log.d(TAG, "limeinitData 840 : "  + (faceToken.image.image == null))
                                 App.isNeedCache = true
                                 processFacePassResult(faceLists)
                             }
                        }
                    }

                }
            }

        }

    } catch (e: Throwable) {
        Log.e(TAG,"limecompany onEventReceiveMsg error:   " + e.message)
    }

    }

    override fun onStop() {
        super.onStop()
        LogUtils.e("副屏初始化onStop")
        resetFacePassRetryDelay()
//        stopFacePassDetect()
        CommonTipsHelper.INSTANCE.setConsumerTipsView(null)
        SystemEventHelper.INSTANCE.removeSystemEventListener(systemEventListener)
        //closeAndReleaseCamera()
        stopFacePassAuth()
        EventBus.getDefault().unregister(this)
    }

    fun ttsSpeak(value: String) {
        try{
        App.TTS.setSpeechRate(1f)
        App.TTS.speak(
            value,
            TextToSpeech.QUEUE_ADD, null
        )

    }catch (e:Exception){

    }
    }

    override fun onClick(v: View?) {
        TODO("Not yet implemented")
    }

    /**
     * 刷新系统时间
     */
    private fun refreshSystemDate(formatDateStr: String) {
        try {
            val split = formatDateStr.split(" ")
            if (split.size > 2) {
                tvTime.text = "${split[1]} ${split[2]}"
            }
        } catch (e: Throwable) {
            Log.e(TAG, "limeException 982: " + e.message)
        }
    }

    /**
     * 刷新网络连接状态
     */
    private fun refreshNetworkStatus(netType: Int, isConnected: Boolean) {
        if (netType == SystemEventHelper.ETHERNET_NET_TYPE) {
            if (isConnected) {
                ivWifiState.setImageResource(R.mipmap.icon_ethernet)
            } else {
                ivWifiState.setImageResource(R.mipmap.icon_ethernetno)
            }
        } else if (netType == SystemEventHelper.WIFI_NET_TYPE) {
            if (isConnected) {
                ivWifiState.setImageResource(R.mipmap.icon_wifi4)
            } else {
                ivWifiState.setImageResource(R.mipmap.icon_wifi0)
            }
        } else if (netType == SystemEventHelper.MOBILE_NET_TYPE) {
            if (isConnected) {
                ivWifiState.setImageResource(R.mipmap.icon_level_4)
            } else {
                ivWifiState.setImageResource(R.mipmap.icon_levelno)
            }
        } else {
            ivWifiState.setImageResource(0)
        }
    }

    /**
     * 刷新网络信号状态
     */
    private fun refreshNetworkRssi(netType: Int, isConnect: Boolean, level: Int) {
        if (netType == SystemEventHelper.WIFI_NET_TYPE) {
            if (isConnect) {
                if (level == 0) {
                    ivWifiState.setImageResource(R.mipmap.icon_wifi0)
                } else if (level == 1) {
                    ivWifiState.setImageResource(R.mipmap.icon_wifi1)
                } else if (level == 2) {
                    ivWifiState.setImageResource(R.mipmap.icon_wifi2)
                } else if (level == 3) {
                    ivWifiState.setImageResource(R.mipmap.icon_wifi3)
                } else if (level == 4) {
                    ivWifiState.setImageResource(R.mipmap.icon_wifi4)
                }
            } else {
                ivWifiState.setImageResource(R.mipmap.icon_wifi0)
            }
        } else if (netType == SystemEventHelper.MOBILE_NET_TYPE) {
            if (isConnect) {
                if (level == 0) {
                    ivWifiState.setImageResource(R.mipmap.icon_levelno)
                } else if (level == 1) {
                    ivWifiState.setImageResource(R.mipmap.icon_level_1)
                } else if (level == 2) {
                    ivWifiState.setImageResource(R.mipmap.icon_level_2)
                } else if (level == 3) {
                    ivWifiState.setImageResource(R.mipmap.icon_level_3)
                } else if (level == 4) {
                    ivWifiState.setImageResource(R.mipmap.icon_level_4)
                }
            } else {
                ivWifiState.setImageResource(R.mipmap.icon_levelno)
            }
        }
    }

    private var batteryDefaultPro: Drawable? = null
    private var batteryChargingPro: Drawable? = null

    /**
     * 刷新电池相关
     */
    private fun refreshBatteryStatus(batteryLevel: Float, isCharging: Boolean) {
        if (batteryChargingPro == null) {
            batteryDefaultPro = if (batteryLevel > 20) {
                resources?.getDrawable(R.drawable.battery_pro_bar_default)
            } else {
                resources?.getDrawable(R.drawable.battery_pro_bar_low)
            }
            batteryChargingPro =
                resources?.getDrawable(R.drawable.battery_pro_bar_charging)
        }
        if (batteryChargingPro != null) {
            if (isCharging) {
                ivBatteryBg.setImageResource(R.mipmap.icon_battery_ischarging)
                pbBattery.progressDrawable = batteryChargingPro
            } else {
                ivBatteryBg.setImageResource(R.mipmap.icon_battery_percent);
                pbBattery.progressDrawable = batteryDefaultPro
            }
            pbBattery.progress = batteryLevel.toInt()
        }
    }

    fun setConsumerListener(consumerListener: ConsumerListener?) {
        this.consumerListener = consumerListener
    }

    fun clearConsumerPresentation() {
        this.consumerListener = null
    }

    //系统事件监听
    private val systemEventListener: SystemEventHelper.OnSystemEventListener =
        object : SystemEventHelper.OnSystemEventListener {
            override fun onDateTick(formatDate: String) {
                refreshSystemDate(formatDate)
            }

            override fun onDateChange(formatDate: String) {
                refreshSystemDate(formatDate)
            }

            override fun onNetworkChanged(netType: Int, isConnect: Boolean) {
                refreshNetworkStatus(netType, isConnect)
            }

            override fun onNetworkRssiChange(netType: Int, isConnect: Boolean, level: Int) {
                refreshNetworkRssi(netType, isConnect, level)
            }

            override fun onBatteryChange(batteryPercent: Float, isChanging: Boolean) {
                refreshBatteryStatus(batteryPercent, isChanging)
            }
        }

    override fun setFacePreview(preview: Boolean) {
        if (fpcFace != null) {
            if (!preview && (outerContext as MainActivity).mainFragment.amountFragment.isCurrentFixAmountMode() && !(outerContext as MainActivity).mainFragment.amountFragment.isPaying()){
                return
            }
            fpcFace!!.setPreviewFace(preview)

        }
    }

    override fun setConsumerTips(tips: String?) {
        setConsumerTips(tips, 0)
    }

    override fun setConsumerTips(tips: String?, consumerPro: Int) {
        if (fpcFace != null) {
            fpcFace!!.setFaceCameraTips(tips)
        }
    }

    override fun setConsumerAuthTips(tips: String?) {
        if (fpcFace != null) {
            fpcFace!!.setFaceCameraTips(tips)
        }
    }

    override fun isConsumerAuthTips(): Boolean {
        return isConsumerAuthTips
    }

    override fun setConsumerConfirmFaceInfo(
        facePassPeopleInfo: FacePassPeopleInfo?,
        needConfirm: Boolean,
        consumerType: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun setConsumerConfirmCardInfo(cardNumber: String?, needConfirm: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setConsumerConfirmScanInfo(scanData: String?, needConfirm: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setConsumerTakeMealWay() {
        TODO("Not yet implemented")
    }

    override fun resetFaceConsumerLayout() {
        if (fpcFace != null) {
            fpcFace!!.resetFaceInfoLayout()
        }
    }

    override fun setNormalConsumeStatus() {
        TODO("Not yet implemented")
    }

    override fun setPayConsumeStatus() {
        TODO("Not yet implemented")
    }

    override fun setPayPrice(payPrice: String?, canCancelPay: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setCanCancelPay(showCancelPay: Boolean) {
        TODO("Not yet implemented")
    }


    protected fun goFacePassAuth() {

        if (!SPUtils.getInstance().getBoolean(Constants.SWITCH_FACE_PASS_PAY, false)){
            return
        }

        if (SPUtils.getInstance().getBoolean(Constants.SWITCH_FACE_PASS_PAY, false)) {
           // RkSysTool.getInstance().setGpioLevel("/proc/rk_gpio/led1", false);
        }

//        recognizeCount = 0
        isRunningFacePassAuth = true
        canSpeakFacePassFail = true
        if (canSpeakFacePassFailObserver != null) {
            canSpeakFacePassFailObserver!!.dispose()
            canSpeakFacePassFailObserver = null
        }

        Log.d(TAG, "limeprocessFacePassResult ========================================== 1045 ")
        setFacePreview(true)
        Log.d(TAG, "limesetPreviewFace    ========================================== true: " + 1135)
         cbgCameraHelper =
            (outerContext as MainActivity).getWeakRefHolder(CBGCameraHelper::class.java)
        Log.d(TAG, "limeprocessFacePassResult ========================================== 1058 " + (cbgCameraHelper == null))
        Log.d(TAG,"limeAmountCard cardData" + 1126)
        cbgCameraHelper?.setOnDetectFaceListener(object :
            CBGFacePassHandlerHelper.OnDetectFaceListener {
            override fun onDetectFaceToken(faceTokenList: List<CBGFacePassRecognizeResult?>?) {
                Log.i(TAG, "limeprocessFacePassResult onDetectFaceToken 1052")
                if ((outerContext as MainActivity).mainFragment.amountFragment != null && (!(outerContext as MainActivity).mainFragment.amountFragment.mIsPayingValue() && !(outerContext as MainActivity).mainFragment.amountFragment.isRefund())) {
                    return
                }

                Log.d(TAG,"limeAmountCard cardData" + 1138)
                Log.d(TAG,"limecardmodifyBalanceByCard: " + 1138)
                if (!isStartFaceScan.get()){
                    return
                }

                isStartFaceScan.set(false)
                faceTokens = faceTokenList
                if (faceTokenList != null && faceTokenList.size > 1) {
                    if ((outerContext as MainActivity).mainFragment.amountFragment.isRefundListShow()){
                        return
                    }

                    if ((outerContext as MainActivity).mainFragment.amountFragment.isShowFaceList()){
                      return
                    }

                    val faceData: MutableList<FaceChooseItemEntity> = ArrayList()
                    if (facePassHelper == null){
                        facePassHelper =
                        (outerContext as MainActivity).getWeakRefHolder(FacePassHelper::class.java)
                    }
                    var i = 0;
                    for (faceToken in faceTokenList) {
                        i ++;
                        facePassHelper?.searchFacePassByFaceToken(
                            faceToken?.faceToken,
                            object : FacePassHelper.OnHandleFaceTokenListener {
                                override fun onHandleLocalFace(
                                    faceToken: String?,
                                    facePassPeopleInfo: FacePassPeopleInfo?
                                ) {
                                    if (facePassPeopleInfo != null) {
                                        faceData.add(
                                            FaceChooseItemEntity(
                                                facePassPeopleInfo.full_Name,
                                                facePassPeopleInfo.phone,
                                                facePassPeopleInfo.imgData,
                                                facePassPeopleInfo.cbgFaceToken,
                                                false)
                                        )
                                        if (faceData.size == faceTokenList.size){
                                            val facePassRecognizeResult = faceTokenList[0]
                                            //Log.w(TAG, "limestartRecognizeFrameTask DifferentDisplay: " + JSON.toJSONString(faceTokenList))
                                            Log.w(TAG, "limestartRecognizeFrameTask DifferentDisplay 1169 facePassRecognizeResult?.isFacePassSuccess: " + facePassRecognizeResult?.isFacePassSuccess)
                                            if (facePassRecognizeResult?.isFacePassSuccess == true) {
                                                ttsSpeak("检测到相似人脸，请选择")
                                                (outerContext as MainActivity).mainFragment.amountFragment.showFaceList(faceData)
                                            }else{
                                                ttsSpeak("识别失败")
                                                //EventBus.getDefault().post(MessageEventBean(MessageEventType.PayError))
                                                cbgCameraHelper?.stopFacePassDetect()
                                                goFacePassAuth()
                                            }

                                        }
                                    }
                                }

                                override fun onHandleLocalFaceError(faceToken: String?) {
                                    Log.i(TAG, "limeprocessFacePassResult processFacePassFailRetryDelay 1107")
                                    processFacePassFailRetryDelay(-1)
                                }
                            })


                    }



                    return

                } else {
                    processFacePassResult(faceTokenList)
                }


            }

            override fun onNoDetectFaceToken() {
                Log.i(TAG, "limeprocessFacePassResult processFacePassFailRetryDelay 1057")
                processFacePassFailRetryDelay(-1)
            }
        })
        Log.d(TAG, "limeprocessFacePassResult ========================================== 1076 " + (cbgCameraHelper == null))
        GlobalScope.launch {
            try {
                if (App.isFirstDetect){
                    App.isFirstDetect = false
                    delay(6500)
                }

                cbgCameraHelper?.prepareFacePassDetect()
                cbgCameraHelper?.startFacePassDetect()

            } catch (e: Throwable) {
                Log.e(TAG, "limeException 1208: " + e.message)
            }
        }

//        fpcFace?.postDelayed({
//                        try {
//                if (App.isFirstDetect){
//                    App.isFirstDetect = false
//                    //delay(3100)
//                }
//                cbgCameraHelper?.prepareFacePassDetect()
//                cbgCameraHelper?.startFacePassDetect()
//
//            } catch (e: Throwable) {
//                Log.e(TAG, "limeException 1208: " + e.message)
//            }
//        },3100L)

    }



    /**
     * 停止人脸识别
     */
    protected fun stopFacePassAuth() {
        //是否正在人脸身份校验
        isRunningFacePassAuth = false
        setFacePreview(false)
        Log.d(TAG, "limesetPreviewFace    ========================================== false: " + 1273)
        val cbgCameraHelper: CBGCameraHelper? =
            (outerContext as MainActivity).getWeakRefHolder(CBGCameraHelper::class.java)
        cbgCameraHelper?.stopFacePassDetect()
    }

    /**
     * 处理识别人脸结果
     *
     */
    protected fun processFacePassResult(faceTokenList: List<CBGFacePassRecognizeResult?>?) {
        Log.w(TAG, "limestartRecognizeFrameTask DifferentDisplay: " + 1157)
        if (faceTokenList != null && !faceTokenList.isEmpty()) {
            Log.w(TAG, "limestartRecognizeFrameTask DifferentDisplay: " + 1162)

            if (facePassHelper == null) {
                try{

                    facePassHelper = FacePassHelper(outerContext as MainActivity)

                } catch (e:Exception ) {
                    Log.e(TAG, "limestartRecognizeFrameTask facePassHelper 1178: " + e.message);
                }
            }
            Log.w(TAG, "limestartRecognizeFrameTask DifferentDisplay: " + 1167)
            val facePassRecognizeResult = faceTokenList[0]
            //Log.w(TAG, "limestartRecognizeFrameTask DifferentDisplay: " + JSON.toJSONString(faceTokenList))
            Log.w(TAG, "limestartRecognizeFrameTask DifferentDisplay 1169 facePassRecognizeResult?.isFacePassSuccess: " + facePassRecognizeResult?.isFacePassSuccess)
            if (facePassRecognizeResult?.isFacePassSuccess == true) {
                Log.w(TAG, "limestartRecognizeFrameTask DifferentDisplay: " + 1171)

                try {
                    Log.w(TAG, "limestartRecognizeFrameTask DifferentDisplay: " + facePassRecognizeResult?.faceToken)

//                    GlobalScope.launch {
                    facePassHelper?.searchFacePassByFaceToken(
                        facePassRecognizeResult?.faceToken,
                        object : FacePassHelper.OnHandleFaceTokenListener {
                            override fun onHandleLocalFace(
                                faceToken: String?,
                                facePassPeopleInfo: FacePassPeopleInfo?
                            ) {
                                Log.w(TAG, "limestartRecognizeFrameTask DifferentDisplay: " + 1175)
                                //停止所有的识别检测
                                stopFacePassAuth()
                                handleFacePassSuccess(facePassPeopleInfo)
                                Log.i(TAG, "limeprocessFacePassResult onDetectFaceToken 1104")
                            }

                            override fun onHandleLocalFaceError(faceToken: String?) {
                                Log.i(TAG, "limeprocessFacePassResult processFacePassFailRetryDelay 1107")
                                processFacePassFailRetryDelay(-1)
                            }
                        })
//                    }


                } catch (e:Exception ) {
                    Log.e(TAG, "limestartRecognizeFrameTask DifferentDisplay 1190: " + e.message);
                }
            } else {
                ttsSpeak("识别失败")
                EventBus.getDefault().post(MessageEventBean(MessageEventType.PayError))
            }
        }
    }

    /**
     * 处理人脸识别失败自动重试
     */
    protected fun processFacePassFailRetryDelay(recognizeState: Int) {
        handleFacePassError(canSpeakFacePassFail, recognizeState)
        // 人脸识别重试

    }


    protected fun handleFacePassSuccess(facePassPeopleInfo: FacePassPeopleInfo?) {
        //人脸识别成功
        Log.d(TAG, "limestartRecognizeFrameTask DifferentDisplay: ---------------------------------------------------------------------" )
        clearFacePassQueueCache()
        resetFacePassRetryDelay()
        //ttsSpeak("识别成功")
        runOnUiThread {
            tvFaceTips2.visibility = View.VISIBLE
            tvFaceTips2.text = "识别成功"
            if (companyMember != null) {
                showSuccessFace(companyMember!!.imgData)
            }
        }
        Log.d(TAG,"limeAmountToken  handleFacePassSuccess Full_Name: " + facePassPeopleInfo?.full_Name)

        Log.i(TAG, "limehandleFacePassSuccess 1314 ")
        if (beforeFaceToken.equals(facePassPeopleInfo?.cbgFaceToken) && (System.currentTimeMillis() - beforeTime < 2000)) {
                return
        }
        beforeFaceToken = facePassPeopleInfo?.cbgFaceToken ?: "";
        beforeTime = System.currentTimeMillis();
        Log.i(TAG, "limehandleFacePassSuccess 1320 ")
        EventBus.getDefault()
            .post(MessageEventBean(MessageEventType.AmountToken, facePassPeopleInfo?.cbgFaceToken,facePassPeopleInfo?.card_Number))


    }

    protected fun handleFacePassError(canSpeakFacePassFail: Boolean, recognizeState: Int) {
        Log.d(TAG, "limehandleFacePassError canSpeakFacePassFail: " + canSpeakFacePassFail)
        Log.i(TAG, "limeprocessFacePassResult ============================================================== 1173")
        //人脸识别失败 canSpeakFacePassFail (5s后置为true)
        recognizeCount ++;
        if (recognizeCount >= 5) {
            ttsSpeak("识别失败，正在重试")
            recognizeCount = 0;
        }

//        if (recognizeCount <= 20) {
//            cbgCameraHelper?.stopFacePassDetect()
            goFacePassAuth()
//        }

    }


}

