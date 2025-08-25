package com.stkj.cashier.utils;

import android.util.Log;

/**
 * Copyright (C), 2015-2025, 洛阳盛图科技有限公司
 * Author: Lime
 * Date: 2025/8/25 9:59
 * Description: 防止重复点击
 */
public class RepeatClickUtils {
    private static long lastClickTime = 0;
    private static long DIFF = 100;


    /**
     * 判断两次点击的间隔，如果小于100，则认为是多次无效点击
     *
     * @return
     */
    public static boolean isFastDoubleClick() {
        return isFastDoubleClick(DIFF);
    }

    /**
     * 判断两次点击的间隔，如果小于diff，则认为是多次无效点击
     *
     * @param diff
     * @return
     */
    public static boolean isFastDoubleClick( long diff) {
        if ((System.currentTimeMillis() - lastClickTime) < diff) {
            Log.v("onClick", "isFastDoubleClick短时间内按钮多次触发");
            return true;
        }
        lastClickTime = System.currentTimeMillis();
        return false;
    }
}
