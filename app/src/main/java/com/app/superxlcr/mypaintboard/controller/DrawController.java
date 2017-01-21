package com.app.superxlcr.mypaintboard.controller;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.app.superxlcr.mypaintboard.model.Line;
import com.app.superxlcr.mypaintboard.model.Point;
import com.app.superxlcr.mypaintboard.model.Protocol;
import com.app.superxlcr.mypaintboard.utils.ProtocolListener;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by superxlcr on 2017/1/21.
 * 绘画控制模块
 */

public class DrawController {

    private static DrawController instance;

    public static DrawController getInstance() {
        if (instance == null) {
            synchronized (DrawController.class) {
                if (instance == null) {
                    instance = new DrawController();
                }
            }
        }
        return instance;
    }

    private ProtocolListener sendDrawListener; // 发送绘制条目用监听器
    private ProtocolListener receiveDrawListener; // 接收绘制条目用监听器
    private ProtocolListener getDrawListListener; // 获取绘制条目用监听器

    private DrawController() {
        sendDrawListener = null;
        receiveDrawListener = null;
    }

    /**
     * 发送绘制条目
     * @param context 上下文
     * @param handler 用于回调消息
     * @param time 发送时间
     * @param roomId 房间id
     * @param line 绘制条目
     * @return 是否发送成功
     */
    public boolean sendDraw(final Context context, final Handler handler, long time, int roomId, Line line) {
        try {
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(roomId);
            // line (pointNumber + point (x , y) + color + width + isEraser)
            jsonArray.put(line.getPointList().size());
            for (Point point : line.getPointList()) {
                jsonArray.put(point.getX());
                jsonArray.put(point.getY());
            }
            jsonArray.put(line.getColor());
            jsonArray.put(line.getWidth());
            jsonArray.put(line.isEraser());
            Protocol sendProtocol = new Protocol(Protocol.DRAW, time, jsonArray);
            // 注册监听器
            sendDrawListener = new ProtocolListener() {
                @Override
                public boolean onReceive(Protocol protocol) {
                    int order = protocol.getOrder();
                    if (order == Protocol.DRAW) {
                        // 通过handler返回协议信息
                        Message message = handler.obtainMessage();
                        message.obj = protocol;
                        handler.sendMessage(message);
                        // 移除监听器
                        CommunicationController.getInstance(context).removeListener(sendDrawListener);
                        return true;
                    }
                    return false;
                }
            };
            CommunicationController.getInstance(context).registerListener(sendDrawListener);
            // 发送信息
            return CommunicationController.getInstance(context).sendProtocol(sendProtocol);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 设置接收绘制推送监听器
     * @param context 上下文
     * @param handler 用于接收回调消息
     */
    public void setReceiveDrawHandler(Context context, final Handler handler) {
        // 连接服务器
        CommunicationController.getInstance(context).connectServer();
        // 注册监听器
        receiveDrawListener = new ProtocolListener() {
            @Override
            public boolean onReceive(Protocol protocol) {
                int order = protocol.getOrder();
                if (order == Protocol.DRAW_PUSH) {
                    // 通过handler返回协议信息
                    Message message = handler.obtainMessage();
                    message.obj = protocol;
                    handler.sendMessage(message);
                    return true;
                }
                return false;
            }
        };
        CommunicationController.getInstance(context).registerListener(receiveDrawListener);
    }

    /**
     * 获取绘制条目列表
     * @param context 上下文
     * @param handler 用于回调消息
     * @param time 发送时间
     * @param roomId 房间id
     * @return 是否发送成功
     */
    public boolean getDrawList(final Context context, final Handler handler, long time, int roomId) {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(roomId);
        Protocol sendProtocol = new Protocol(Protocol.GET_DRAW_LIST, time, jsonArray);
        // 注册监听器
        getDrawListListener = new ProtocolListener() {
            @Override
            public boolean onReceive(Protocol protocol) {
                int order = protocol.getOrder();
                if (order == Protocol.GET_DRAW_LIST) {
                    // 通过handler返回协议信息
                    Message message = handler.obtainMessage();
                    message.obj = protocol;
                    handler.sendMessage(message);
                    // 移除监听器
                    CommunicationController.getInstance(context).removeListener(getDrawListListener);
                    return true;
                }
                return false;
            }
        };
        CommunicationController.getInstance(context).registerListener(getDrawListListener);
        // 发送信息
        return CommunicationController.getInstance(context).sendProtocol(sendProtocol);
    }
}
