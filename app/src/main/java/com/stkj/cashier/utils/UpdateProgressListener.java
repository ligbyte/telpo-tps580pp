package com.stkj.cashier.utils;

public interface UpdateProgressListener {
    /**
     * download start
     */
    void start();

    /**
     * update download progress
     * @param progress
     */
    void update(int progress, int currentSize, int totalSize);

    /**
     * download success
     */
    void success();

    /**
     * download error
     */
    void error();
}