package com.app.superxlcr.mypaintboard.controller;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.app.superxlcr.mypaintboard.model.Protocol;
import com.app.superxlcr.mypaintboard.utils.ProtocolListener;

import org.json.JSONArray;

/**
 * Created by superxlcr on 2017/1/19.
 * 成员控制模块
 */

public class MemberController {

    private static MemberController instance;

    public static MemberController getInstance() {
        if (instance == null) {
            synchronized (MemberController.class) {
                if (instance == null) {
                    instance = new MemberController();
                }
            }
        }
        return instance;
    }

    private ProtocolListener receiveListener; // 接受用监听器

    private MemberController() {
    }

    /**
     * 获取房间成员列表
     * @param context 上下文
     * @param roomId 房间id
     * @param time 发送时间
     * @return 是否发送消息成功，没注册监听器返回false
     */
    public boolean getRoomMember(Context context, int roomId, long time) {
        if (receiveListener == null) { // 没注册监听器返回false
            return false;
        }
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(roomId);
        Protocol sendProtocol = new Protocol(Protocol.GET_ROOM_MEMBER, time, jsonArray);
        return CommunicationController.getInstance(context).sendProtocol(sendProtocol);
    }

    /**
     * 注册消息监听器
     * @param context 上下文
     * @param handler 用于回调消息
     */
    public void setReceiveHandler(Context context, final Handler handler) {
        // 清除旧监听器
        if (receiveListener != null) {
            CommunicationController.getInstance(context).removeListener(receiveListener);
        }
        // 连接服务器
        CommunicationController.getInstance(context).connectServer();
        // 注册监听器
        receiveListener = new ProtocolListener() {
            @Override
            public boolean onReceive(Protocol protocol) {
                int order = protocol.getOrder();
                if (order == Protocol.GET_ROOM_MEMBER) {
                    // 通过handler返回协议信息
                    Message message = handler.obtainMessage();
                    message.obj = protocol;
                    handler.sendMessage(message);
                    return true;
                }
                return false;
            }
        };
        CommunicationController.getInstance(context).registerListener(receiveListener);
    }

}
