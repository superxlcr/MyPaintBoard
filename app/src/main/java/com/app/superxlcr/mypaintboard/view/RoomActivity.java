package com.app.superxlcr.mypaintboard.view;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import com.app.superxlcr.mypaintboard.R;
import com.app.superxlcr.mypaintboard.controller.ChatController;
import com.app.superxlcr.mypaintboard.controller.RoomController;
import com.app.superxlcr.mypaintboard.controller.UserController;
import com.app.superxlcr.mypaintboard.model.ChatMessage;
import com.app.superxlcr.mypaintboard.model.Protocol;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.SoftReference;

/**
 * Created by superxlcr on 2017/1/15.
 * 房间界面
 */

public class RoomActivity extends Activity {

    private static Handler handler;

    private int roomId;
    private String nickname;

    private MyChatView myChatView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        handler = new MyHandler(this);

        // 初始化房间id
        if (RoomController.getInstance().getRoom() == null) {
            Toast.makeText(this, "您还未进入任何房间", Toast.LENGTH_SHORT).show();
            finish();
        }
        roomId = RoomController.getInstance().getRoom().getId();
        // 初始化用户名
        if (UserController.getInstance().getUser() == null) {
            Toast.makeText(this, "您还未进行登录", Toast.LENGTH_SHORT).show();
            finish();
        }
        nickname = UserController.getInstance().getUser().getNickname();

        // TODO 初始化view
        myChatView = (MyChatView) findViewById(R.id.my_chat_view);
        myChatView.getSendBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 输入框不为空
                if (!myChatView.getInputET().getText().toString().isEmpty()) {
                    long time = System.currentTimeMillis();
                    String msg = myChatView.getInputET().getText().toString();
                    if (ChatController.getInstance().sendMessage(RoomActivity.this, handler, time, roomId, msg)) {
                        // 正在发送
                        ChatMessage chatMessage = new ChatMessage(nickname, msg, ChatMessage.SEND, time, true);
                        myChatView.getMyChatMessageList().add(chatMessage);
                        myChatView.getAdapter().notifyDataSetChanged();
                        // 清空输入框
                        myChatView.getInputET().getText().clear();
                    }
                }
            }
        });

        // 初始化聊天控制模块
        ChatController.getInstance().setReceiveMsgHandler(this, handler);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TODO 退出房间
    }

    static class MyHandler extends Handler {

        private SoftReference<RoomActivity> reference;

        public MyHandler(RoomActivity activity) {
            reference = new SoftReference<RoomActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            // TODO 处理指令
            RoomActivity activity = reference.get();
            if (activity != null && msg != null && msg.obj instanceof Protocol) {
                Protocol protocol = (Protocol) msg.obj;
                JSONArray content = protocol.getContent();
                try {
                    switch (protocol.getOrder()) {
                        case Protocol.MESSAGE: { // 发送消息回复
                            int stateCode = content.getInt(0);
                            switch (stateCode) {
                                case Protocol.MESSAGE_SUCCESS: { // 发送成功
                                    long time = protocol.getTime();
                                    // 根据时间寻找聊天记录
                                    for (ChatMessage chatMessage : activity.myChatView.getMyChatMessageList()) {
                                        if (time == chatMessage.getTime()) {
                                            // 已发送成功
                                            chatMessage.setWaiting(false);
                                            break;
                                        }
                                    }
                                    // 更改动画
                                    activity.myChatView.getAdapter().notifyDataSetChanged();
                                    break;
                                }
                                case Protocol.MESSAGE_WRONG_ROOM_ID: { // 错误的房间id
                                    // TODO
                                    showToast("当前房间id发生错误，消息发送失败，请退出重试");
                                    break;
                                }
                                case Protocol.MESSAGE_UNKNOW_PRO: { // 未知错误
                                    // TODO
                                    showToast("发生未知错误，消息发送失败");
                                    break;
                                }
                            }
                            break;
                        }
                        case Protocol.MESSAGE_PUSH: { // 接收消息推送
                            int roomId = content.getInt(0);
                            String nickname = content.getString(1);
                            String message = content.getString(2);

                            // 判断消息是否有效
                            if (roomId != activity.roomId || nickname == activity.nickname) {
                                showToast("接收到无效的消息");
                            } else {
                                // 更新消息列表
                                ChatMessage chatMessage = new ChatMessage(nickname, message, ChatMessage.RECEIVE, System.currentTimeMillis(), false);
                                activity.myChatView.getMyChatMessageList().add(chatMessage);
                                activity.myChatView.getAdapter().notifyDataSetChanged();
                            }
                            break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    showToast("协议内容解析错误");
                }
            }
        }

        private void showToast(String msg) {
            if (reference != null && reference.get() != null) {
                Toast.makeText(reference.get(), msg, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
