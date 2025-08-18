package com.stkj.cashier.common.utils;

import java.util.Base64;

public class Base64Utils {

    public static String encode(String originalData) {
        return  Base64.getEncoder().encodeToString(originalData.getBytes());
    }


    public static String decode(String encodedData) {
        return new String(Base64.getDecoder().decode(encodedData));
    }

}
