package com.app.superxlcr.mypaintboard.model;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by superxlcr on 2017/1/17.
 * 聊天消息模型
 */

public class ChatMessage {

    public static final int SEND = 0; // 发送
    public static final int RECEIVE = 1; // 接收

    private String nickname;
    private String message;
    private int state; // 状态（发送还是接收）
    private long time; // 发送时间
    private boolean waiting; // 是否正在发送
    private boolean sendFail; // 是否发送失败

    public ChatMessage(String nickname, String message, int state, long time, boolean waiting) {
        this.nickname = nickname;
        this.message = message;
        this.state = state;
        this.time = time;
        this.waiting = waiting;
        this.sendFail = false;
    }

    public boolean isSendFail() {
        return sendFail;
    }

    public void setSendFail(boolean sendFail) {
        this.sendFail = sendFail;
    }

    public void setWaiting(boolean waiting) {
        this.waiting = waiting;
    }

    public String getNickname() {
        return nickname;
    }

    public String getMessage() {
        return message;
    }

    public int getState() {
        return state;
    }

    public long getTime() {
        return time;
    }

    public String getSimpleFormatTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(time));
    }

    public boolean isWaiting() {
        return waiting;
    }
}
