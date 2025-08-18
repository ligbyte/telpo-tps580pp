package com.stkj.cashier.bean

import com.google.gson.annotations.SerializedName

class QueryBalanceBean {
    //{"Code":10000,"Message":"成功","Data":{"consumption_Mone":0,"full_name":"郭宇雷","balance":6544.34,"bill_count":0,"customerNo":"1992539284"}}
    @SerializedName("balance")
    var balance: String? = null

    @SerializedName("consumption_Mone")
    var consumptionMone = 0.0

    @SerializedName("full_name")
    var fullName: String? = null

    @SerializedName("bill_count")
    var billCount: String? = null

    @SerializedName("customerNo")
    var customerNo: String? = null

    @SerializedName("payNo")
    var payNo: String? = null
}