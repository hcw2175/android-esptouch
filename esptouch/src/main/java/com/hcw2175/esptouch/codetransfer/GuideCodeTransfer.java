/*
 * Copyright (c) 2016 - 小哈伙伴
 * All rights reserved.
 *
 * Created on 2017-01-17
 */
package com.hcw2175.esptouch.codetransfer;

import com.hcw2175.esptouch.util.ByteUtil;

/**
 * EspTouch前导码转换器
 *
 * @author hucw
 * @since 1.0.0
 */
public class GuideCodeTransfer implements CodeTransfer {

    public static final int GUIDE_CODE_LEN = 4;

    @Override
    public byte[] getBytes() {
        throw new RuntimeException("GuideCodeTransfer不支持getBytes()方法");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        char[] dataU8s = getU8s();
        for (int i = 0; i < GUIDE_CODE_LEN; i++) {
            String hexString = ByteUtil.convertU8ToHexString(dataU8s[i]);
            sb.append("0x");
            if (hexString.length() == 1) {
                sb.append("0");
            }
            sb.append(hexString).append(" ");
        }
        return sb.toString();
    }

    @Override
    public char[] getU8s() {
        // 前导码固定
        char[] guidesU8s = new char[GUIDE_CODE_LEN];
        guidesU8s[0] = 515;
        guidesU8s[1] = 514;
        guidesU8s[2] = 513;
        guidesU8s[3] = 512;
        return guidesU8s;
    }
}
