/*
 * Copyright (c) 2017 - 小哈伙伴
 * All rights reserved.
 *
 * Created on 2017-01-12
 */
package com.hcw2175.esptouch.task;

/**
 * EspTouch Socket 数据请求/发送处理
 *
 * @author huchiwei
 * @version 1.0.0
 */
public class EsptouchRequestParameters {

    private long intervalGuideCodeMillisecond;                                 // 前导码发送间隔
    private long intervalDataCodeMillisecond;                                  // 基准码发送间隔
    private long timeoutGuideCodeMillisecond;                                  // 前导码超时时间
    private long timeoutDataCodeMillisecond;                                   // 基准码超时时间

    private int totalRepeatTime;                                               // 全部重复测试
    private int esptouchResultOneLen;                                          // EspTouch接收结果长度
    private int esptouchResultMacLen;                                          // EspTouch接收Mac结果长度
    private int esptouchResultIpLen;                                           // EspTouch接收IP结果长度
    private int esptouchResultTotalLen;                                        // EspTouch接收全部结果长度
    private int portListening;                                                 // EspTouch接收结果端口

    private int targetPort;                                                    // 设备目标端口
    private int waitUdpReceivingMilliseond;                                    // 等待接收时间
    private int waitUdpSendingMillisecond;                                     // 等待发送时间
    private int thresholdSucBroadcastCount;                                    // 正确接收广播次数
    private int expectTaskResultCount;

    private int dataGramCount = 0;                                              // 用于计算目标IP地址

    public EsptouchRequestParameters() {
        intervalGuideCodeMillisecond = 8;
        intervalDataCodeMillisecond = 8;
        timeoutGuideCodeMillisecond = 2000;
        timeoutDataCodeMillisecond = 4000;

        totalRepeatTime = 1;
        esptouchResultOneLen = 1;
        esptouchResultMacLen = 6;
        esptouchResultIpLen = 4;
        esptouchResultTotalLen = 1 + 6 + 4;
        portListening = 18266;

        targetPort = 7001;
        waitUdpReceivingMilliseond = 15000;
        waitUdpSendingMillisecond = 48000;
        thresholdSucBroadcastCount = 1;
        expectTaskResultCount = 1;
    }

    // ==================================================================
    // methods ==========================================================
    public int getWaitUdpTotalMillisecond() {
        return waitUdpReceivingMilliseond + waitUdpSendingMillisecond;
    }

    public long getTimeoutTotalCodeMillisecond() {
        return timeoutGuideCodeMillisecond + timeoutDataCodeMillisecond;
    }

    // target hostname is : 234.1.1.1, 234.2.2.2, 234.3.3.3 to 234.100.100.100
    public String getTargetHostname() {
        return "255.255.255.255";
        /*int count = 1 + ( dataGramCount++ ) % 100;
        return "234." + count + "." + count + "." + count;*/
    }


    // ==================================================================
    // setter/getter ====================================================
    public long getIntervalGuideCodeMillisecond() {
        return intervalGuideCodeMillisecond;
    }

    public long getIntervalDataCodeMillisecond() {
        return intervalDataCodeMillisecond;
    }

    public long getTimeoutGuideCodeMillisecond() {
        return timeoutGuideCodeMillisecond;
    }

    public long getTimeoutDataCodeMillisecond() {
        return timeoutDataCodeMillisecond;
    }

    public int getTotalRepeatTime() {
        return totalRepeatTime;
    }

    public int getEsptouchResultOneLen() {
        return esptouchResultOneLen;
    }

    public int getEsptouchResultMacLen() {
        return esptouchResultMacLen;
    }

    public int getEsptouchResultIpLen() {
        return esptouchResultIpLen;
    }

    public int getEsptouchResultTotalLen() {
        return esptouchResultTotalLen;
    }

    public int getPortListening() {
        return portListening;
    }

    public int getTargetPort() {
        return targetPort;
    }

    public int getWaitUdpReceivingMilliseond() {
        return waitUdpReceivingMilliseond;
    }

    public int getWaitUdpSendingMillisecond() {
        return waitUdpSendingMillisecond;
    }

    public int getThresholdSucBroadcastCount() {
        return thresholdSucBroadcastCount;
    }

    public int getExpectTaskResultCount() {
        return expectTaskResultCount;
    }

    public int getDataGramCount() {
        return dataGramCount;
    }
}
