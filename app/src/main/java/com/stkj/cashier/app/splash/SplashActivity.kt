package com.stkj.cashier.app.splash

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.permissionx.guolindev.PermissionX
import com.stkj.cashier.App
import com.stkj.cashier.R
import com.stkj.cashier.app.base.BaseActivity
import com.stkj.cashier.app.main.MainActivity
import com.stkj.cashier.databinding.SplashActivityBinding
import com.stkj.cashier.utils.util.EncryptUtils
import com.stkj.cashier.utils.util.LogUtils
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit


/**
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 */
@AndroidEntryPoint
class SplashActivity : BaseActivity<SplashViewModel, SplashActivityBinding>() {


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        requestPermission()

    }
    override fun initData(savedInstanceState: Bundle?) {
        super.initData(savedInstanceState)


        // 避免从桌面启动程序后，会重新实例化入口类的activity
        // 判断当前activity是不是所在任务栈的根
        val intent: Intent? = intent
        if (intent != null) {
            val action: String = intent.getAction().toString()
            //1.避免从桌面启动程序后，会重新实例化入口类的activity , 判断当前activity是不是所在任务栈的根
            LogUtils.e("重启"+isTaskRoot+"/"+Gson().toJson(intent))
            if (!isTaskRoot) {
                LogUtils.e("重启"+ Intent.ACTION_MAIN.equals(action)+"/"+intent.hasCategory(Intent.CATEGORY_LAUNCHER))
                LogUtils.e("重启"+Gson().toJson(intent.getCategories()))
                if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(action)) {
                    LogUtils.e("重启finish")
                    finish()
                    return
                }
                finish()
                return
            }
            //2.经过路由跳转的，判断当前应用是否已经初始化过，首页是否存在并且未销毁
            if (Intent.ACTION_VIEW.equals(action)) {
                val isActivityRunning = App.mMainActivity?.let { isActivityRunning(it, MainActivity::class.java.name) }
                LogUtils.e("重启isActivityRunning"+isActivityRunning)
                if (isActivityRunning == true) {
                    LogUtils.e("重启经过路由跳finish")
                    finish()
                    return
                }
                /*val homeActivity: Activity = AppManager.INSTANCE.getMainActivity()
                if (!ActivityUtils.isActivityFinished(homeActivity)) {
                    finish()
                    return
                }*/
            }
        }

        val map: MutableMap<String, Any> = HashMap()
        var deviceId = App.serialNumber
        map["mode"] = "GetIntervalCardType"
        map["machine_Number"] = deviceId
        var md5 = EncryptUtils.encryptMD5ToString16(deviceId)
        map["sign"] = md5
//        viewModel.getIntervalCardType(map);
        viewModel.intervalCardType.observe(this) {
            LogUtils.e("intervalCardType", Gson().toJson(it))
//            SPStaticUtils.put(SPKey.KEY_CONFIG,it.getAiStatus())
            App.intervalCardType = it
        }
//        requestConsumptionType()
        startAnimation(viewDataBinding.rootView)

    }

    override fun getLayoutId(): Int {
        return R.layout.splash_activity
    }



    @SuppressLint("AutoDispose", "CheckResult")
    private fun startAnimation(view: View) {
        val anim = AnimationUtils.loadAnimation(context, R.anim.splash_anim)
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {

            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onAnimationEnd(animation: Animation) {
                if (App.mFacePassHandler != null) {
                    startActivity()
                }
            }

            override fun onAnimationRepeat(animation: Animation) {

            }
        })
        view.startAnimation(anim)
        Observable.timer(2000, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { aLong ->
                requestPermission()
            }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startActivity() {
        startMainActivity()
        finish()
    }


    private fun requestPermission() {

        PermissionX.init(this)
            .permissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    "请同意该权限才能继续使用该功能！",
                    "同意",
                    "取消"
                )
            }

            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    startActivity()
                } else {
                    Toast.makeText(this, "您拒绝了权限", Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun isActivityRunning(context: Context, activityName: String): Boolean {
        val am: ActivityManager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        if (am != null) {
            // 获取当前运行的任务信息
            for (taskInfo in am.getRunningTasks(Int.MAX_VALUE)) {
                if (taskInfo.topActivity?.getClassName().equals(activityName)) {
                    // 如果找到对应的Activity，且它是顶部活动，则认为它存在且未销毁
                    return true
                }
            }
        }
        return false
    }
    override fun onDestroy() {
        super.onDestroy()
    }


}