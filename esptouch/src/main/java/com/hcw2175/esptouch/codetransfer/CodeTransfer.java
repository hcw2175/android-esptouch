/*
 * Copyright (c) 2016 - 小哈伙伴
 * All rights reserved.
 *
 * Created on 2017-01-17
 */
package com.hcw2175.esptouch.codetransfer;

/**
 * UDP协议代码指令转换接口
 *
 * @author afunx
 * @author hucw
 * @since 1.0.0
 */
public interface CodeTransfer {

	/**
	 * 获取要转换的byte[]数据
	 *
	 * @return 需要转换的数据
	 */
	byte[] getBytes();

	/**
	 * 获取需要转换的chat[](u8[])数据
	 * 
	 * @return 需要转换的chat[](u8[])数据
	 */
	char[] getU8s();
}
