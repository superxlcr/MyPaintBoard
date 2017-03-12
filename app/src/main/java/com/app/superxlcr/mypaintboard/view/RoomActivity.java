package com.app.superxlcr.mypaintboard.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
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
import com.app.superxlcr.mypaintboard.controller.CommunicationController;
import com.app.superxlcr.mypaintboard.controller.DrawController;
import com.app.superxlcr.mypaintboard.controller.MemberController;
import com.app.superxlcr.mypaintboard.controller.RoomController;
import com.app.superxlcr.mypaintboard.controller.UserController;
import com.app.superxlcr.mypaintboard.model.ChatMessage;
import com.app.superxlcr.mypaintboard.model.Line;
import com.app.superxlcr.mypaintboard.model.Point;
import com.app.superxlcr.mypaintboard.model.Protocol;
import com.app.superxlcr.mypaintboard.model.Room;
import com.app.superxlcr.mypaintboard.model.User;
import com.app.superxlcr.mypaintboard.utils.LoadingDialogUtils;
import com.app.superxlcr.mypaintboard.utils.MyLog;
import com.app.superxlcr.mypaintboard.utils.UploadFileUtil;
import com.readystatesoftware.viewbadger.BadgeView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by superxlcr on 2017/1/15.
 * 房间界面
 */

public class RoomActivity extends BaseActivity {

    private static String TAG = RoomActivity.class.getSimpleName();

    private static MyHandler handler = new MyHandler();

    private User user;
    private Room room;
    private boolean isAdmin;

    // 绘制view
    private MyPaintView myPaintView;

    // 层叠view
    private CascadeLayout cascadeLayout;
    private TextView triggerView;

    // 上传图片选项
    private MenuItem uploadPicItem;

    // 成员相关
    private ListView memberListView;
    private List<Member> memberList;
    private MemberAdapter memberAdapter;

    // 聊天相关
    private BadgeView badgeView;
    private MyChatView myChatView;
    private int newMessageNumber; // 新消息条目数
    private int oldPosition; // 旧消息位置

    // 上传图片相关
    private static final int TAKE_PHOTO = 0;
    private static final int FROM_LOCAL = 1;
    private static final int CROP_PHOTO = 2;

    private final String photoTempName = "tempPhoto";
    private Uri imageUri;

    // 接收背景图片用
    private FileOutputStream fos;
    private Dialog receiveBgPicDialog;

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
        room = RoomController.getInstance().getRoom();

        // 初始化用户名昵称
        if (UserController.getInstance().getUser() == null) {
            MyLog.d(TAG, "获取已登录用户失败！");
            Toast.makeText(this, "您还未进行登录", Toast.LENGTH_SHORT).show();
            finish();
        }
        user = UserController.getInstance().getUser();

        // 是否房间管理员初始化为false
        isAdmin = false;

        // 初始化相片存储地址
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File outputImage = new File(path, photoTempName + ".jpg");
        try {
            if (outputImage.exists()) {
                outputImage.delete();
            }
            outputImage.createNewFile();
        } catch (IOException e) {
            MyLog.e(TAG, Log.getStackTraceString(e));
        }
        imageUri = Uri.fromFile(outputImage);

        // TODO 语音系统

        // 绘制view
        myPaintView = (MyPaintView) findViewById(R.id.my_paint_view);
        myPaintView.setHandler(handler);
        myPaintView.setRoomId(room.getId());
        // 设置监听器
        DrawController.getInstance().setReceiveDrawHandler(this, handler);
        DrawController.getInstance().setReceiveBgPicHandler(this, handler);

//        // 获取旧线段
        DrawController.getInstance().getDrawList(this, handler, System.currentTimeMillis(), room.getId());

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
        MemberController.getInstance().getRoomMember(this, room.getId(), System.currentTimeMillis());

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
                    if (ChatController.getInstance().sendMessage(RoomActivity.this, handler, time, room.getId(), msg)) {
                        // 正在发送
                        ChatMessage chatMessage = new ChatMessage(user.getNickname(), msg, ChatMessage.SEND, time, true);
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
        // 一般情况下隐藏这个选项
        uploadPicItem = menu.findItem(R.id.upload_pic);
        if (!isAdmin) {
            uploadPicItem.setVisible(false);
        } else {
            uploadPicItem.setVisible(true);
        }

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
            case R.id.upload_pic: // 上传图片
                // 请求传输图片
                DrawController.getInstance().askUploadPic(this, handler);
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case TAKE_PHOTO: { // 拍照
                    if (imageUri != null) {
                        cropPhoto(imageUri);
                    } else {
                        MyLog.e(TAG, "take_photo null!");
                    }
                    break;
                }
                case FROM_LOCAL: { // 从相册中选择
                    if (data != null && data.getData() != null) {
                        cropPhoto(data.getData());
                    } else {
                        MyLog.e(TAG, "from_local null!");
                    }
                    break;
                }
                case CROP_PHOTO: {
                    // 开始上传图片
                    UploadFileUtil.uploadPic(this, imageUri);
                    // 设置图片背景
                    try {
                        InputStream is = getContentResolver().openInputStream(imageUri);
                        Bitmap imageBitmap = BitmapFactory.decodeStream(is);
                        myPaintView.setBackground(new BitmapDrawable(imageBitmap));
                    } catch (FileNotFoundException e) {
                        MyLog.e(TAG, Log.getStackTraceString(e));
                    }
                    break;
                }
                default:
                    break;
            }
        } else {
            Toast.makeText(this, "获取图片失败，请重新尝试！", Toast.LENGTH_SHORT).show();
        }
    }

    // 选择上传图片
    private void selectUploadPic() {
        String[] items = new String[]{"拍照", "从相册中选择"};
        new AlertDialog.Builder(this).setTitle("请选择上传图片").setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: { // 拍照
                        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        startActivityForResult(intent, TAKE_PHOTO);
                        break;
                    }
                    case 1: { // 从相册中选择
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(intent, FROM_LOCAL);
                        break;
                    }
                    default:
                        break;
                }
            }
        }).create().show();
    }

    private void cropPhoto(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 保持裁剪比例
        intent.putExtra("scale", true);
        // 裁剪比例
        intent.putExtra("aspectX", myPaintView.getWidth());
        intent.putExtra("aspectY", myPaintView.getHeight());
        // 裁剪宽高
        intent.putExtra("outputX", myPaintView.getWidth());
        intent.putExtra("outputY", myPaintView.getHeight());
        // 文件输出位置
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CROP_PHOTO);
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
                            if (roomId != activity.room.getId()) {
                                MyLog.d(TAG, "接收到无效的消息推送，房间id错误");
                            } else if (nickname == activity.user.getNickname()) {
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
                                    if (roomId != activity.room.getId()) {
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
                                        // 房间管理员为本人，开启上传图片选项
                                        if (admin && activity.user.getUsername().equals(username)) {
                                            activity.isAdmin = true;
                                            if (activity.uploadPicItem != null) {
                                                // 已获取选项
                                                activity.uploadPicItem.setVisible(true);
                                            }
                                        }
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

                            if (roomId != activity.room.getId()) { // 判断房间id
                                MyLog.d(TAG, "接收到无效的绘制线段，房间id错误");
                            } else if (username == activity.user.getUsername()) {
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
                        case Protocol.GET_DRAW_LIST: { // 同步绘制消息
                            int stateCode = content.getInt(0);
                            switch (stateCode) {
                                case Protocol.GET_DRAW_LIST_SUCCESS: { // 同步绘制消息成功
                                    showToast("正在同步绘制消息");
                                    MyLog.d(TAG, "正在同步绘制消息");
                                    break;
                                }
                                case Protocol.GET_DRAW_LIST_WRONG_ROOM_ID: { // 房间id错误，无法同步绘制消息
                                    MyLog.d(TAG, "房间id错误，无法同步绘制消息");
                                    break;
                                }
                                case Protocol.GET_DRAW_LIST_UNKONW_PRO: { // 未知错误，无法同步绘制消息
                                    MyLog.d(TAG, "未知错误，无法同步绘制消息");
                                    break;
                                }
                            }
                            break;
                        }
                        case Protocol.UPLOAD_PIC: {
                            int respondCode = content.getInt(0);
                            switch (respondCode) {
                                case Protocol.UPLOAD_PIC_OK: { // 可以上传图片
                                    // 打开选择图片，裁剪界面
                                    activity.selectUploadPic();
                                    break;
                                }
                                case Protocol.UPLOAD_PIC_FAIL: { // 禁止上传图片
                                    showToast("服务器禁止传输图片");
                                    MyLog.d(TAG, "服务器禁止传输图片");
                                    break;
                                }
                            }
                        }
                        case Protocol.BG_PIC_PUSH: { // 背景图片推送
                            int code = content.getInt(0);
                            switch (code) {
                                case Protocol.BG_PIC_PUSH_ASK: { // 请求传输
                                    try {
                                        // 打开文件流
                                        File image = new File(new URI(activity.imageUri.toString()));
                                        if (image.exists()) {
                                            image.delete();
                                        }
                                        image.createNewFile();
                                        activity.fos = new FileOutputStream(image);
                                        // 回复确认接收背景图片
                                        JSONArray sendContent = new JSONArray();
                                        sendContent.put(Protocol.BG_PIC_PUSH_OK);
                                        Protocol sendProtocol = new Protocol(Protocol.BG_PIC_PUSH, System.currentTimeMillis(), sendContent);
                                        CommunicationController.getInstance(activity).sendProtocol(sendProtocol);
                                        // 开启等待进度条
                                        activity.receiveBgPicDialog = LoadingDialogUtils.showDialog(activity, "正在更新背景图片...", false);
                                    } catch (URISyntaxException | IOException e) {
                                        MyLog.e(TAG, Log.getStackTraceString(e));
                                        // 发送拒绝接收背景图片
                                        JSONArray sendContent = new JSONArray();
                                        sendContent.put(Protocol.BG_PIC_PUSH_FAIL);
                                        Protocol sendProtocol = new Protocol(Protocol.BG_PIC_PUSH, System.currentTimeMillis(), sendContent);
                                        CommunicationController.getInstance(activity).sendProtocol(sendProtocol);
                                    }
                                    break;
                                }
                                case Protocol.BG_PIC_PUSH_CONTINUE: { // 继续传输文件
                                    int len = content.getInt(1);
                                    String bytesStr = content.getString(2);
                                    if (activity.fos != null) {
                                        try {
                                            byte[] fileBytes = bytesStr.getBytes("ISO-8859-1");
                                            activity.fos.write(fileBytes, 0, fileBytes.length);
                                            activity.fos.flush();
                                        } catch (IOException e) {
                                            MyLog.e(TAG, Log.getStackTraceString(e));
                                            // 关闭进度条
                                            LoadingDialogUtils.closeDialog(activity.receiveBgPicDialog);
                                        }
                                    }
                                    break;
                                }
                                case Protocol.BG_PIC_PUSH_FINISH: { // 传输文件结束
                                    if (activity.fos != null) {
                                        try {
                                            activity.fos.close();
                                        } catch (IOException e) {
                                            MyLog.e(TAG, Log.getStackTraceString(e));
                                        }
                                        // 设置图片背景
                                        try {
                                            InputStream is = activity.getContentResolver().openInputStream(activity.imageUri);
                                            Bitmap imageBitmap = BitmapFactory.decodeStream(is);
                                            activity.myPaintView.setBackground(new BitmapDrawable(imageBitmap));
                                        } catch (FileNotFoundException e) {
                                            MyLog.e(TAG, Log.getStackTraceString(e));
                                        }
                                        // 关闭进度条
                                        LoadingDialogUtils.closeDialog(activity.receiveBgPicDialog);
                                    }
                                    break;
                                }
                            }
                        }
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
