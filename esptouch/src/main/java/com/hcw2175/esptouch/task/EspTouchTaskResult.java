/*
 * Copyright (c) 2017 - 小哈伙伴
 * All rights reserved.
 *
 * Created on 2017-01-12
 */
package com.hcw2175.esptouch.task;

import java.net.InetAddress;

/**
 * EspTouch任务处理结果
 *
 * @author huchiwei
 * @version 1.0.0
 */
public class EspTouchTaskResult{

    private boolean isSuccess;             // 结果是否成功
    private String message;                // 提示消息

    private String bssid;                  // 设备绑定mac地址
    private InetAddress inetAddress;       // 设备绑定的IP地址

    private String esptouchResult;         // 原始结果
    private int esptouchOffset;            // 结果开始截取位置
    private int esptouchLen;               // 结果所需要长度

    // ==================================================================
    // constructor ======================================================
    public EspTouchTaskResult(){}

    /**
     * EspTouch任务处理结果构造器
     *
     * @param inetAddress 设备的地址信息
     * @param isSuccess   是否成功
     * @param bssid       mac地址
     */
    public EspTouchTaskResult(InetAddress inetAddress, boolean isSuccess, String bssid) {
        this.isSuccess = isSuccess;
        this.bssid = bssid;
        this.inetAddress = inetAddress;
    }

    // ==================================================================
    // setter/getter ====================================================
    public boolean isSuccess() {
        return isSuccess;
    }
    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public String getBssid() {
        return bssid;
    }
    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }
    public void setInetAddress(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }

    public String getEsptouchResult() {
        return esptouchResult;
    }

    public void setEsptouchResult(String esptouchResult) {
        this.esptouchResult = esptouchResult;
    }

    public int getEsptouchOffset() {
        return esptouchOffset;
    }

    public void setEsptouchOffset(int esptouchOffset) {
        this.esptouchOffset = esptouchOffset;
    }

    public int getEsptouchLen() {
        return esptouchLen;
    }

    public void setEsptouchLen(int esptouchLen) {
        this.esptouchLen = esptouchLen;
    }
}
