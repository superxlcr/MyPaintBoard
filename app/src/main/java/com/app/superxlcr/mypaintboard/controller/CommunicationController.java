package com.app.superxlcr.mypaintboard.controller;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.Toast;

import com.app.superxlcr.mypaintboard.model.Protocol;
import com.app.superxlcr.mypaintboard.utils.MyLog;
import com.app.superxlcr.mypaintboard.utils.ProtocolListener;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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

    // 服务器IP
    public static final String SERVER_IP = "123.207.118.193";
//    public static final String SERVER_IP = "192.168.191.1";

    private static String TAG = CommunicationController.class.getSimpleName();

    private static CommunicationController instance = null;

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
    private SocketLock socketLock; // 通信锁
    private Timer timer; // 心跳包计时器任务
    private BufferedWriter writer;
    private BufferedReader reader;

    private CommunicationController(Context context) {
        this.context = context.getApplicationContext();
        this.listenerList = new CopyOnWriteArrayList<>();
        this.socketLock = new SocketLock();
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
                    synchronized (socketLock) {
                        if (socket == null) {
                            socket = new Socket(SERVER_IP, Protocol.PORT);
                            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
                            MyLog.d(TAG, "连接服务器成功");

                            // 定时器子线程，开始发送心跳包
                            timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    try {
                                        synchronized (writer) {
                                            if (socket != null) {
                                                // 发送心跳信息
                                                JSONArray jsonArray = new JSONArray();
                                                Protocol sendProtocol = new Protocol(Protocol.HEART_BEAT, System.currentTimeMillis(), jsonArray);
                                                writer.write(sendProtocol.getJsonStr());
                                                writer.newLine();
                                                writer.flush();
                                            } else {
                                                // 连接中断终止任务
                                                timer.cancel();
                                            }
                                        }
                                    } catch (IOException e) {
                                        // 出现错误，终止定时器，关闭连接
                                        MyLog.e(TAG, Log.getStackTraceString(e));
                                        clearSocket();
                                        timer.cancel();
                                    }
                                }
                            }, 0, Protocol.HEART_BEAT_PERIOD);
                            MyLog.d(TAG, "心跳包定时器设置成功");

                            // 开始监听信息
                            try {
                                reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                                while (socket != null) {
                                    String jsonString = null;
                                    while ((jsonString = reader.readLine()) != null) {
                                        try {
                                            Protocol protocol = new Protocol(jsonString);
                                            // 打印非心跳包的信息
                                            if (protocol.getOrder() != Protocol.HEART_BEAT) {
                                                MyLog.d(TAG, "Receive\nlen :" + jsonString.length() + "\n" + protocol.toString());
                                            }
                                            // 分发处理协议内容
                                            for (ProtocolListener listener : listenerList) {
                                                if (listener.onReceive(protocol)) { // 返回true则终止传递
                                                    break;
                                                }
                                            }
                                        } catch (JSONException e) {
                                            // 协议解析错误，丢弃内容
                                            MyLog.e(TAG, Log.getStackTraceString(e));
                                        }
                                    }
                                }
                            } catch (IOException e) {
                                MyLog.e(TAG, Log.getStackTraceString(e));
                                clearSocket();
                            }
                        }
                    }
                } catch (IOException e) {
                    MyLog.e(TAG, Log.getStackTraceString(e));
                    socket = null;
                }
            }
        }).start();
    }

    /**
     * 清理并关闭连接
     */
    public void clearSocket() {
        MyLog.d(TAG, "已清除socket连接");
        if (socket != null) {
            try {
                // 关闭输入输出流
                writer.close();
                reader.close();
                socket.shutdownInput();
                socket.shutdownOutput();
                // 关闭连接
                socket.close();
            } catch (IOException e) {
                MyLog.e(TAG, Log.getStackTraceString(e));
            }
            socket = null;
        }
        // 重置状态
        // 清空用户状态
        UserController.getInstance().setUser(null);
        // 清空房间状态
        RoomController.getInstance().setRoom(null);
        RoomController.getInstance().setList(null);
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
            synchronized (writer) {
                writer.write(protocol.getJsonStr());
                writer.newLine();
                writer.flush();
                // 打印非心跳包的信息
                if (protocol.getOrder() != Protocol.HEART_BEAT) {
                    MyLog.d(TAG, "Send\nlen :" + protocol.getJsonStr().length() + "\n" + protocol.toString());
                }
            }
            return true;
        } catch (IOException e) {
            MyLog.e(TAG, Log.getStackTraceString(e));
            clearSocket();
            return false;
        }
    }

    class SocketLock {
    }
}
