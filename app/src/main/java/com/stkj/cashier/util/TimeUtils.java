package com.stkj.cashier.util;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.stkj.cashier.util.util.LogUtils;

import java.time.LocalTime;

public class TimeUtils {

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static boolean isCurrentTimeIsInRound(String start, String end){
        LocalTime beginTime = null;
        LocalTime endTime = null;

        try {
            beginTime = LocalTime.parse(start);
            endTime = LocalTime.parse(end);
        } catch (Throwable e) {
            Log.e("TAG", "checkCurrentAmountMode 21: " + e.getMessage());
        }
        if (beginTime == null || endTime == null) {
            return false;
        }
        LogUtils.e("checkCurrentAmountMode RefreshFixAmountMode split[0]: " + 28);
        LocalTime nowTime = LocalTime.now();

        return nowTime.isAfter(beginTime) && nowTime.isBefore(endTime);
    }

}
