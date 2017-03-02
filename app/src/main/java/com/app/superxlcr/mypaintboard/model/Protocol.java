package com.app.superxlcr.mypaintboard.model;

import org.json.*;

/**
 * 协议
 * 
 * @author superxlcr
 *
 */
public class Protocol {

	// 服务器端口
	public static final int PORT = 9999;
	// TODO 心跳包间隔
	public static long HEART_BEAT_PERIOD = 5000;

	// 指令类型

	// 登录
	// c to s: username + password
	// s to c: stateCode + (id + username + nickname)(if success)
	public static final int LOGIN = 0;
	public static final int LOGIN_SUCCESS = 0; // 成功
	public static final int LOGIN_NO_USERNAME = LOGIN_SUCCESS + 1; // 非法用户名
	public static final int LOGIN_WRONG_PASSWORD = LOGIN_NO_USERNAME + 1; // 错误密码
	public static final int LOGIN_UNKNOW_PRO = LOGIN_WRONG_PASSWORD + 1; // 未知错误
	public static final int LOGIN_ALREADY_LOGIN = LOGIN_UNKNOW_PRO + 1; // 用户已在线

	// 注册
	// c to s: username + password + nickname
	// s to c: stateCode + (id + username + nickname)(if success)
	public static final int REGISTER = LOGIN + 1;
	public static final int REGISTER_SUCCESS = 0; // 成功
	public static final int REGISTER_REPEAT_USERNAME = REGISTER_SUCCESS + 1; // 重复用户名
	public static final int REGISTER_REPEAT_NICKNAME = REGISTER_REPEAT_USERNAME + 1; // 重复昵称
	public static final int REGISTER_UNKNOW_PRO = REGISTER_REPEAT_NICKNAME + 1; // 未知错误

	// 修改资料
	// c to s: id + username + password（为空表示不作修改） + nickname
	// s to c: stateCode
	public static final int EDIT_INFO = REGISTER + 1;
	public static final int EDIT_INFO_SUCCESS = 0; // 成功
	public static final int EDIT_INFO_UNKNOW_PRO = EDIT_INFO_SUCCESS + 1; // 未知错误
	public static final int EDIT_INFO_REPEAT_NICKNAME = EDIT_INFO_UNKNOW_PRO + 1; // 昵称重复

	// 获取房间列表
	// c to s:
	// s to c: roomNumber + (roomId + roomName + roomMemberNumber)(per room)
	public static final int GET_ROOM_LIST = EDIT_INFO + 1;

	// 创建房间
	// c to s: roomName
	// s to c: roomId + roomName
	public static final int CREATE_ROOM = GET_ROOM_LIST + 1;

	// 加入房间
	// c to s: roomId
	// s to c: stateCode + (roomId + roomName) (if success)
	public static final int JOIN_ROOM = CREATE_ROOM + 1;
	public static final int JOIN_ROOM_SUCCESS = 0; // 成功
	public static final int JOIN_ROOM_INVALID_ROOMID = JOIN_ROOM_SUCCESS + 1; // 非法房间id
	public static final int JOIN_ROOM_UNKNOW_PRO = JOIN_ROOM_INVALID_ROOMID + 1; // 未知错误
	public static final int JOIN_ROOM_ALREADY_IN = JOIN_ROOM_UNKNOW_PRO + 1; // 用户已在该房间

	// 退出房间
	// c to s:
	// s to c: stateCode
	public static final int EXIT_ROOM = JOIN_ROOM + 1;
	public static final int EXIT_ROOM_SUCCESS = 0; // 成功
	public static final int EXIT_ROOM_NOT_IN = EXIT_ROOM_SUCCESS + 1; // 用户不在任何房间
	public static final int EXIT_ROOM_UNKNOW_PRO = EXIT_ROOM_NOT_IN + 1; // 未知错误

	// 获取房间成员
	// c to s: roomId
	// s to c: stateCode + roomId + memberNumber + (username +
	// nickname + isAdmin)(per user)
	// push: stateCode + roomId + memberNumber + (username +
	// nickname + isAdmin)(per user)
	public static final int GET_ROOM_MEMBER = EXIT_ROOM + 1;
	public static final int GET_ROOM_MEMBER_SUCCESS = 0; // 成功才有后续内容
	public static final int GET_ROOM_MEMBER_WRONG_ROOM_ID = GET_ROOM_MEMBER_SUCCESS + 1; // 房间id错误
	public static final int GET_ROOM_MEMBER_UNKNOW_PRO = GET_ROOM_MEMBER_WRONG_ROOM_ID + 1; // 未知错误

	// 聊天消息
	// c to s: roomId + message
	// s to c: stateCode
	public static final int MESSAGE = GET_ROOM_MEMBER + 1;
	public static final int MESSAGE_SUCCESS = 0;
	public static final int MESSAGE_WRONG_ROOM_ID = MESSAGE_SUCCESS + 1; // 错误的房间id
	public static final int MESSAGE_UNKNOW_PRO = MESSAGE_WRONG_ROOM_ID + 1; // 未知错误

	// 聊天消息推送
	// push: roomId + nickname + message
	public static final int MESSAGE_PUSH = MESSAGE + 1;

	// 绘制消息
	// c to s: roomId + line (pointNumber + point (x , y) +
	// color + paintWidth +
	// isEraser + width + height)
	// s to c: stateCode
	public static final int DRAW = MESSAGE_PUSH + 1;
	public static final int DRAW_SUCCESS = 0;
	public static final int DRAW_WRONG_ROOM_ID = DRAW_SUCCESS + 1; // 错误的房间id
	public static final int DRAW_UNKNOW_PRO = DRAW_WRONG_ROOM_ID + 1; // 未知错误

	// 绘制消息推送
	// push: roomId + username + line (pointNumber + point (x ,
	// y) +
	// color + width + isEraser + width + height)
	public static final int DRAW_PUSH = DRAW + 1;

	// 同步绘制消息
	// c to s: roomId
	// s to c: stateCode (if success, client will get draw_push)
	public static final int GET_DRAW_LIST = DRAW_PUSH + 1;
	public static final int GET_DRAW_LIST_SUCCESS = 0;
	public static final int GET_DRAW_LIST_WRONG_ROOM_ID = GET_DRAW_LIST_SUCCESS + 1; // 错误的房间id
	public static final int GET_DRAW_LIST_UNKONW_PRO = GET_DRAW_LIST_WRONG_ROOM_ID + 1; // 未知错误

	// 登录过期推送
	// push:
	public static final int LOGIN_TIME_OUT_PUSH = GET_DRAW_LIST + 1;
	
	// 心跳包
	// c to s:
	// s to c:
	public static final int HEART_BEAT = LOGIN_TIME_OUT_PUSH + 1;

	// 协议：指令 + 时间戳（防重复功能） + 内容

	public static final String ORDER = "order";
	public static final String TIME = "time";
	public static final String CONTENT = "content";

	// 指令
	private int order;
	// 时间戳
	// 客户端查询：客户端时间为准
	// 服务器推送：服务器时间为准
	private long time;
	// 内容
	private JSONArray content;

	// json字符串
	private String jsonStr;

	public Protocol(int order, long time, JSONArray content) {
		this.order = order;
		this.time = time;
		this.content = content;
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(ORDER, order);
			jsonObject.put(TIME, time);
			jsonObject.put(CONTENT, content.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		this.jsonStr = jsonObject.toString();
	}

	public Protocol(String jsonStr) throws JSONException {
		this.jsonStr = jsonStr;
		JSONObject jsonObject = new JSONObject(jsonStr);
		this.order = jsonObject.getInt(ORDER);
		this.time = jsonObject.getLong(TIME);
		String contentStr = jsonObject.getString(CONTENT);
		this.content = new JSONArray(contentStr);
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public JSONArray getContent() {
		return content;
	}

	public void setContent(JSONArray content) {
		this.content = content;
	}

	public String getJsonStr() {
		return jsonStr;
	}

	public void setJsonStr(String jsonStr) {
		this.jsonStr = jsonStr;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		String orderStr = "unknow";
		switch (order) {
		case LOGIN:
			orderStr = "LOGIN";
			break;
		case REGISTER:
			orderStr = "REGISTER";
			break;
		case EDIT_INFO:
			orderStr = "EDIT_INFO";
			break;
		case GET_ROOM_LIST:
			orderStr = "GET_ROOM_LIST";
			break;
		case CREATE_ROOM:
			orderStr = "CREATE_ROOM";
			break;
		case JOIN_ROOM:
			orderStr = "JOIN_ROOM";
			break;
		case EXIT_ROOM:
			orderStr = "EXIT_ROOM";
			break;
		case GET_ROOM_MEMBER:
			orderStr = "GET_ROOM_MEMBER";
			break;
		case MESSAGE:
			orderStr = "MESSAGE";
			break;
		case MESSAGE_PUSH:
			orderStr = "MESSAGE_PUSH";
			break;
		case DRAW:
			orderStr = "DRAW";
			break;
		case DRAW_PUSH:
			orderStr = "DRAW_PUSH";
			break;
		case GET_DRAW_LIST:
			orderStr = "GET_DRAW_LIST";
			break;
		case HEART_BEAT:
			orderStr = "HEART_BEAT";
			break;
		default:
			break;
		}
		sb.append("order :" + orderStr + "\n");
		sb.append("time :" + time + "\n");
		sb.append("content :" + content + "\n");
		return sb.toString();
	}

}
