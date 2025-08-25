package com.stkj.cashier.utils;

import java.util.Random;

public class RandomStringGenerator {

    // 定义字符池，包括大小写字母和数字
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    // 随机数生成器
    private static final Random RANDOM = new Random();

    /**
     * 随机生成指定长度的字符串
     *
     * @param length 字符串长度
     * @return 随机生成的字符串
     */
    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            // 随机选择一个字符
            int index = RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }

    /**
     * 随机生成10位字符串
     *
     * @return 随机生成的10位字符串
     */
    public static String generateTenDigitString() {
        return generateRandomString(10);
    }

    public static void main(String[] args) {
        // 测试生成10位随机字符串
        String randomString = generateTenDigitString();
        System.out.println("生成的10位随机字符串: " + randomString);
    }
}
