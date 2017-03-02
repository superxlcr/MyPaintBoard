package com.app.superxlcr.mypaintboard.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.superxlcr.mypaintboard.R;
import com.app.superxlcr.mypaintboard.controller.ChatController;
import com.app.superxlcr.mypaintboard.controller.DrawController;
import com.app.superxlcr.mypaintboard.controller.MemberController;
import com.app.superxlcr.mypaintboard.controller.RoomController;
import com.app.superxlcr.mypaintboard.controller.UserController;
import com.app.superxlcr.mypaintboard.model.ChatMessage;
import com.app.superxlcr.mypaintboard.model.Line;
import com.app.superxlcr.mypaintboard.model.Point;
import com.app.superxlcr.mypaintboard.model.Protocol;
import com.app.superxlcr.mypaintboard.utils.MyLog;
import com.readystatesoftware.viewbadger.BadgeView;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by superxlcr on 2017/1/15.
 * 房间界面
 */

public class RoomActivity extends BaseActivity {

    private static String TAG = RoomActivity.class.getSimpleName();

    private static MyHandler handler = new MyHandler();

    private int roomId;
    private String username;
    private String nickname;

    // 绘制view
    private MyPaintView myPaintView;

    // 层叠view
    private CascadeLayout cascadeLayout;
    private TextView triggerView;

    // 成员相关
    private ListView memberListView;
    private List<Member> memberList;
    private MemberAdapter memberAdapter;

    // 聊天相关
    private BadgeView badgeView;
    private MyChatView myChatView;
    private int newMessageNumber; // 新消息条目数
    private int oldPosition; // 旧消息位置

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        handler.setReference(new SoftReference<RoomActivity>(this));

        // 初始化房间id
        if (RoomController.getInstance().getRoom() == null) {
            MyLog.d(TAG, "获取房间id失败！");
            Toast.makeText(this, "您还未进入任何房间", Toast.LENGTH_SHORT).show();
            finish();
        }
        roomId = RoomController.getInstance().getRoom().getId();

        // 初始化用户名昵称
        if (UserController.getInstance().getUser() == null) {
            MyLog.d(TAG, "获取已登录用户失败！");
            Toast.makeText(this, "您还未进行登录", Toast.LENGTH_SHORT).show();
            finish();
        }
        username = UserController.getInstance().getUser().getUsername();
        nickname = UserController.getInstance().getUser().getNickname();

        // TODO 语音系统

        // 绘制view
        myPaintView = (MyPaintView) findViewById(R.id.my_paint_view);
        myPaintView.setHandler(handler);
        myPaintView.setRoomId(roomId);
        // 设置监听器
        DrawController.getInstance().setReceiveDrawHandler(this, handler);

//        // 获取旧线段
//        DrawController.getInstance().getDrawList(this, handler, System.currentTimeMillis(), roomId);

        // 层叠view
        cascadeLayout = (CascadeLayout) findViewById(R.id.cascade_layout);
        triggerView = (TextView) findViewById(R.id.trigger_view);
        cascadeLayout.setTriggerView(triggerView);
        cascadeLayout.setAnimatorTime(500);

        // 显示聊天数字view
        newMessageNumber = 0;
        badgeView = new BadgeView(this, triggerView);
        badgeView.setText(newMessageNumber + "");
        badgeView.hide();
        cascadeLayout.setListener(new CascadeLayout.CascadeLayoutListener() {
            @Override
            public void onOpenLayout() {
                // 展开聊天界面时关闭数字显示
                badgeView.hide();
            }
        });

        // 成员列表view
        memberListView = (ListView) findViewById(R.id.member_list);
        memberList = new ArrayList<>();
        memberAdapter = new MemberAdapter(this, R.layout.item_member, memberList);
        memberListView.setAdapter(memberAdapter);

        // 初始化成员模块，获取房间成员信息
        MemberController.getInstance().setReceiveHandler(this, handler);
        MemberController.getInstance().getRoomMember(this, roomId, System.currentTimeMillis());

        // 聊天view
        oldPosition = 0;
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
        // 退出房间
        RoomController.getInstance().exitRoom(this, handler, System.currentTimeMillis());
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_room_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 设置选中效果
        item.setChecked(true);
        switch (item.getItemId()) {
            case R.id.black: // 黑色
                myPaintView.setPaintColor(Color.BLACK);
                break;
            case R.id.blue: // 蓝色
                myPaintView.setPaintColor(Color.BLUE);
                break;
            case R.id.cyan: // 青色
                myPaintView.setPaintColor(Color.CYAN);
                break;
            case R.id.gray: // 灰色
                myPaintView.setPaintColor(Color.GRAY);
                break;
            case R.id.green: // 绿色
                myPaintView.setPaintColor(Color.GREEN);
                break;
            case R.id.magenta: // 洋红色
                myPaintView.setPaintColor(Color.MAGENTA);
                break;
            case R.id.red: // 红色
                myPaintView.setPaintColor(Color.RED);
                break;
            case R.id.yellow: // 黄色
                myPaintView.setPaintColor(Color.YELLOW);
                break;
            case R.id.paint_width_1:
                myPaintView.setPaintWidth(1);
                break;
            case R.id.paint_width_5:
                myPaintView.setPaintWidth(5);
                break;
            case R.id.paint_width_10:
                myPaintView.setPaintWidth(10);
                break;
            case R.id.paint_width_15:
                myPaintView.setPaintWidth(15);
                break;
            case R.id.paint_width_20:
                myPaintView.setPaintWidth(20);
                break;
            case R.id.paint: // 画笔模式
                myPaintView.setEraser(false);
                break;
            case R.id.eraser: // 擦除模式
                myPaintView.setEraser(true);
                break;
        }
        return true;
    }

    static class Member {
        String nickname;
        boolean admin;

        public Member(String nickname, boolean admin) {
            this.nickname = nickname;
            this.admin = admin;
        }
    }

    static class MemberAdapter extends ArrayAdapter<Member> {

        private LayoutInflater inflater;
        private int resourceId;
        private List<Member> list;

        public MemberAdapter(Context context, int resource, List<Member> objects) {
            super(context, resource, objects);
            inflater = LayoutInflater.from(context);
            resourceId = resource;
            list = objects;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            // 初始化view
            if (convertView == null) {
                view = inflater.inflate(resourceId, parent, false);
                TextView nicknameTV = (TextView) view.findViewById(R.id.nickname);
                TextView isAdminTV = (TextView) view.findViewById(R.id.is_admin);
                MemberViewHolder holder = new MemberViewHolder(nicknameTV, isAdminTV);
                view.setTag(holder);
            } else {
                view = convertView;
            }
            // 填充内容
            MemberViewHolder holder = (MemberViewHolder) view.getTag();
            Member member = list.get(position);
            holder.nicknameTV.setText(member.nickname);
            if (member.admin) {
                holder.isAdminTV.setVisibility(View.VISIBLE);
            } else {
                holder.isAdminTV.setVisibility(View.GONE);
            }
            return view;
        }
    }

    static class MemberViewHolder {
        TextView nicknameTV;
        TextView isAdminTV;

        public MemberViewHolder(TextView nicknameTV, TextView isAdminTV) {
            this.nicknameTV = nicknameTV;
            this.isAdminTV = isAdminTV;
        }
    }

    static class MyHandler extends Handler {

        private SoftReference<RoomActivity> reference;

        public MyHandler() {
        }

        public void setReference(SoftReference<RoomActivity> reference) {
            this.reference = reference;
        }

        @Override
        public void handleMessage(Message msg) {
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
                                    // 发送失败状态更改
                                    long time = protocol.getTime();
                                    // 根据时间寻找聊天记录
                                    for (ChatMessage chatMessage : activity.myChatView.getMyChatMessageList()) {
                                        if (time == chatMessage.getTime()) {
                                            // 已发送失败
                                            chatMessage.setWaiting(false);
                                            chatMessage.setSendFail(true);
                                            break;
                                        }
                                    }
                                    // 更改动画
                                    activity.myChatView.getAdapter().notifyDataSetChanged();
                                    MyLog.d(TAG, "当前房间id发生错误，消息发送失败！");
                                    showToast("消息发送失败，请退出房间重试");
                                    break;
                                }
                                case Protocol.MESSAGE_UNKNOW_PRO: { // 未知错误
                                    // 发送失败状态更改
                                    long time = protocol.getTime();
                                    // 根据时间寻找聊天记录
                                    for (ChatMessage chatMessage : activity.myChatView.getMyChatMessageList()) {
                                        if (time == chatMessage.getTime()) {
                                            // 已发送失败
                                            chatMessage.setWaiting(false);
                                            chatMessage.setSendFail(true);
                                            break;
                                        }
                                    }
                                    // 更改动画
                                    activity.myChatView.getAdapter().notifyDataSetChanged();
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
                            if (roomId != activity.roomId) {
                                MyLog.d(TAG, "接收到无效的消息推送，房间id错误");
                            } else if (nickname == activity.nickname) {
                                MyLog.d(TAG, "接收到无效的消息推送，昵称为当前登录用户");
                            } else { // 收到成功的消息
                                if (activity.cascadeLayout.getState() == CascadeLayout.OPEN) {
                                    // 显示聊天界面时，不记录未读消息数
                                    activity.newMessageNumber = 0;
                                } else if (activity.cascadeLayout.getState() == CascadeLayout.CLOSE) {
                                    // 增加新的未读消息数
                                    activity.newMessageNumber++;
                                    // 显示未读消息数
                                    activity.badgeView.setText(activity.newMessageNumber + "");
                                    activity.badgeView.show();
                                }
                                // 更新消息列表
                                ChatMessage chatMessage = new ChatMessage(nickname, message, ChatMessage.RECEIVE, System.currentTimeMillis(), false);
                                activity.myChatView.getMyChatMessageList().add(chatMessage);
                                activity.myChatView.getAdapter().notifyDataSetChanged();
                                // 列表跳转到最新消息位置
                                if (activity.oldPosition != 0) {
                                    activity.myChatView.getChatListView().setSelection(activity.oldPosition);
                                }
                                activity.oldPosition = activity.myChatView.getMyChatMessageList().size();
                            }
                            break;
                        }
                        case Protocol.EXIT_ROOM: { // 退出房间
                            int stateCode = content.getInt(0);
                            switch (stateCode) {
                                case Protocol.EXIT_ROOM_NOT_IN: { // 用户不在任何房间
                                    MyLog.d(TAG, "用户不在该房间，退出错误");
                                    break;
                                }
                                case Protocol.EXIT_ROOM_SUCCESS: { // 退出成功
                                    MyLog.d(TAG, "退出房间成功");
                                    // 清空保存的房间
                                    RoomController.getInstance().setRoom(null);
                                    break;
                                }
                                case Protocol.EXIT_ROOM_UNKNOW_PRO: { // 未知错误
                                    MyLog.d(TAG, "退出房间出现未知错误");
                                    break;
                                }
                            }
                            break;
                        }
                        case Protocol.GET_ROOM_MEMBER: { // 房间成员列表信息
                            int index = 0;
                            int stateCode = content.getInt(index++);
                            switch (stateCode) {
                                case Protocol.GET_ROOM_MEMBER_SUCCESS: { // 获取成员列表成功
                                    // 检查房间id
                                    int roomId = content.getInt(index++);
                                    if (roomId != activity.roomId) {
                                        MyLog.d(TAG, "获取了错误房间的成员列表");
                                        break;
                                    }
                                    // 获取房间成员
                                    int memberNumber = content.getInt(index++);
                                    activity.memberList.clear();
                                    for (int i = 0; i < memberNumber; i++) {
                                        String username = content.getString(index++);
                                        String nickname = content.getString(index++);
                                        boolean admin = content.getBoolean(index++);
                                        activity.memberList.add(new Member(nickname, admin));
                                    }
                                    // 更新成员列表
                                    activity.memberAdapter.notifyDataSetChanged();
                                    showToast("成员列表已更新");
                                    break;
                                }
                                case Protocol.GET_ROOM_MEMBER_UNKNOW_PRO: { // 未知错误
                                    MyLog.d(TAG, "获取成员列表出现未知错误");
                                    break;
                                }
                                case Protocol.GET_ROOM_MEMBER_WRONG_ROOM_ID: { // 房间id错误
                                    MyLog.d(TAG, "在错误的房间获取成员列表");
                                    break;
                                }
                            }
                            break;
                        }
                        case Protocol.DRAW: { // 绘制反馈
                            int stateCode = content.getInt(0);
                            switch (stateCode) {
                                case Protocol.DRAW_SUCCESS: {
                                    MyLog.d(TAG, "发送绘制线段成功");
                                    break;
                                }
                                case Protocol.DRAW_UNKNOW_PRO: { // 未知错误
                                    MyLog.d(TAG, "发生未知错误，发送绘制线段失败");
                                    break;
                                }
                                case Protocol.DRAW_WRONG_ROOM_ID: { // 房间id错误
                                    MyLog.d(TAG, "房间id错误，发送绘制线段失败");
                                    break;
                                }
                            }
                            break;
                        }
                        case Protocol.DRAW_PUSH: { // 绘制推送
                            int index = 0;
                            int roomId = content.getInt(index++);
                            String username = content.getString(index++);

                            if (roomId != activity.roomId) { // 判断房间id
                                MyLog.d(TAG, "接收到无效的绘制线段，房间id错误");
                            } else if (username == activity.username) {
                                MyLog.d(TAG, "接收到无效的绘制线段，用户名重复");
                            } else { // 绘制线段
                                int pointNumber = content.getInt(index++);
                                Point points[] = new Point[pointNumber];
                                for (int i = 0; i < pointNumber; i++) {
                                    double x = content.getDouble(index++);
                                    double y = content.getDouble(index++);
                                    points[i] = new Point(x, y);
                                }
                                int color = content.getInt(index++);
                                double paintWidth = content.getDouble(index++);
                                boolean isEraser = content.getBoolean(index++);
                                int width = content.getInt(index++);
                                int height = content.getInt(index++);
                                Line line = new Line(points, color, paintWidth, isEraser, width, height);
                                activity.myPaintView.drawLine(line);
                            }
                            break;
                        }
//                        case Protocol.GET_DRAW_LIST: { // 同步绘制消息
//                            int stateCode = content.getInt(0);
//                            switch (stateCode) {
//                                case Protocol.GET_DRAW_LIST_SUCCESS: { // 同步绘制消息成功
//                                    MyLog.d(TAG, "正在同步绘制消息");
//                                    break;
//                                }
//                                case Protocol.GET_DRAW_LIST_WRONG_ROOM_ID: { // 房间id错误，无法同步绘制消息
//                                    MyLog.d(TAG, "房间id错误，无法同步绘制消息");
//                                    break;
//                                }
//                                case Protocol.GET_DRAW_LIST_UNKONW_PRO: { // 未知错误，无法同步绘制消息
//                                    MyLog.d(TAG, "未知错误，无法同步绘制消息");
//                                    break;
//                                }
//                            }
//                            break;
//                        }
                    }
                } catch (JSONException e) {
                    MyLog.d(TAG, Log.getStackTraceString(e));
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
