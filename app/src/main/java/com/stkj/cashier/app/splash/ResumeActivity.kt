package com.stkj.cashier.app.splash

import android.os.Bundle
import com.stkj.cashier.R
import com.stkj.cashier.app.base.BaseActivity
import com.stkj.cashier.bean.MessageEventBean
import com.stkj.cashier.config.MessageEventType
import com.stkj.cashier.databinding.ResumeActivityBinding
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus

/**
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 */
@AndroidEntryPoint
class ResumeActivity : BaseActivity<SplashViewModel, ResumeActivityBinding>() {


    override fun initData(savedInstanceState: Bundle?) {
        super.initData(savedInstanceState)
        binding.rootView.setOnClickListener {
            finish()
            EventBus.getDefault().post(MessageEventBean(MessageEventType.ScreenOffTimeout))
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.resume_activity
    }


    private fun startActivity() {
//        if (SPStaticUtils.getString(SPKey.KEY_TOKEN).isNullOrBlank()){
//            startActivity(LoginActivity::class.java);
//        }else{
        startMainActivity()
//        }

        finish()
    }


}