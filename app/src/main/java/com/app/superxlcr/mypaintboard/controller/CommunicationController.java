package com.app.superxlcr.mypaintboard.controller;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.Toast;

import com.app.superxlcr.mypaintboard.model.Protocol;
import com.app.superxlcr.mypaintboard.utils.ProtocolListener;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by superxlcr on 2017/1/6.
 * <p>
 * 通讯控制模块
 */

public class CommunicationController {

    // TODO 服务器IP
    public static String SERVER_IP = "192.168.1.108";

    private static String TAG = CommunicationController.class.getSimpleName();

    public static CommunicationController instance = null;

    public static CommunicationController getInstance(Context context) {
        if (instance == null) {
            synchronized (CommunicationController.class) {
                if (instance == null) {
                    instance = new CommunicationController(context);
                }
            }
        }
        return instance;
    }

    private Context context;
    private List<ProtocolListener> listenerList; // 监听器列表
    private Socket socket; // 通信对象，若为null则无连接
    private Timer timer; // 心跳包计时器任务

    private CommunicationController(Context context) {
        this.context = context.getApplicationContext();
        this.listenerList = new CopyOnWriteArrayList<>();
    }

    /**
     * 连接服务器
     */
    public synchronized void connectServer() {
        // 已连接到服务器
        if (socket != null) {
            return;
        }
        // 检查是否连接wifi或流量
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Activity.CONNECTIVITY_SERVICE);
        boolean wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
        boolean internet = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        if (!wifi && !internet) {
            Toast.makeText(context, "您已断开网络连接，请检查网络状况！", Toast.LENGTH_LONG).show();
            return;
        }

        // 不能在主线程打开网络连接
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 连接服务器
                try {
                    socket = new Socket(SERVER_IP, Protocol.PORT);
                    Log.d(TAG, "连接服务器成功");

                    // 定时器子线程，开始发送心跳包
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            try {
                                if (socket != null) {
                                    // 发送心跳信息
                                    Writer writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
                                    JSONArray jsonArray = new JSONArray();
                                    Protocol sendProtocol = new Protocol(Protocol.HEART_BEAT, System.currentTimeMillis(), jsonArray);
                                    writer.write(sendProtocol.getJsonStr());
                                    writer.flush();
                                } else {
                                    // 连接中断终止任务
                                    timer.cancel();
                                }
                            } catch (IOException e) {
                                // 出现错误，终止定时器，关闭连接
                                e.printStackTrace();
                                clearSocket();
                                timer.cancel();
                            }
                        }
                    }, 0, Protocol.HEART_BEAT_PERIOD);
                    Log.d(TAG, "心跳包定时器设置成功");

                    // 开始监听信息
                    try {
                        Reader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                        while (socket != null) {
                            char data[] = new char[99999];
                            int len;
                            while ((len = reader.read(data)) != -1) {
                                String jsonString = new String(data, 0, len);
                                Protocol protocol = new Protocol(jsonString);
                                Log.d(TAG, protocol.toString());
                                // 分发处理协议内容
                                for (ProtocolListener listener : listenerList) {
                                    if (listener.onReceive(protocol)) { // 返回true则终止传递
                                        break;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        clearSocket();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    socket = null;
                }
            }
        }).start();
    }

    /**
     * 清理并关闭连接
     */
    public void clearSocket() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket = null;
        }
    }

    /**
     * 添加监听器
     *
     * @param listener 监听器
     * @return 是否添加成功
     */
    public boolean registerListener(ProtocolListener listener) {
        if (!listenerList.contains(listener)) {
            return listenerList.add(listener);
        }
        return false;
    }

    /**
     * 移除监听器
     *
     * @param listener 监听器
     * @return 是否移除成功
     */
    public boolean removeListener(ProtocolListener listener) {
        return listenerList.remove(listener);
    }

    /**
     * 发送消息
     *
     * @param protocol 协议内容
     * @return 是否发送成功
     */
    public boolean sendProtocol(Protocol protocol) {
        // 尝试连接服务器
        connectServer();
        // 判断是否连接上服务器
        if (socket == null) {
            return false;
        }
        try {
            Writer writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            writer.write(protocol.getJsonStr());
            writer.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
