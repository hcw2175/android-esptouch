/*
 * Copyright (c) 2016 - 小哈伙伴
 * All rights reserved.
 *
 * Created on 2017-01-17
 */
package com.hcw2175.esptouch.socket;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * UDP数据发送客户端
 * 
 * @author afunx
 * @author hucw
 */
public class UDPSocketClient {
    public static final String TAG = "UDPSocketClient";

	private DatagramSocket client;                            // datagram socket 实例

	private volatile boolean isStop;                          // datagram socket是否已停止
	private volatile boolean isClosed;                        // datagram socket是否已关闭

    // ==================================================================
    // constructor ======================================================
    public UDPSocketClient(){
        try {
            this.client = new DatagramSocket();

            this.isStop = false;
            this.isClosed = false;
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    // ==================================================================
    // methods ==========================================================
	@Override
	protected void finalize() throws Throwable {
        super.finalize();
        this.close();
	}

    /**
     * 中断UDP socket
     */
	public void interrupt() {
        this.isStop = true;
        this.close();
	}

    /**
     * 关闭UDP socket
     */
	private synchronized void close() {
		if (!this.isClosed) {
            this.isClosed = true;
            client.close();

            Log.d(TAG, "USP Socket Client 已关闭");
        }
	}

	/**
	 * 发送UDP数据
	 *
     * @param targetHost  设备IP
     * @param targetPort  设备端口
	 * @param data     需要发送的数据
	 * @param interval UDP数据发送间隔（毫秒），即每隔多少毫秒发送一次
	 */
	public void sendData(String targetHost, int targetPort, byte[][] data, long interval) {
		sendData(targetHost, targetPort, data, 0, data.length, interval);
	}
	
	
	/**
	 * 发送UDP数据
	 *
     * @param targetHost  设备IP
     * @param targetPort  设备端口
     * @param data     需要发送的数据
	 * @param offset   起始点
	 * @param count    发送次数
     * @param interval UDP数据发送间隔（毫秒），即每隔多少毫秒发送一次
	 */
	public void sendData(String targetHost, int targetPort, byte[][] data, int offset, int count, long interval) {
		if (data == null || data.length <= 0) {
            Log.e(TAG, "sendData: UDP数据为空，不执行发送");
            return;
		}

        for(int i = offset; !isStop && i < offset+count; i++){
            if (data[i].length == 0) {
                continue;
            }

            try {
                // 执行数据发送
                DatagramPacket localDatagramPacket = new DatagramPacket(data[i], data[i].length, InetAddress.getByName(targetHost), targetPort);
                client.send(localDatagramPacket);
            } catch (UnknownHostException e) {
                Log.e(TAG, "DatagramSocket Client host is unknown: 目标IP地址未知, 中断UDP数据发送", e);
                isStop = true;
                break;
            } catch (IOException e) {
                // for the Ap will make some troubles when the phone send too many UDP packets,
                // but we don't expect the UDP packet received by others, so just ignore it
                // LogUtil.w("注意：UDP数据发送发生IOException异常，但可忽略");
            }

            try {
                // 线程休眠后再次执行发送
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
                isStop = true;
                break;
            }
        }

        if (isStop) {
            close();
        }
	}
}
