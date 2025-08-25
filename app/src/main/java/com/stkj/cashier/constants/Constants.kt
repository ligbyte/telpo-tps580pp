package com.stkj.cashier.constants

/**
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 */
object Constants {

    /**
     * 是否支持动态切换接口地址
     */
    const val isDomain = false

    // 新安县私有化部署
    const val BASE_OFFICIAL_URL = "https://xaapi.shengtudx.cn/"

    // 中医药学校正式
    //const val BASE_OFFICIAL_URL = "https://restaurant.shengtudx.cn/"
    // 汝阳正式
    //const val BASE_OFFICIAL_URL = "http://101.42.54.44:9003"
    // 万基正式
    //const val BASE_OFFICIAL_URL = "https://cater.wanjigroup.com:9997"

    // 测试
    //  const val BASE_OFFICIAL_URL = "http://101.43.252.67:9003/"
    // 本地
    // const val BASE_OFFICIAL_URL = "http://192.168.2.197:9003/"

    // TODO Bugly 申请的 AppId
    const val BUGLY_APP_ID = "9d0b428b32"

    const val TAG = "Jenly"

    const val PAGE_SIZE = 20

    const val DOUBLE_CLICK_EXIT_TIME = 2500
    const val DEFAULT_PASS_WORD = "666666"

    //---------------------------------------------

    const val VERIFY_CODE_COUNT_DOWN_DURATION = 60_000L
    const val VERIFY_CODE_COUNT_DOWN_INTERVAL = 1_000L

    //---------------------------------------------

    const val KEY_INTERVALCARDTYPE = "key_intervalcardtype"

    const val KEY_TENANT_ID = "key_tenant_id"

    const val KEY_TITLE = "key_title"

    const val KEY_URL = "key_url"

    const val KEY_TOKEN = "key_token"

    const val KEY_USERNAME = "key_username"
    const val KEY_DEVICE_NAME = "key_device_name"

    const val KEY_BEAN = "key_bean"

    const val KEY_ID = "key_id"
    const val KEY_IMAGE_URL = "key_image_url"

    const val KEY_LIST = "key_list"

    const val KEY_TYPE = "key_type"

    const val KEY_TIPS = "key_tips"

    const val KEY_MAX = "key_max"

    const val KEY_CONTENT = "key_content"

    const val KEY_CLEAR_TASK = "key_clear_task"

    const val KEY_ARRAY = "key_array"

    const val KEY_SHOW_TOOLBAR = "key_show_toolbar"

    const val KEY_SHOW_BACK = "key_show_back"

    const val KEY_TEXT = "key_text"

    const val OPERATION_PLUS = "PLUS"
    const val OPERATION_SUB = "SUB"
    const val MODE_VALUE = "MODE_VALUE"
    const val AUTH_VALUE = "AUTH_VALUE"
    const val SWITCH_STATICS = "SWITCH_STATICS"
    const val SWITCH_CONSUMPTION_DIALOG = "SWITCH_CONSUMPTION_DIALOG"
    const val PASS_WORD = "PASS_WORD"
    const val GROUP_NAME = "face_pass_1"
    const val SCREEN_OFF_TIMEOUT = "screenOffTimeout"
    const val VOLUME_SPEECH = "volumeSpeech"
    const val PICK_UP_SUCCESS = "pickUpSuccess"
    const val CONSUMPTION_SUCCESS = "consumptionSuccess"
    const val SWITCH_PAY = "SWITCH_PAY" //消费后自动清零
    const val SWITCH_FACE_PASS_PAY = "switch_face_pass_pay" //刷脸消费
    const val SWITCH_TONG_LIAN_PAY = "switch_tong_lian_pay" //通联支付
    const val SWITCH_OFFLINE_PAY = "switch_offline_pay" //离线支付
    const val SWITCH_FIX_AMOUNT = "SWITCH_FIX_AMOUNT" //定额模式开关
    const val SWITCH_SAFE_SETTINGS = "switch_safe_settings" //安全设置
    const val SAFE_SETTINGS_PWD = "safe_settings_pwd" //安全设置密码
    const val SAFE_SETTINGS_TIME = "safe_settings_time" //安全设置时间
    const val CURRENT_FIX_AMOUNT_TIME = "CURRENT_FIX_AMOUNT_TIME" //当前定额模式时间段

    const val MANAGER_TEL = "manager_tel"


    const val BREAKFAST_SWITCH = "BREAKFAST_SWITCH" //早餐开关
    const val BREAKFAST_AMOUNT = "BREAKFAST_AMOUNT" //早餐金额

    const val LUNCH_SWITCH = "LUNCH_SWITCH" //午餐开关
    const val LUNCH_AMOUNT = "LUNCH_AMOUNT" //午餐金额

    const val DINNER_SWITCH = "DINNER_SWITCH" //晚餐开关
    const val DINNER_AMOUNT = "DINNER_AMOUNT" //晚餐金额

    const val YECAN_SWITCH = "YECAN_SWITCH" //夜餐开关
    const val YECAN_AMOUNT = "YECAN_AMOUNT" //夜餐金额
    /*
    人脸识别设置
     */
    const val FACE_SEARCH_THRESHOLD = "searchThreshold"
    const val FACE_LIVENESS_GA_THRESHOLD = "livenessGaThreshold"
    const val FACE_LIVENESS_GA_THRESHOLD_ENABLED = "livenessGaThresholdEnabled"
    const val FACE_LIVENESS_THRESHOLD = "livenessThreshold"
    const val FACE_LIVENESS_ENABLED = "livenessEnabled"
    const val FACE_RCATTRIBUTEANDOCCLUSIONMODE = "rcAttributeAndOcclusionMode"
    const val FACE_MIN_THRESHOLD = "faceMinThreshold"
    const val FACE_ADDRESS = "faceAddress"
    const val FACE_HEAD_BEAT = "faceHeadBeat"
    const val FACE_SUCCESS_TIME = "faceSuccessTime"
    const val FACE_MEMBER_NUMBER = "faceMemberNumber"

    const val MEMORY_UNIT_PRICE = "memoryUnitPrice"

    //消费模式设置页面
    const val FRAGMENT_SET = "ConsumptionSettingFragment"

    const val ShowFourPage = "showfourpage"

    //消费模式设置页面
    const val FRAGMENT_AMOUNT = "AmountFragment"
    //---------------------------------------------
}