/*
 * Copyright (c) 2016 - 小哈伙伴
 * All rights reserved.
 *
 * Created on 2017-01-17
 */
package com.hcw2175.esptouch.socket;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.hcw2175.esptouch.util.ByteUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;

/**
 * UDP数据接收服务端
 *
 * @author hucw
 */
public class UDPSocketServer {
    public static final String TAG = "UDPSocketServer";

    public static final String WIFI_LOCK_TAG = "IotAppLock";

    private DatagramSocket serverSocket;
	private WifiManager.MulticastLock multicastLock;

    // ==================================================================
    // constructor ======================================================
    public UDPSocketServer(Context context, int port, int socketTimeout) {
        try {
            this.serverSocket = new DatagramSocket(port);
            this.serverSocket.setSoTimeout(socketTimeout);

            // 获取组播锁，Android应用接收组播信息
            WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            multicastLock = manager.createMulticastLock(WIFI_LOCK_TAG);

            Log.d(TAG, "UDPSocketServer: UDP数据接收服务端实例构建，端口号：" + port + ", socket timeout: " + socketTimeout);
        } catch (IOException e) {
            Log.e(TAG, "UDPSocketServer初始化失败", e);
            e.printStackTrace();
        }
    }

    // ==================================================================
    // methods ==========================================================
    /**
     * 设置DatagramSocket超时时间（毫秒）
     *
     * @param timeout 超时时间（毫秒），0表示无超时
     * @return 返回true表示设置超时成功，否则失败。
     */
    public boolean setSoTimeout(int timeout) {
        try {
            if(null != this.serverSocket)
                this.serverSocket.setSoTimeout(timeout);
            return true;
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 中断UDP数据接收
     */
    public synchronized void interrupt() {
        if (null != serverSocket && !serverSocket.isClosed()) {
            serverSocket.close();
            releaseLock();

            Log.d(TAG, "UDP Socket Server 已关闭");
        }
    }

    /**
     * 从指定的端口接收特定长度的的byte数据
     *
     * 21,24,-2,52,-102,-93,-60
     * 15,18,fe,34,9a,a3,c4
     * @return byte数据
     */
    public byte[] receiveSpecLenBytes(int len) {
        try {
            byte[] buffer = new byte[64];
            DatagramPacket receivePacket = new DatagramPacket(buffer, 64);

            acquireLock();
            serverSocket.receive(receivePacket);

            byte[] receiverData = Arrays.copyOf(receivePacket.getData(), receivePacket.getLength());
            Log.i(TAG, "接收到数据：" + ByteUtil.getHexs(receiverData));

            if (receiverData.length != len) {
                Log.d(TAG, "注意：接收到的数据长度不是指定的数据长度，返回null");
                return null;
            }

            //return receiverData;
            return receiverData;
        } catch (SocketTimeoutException e){
            Log.e(TAG, "Socket Timeout: 数据接收超时", e);
        } catch (IOException ie) {
            Log.e(TAG, "Socket Exception: 数据接收异常", ie);
        }
        return null;
    }

    // ==================================================================
    // private methods ==================================================
	private synchronized void acquireLock() {
		if (multicastLock != null && !multicastLock.isHeld()) {
			multicastLock.acquire();
		}
	}

	private synchronized void releaseLock() {
		if (multicastLock != null && multicastLock.isHeld()) {
			try {
				multicastLock.release();
			} catch (Throwable th) {
                // ignoring this exception, probably wakeLock was already released
            }
		}
	}
}
