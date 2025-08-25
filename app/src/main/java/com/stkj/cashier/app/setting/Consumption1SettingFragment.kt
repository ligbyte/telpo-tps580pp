package com.stkj.cashier.app.setting

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Choreographer
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.jakewharton.processphoenix.ProcessPhoenix
import com.king.base.util.SystemUtils
import com.stkj.cashier.App
import com.stkj.cashier.BuildConfig
import com.stkj.cashier.R
import com.stkj.cashier.app.base.BaseFragment
import com.stkj.cashier.app.base.helper.CommonTipsHelper
import com.stkj.cashier.app.main.MainActivity
import com.stkj.cashier.app.main.SettingViewModel
import com.stkj.cashier.bean.MessageEventBean
import com.stkj.cashier.cbgfacepass.FacePassHelper
import com.stkj.cashier.cbgfacepass.data.FacePassDateBaseMMKV
import com.stkj.cashier.config.MessageEventType
import com.stkj.cashier.constants.Constants
import com.stkj.cashier.databinding.Consumption1SettingFragmentBinding
import com.stkj.cashier.dict.HomeMenu
import com.stkj.cashier.utils.ShellUtils
import com.stkj.cashier.utils.util.LogUtils
import com.stkj.cashier.utils.util.SPUtils
import com.stkj.cashier.utils.util.SpanUtils
import com.stkj.cashier.utils.util.ToastUtils
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus

/**
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 */
@AndroidEntryPoint
class Consumption1SettingFragment :
    BaseFragment<SettingViewModel, Consumption1SettingFragmentBinding>(), View.OnClickListener {


    companion object {
        fun newInstance(): Consumption1SettingFragment {
            return Consumption1SettingFragment()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        LogUtils.e("消费模式onHiddenChanged" + hidden)
        if (hidden) {
            SPUtils.getInstance().put(Constants.FRAGMENT_SET, false)
        } else {
            SPUtils.getInstance().put(Constants.FRAGMENT_SET, true)
        }
    }

    var facePassHelper: FacePassHelper? = null;

    //当前页面选中状态索引
    private var currentSelectIndex = -1

    //当前页面索引
    private var mPageIndex = 0

    //第一层页面总item数量
    private val firstPageSelectItemCount = 8

    //第二层页面总item数量
    private val secondPageSelectItemCount = 8


    //第三层页面总item数量
    private val threePageSelectItemCount = 5

    private val fixAmountDataMap: MutableMap<String, String> = mutableMapOf()

    /**
     * 刷新时段信息
     */
    private fun refreshIntervalCardType() {
        //早餐时间
        val intervalCardType = App.intervalCardType
        if (intervalCardType.isNotEmpty()) {
            val cardTypeBean = intervalCardType[0]
            val oneTime = cardTypeBean.oneTime
            if (!TextUtils.isEmpty(oneTime)) {
                binding.llSwitchBreakfast.visibility = View.VISIBLE
                binding.tvBreakfastTime.text = oneTime
                val breakfastSwitch =
                    SPUtils.getInstance().getBoolean(Constants.BREAKFAST_SWITCH, true)
                if (breakfastSwitch) {
                    binding.llBreakfastSetting.visibility = View.VISIBLE
                    binding.ivSwitchBreakfast.setImageResource(R.mipmap.icon_check_selected)
                } else {
                    binding.llBreakfastSetting.visibility = View.GONE
                    binding.ivSwitchBreakfast.setImageResource(0)
                }
            } else {
                binding.llSwitchBreakfast.visibility = View.GONE
                binding.llBreakfastSetting.visibility = View.GONE
            }
            val twoTime = cardTypeBean.twoTime
            if (!TextUtils.isEmpty(twoTime)) {
                binding.llSwitchLunch.visibility = View.VISIBLE
                binding.tvLunchTime.text = twoTime
                val lunchSwitch =
                    SPUtils.getInstance().getBoolean(Constants.LUNCH_SWITCH, true)
                if (lunchSwitch) {
                    binding.llLunchSetting.visibility = View.VISIBLE
                    binding.ivSwitchLunch.setImageResource(R.mipmap.icon_check_selected)
                } else {
                    binding.llLunchSetting.visibility = View.GONE
                    binding.ivSwitchLunch.setImageResource(0)
                }
            } else {
                binding.llSwitchLunch.visibility = View.GONE
                binding.llLunchSetting.visibility = View.GONE
            }
            val threeTime = cardTypeBean.threeTime
            if (!TextUtils.isEmpty(threeTime)) {
                binding.llSwitchDinner.visibility = View.VISIBLE
                binding.tvDinnerTime.text = threeTime
                val dinnerSwitch =
                    SPUtils.getInstance().getBoolean(Constants.DINNER_SWITCH, true)
                if (dinnerSwitch) {
                    binding.llDinnerSetting.visibility = View.VISIBLE
                    binding.ivSwitchDinner.setImageResource(R.mipmap.icon_check_selected)
                } else {
                    binding.llDinnerSetting.visibility = View.GONE
                    binding.ivSwitchDinner.setImageResource(0)
                }
            } else {
                binding.llSwitchDinner.visibility = View.GONE
                binding.llDinnerSetting.visibility = View.GONE
            }
        } else {
            binding.llSwitchBreakfast.visibility = View.GONE
            binding.llBreakfastSetting.visibility = View.GONE
            binding.llSwitchLunch.visibility = View.GONE
            binding.llLunchSetting.visibility = View.GONE
            binding.llSwitchDinner.visibility = View.GONE
            binding.llDinnerSetting.visibility = View.GONE
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
        super.initData(savedInstanceState)
     try{
        showFirstPage()
    } catch (e: Throwable) {
         Log.e("TAG", "limeException 142: " + e.message)
    }
    }

    private fun scrollPreItem() {
        currentSelectIndex--;
        if (mPageIndex == 0) {
            if (!binding.flSwitchFacePass.isVisible){
                currentSelectIndex--
            }
            if (currentSelectIndex <= -1) {
                currentSelectIndex = firstPageSelectItemCount - 1
            }
        } else if (mPageIndex == 1) {
            Log.e("selectScrollItem", "-scrollPreItem-mPageIndex- = $currentSelectIndex")
            if (currentSelectIndex <= -1) {
                currentSelectIndex = secondPageSelectItemCount - 1
                Log.e("selectScrollItem", "-scrollPreItem--resetIndex- = $currentSelectIndex")
            }
            if (binding.llDinnerSetting.visibility == View.GONE) {
                Log.e("selectScrollItem", "-scrollPreItem-llDinnerSetting-GONE")
                if (currentSelectIndex == 5) {
                    currentSelectIndex -= 1;
                }
            }
            if (binding.llLunchSetting.visibility == View.GONE) {
                Log.e("selectScrollItem", "-scrollPreItem-llLunchSetting-GONE")
                if (currentSelectIndex == 4) {
                    currentSelectIndex -= 1;
                }
            }
            if (binding.llBreakfastSetting.visibility == View.GONE) {
                Log.e("selectScrollItem", "-scrollPreItem-llBreakfastSetting-GONE")
                if (currentSelectIndex == 3) {
                    currentSelectIndex -= 1;
                }
            }
            if (binding.llSwitchDinner.visibility == View.GONE) {
                Log.e("selectScrollItem", "-scrollPreItem-llSwitchDinner-GONE")
                if (currentSelectIndex == 2) {
                    currentSelectIndex -= 1;
                }
            }
            if (binding.llSwitchLunch.visibility == View.GONE) {
                Log.e("selectScrollItem", "-scrollPreItem-llSwitchLunch-GONE")
                if (currentSelectIndex == 1) {
                    currentSelectIndex -= 1;
                }
            }
            if (binding.llSwitchBreakfast.visibility == View.GONE) {
                Log.e("selectScrollItem", "-scrollPreItem-llSwitchBreakfast-GONE")
                if (currentSelectIndex == 0) {
                    currentSelectIndex -= 1;
                }
            }
            if (currentSelectIndex <= -1) {
                currentSelectIndex = secondPageSelectItemCount - 1
                Log.e("selectScrollItem", "-scrollPreItem--resetIndex- = $currentSelectIndex")
            }
        }else if (mPageIndex == 2) {
            if (currentSelectIndex <= -1) {
                currentSelectIndex = threePageSelectItemCount - 1
            }
        }
        Log.e("selectScrollItem", "-scrollPreItem-currentSelectIndex-- = $currentSelectIndex")
        selectScrollItem(currentSelectIndex)
    }

    private fun scrollNextItem() {
        currentSelectIndex++

        if (mPageIndex == 0) {
            if (!binding.flSwitchFacePass.isVisible){
                currentSelectIndex++
            }
            if (currentSelectIndex >= firstPageSelectItemCount) {
                currentSelectIndex = 0
            }
        } else if (mPageIndex == 1) {
            Log.e("selectScrollItem", "-scrollNextItem-mPageIndex-$currentSelectIndex")
            if (currentSelectIndex >= secondPageSelectItemCount) {
                currentSelectIndex = 0
                Log.e("selectScrollItem", "-scrollNextItem--resetIndex- = $currentSelectIndex")
            }
            if (binding.llSwitchBreakfast.visibility == View.GONE) {
                Log.e("selectScrollItem", "-scrollNextItem-llSwitchBreakfast-GONE")
                if (currentSelectIndex == 0) {
                    currentSelectIndex += 1;
                }
            }
            if (binding.llSwitchLunch.visibility == View.GONE) {
                Log.e("selectScrollItem", "-scrollNextItem-llSwitchLunch-GONE")
                if (currentSelectIndex == 1) {
                    currentSelectIndex += 1;
                }
            }
            if (binding.llSwitchDinner.visibility == View.GONE) {
                Log.e("selectScrollItem", "-scrollNextItem-llSwitchDinner-GONE")
                if (currentSelectIndex == 2) {
                    currentSelectIndex += 1;
                }
            }
            if (binding.llBreakfastSetting.visibility == View.GONE) {
                Log.e("selectScrollItem", "-scrollNextItem-llBreakfastSetting-GONE")
                if (currentSelectIndex == 3) {
                    currentSelectIndex += 1;
                }
            }
            if (binding.llLunchSetting.visibility == View.GONE) {
                Log.e("selectScrollItem", "-scrollNextItem-llLunchSetting-GONE")
                if (currentSelectIndex == 4) {
                    currentSelectIndex += 1;
                }
            }
            if (binding.llDinnerSetting.visibility == View.GONE) {
                Log.e("selectScrollItem", "-scrollNextItem-llDinnerSetting-GONE")
                if (currentSelectIndex == 5) {
                    currentSelectIndex += 1;
                }
            }
            if (currentSelectIndex >= secondPageSelectItemCount) {
                currentSelectIndex = 0
                Log.e("selectScrollItem", "-scrollNextItem--resetIndex- = $currentSelectIndex")
            }
        }else if (mPageIndex == 2) {
            if (currentSelectIndex >= threePageSelectItemCount) {
                currentSelectIndex = 0
            }
        }
        Log.d("selectScrollItem", "limecurrentSelectIndex = $currentSelectIndex")
        selectScrollItem(currentSelectIndex)
    }

    private fun selectScrollItem(itemIndex: Int) {

        if (mPageIndex == 0) {
            binding.flFixAmountMode.background = null
            binding.flSwitchFacePass.background = null
            binding.flRestartApp.background = null
            binding.flShutdownDevice.background = null
            binding.flRebootDevice.background = null
            binding.flSwitchTongLianPay.background = null
            binding.flFaceUpdate.background = null
            binding.flSafeSettings.background = null

            var focusView: View? = null
            when (itemIndex) {
                0 -> {
                    binding.flFixAmountMode.background = ColorDrawable(0x12ffffff)
                    focusView = binding.flFixAmountMode
                }

                1 -> {
                    binding.flSwitchFacePass.background = ColorDrawable(0x12ffffff)
                    focusView = binding.flSwitchFacePass
                }

                2 -> {
                    binding.flFaceUpdate.background = ColorDrawable(0x12ffffff)
                    focusView = binding.flFaceUpdate
                }

                3 -> {
                    binding.flSafeSettings.background = ColorDrawable(0x12ffffff)
                    focusView = binding.flSafeSettings
                }

                4 -> {
                    binding.flSwitchTongLianPay.background = ColorDrawable(0x12ffffff)
                    focusView = binding.flSwitchTongLianPay
                }

                5 -> {
                    binding.flRestartApp.background = ColorDrawable(0x12ffffff)
                    focusView = binding.flRestartApp
                }

                6 -> {
                    binding.flShutdownDevice.background = ColorDrawable(0x12ffffff)
                    focusView = binding.flShutdownDevice
                }

                7 -> {
                    binding.flRebootDevice.background = ColorDrawable(0x12ffffff)
                    focusView = binding.flRebootDevice
                }
            }
            if (focusView != null) {
                binding.svContent.smoothScrollTo(0, focusView.top)
            }
        } else if (mPageIndex == 1) {
            binding.flCanteenTimeSetting.background = null
            binding.llSwitchBreakfast.isSelected = false
            binding.ivSwitchBreakfast.isSelected = false
            binding.tvSwitchBreakfast.isSelected = false
            binding.llSwitchLunch.isSelected = false
            binding.ivSwitchLunch.isSelected = false
            binding.tvSwitchLunch.isSelected = false
            binding.llSwitchDinner.isSelected = false
            binding.ivSwitchDinner.isSelected = false
            binding.tvSwitchDinner.isSelected = false
            //早餐
            binding.llBreakfastSetting.background = null
            binding.tvBreakfastAmount.isSelected = false
            binding.tvBreakfastAmount.setTextColor(Color.parseColor("#ffffffff"))
            //午餐
            binding.llLunchSetting.background = null
            binding.tvLunchAmount.isSelected = false
            binding.tvLunchAmount.setTextColor(Color.parseColor("#ffffffff"))
            //晚餐
            binding.llDinnerSetting.background = null
            binding.tvDinnerAmount.isSelected = false
            binding.tvDinnerAmount.setTextColor(Color.parseColor("#ffffffff"))
            //开关
            binding.llSwitchFixAmount.background = null
            binding.tvOpenFixAmount.isSelected = false
            binding.tvCloseFixAmount.isSelected = false
            var focusView: View? = null
            when (itemIndex) {
                //早餐开关
                0 -> {
                    binding.flCanteenTimeSetting.background = ColorDrawable(0x12ffffff)
                    binding.llSwitchBreakfast.isSelected = true
                    binding.ivSwitchBreakfast.isSelected = true
                    binding.tvSwitchBreakfast.isSelected = true
                    focusView = binding.flCanteenTimeSetting
                }
                //午餐开关
                1 -> {
                    binding.flCanteenTimeSetting.background = ColorDrawable(0x12ffffff)
                    binding.llSwitchLunch.isSelected = true
                    binding.ivSwitchLunch.isSelected = true
                    binding.tvSwitchLunch.isSelected = true
                    focusView = binding.flCanteenTimeSetting
                }
                //晚餐开关
                2 -> {
                    binding.flCanteenTimeSetting.background = ColorDrawable(0x12ffffff)
                    binding.llSwitchDinner.isSelected = true
                    binding.ivSwitchDinner.isSelected = true
                    binding.tvSwitchDinner.isSelected = true
                    focusView = binding.flCanteenTimeSetting
                }

                //早餐金额
                3 -> {
                    binding.llBreakfastSetting.background = ColorDrawable(0x12ffffff)
                    binding.tvBreakfastAmount.isSelected = true
                    binding.tvBreakfastAmount.setTextColor(Color.parseColor("#ff00dc82"))
                    focusView = binding.llBreakfastSetting
                }

                //午餐金额
                4 -> {
                    binding.llLunchSetting.background = ColorDrawable(0x12ffffff)
                    binding.tvLunchAmount.isSelected = true
                    binding.tvLunchAmount.setTextColor(Color.parseColor("#ff00dc82"))
                    focusView = binding.llLunchSetting
                }

                //晚餐金额
                5 -> {
                    binding.llDinnerSetting.background = ColorDrawable(0x12ffffff)
                    binding.tvDinnerAmount.isSelected = true
                    binding.tvDinnerAmount.setTextColor(Color.parseColor("#ff00dc82"))
                    focusView = binding.llDinnerSetting
                }

                //开启
                6 -> {
                    binding.llSwitchFixAmount.background = ColorDrawable(0x12ffffff)
                    binding.tvOpenFixAmount.isSelected = true
                    focusView = binding.llSwitchFixAmount
                }

                //关闭
                7 -> {
                    binding.llSwitchFixAmount.background = ColorDrawable(0x12ffffff)
                    binding.tvCloseFixAmount.isSelected = true
                    focusView = binding.llSwitchFixAmount
                }
            }
            if (focusView != null) {
                binding.svContent.smoothScrollTo(0, focusView.top)
            }
        }else if (mPageIndex == 2) {
            binding.flSwitchSafeSettings.background = null
            binding.flAutoLock.background = null
            binding.flLockPwd.background = null
            binding.llSwitchLock.background = null
            binding.tvOpenLock.isSelected = false
            binding.tvLockCancle.isSelected = false
            binding.tvLockPwd.isSelected = false
            binding.tvLockTime.isSelected = false

            var focusView: View? = null
            when (itemIndex) {
                0 -> {
                    binding.flSwitchSafeSettings.background = ColorDrawable(0x12ffffff)
                    focusView = binding.flSwitchSafeSettings
                }

                1 -> {
                    binding.flAutoLock.background = ColorDrawable(0x12ffffff)
                    binding.tvLockTime.isSelected = true
                    focusView = binding.flAutoLock
                }

                2 -> {
                    binding.flLockPwd.background = ColorDrawable(0x12ffffff)
                    binding.tvLockPwd.isSelected = true
                    focusView = binding.flLockPwd
                }


                3 -> {
                    binding.llSwitchLock.background = ColorDrawable(0x12ffffff)
                    binding.tvOpenLock.isSelected = true
                    focusView = binding.llSwitchLock
                }


                4 -> {
                    binding.llSwitchLock.background = ColorDrawable(0x12ffffff)
                    binding.tvLockCancle.isSelected = true
                    focusView = binding.llSwitchLock
                }

            }
            if (focusView != null) {
                binding.svContent.smoothScrollTo(0, focusView.top)
            }
        }

    }

    override fun getLayoutId(): Int {
        return R.layout.consumption1_setting_fragment
    }

    public fun onHandleEventMsg(message: MessageEventBean) {
        when (message.type) {
            MessageEventType.KeyEventNumber -> {
                //LogUtils.e("金额模式 按键")
                if (SPUtils.getInstance().getBoolean(Constants.FRAGMENT_SET, false)) {
                    App.lastOperTime = System.currentTimeMillis()
                    message.content?.let {
                        LogUtils.e("消费模式 按键" + it)
                        when (it) {
                            "向左",
                            "向上" -> {
                                if (binding.flClosePwd.isVisible){
                                    return
                                }
                                if (mPageIndex == 1) {
                                    if (binding.flTips.visibility == View.VISIBLE) {
                                        hidTips()
                                        return
                                    }
                                }
                                scrollPreItem()
                            }

                            "向右",
                            "向下" -> {
                                if (binding.flClosePwd.isVisible){
                                    return
                                }
                                if (mPageIndex == 1) {
                                    if (binding.flTips.visibility == View.VISIBLE) {
                                        hidTips()
                                        return
                                    }
                                }
                                scrollNextItem()
                            }

                            "删除" -> {
                                if (binding.updateFaceConfirm.isVisible) {
                                    binding.updateFaceConfirm.visibility = View.GONE
                                    return
                                }
                                if (mPageIndex == 1) {
                                    if (currentSelectIndex == 3 || currentSelectIndex == 4 || currentSelectIndex == 5) {
                                        if (!processDelNumber()) {
                                            // TODO:
                                        } else {

                                        }
                                    } else {
                                        backPress()
                                    }
                                }else  if (mPageIndex == 2) {

                                    if (binding.flClosePwd.isVisible){
                                        if (!TextUtils.isEmpty(binding.tvInputPwdClose.text.toString())) {
                                            processDelNumber()
                                        } else {
                                            binding.flClosePwd.visibility = View.GONE
                                        }
                                        return
                                    }

                                    if (currentSelectIndex == 1 || currentSelectIndex == 2) {
                                        if (!processDelNumber()) {
                                            // TODO:
                                        } else {

                                        }
                                    } else {
                                        backPress()
                                    }
                                }else {
                                    backPress()
                                }
                            }

                            "取消" -> {
                                backPress()
                            }

                            "确认" -> {
                                 if (mPageIndex == 2) {
                                    if (currentSelectIndex == 0) {

                                        if (binding.ivSwitchSafeSettings.isSelected && !binding.flClosePwd.isVisible && !TextUtils.isEmpty(SPUtils.getInstance().getString(Constants.SAFE_SETTINGS_PWD))) {
                                            binding.flClosePwd.visibility = View.VISIBLE
                                            binding.tvInputPwdClose.text = ""
                                            binding.tvInputPwdClose.isSelected = true
                                            return
                                        }

                                        if (binding.flClosePwd.isVisible && binding.ivSwitchSafeSettings.isSelected && TextUtils.isEmpty(binding.tvInputPwdClose.text.toString())){
                                            ttsSpeak("请输入锁屏密码")
                                            return
                                        }

                                        if (binding.ivSwitchSafeSettings.isSelected && !TextUtils.isEmpty(SPUtils.getInstance().getString(Constants.SAFE_SETTINGS_PWD))) {

                                            if (binding.tvInputPwdClose.text.toString() == SPUtils.getInstance().getString(Constants.SAFE_SETTINGS_PWD)){
                                                ttsSpeak("关闭成功")
                                                binding.ivSwitchSafeSettings.isSelected = false
                                                App.lastOperTime = 0
                                                binding.flClosePwd.visibility = View.GONE
                                                binding.tvLockPwd.text = ""
                                                SPUtils.getInstance().put(Constants.SWITCH_SAFE_SETTINGS, false)
                                                SPUtils.getInstance().put(Constants.SAFE_SETTINGS_TIME, "30")
                                                SPUtils.getInstance().put(Constants.SAFE_SETTINGS_PWD, "")

                                            }else{
                                                ttsSpeak("密码错误,请重新输入")
                                                CommonTipsHelper.INSTANCE.setTipsDelayHide("密码错误,请重新输入")
                                                binding.tvInputPwdClose.text = ""
                                            }

                                            return
                                        }

                                        val switchSafeSettings = binding.ivSwitchSafeSettings.isSelected

                                        binding.ivSwitchSafeSettings.isSelected = !switchSafeSettings

                                        if (binding.ivSwitchSafeSettings.isSelected) {


                                        } else {
                                            SPUtils.getInstance()
                                                .put(Constants.SWITCH_SAFE_SETTINGS, false)
                                        }

                                    } else if (currentSelectIndex == 3) {


                                        if (!binding.ivSwitchSafeSettings.isSelected) {
                                            ttsSpeak("请打开设置锁屏")
                                            CommonTipsHelper.INSTANCE.setTipsDelayHide("请打开设置锁屏")
                                            return
                                        }

                                            if (TextUtils.isEmpty(binding.tvLockTime.text)) {
                                                ttsSpeak("请输入锁屏时间")
                                            } else {
                                                if (TextUtils.isEmpty(binding.tvLockPwd.text) && binding.ivSwitchSafeSettings.isSelected) {
                                                    ttsSpeak("请输入锁屏密码")
                                                } else {

                                                    if (binding.tvLockPwd.text.length < 4 && binding.ivSwitchSafeSettings.isSelected){
                                                        showTips("请至少输入4个数字")
                                                        ttsSpeak("请至少输入4个数字密码")
                                                        return
                                                    }

                                                    if (binding.ivSwitchSafeSettings.isSelected) {
                                                        SPUtils.getInstance().put(Constants.SAFE_SETTINGS_TIME, binding.tvLockTime.text.toString())
                                                    }else{
                                                        SPUtils.getInstance().put(Constants.SAFE_SETTINGS_TIME, "30")
                                                    }

                                                    if (binding.ivSwitchSafeSettings.isSelected) {
                                                        SPUtils.getInstance().put(Constants.SAFE_SETTINGS_PWD, binding.tvLockPwd.text.toString())
                                                    }else{
                                                        SPUtils.getInstance().put(Constants.SAFE_SETTINGS_PWD, "")
                                                    }

                                                    SPUtils.getInstance().put(Constants.SWITCH_SAFE_SETTINGS, binding.ivSwitchSafeSettings.isSelected)
                                                    showFirstPage()
                                                    //returnMainPage()
                                                }
                                            }



                                    }else if (currentSelectIndex == 4) {
                                        if (!binding.ivSwitchSafeSettings.isSelected) {
                                            SPUtils.getInstance()
                                                .put(Constants.SWITCH_SAFE_SETTINGS, false)
                                        }
                                        showFirstPage()
                                        //returnMainPage()
                                    }else{

                                    }
                                }else if (mPageIndex == 0) {
                                    if (binding.updateFaceConfirm.isVisible) {
                                        binding.updateFaceConfirm.visibility = View.GONE
                                        if (!SystemUtils.isNetWorkActive(getApp())) {
                                            //ttsSpeak(getString(R.string.result_network_unavailable_error))
                                            ttsSpeak("网络已断开，请检查网络。")
                                            return
                                        }

                                        if (!App.initFaceSDKSuccess) {
                                            //ttsSpeak(getString(R.string.result_network_unavailable_error))
                                            ttsSpeak("请激活人脸识别SDK")
                                            ToastUtils.showLong("请激活人脸识别SDK")
                                            return
                                        }


                                        // 下载人脸
                                        EventBus.getDefault()
                                            .post(MessageEventBean(MessageEventType.ShowLoadingDialog, "下载人脸","Downloading"))
                                        if (facePassHelper == null) {
                                            facePassHelper = FacePassHelper(activity as MainActivity);
                                        }
                                        Log.d(TAG,"limeFacePassHelper 1195 facePassHelper == null: " + (facePassHelper == null) )
                                        facePassHelper!!.deleteAllFaceGroup(true);
                                        Log.d(TAG,"limeFacePassHelper 1196")
                                        Log.d(TAG, "limeIndex 确认 = " + 535)
                                        return
                                    }

                                    if (currentSelectIndex == 1) {
                                        val switchFacePassPay = binding.ivSwitchFacePass.isSelected
                                        binding.ivSwitchFacePass.isSelected = !switchFacePassPay
                                        if (binding.ivSwitchFacePass.isSelected) {
                                            SPUtils.getInstance()
                                                .put(Constants.SWITCH_FACE_PASS_PAY, true)
                                            val mainActivity = activity as MainActivity
                                            if (mainActivity.mainFragment.amountFragment.isPaying()) {
                                                EventBus.getDefault()
                                                    .post(MessageEventBean(MessageEventType.OpenFacePassPay))
                                            } else {

                                            }
                                        } else {
                                            SPUtils.getInstance()
                                                .put(Constants.SWITCH_FACE_PASS_PAY, false)
                                            EventBus.getDefault()
                                                .post(MessageEventBean(MessageEventType.CloseFacePassPay))
                                        }
//                                        ttsSpeak("人脸识别正在开发中，敬请期待")

                                    } else if (currentSelectIndex == 2) {
                                        binding.updateFaceConfirm.visibility = View.VISIBLE
                                    }else if (currentSelectIndex == 3) {
                                        //ToastUtils.showLong("安全设置")
                                        showThreePage()
                                    }else if (currentSelectIndex == 4) {
                                        val switchTongLianPay = binding.ivSwitchTongLianPay.isSelected
                                        binding.ivSwitchTongLianPay.isSelected = !switchTongLianPay
                                        if (binding.ivSwitchTongLianPay.isSelected) {
                                            SPUtils.getInstance()
                                                .put(Constants.SWITCH_TONG_LIAN_PAY, true)
                                            EventBus.getDefault()
                                                .post(MessageEventBean(MessageEventType.OpenTongLianPayPay))
                                        } else {
                                            SPUtils.getInstance()
                                                .put(Constants.SWITCH_TONG_LIAN_PAY, false)
                                            EventBus.getDefault()
                                                .post(MessageEventBean(MessageEventType.CloseTongLianPayPay))
                                        }
                                    }else if (currentSelectIndex == 0) {
                                        showSecondPage()
                                    } else if (currentSelectIndex == 5) {
                                        ProcessPhoenix.triggerRebirth(App.applicationContext)
                                    } else if (currentSelectIndex == 6){
                                        ShellUtils.execCommand("reboot -p",false)
                                    } else if (currentSelectIndex == 7){
                                        ShellUtils.execCommand("reboot",false)
                                    } else {

                                    }
                                } else if (mPageIndex == 1) {
                                    if (binding.flTips.visibility == View.VISIBLE) {
                                        hidTips()
                                    }
                                    if (currentSelectIndex == 0 || currentSelectIndex == 1 || currentSelectIndex == 2) {
                                        handleCanteenTimeSetting()
                                    } else if (currentSelectIndex == 3 || currentSelectIndex == 4 || currentSelectIndex == 5){
                                        scrollNextItem()
                                    } else if (currentSelectIndex == 6) {

                                        if (binding.llBreakfastSetting.visibility == View.VISIBLE) {
                                            SPUtils.getInstance()
                                                .put(Constants.BREAKFAST_SWITCH, true)
                                            //早餐金额判断
                                            var breakfastAmountStr =
                                                binding.tvBreakfastAmount.text.toString()
                                            val breakfastAmount =
                                                breakfastAmountStr.toDoubleOrNull()
                                            if (breakfastAmount == null || breakfastAmount <= 0) {
                                                showTips("早餐金额未设置，不能开启定额模式")
                                                return
                                            }
                                            //保存本地金额
                                            SPUtils.getInstance()
                                                .put(Constants.BREAKFAST_AMOUNT, breakfastAmountStr)
                                        } else {
                                            SPUtils.getInstance()
                                                .put(Constants.BREAKFAST_SWITCH, false)
                                        }

                                        if (binding.llLunchSetting.visibility == View.VISIBLE) {
                                            SPUtils.getInstance().put(Constants.LUNCH_SWITCH, true)
                                            //午餐金额判断
                                            var lunchAmountStr =
                                                binding.tvLunchAmount.text.toString()
                                            val lunchAmount =
                                                lunchAmountStr.toDoubleOrNull()
                                            if (lunchAmount == null || lunchAmount <= 0) {
                                                showTips("午餐金额未设置，不能开启定额模式")
                                                return
                                            }
                                            //保存本地金额
                                            SPUtils.getInstance()
                                                .put(Constants.LUNCH_AMOUNT, lunchAmountStr)
                                        } else {
                                            SPUtils.getInstance().put(Constants.LUNCH_SWITCH, false)
                                        }

                                        if (binding.llDinnerSetting.visibility == View.VISIBLE) {
                                            SPUtils.getInstance().put(Constants.DINNER_SWITCH, true)
                                            //晚餐金额判断
                                            var dinnerAmountStr =
                                                binding.tvDinnerAmount.text.toString()
                                            val dinnerAmount =
                                                dinnerAmountStr.toDoubleOrNull()
                                            if (dinnerAmount == null || dinnerAmount <= 0) {
                                                showTips("晚餐金额未设置，不能开启定额模式")
                                                return
                                            }
                                            //保存本地金额
                                            SPUtils.getInstance()
                                                .put(Constants.DINNER_AMOUNT, dinnerAmountStr)
                                        } else {
                                            SPUtils.getInstance()
                                                .put(Constants.DINNER_SWITCH, false)
                                        }

                                        SPUtils.getInstance().put(Constants.SWITCH_FIX_AMOUNT, true)
                                        EventBus.getDefault()
                                            .post(MessageEventBean(MessageEventType.OpenFixAmountMode))
                                        val facePassPaySwitch =
                                            SPUtils.getInstance().getBoolean(Constants.SWITCH_FACE_PASS_PAY, false)

                                        if (facePassPaySwitch){
                                            EventBus.getDefault()
                                                .post(MessageEventBean(MessageEventType.OpenFacePassPay))
                                        }
                                        showFirstPage()
                                        returnMainPage()
                                    } else if (currentSelectIndex == 7) {
                                        //关闭定额
                                        SPUtils.getInstance()
                                            .put(Constants.SWITCH_FIX_AMOUNT, false)
                                        EventBus.getDefault()
                                            .post(MessageEventBean(MessageEventType.CloseFixAmountMode))
                                        val facePassPaySwitch =
                                            SPUtils.getInstance().getBoolean(Constants.SWITCH_FACE_PASS_PAY, false)
                                        if (facePassPaySwitch){
                                            EventBus.getDefault()
                                                .post(MessageEventBean(MessageEventType.CloseFacePassPay))
                                        }
                                        showFirstPage()
                                        returnMainPage()
                                    } else {

                                    }
                                }  else {

                                }
                            }

                            "0",
                            "1",
                            "2",
                            "3",
                            "4",
                            "5",
                            "6",
                            "7",
                            "8",
                            "9",
                            ".",
                                -> {
                                if (mPageIndex == 1) {
                                    if (binding.flTips.visibility == View.VISIBLE) {
                                        hidTips()
                                        return
                                    }
                                    try {
                                        handleInputNumber(it)
                                    } catch (e: Throwable) {
                                        e.printStackTrace()
                                        showTips("输入数字异常,请重新输入")
                                    }
                                } else if (mPageIndex == 2) {
                                    if (binding.flTips.visibility == View.VISIBLE) {
                                        hidTips()
                                        return
                                    }
                                    try {
                                        handleInputNumber(it)
                                    } catch (e: Throwable) {
                                        e.printStackTrace()
                                        showTips("输入数字异常,请重新输入")
                                    }
                                } else {

                                }
                            }

                            else -> {}
                        }

                    }
                }

            }

            MessageEventType.IntervalCardType -> {
                refreshIntervalCardType()
            }

            MessageEventType.RestAmountUI -> {
                showFirstPage()
                returnMainPage()
            }

            MessageEventType.ShowFaceCount -> {
                message.content?.toLong()?.let { refreshFacePassTotalCount(it) }
            }

        }
    }

    private fun refreshFacePassTotalCount(count: Long) {
        Log.d(FacePassHelper.TAG, "limeaddFacePassToLocal getFaceCount 836 " + count)
        activity?.getColor(R.color.color_00dc82)?.let {
            SpanUtils.with(binding.tvFaceCount)
                .append(count.toString())
                .setForegroundColor(it)
                .append(" 人已入库")
                .create()
        }

        FacePassDateBaseMMKV.setFaceCount(count)

    }

    override fun onEventReceiveMsg(message: MessageEventBean) {
        super.onEventReceiveMsg(message)
        onHandleEventMsg(message)
    }

    private fun handleDelAmountNumber(
        amountConst: String,
        amountTextView: TextView
    ): Boolean {
        try {
            val amountTxt = amountTextView.text.toString()
            if (amountTxt.length == 0) {
                fixAmountDataMap[amountConst] = "-"
                amountTextView.text = ""
                return false
            } else if (amountTxt.length == 1) {
                fixAmountDataMap[amountConst] = "-"
                amountTextView.text = ""
            } else {
                amountTextView.text = amountTxt.substring(0, amountTxt.length - 1)
            }
            return true
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return false
    }

    private fun handleDelTimeNumber(
        amountConst: String,
        amountTextView: TextView
    ): Boolean {
        try {
            val amountTxt = amountTextView.text.toString()
            if (amountTxt.length == 0) {
                amountTextView.text = ""
                return false
            } else if (amountTxt.length == 1) {
                amountTextView.text = ""
            } else {
                amountTextView.text = amountTxt.substring(0, amountTxt.length - 1)
            }
            return true
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return false
    }

    private fun processDelNumber(): Boolean {
        if (mPageIndex == 1) {
            when (currentSelectIndex) {

                //早餐金额
                3 -> {
                    return handleDelAmountNumber(
                        Constants.BREAKFAST_AMOUNT,
                        binding.tvBreakfastAmount
                    )
                }

                //午餐金额
                4 -> {
                    return handleDelAmountNumber(
                        Constants.LUNCH_AMOUNT,
                        binding.tvLunchAmount
                    )
                }

                //晚餐金额
                5 -> {
                    return handleDelAmountNumber(
                        Constants.DINNER_AMOUNT,
                        binding.tvDinnerAmount
                    )
                }
            }
        }else  if (mPageIndex == 2) {

            if (binding.flClosePwd.isVisible){
                handleDelTimeNumber(
                    Constants.SAFE_SETTINGS_TIME,
                    binding.tvInputPwdClose
                )
                return false
            }

            when (currentSelectIndex) {

                //锁屏时间
                1 -> {
                    return handleDelTimeNumber(
                        Constants.SAFE_SETTINGS_TIME,
                        binding.tvLockTime
                    )
                }

                //锁屏密码
                2 -> {
                    if (TextUtils.isEmpty(SPUtils.getInstance().getString(Constants.SAFE_SETTINGS_PWD))) {
                        return handleDelTimeNumber(
                            Constants.SAFE_SETTINGS_PWD,
                            binding.tvLockPwd
                        )
                    }

                }

            }
        }
        return false
    }

    private var hidTipsTask = {
        binding.flTips.visibility = View.GONE
    }

    private fun backPress() {
        if (mPageIndex == 0) {
            returnMainPage()
        } else if (mPageIndex == 1) {
            if (binding.flTips.visibility == View.VISIBLE) {
                hidTips()
                return
            }
            showFirstPage()
        }else if (mPageIndex == 2) {
            if (binding.flTips.visibility == View.VISIBLE) {
                hidTips()
                return
            }
            showFirstPage()
        }
    }

    private fun returnMainPage() {
        val mainActivity = activity as MainActivity
        mainActivity.showFragment(HomeMenu.MENU1)
        SPUtils.getInstance().put(Constants.FRAGMENT_SET, false)
    }

    private fun showFirstPage() {
        mPageIndex = 0
        currentSelectIndex = -1
        binding.llPageFirst.visibility = View.VISIBLE
        binding.llPageSecond.visibility = View.GONE
        binding.llPageThree.visibility = View.GONE
        binding.flClosePwd.visibility = View.GONE
        refreshFirstPageData()
        Choreographer.getInstance().postFrameCallbackDelayed( {
            scrollNextItem()
        },50)
    }

    private fun refreshFirstPageData() {
        binding.tvDeviceSerialNumber.text = App.serialNumber + "/" + BuildConfig.VERSION_NAME
        val facePassPaySwitch =
            SPUtils.getInstance().getBoolean(Constants.SWITCH_FACE_PASS_PAY, false)
        binding.ivSwitchFacePass.isSelected = facePassPaySwitch
        binding.ivSwitchFacePass.isSelected =  SPUtils.getInstance().getBoolean(Constants.SWITCH_FACE_PASS_PAY, false)
        binding.ivSwitchTongLianPay.isSelected = SPUtils.getInstance().getBoolean(Constants.SWITCH_TONG_LIAN_PAY)

        if (facePassHelper == null) {
            facePassHelper = FacePassHelper(activity as MainActivity);
        }

        refreshFacePassTotalCount(facePassHelper!!.faceCount)
    }

    private fun showSecondPage() {
        try{
        mPageIndex = 1
        currentSelectIndex = -1
        binding.llPageFirst.visibility = View.GONE
        binding.llPageSecond.visibility = View.VISIBLE
            binding.flClosePwd.visibility = View.GONE
        refreshSecondPageData()
        Choreographer.getInstance().postFrameCallbackDelayed( {
            scrollNextItem()
        },50)
    } catch (e: Throwable) {
        e.printStackTrace()
    }
    }

    private fun refreshSecondPageData() {
        //时段信息
        refreshIntervalCardType()

        //早餐
        binding.tvBreakfastAmount.text =
            SPUtils.getInstance().getString(Constants.BREAKFAST_AMOUNT, "") //早餐金额

        //午餐
        binding.tvLunchAmount.text =
            SPUtils.getInstance().getString(Constants.LUNCH_AMOUNT, "") //午餐金额

        //晚餐
        binding.tvDinnerAmount.text =
            SPUtils.getInstance().getString(Constants.DINNER_AMOUNT, "") //晚餐金额
    }

    private fun showThreePage() {
        try{

            mPageIndex = 2
            currentSelectIndex = -1
            binding.llPageFirst.visibility = View.GONE
            binding.llPageSecond.visibility = View.GONE
            binding.llPageThree.visibility = View.VISIBLE
            binding.flClosePwd.visibility = View.GONE
            refreshThreePageData()
            Choreographer.getInstance().postFrameCallbackDelayed( {
                scrollNextItem()
            },50)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }


    private fun refreshThreePageData() {
        val safeSettingsSwitch =
            SPUtils.getInstance().getBoolean(Constants.SWITCH_SAFE_SETTINGS, false)
        binding.ivSwitchSafeSettings.isSelected = safeSettingsSwitch
        binding.tvLockTime.text = SPUtils.getInstance().getString(Constants.SAFE_SETTINGS_TIME,"30")
        binding.tvLockPwd.text = SPUtils.getInstance().getString(Constants.SAFE_SETTINGS_PWD)

    }

    private fun showTips(tips: String) {
        Log.d(TAG, "limeshowTips: " + 1047)
        binding.flTips.removeCallbacks(hidTipsTask)
        binding.flTips.visibility = View.VISIBLE
        binding.tvTips.text = tips
        binding.flTips.postDelayed(hidTipsTask, 1500)
    }

    private fun hidTips() {
        binding.flTips.removeCallbacks(hidTipsTask)
        binding.flTips.visibility = View.GONE
    }

    private fun handleInputAmountNumber(
        insetNumber: String,
        amountTextView: TextView
    ) {
        val text = amountTextView.text.toString()
        if (TextUtils.isEmpty(text)) {
            if (insetNumber == ".") {
                amountTextView.text = "0."
            } else {
                amountTextView.text = insetNumber
            }
        } else {
            //判断数字长度不能超多
            val pointIndex: Int = text.indexOf(".")
            if (pointIndex != -1) {
                //获取小数点位数 最多两位小数
                if (text.length - 1 >= pointIndex + 2) {
                    showTips("最多两位小数")
                    return
                }
            } else {
                if (text.length >= 2 && insetNumber != ".") {
                    showTips("超过最大限值")
                    return
                }
            }
            if (insetNumber == ".") {
                if (!text.contains(".")) {
                    amountTextView.text = "$text."
                }
            } else {
                amountTextView.text = "$text$insetNumber"
            }
        }
    }

    private fun handleInputTimeNumber(
        insetNumber: String,
        amountTextView: TextView
    ) {
        val text = amountTextView.text.toString()
        if (TextUtils.isEmpty(text)) {
            if (insetNumber == ".") {
                //amountTextView.text = "0."
            } else {
                amountTextView.text = insetNumber
            }
        } else {
            //判断数字长度不能超多
            val pointIndex: Int = text.indexOf(".")
            if (pointIndex != -1) {
                //获取小数点位数 最多两位小数
                if (text.length - 1 >= pointIndex + 2) {
                    showTips("最多两位小数")
                    return
                }
            } else {
                if (text.length >= 6 && insetNumber != ".") {
                    showTips("密码最多6位")
                    return
                }
            }
            if (insetNumber == ".") {
//                if (!text.contains(".")) {
//                    amountTextView.text = "$text."
//                }
            } else {
                amountTextView.text = "$text$insetNumber"
            }
        }
    }

    private fun handleInputLockTimeNumber(
        insetNumber: String,
        amountTextView: TextView
    ) {
        val text = amountTextView.text.toString()
        if (TextUtils.isEmpty(text)) {
            if (insetNumber == "." || insetNumber == "0") {
                //amountTextView.text = "0."
            } else {
                amountTextView.text = insetNumber
            }
        } else {
            //判断数字长度不能超多
            val pointIndex: Int = text.indexOf(".")
            if (pointIndex != -1) {
                //获取小数点位数 最多两位小数
                if (text.length - 1 >= pointIndex + 2) {
                    showTips("最多两位小数")
                    return
                }
            } else {

                if ("$text$insetNumber".toFloat() > 999.0) {
                    showTips("不能超过最大值999")
                    return
                }
            }
            if (insetNumber == ".") {
//                if (!text.contains(".")) {
//                    amountTextView.text = "$text."
//                }
            } else {
                amountTextView.text = "$text$insetNumber"
            }
        }
    }

    private fun handleInputNumber(insetNumber: String) {
         if (mPageIndex == 1) {
             when (currentSelectIndex) {

                 //早餐金额
                 3 -> {
                     handleInputAmountNumber(
                         insetNumber,
                         binding.tvBreakfastAmount
                     )
                 }

                 //午餐金额
                 4 -> {
                     handleInputAmountNumber(
                         insetNumber,
                         binding.tvLunchAmount
                     )
                 }

                 //晚餐金额
                 5 -> {
                     handleInputAmountNumber(
                         insetNumber,
                         binding.tvDinnerAmount
                     )
                 }
             }

         }else if (mPageIndex == 2) {
             Log.d(TAG,"limecurrentSelectIndex1125 1125 + currentSelectIndex: " + currentSelectIndex)
             Log.i(TAG,"limecurrentSelectIndex1125 1125 + insetNumber: " + insetNumber)
             if (binding.flClosePwd.isVisible){
                 handleInputTimeNumber(
                     insetNumber,
                     binding.tvInputPwdClose
                 )
                 return
             }
             when (currentSelectIndex) {

                 //锁屏时间
                 1 -> {
                     handleInputLockTimeNumber(
                         insetNumber,
                         binding.tvLockTime
                     )
                 }

                 //锁屏密码
                 2 -> {
                     if (TextUtils.isEmpty(SPUtils.getInstance().getString(Constants.SAFE_SETTINGS_PWD))) {
                         handleInputTimeNumber(
                             insetNumber,
                             binding.tvLockPwd
                         )
                     }
                 }


             }
         }
    }

    private fun handleCanteenTimeSetting() {
        when (currentSelectIndex) {
            //早餐开关
            0 -> {
                if (binding.llBreakfastSetting.visibility == View.VISIBLE) {
                    binding.llBreakfastSetting.visibility = View.GONE
                    binding.ivSwitchBreakfast.setImageResource(0)
                } else {
                    binding.llBreakfastSetting.visibility = View.VISIBLE
                    binding.ivSwitchBreakfast.setImageResource(R.mipmap.icon_check_selected)
                }
            }

            //午餐开关
            1 -> {
                if (binding.llLunchSetting.visibility == View.VISIBLE) {
                    binding.llLunchSetting.visibility = View.GONE
                    binding.ivSwitchLunch.setImageResource(0)
                } else {
                    binding.llLunchSetting.visibility = View.VISIBLE
                    binding.ivSwitchLunch.setImageResource(R.mipmap.icon_check_selected)
                }
            }

            //晚餐开关
            2 -> {
                if (binding.llDinnerSetting.visibility == View.VISIBLE) {
                    binding.llDinnerSetting.visibility = View.GONE
                    binding.ivSwitchDinner.setImageResource(0)
                } else {
                    binding.llDinnerSetting.visibility = View.VISIBLE
                    binding.ivSwitchDinner.setImageResource(R.mipmap.icon_check_selected)
                }
            }
        }
    }

    override fun onClick(v: View?) {

    }

}