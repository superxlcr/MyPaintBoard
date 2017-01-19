package com.app.superxlcr.mypaintboard.controller;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.app.superxlcr.mypaintboard.model.Protocol;
import com.app.superxlcr.mypaintboard.model.Room;
import com.app.superxlcr.mypaintboard.utils.ProtocolListener;

import org.json.JSONArray;

import java.util.List;

/**
 * Created by superxlcr on 2017/1/10.
 *
 * 房间控制模块
 */

public class RoomController {

    private static RoomController instance;

    public static RoomController getInstance() {
        if (instance == null) {
            synchronized (RoomController.class) {
                if (instance == null) {
                    instance = new RoomController();
                }
            }
        }
        return instance;
    }

    private Room room; // 存储用户当前房间
    private List<Room> list; // 房间列表

    private ProtocolListener getRoomListListener; // 获取房间列表用监听器
    private ProtocolListener createRoomListener; // 创建房间用监听器
    private ProtocolListener joinRoomListener; // 加入房间用监听器
    private ProtocolListener exitRoomListener; // 退出房间用监听器

    private RoomController() {
        room = null;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public List<Room> getList() {
        return list;
    }

    public void setList(List<Room> list) {
        this.list = list;
    }

    /**
     * 获取房间信息
     * @param context 上下文
     * @param handler 用于回调消息
     * @param time 发送时间
     * @return 是否成功发送信息
     */
    public boolean getRoomList(final Context context, final Handler handler, long time) {
        JSONArray jsonArray = new JSONArray();
        Protocol sendProtocol = new Protocol(Protocol.GET_ROOM_LIST, time, jsonArray);
        // 注册监听器，监听结果
        getRoomListListener = new ProtocolListener() {
            @Override
            public boolean onReceive(Protocol protocol) {
                int order = protocol.getOrder();
                if (order == Protocol.GET_ROOM_LIST) {
                    // 通过handler返回协议信息
                    Message message = handler.obtainMessage();
                    message.obj = protocol;
                    handler.sendMessage(message);
                    // 移除监听器
                    CommunicationController.getInstance(context).removeListener(getRoomListListener);
                    return true;
                }
                return false;
            }
        };
        CommunicationController.getInstance(context).registerListener(getRoomListListener);
        // 发送信息
        return CommunicationController.getInstance(context).sendProtocol(sendProtocol);
    }

    /**
     * 创建房间
     * @param context 上下文
     * @param handler 用于回调消息
     * @param time 发送时间
     * @param roomName 房间名称
     * @return 是否成功发送信息
     */
    public boolean createRoom(final Context context, final Handler handler, long time, String roomName) {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(roomName);
        Protocol sendProtocol = new Protocol(Protocol.CREATE_ROOM, time, jsonArray);
        // 注册监听器，监听结果
        createRoomListener = new ProtocolListener() {
            @Override
            public boolean onReceive(Protocol protocol) {
                int order = protocol.getOrder();
                if (order == Protocol.CREATE_ROOM) {
                    // 通过handler返回协议信息
                    Message message = handler.obtainMessage();
                    message.obj = protocol;
                    handler.sendMessage(message);
                    // 移除监听器
                    CommunicationController.getInstance(context).removeListener(createRoomListener);
                    return true;
                }
                return false;
            }
        };
        CommunicationController.getInstance(context).registerListener(createRoomListener);
        // 发送信息
        return CommunicationController.getInstance(context).sendProtocol(sendProtocol);
    }

    /**
     * 加入房间
     * @param context 上下文
     * @param handler 用于回调消息
     * @param time 发送时间
     * @param roomId 房间id
     * @return 是否成功发送信息
     */
    public boolean joinRoom(final Context context, final Handler handler, long time, int roomId) {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(roomId);
        Protocol sendProtocol = new Protocol(Protocol.JOIN_ROOM, time, jsonArray);
        // 注册监听器，监听结果
        joinRoomListener = new ProtocolListener() {
            @Override
            public boolean onReceive(Protocol protocol) {
                int order = protocol.getOrder();
                if (order == Protocol.JOIN_ROOM) {
                    // 通过handler返回协议信息
                    Message message = handler.obtainMessage();
                    message.obj = protocol;
                    handler.sendMessage(message);
                    // 移除监听器
                    CommunicationController.getInstance(context).removeListener(joinRoomListener);
                    return true;
                }
                return false;
            }
        };
        CommunicationController.getInstance(context).registerListener(joinRoomListener);
        // 发送信息
        return CommunicationController.getInstance(context).sendProtocol(sendProtocol);
    }

    /**
     * 退出房间
     * @param context 上下文
     * @param handler 用于回调消息
     * @param time 发送时间
     * @return 是否成功发送信息
     */
    public boolean exitRoom(final Context context, final Handler handler, long time) {
        JSONArray jsonArray = new JSONArray();
        Protocol sendProtocol = new Protocol(Protocol.EXIT_ROOM, time, jsonArray);
        // 注册监听器，监听结果
        exitRoomListener = new ProtocolListener() {
            @Override
            public boolean onReceive(Protocol protocol) {
                int order = protocol.getOrder();
                if (order == Protocol.EXIT_ROOM) {
                    // 通过handler返回协议信息
                    Message message = handler.obtainMessage();
                    message.obj = protocol;
                    handler.sendMessage(message);
                    // 移除监听器
                    CommunicationController.getInstance(context).removeListener(exitRoomListener);
                    return true;
                }
                return false;
            }
        };
        CommunicationController.getInstance(context).registerListener(exitRoomListener);
        // 发送信息
        return CommunicationController.getInstance(context).sendProtocol(sendProtocol);
    }
}
