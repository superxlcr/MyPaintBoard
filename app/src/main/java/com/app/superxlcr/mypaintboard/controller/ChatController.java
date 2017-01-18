package com.app.superxlcr.mypaintboard.controller;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.app.superxlcr.mypaintboard.model.Protocol;
import com.app.superxlcr.mypaintboard.utils.ProtocolListener;

import org.json.JSONArray;

/**
 * Created by superxlcr on 2017/1/17.
 * 聊天控制模块
 */

public class ChatController {

    private static ChatController instance;

    public static ChatController getInstance() {
        if (instance == null) {
            synchronized (ChatController.class) {
                if (instance == null) {
                    instance = new ChatController();
                }
            }
        }
        return instance;
    }

    private ProtocolListener sendMsgListener; // 发送消息用监听器
    private ProtocolListener receiveMsgListener; // 接受消息用监听器

    private ChatController() {}

    /**
     * 发送聊天消息
     * @param context 上下文
     * @param handler 回调处理器
     * @param time 发送时间
     * @param roomId 房间id
     * @param message 消息
     * @return 是否发送成功
     */
    public boolean sendMessage(final Context context, final Handler handler, long time, int roomId, String message) {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(roomId);
        jsonArray.put(message);
        Protocol sendProtocol = new Protocol(Protocol.MESSAGE, time, jsonArray);
        // 注册监听器
        sendMsgListener = new ProtocolListener() {
            @Override
            public boolean onReceive(Protocol protocol) {
                int order = protocol.getOrder();
                if (order == Protocol.MESSAGE) {
                    // 通过handler返回协议信息
                    Message message = handler.obtainMessage();
                    message.obj = protocol;
                    handler.sendMessage(message);
                    // 移除监听器
                    CommunicationController.getInstance(context).removeListener(sendMsgListener);
                    return true;
                }
                return false;
            }
        };
        CommunicationController.getInstance(context).registerListener(sendMsgListener);
        // 发送信息
        return CommunicationController.getInstance(context).sendProtocol(sendProtocol);
    }

    /**
     * 注册接受消息推送监听器
     * @param context
     * @param handler
     */
    public void setReceiveMsgHandler(Context context, final Handler handler) {
        // 连接服务器
        CommunicationController.getInstance(context).connectServer();
        // 注册监听器
        receiveMsgListener = new ProtocolListener() {
            @Override
            public boolean onReceive(Protocol protocol) {
                int order = protocol.getOrder();
                if (order == Protocol.MESSAGE_PUSH) {
                    // 通过handler返回协议信息
                    Message message = handler.obtainMessage();
                    message.obj = protocol;
                    handler.sendMessage(message);
                    return true;
                }
                return false;
            }
        };
        CommunicationController.getInstance(context).registerListener(receiveMsgListener);
    }
}
