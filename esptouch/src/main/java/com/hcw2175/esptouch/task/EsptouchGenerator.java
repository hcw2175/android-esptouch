/*
 * Copyright (c) 2016 - 小哈伙伴
 * All rights reserved.
 *
 * Created on 2017-01-17
 */
package com.hcw2175.esptouch.task;

import com.hcw2175.esptouch.codetransfer.DatumCodeTransfer;
import com.hcw2175.esptouch.codetransfer.GuideCodeTransfer;
import com.hcw2175.esptouch.util.ByteUtil;

import java.net.InetAddress;

/**
 * esptouch协议请求数据转换生成器
 *
 * @author hucw
 * @since 1.0.0
 */
public class EsptouchGenerator{

    private final byte[][] mGcBytes2;
    private final byte[][] mDcBytes2;

    /**
     * esptouch协议数据转换生成器构造器
     *
     * @param inetAddress  本地网络地址信息
     * @param ssid         WIFI名称
     * @param bssid        WIFI接入口mac地址
     * @param pwd          WIFI密码
     * @param isHidden     WIFI是否隐藏
     */
    public EsptouchGenerator(InetAddress inetAddress, String ssid, String bssid, String pwd, boolean isHidden) {

        GuideCodeTransfer gc = new GuideCodeTransfer();
        char[] gcU81 = gc.getU8s();
        mGcBytes2 = new byte[gcU81.length][];

        for (int i = 0; i < mGcBytes2.length; i++) {
            mGcBytes2[i] = ByteUtil.genSpecBytes(gcU81[i]);
        }

        // generate data code
        DatumCodeTransfer dc = new DatumCodeTransfer(ssid, bssid, pwd, inetAddress, isHidden);
        char[] dcU81 = dc.getU8s();
        mDcBytes2 = new byte[dcU81.length][];

        for (int i = 0; i < mDcBytes2.length; i++) {
            mDcBytes2[i] = ByteUtil.genSpecBytes(dcU81[i]);
        }
    }

    public byte[][] getGCBytes2() {
        return mGcBytes2;
    }

    public byte[][] getDCBytes2() {
        return mDcBytes2;
    }

}
