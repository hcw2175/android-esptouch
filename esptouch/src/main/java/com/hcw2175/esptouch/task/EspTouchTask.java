/*
 * Copyright (c) 2017 - 小哈伙伴
 * All rights reserved.
 *
 * Created on 2017-01-12
 */
package com.hcw2175.esptouch.task;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.hcw2175.esptouch.socket.UDPSocketClient;
import com.hcw2175.esptouch.socket.UDPSocketServer;
import com.hcw2175.esptouch.util.ByteUtil;
import com.hcw2175.esptouch.util.EspNetUtil;

import java.net.InetAddress;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * EspTouch处理任务
 *
 * @author huchiwei
 * @version 1.0.0
 */
public class EspTouchTask {
    public static final String TAG = "EspTouchTask";

    private static final int ONE_DATA_LEN = 3;

    private final String ssid;                                              // WIFI名称
    private final String bssid;                                             // WIFI接入MAC地址
    private final String pwd;                                               // WIFI密码
    private final boolean isHidden;                                         // WIFI是否隐藏

    private volatile boolean isInterrupted = false;                         // 是否中断
    private volatile boolean isExecuted = false;                            // 是否已开始执行

    private EsptouchRequestParameters requestParameters;                    // 请求参数封装

    private final UDPSocketClient socketClient;                             // UDP数据发送客户端
    private final UDPSocketServer socketServer;                             // UDP数据接收服务

    private OnEspTouchResultListener espTouchResultListener;                // 设置结果监听器

    // ==================================================================
    // constructor ======================================================
    public EspTouchTask(Context context, String ssid, String bssid, String pwd, boolean isHidden) {
        if (TextUtils.isEmpty(ssid)) {
            throw new IllegalArgumentException("the apSsid should be null or empty");
        }

        this.ssid = ssid;
        this.bssid = bssid;
        this.pwd = pwd == null ? "" : pwd;
        this.isHidden = isHidden;

        // 实例化请求参数
        requestParameters = new EsptouchRequestParameters();

        // 实例化UDP发送客户端
        socketClient = new UDPSocketClient();

        // 实例化UDP数据接收服务端
        socketServer = new UDPSocketServer(context, requestParameters.getPortListening(), requestParameters.getWaitUdpTotalMillisecond());
    }


    // ==================================================================
    // methods ==========================================================
    public void setEspTouchResultListener(OnEspTouchResultListener espTouchResultListener) {
        this.espTouchResultListener = espTouchResultListener;
    }


    /**
     * 执行任务
     *
     * @param context android上下文
     * @return UDP设置结果
     * @throws RuntimeException
     */
    public void excute(Context context) throws RuntimeException {
        if (this.isExecuted) {
            Log.d(TAG, "WIFI设置任务已开始执行，禁止重复执行。");
            return;
        }
        this.isExecuted = true;
        this.isInterrupted = false;

        if(null == espTouchResultListener){
            Log.w(TAG, "警告: 请配置OnEspTouchResultListener实例, 否则无法回传配置结果。");
            return;
        }

        // 先开始循环接收设置结果数据任务
        this.receiveUdpData();

        // 然后开始发送数据任务
        this.sendUdpData(context);
    }

    /**
     * 中断任务
     */
    public synchronized void interrupt() {
        if (!isInterrupted) {
            isInterrupted = true;
            socketClient.interrupt();
            socketServer.interrupt();
        }
    }

    // =========================================================================
    // 接收WiFi配置结果 ==========================================================
    private void receiveUdpData() {
        createReceiveUdpDataObservable()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<EspTouchTaskResult>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        isExecuted = false;
                        interrupt();

                        Log.e(TAG, "WiFi设置失败", e);

                        EspTouchTaskResult failResult = new EspTouchTaskResult();
                        failResult.setSuccess(false);
                        failResult.setMessage("抱歉，出现未知异常");
                        espTouchResultListener.onSuccess(failResult);
                    }

                    @Override
                    public void onNext(EspTouchTaskResult espTouchTaskResult) {
                        isExecuted = false;
                        interrupt();

                        espTouchResultListener.onSuccess(espTouchTaskResult);
                    }
                });
    }

    /**
     * 创建UDP Socket数据监听器
     *
     * @return UDP Socket数据异步监听器
     */
    private Observable<EspTouchTaskResult> createReceiveUdpDataObservable() {
        // esptouche请求结果数据长度
        final int esptouchResultTotalLen = requestParameters.getEsptouchResultTotalLen();

        return Observable.create(new Observable.OnSubscribe<EspTouchTaskResult>() {
            @Override
            public void call(Subscriber<? super EspTouchTaskResult> subscriber) {
                Log.d(TAG, "正在监听WiFi配置结果...");

                EspTouchTaskResult esptouchResult = new EspTouchTaskResult();

                // 计算接收数据的长度
                byte[] apSsidAndPassword = ByteUtil.getBytesByString(ssid + pwd);
                byte expectOneByte = (byte) (apSsidAndPassword.length + 9);
                Log.d(TAG, "expectOneByte：" + expectOneByte);

                byte receiveOneByte = -1;
                byte[] receiveBytes = null;
                long startTimestamp = System.currentTimeMillis();

                // 若成功接收次数小于1则继续
                int correctBroadcastCount = 0;
                while (correctBroadcastCount < requestParameters.getThresholdSucBroadcastCount() && !isInterrupted) {
                    // 接收UDP数据
                    receiveBytes = socketServer.receiveSpecLenBytes(esptouchResultTotalLen);
                    if (receiveBytes != null) {
                        receiveOneByte = receiveBytes[0];
                    } else {
                        receiveOneByte = -1;
                    }
                    Log.d(TAG, "receiveOneByte：" + expectOneByte);

                    // 判断是否超时
                    // 任务执行的时间 = 等待UDP返回数据总时间(发送时间+接收时间) - 设置结束时间
                    long costTime = System.currentTimeMillis() - startTimestamp;
                    int timeout = (int) (requestParameters.getWaitUdpTotalMillisecond() - costTime);
                    if (timeout < 0) {
                        esptouchResult.setSuccess(false);
                        esptouchResult.setMessage("等待超时，未接收到WIFI设置结果。");
                        Log.d(TAG, "接收数据时长超时，WiFi配置失败。");
                        break;
                    }

                    // WIFI设置结果返回
                    if (receiveOneByte == expectOneByte) {
                        correctBroadcastCount++;

                        //LogUtil.i("监听服务新超时：" + timeout + "毫秒");
                        socketServer.setSoTimeout(timeout);

                        if (correctBroadcastCount == requestParameters.getThresholdSucBroadcastCount()) {
                            // 解析设备已绑定的mac地址
                            String bssid = ByteUtil.parseBssid(receiveBytes, requestParameters.getEsptouchResultOneLen(), requestParameters.getEsptouchResultMacLen());
                            Log.d(TAG, "配网成功，解析获取到的MAC地址是：" + bssid);

                            InetAddress inetAddress = EspNetUtil.parseInetAddr(receiveBytes,
                                    requestParameters.getEsptouchResultOneLen() + requestParameters.getEsptouchResultMacLen(),
                                    requestParameters.getEsptouchResultIpLen());

                            esptouchResult.setSuccess(true);
                            esptouchResult.setMessage("WIFI设置成功");
                            esptouchResult.setInetAddress(inetAddress);
                            esptouchResult.setBssid(bssid);
                        }
                    }else{
                        Log.w(TAG, "垃圾数据，忽略掉：" + ByteUtil.getHexs(receiveBytes));
                    }
                }

                if (!esptouchResult.isSuccess() && isInterrupted) {
                    esptouchResult.setSuccess(false);
                    esptouchResult.setMessage("WiFi配置任务中断");
                }

                subscriber.onNext(esptouchResult);
            }
        });
    }


    // =========================================================================
    // 发送WiFi配置 =============================================================
    /**
     * 发送UDP数据数据
     *
     * @param context 上下文
     * @return 返回true表示发送成功，否则失败
     */
    private void sendUdpData(final Context context) {
        createSendUdpDataObservable(context)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "UDP数据发送失败", e);
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if(!isInterrupted)
                            Log.d(TAG, "UDP数据已发送完毕，等待设备响应。");
                    }
                });
    }

    private Observable<Boolean> createSendUdpDataObservable(final Context context){
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                InetAddress localInetAddress = EspNetUtil.getLocalInetAddress(context);
                Log.d(TAG, "localInetAddress：本地手机IP " + localInetAddress);

                EsptouchGenerator generator = new EsptouchGenerator(localInetAddress, ssid, bssid, pwd, isHidden);
                long startTime = System.currentTimeMillis();
                long currentTime = startTime;
                long lastTime = currentTime - requestParameters.getTimeoutTotalCodeMillisecond();

                byte[][] gcBytes2 = generator.getGCBytes2();
                byte[][] dcBytes2 = generator.getDCBytes2();

                for (int i = 0; i < requestParameters.getTotalRepeatTime(); i++) {

                    // socket未中断则继续处理
                    int index = 0;

                    while (!isInterrupted) {
                        if (currentTime - lastTime >= requestParameters.getTimeoutTotalCodeMillisecond()) {
                            //LogUtil.i("发送 gc code");

                            // send guide code
                            while (!isInterrupted && System.currentTimeMillis() - currentTime < requestParameters.getTimeoutGuideCodeMillisecond()) {
                                // 发送数据
                                socketClient.sendData(
                                        requestParameters.getTargetHostname(),
                                        requestParameters.getTargetPort(),
                                        gcBytes2,
                                        requestParameters.getIntervalGuideCodeMillisecond());

                                // 如果数据发送处理时间 大于 已定义等待UDP发送时间，则结束发送
                                if (System.currentTimeMillis() - startTime > requestParameters.getWaitUdpSendingMillisecond()) {
                                    break;
                                }
                            }

                            lastTime = currentTime;
                        } else {
                            //LogUtil.i("发送 dc code");
                            socketClient.sendData(
                                    requestParameters.getTargetHostname(),
                                    requestParameters.getTargetPort(),
                                    dcBytes2,
                                    index,
                                    ONE_DATA_LEN,
                                    requestParameters.getIntervalDataCodeMillisecond());
                            index = (index + ONE_DATA_LEN) % dcBytes2.length;
                        }

                        currentTime = System.currentTimeMillis();

                        // 如果数据发送处理时间 大于 已定义等待UDP发送时间，则结束发送
                        if (currentTime - startTime > requestParameters.getWaitUdpSendingMillisecond()) {
                            break;
                        }
                    }
                }

                subscriber.onNext(true);
            }
        });
    }
}
