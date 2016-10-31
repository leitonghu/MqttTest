package com.chaohui.im.common.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.chaohui.im.common.util.utils.NotifyUtil;
import com.chaohui.im.common.util.utils.Tools;
import com.chaohui.mqtttest.R;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by lei on 2016/10/31.
 */
public class PushService extends Service {

    private String host = ServiceURL.Push_Service_Url + ":"
            + ServiceURL.Push_Port;

    private MqttCallback mMqttCallBack;
    private MqttConnectOptions options;
    private MqttClient client;
    private ScheduledExecutorService scheduler;

    private Handler handler;
    private Context context;
    private NotificationManager mNotifMan;
    private String myTopic = "testtesttest";

    // Static method to start the service
    public static void actionStart(Context ctx) {
        Intent i = new Intent(ctx, PushService.class);
        ctx.startService(i);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = PushService.this;
        mNotifMan = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                try {
                    if (msg.what == 2) {
                        try {
                            client.subscribe(myTopic, 1);
//                            client.subscribe(myTopic_info, 1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (msg.what == 3) {

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        mMqttCallBack = new MqttCallback() {

            @Override
            public void connectionLost(Throwable cause) {
                // 连接丢失后，一般在这里面进行重连
                System.out.println("connectionLost----------"
                        + cause.getStackTrace().toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // publish后会执行到这里
                System.out.println("deliveryComplete---------"
                        + token.isComplete());
            }

            @Override
            public void messageArrived(String topicName, MqttMessage message)
                    throws Exception {
                // subscribe后得到的消息会执行到这里面
                Log.i("push------topicName-", topicName);
                Log.i("push------message--", message.toString());


                if (topicName.equals(myTopic)) {

                }

                    /*HistoryMsgListBean parser = PushParser
                            .ArrivedMsgParser(message.toString());

                    if (!parser.getPuid().equals("0") && !parser.getPuid().equals(SpTools.getUid(PushService.this))) {
                        return;
                    }

                    if (parser.getSysType().toString().trim()
                            .equals(SYSTYPE_CHAT)) { // 咨询
                        Message msg = new Message();
                        msg.what = MSG_ARRIVE;
                        msg.obj = message.toString();
                        handler.sendMessage(msg);
                    } else if (parser.getSysType().toString().trim()
                            .equals(SYSTYPE_OFFLINE)) {// 离线
                        handler.sendEmptyMessage(OFFINE);
                    }*/
            }

        };
        init();
        startReconnect();
    }


    private void init() {
        try {
            // host为主机名，test为clientid即连接MQTT的客户端ID，一般以客户端唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
            client = new MqttClient(host, "YDY/" + Tools.getMac(this),
                    new MemoryPersistence());
            // MQTT的连接设置
            options = new MqttConnectOptions();
            // 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
            options.setCleanSession(true);
            // 设置连接的用户名
            options.setUserName("");
            // 设置连接的密码
            options.setPassword("".toCharArray());
            // 设置超时时间 单位为秒
            options.setConnectionTimeout(10);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
            options.setKeepAliveInterval(4);
            // 设置回调
            client.setCallback(mMqttCallBack);

            //设置最终端口的通知消息
//			options.setWill(client.getTopic(myTopic_chat), "网络异常，连接断开 !".getBytes(), 1, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startReconnect() {

        if (scheduler == null) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
            synchronized (scheduler) {
                scheduler.scheduleAtFixedRate(new Runnable() {

                    @Override
                    public void run() {
                        if (!client.isConnected()) {
                            Tools.toast(PushService.this, "startReconnect重连.......");
                            connect();
                        }
                    }
                }, 0 * 1000, 5 * 1000, TimeUnit.MILLISECONDS);
            }
        }
    }


    private void connect() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    client.connect(options);
                    Tools.toast(PushService.this, "正在连接服务器.......");
                    Message msg = new Message();
                    msg.what = 2;
                    handler.sendMessage(msg);
                } catch (Exception e) {

                    try {
                        client.disconnect();
                    } catch (MqttException e1) {
                        e1.printStackTrace();
                    }
                    e.printStackTrace();
                    Tools.toast(PushService.this, "连接服务器异常.......");
                    Message msg = new Message();
                    msg.what = 3;
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }


    private void showNotification(String text, PendingIntent pi, int tag) {
        try {
            int smallIcon;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                smallIcon = R.mipmap.ic_launcher;
            } else {
                smallIcon = R.mipmap.ic_launcher;
            }
            if (text.length() >= 20) {
                text = text.substring(0, 20) + "...";
            }
            String ticker = text;
            String title = "你有收到新的推送";
            String content = text;

            //实例化工具类，并且调用接口
            NotifyUtil notify1 = new NotifyUtil(this, tag);
            notify1.notify_normal_singline(pi, smallIcon, ticker, title, content, true, true, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}