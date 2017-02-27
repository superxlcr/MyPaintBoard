package com.app.superxlcr.mypaintboard.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.superxlcr.mypaintboard.R;
import com.app.superxlcr.mypaintboard.controller.CommunicationController;
import com.app.superxlcr.mypaintboard.controller.RoomController;
import com.app.superxlcr.mypaintboard.controller.UserController;
import com.app.superxlcr.mypaintboard.model.Protocol;
import com.app.superxlcr.mypaintboard.model.Room;
import com.app.superxlcr.mypaintboard.model.User;
import com.app.superxlcr.mypaintboard.utils.LoadingDialogUtils;
import com.app.superxlcr.mypaintboard.utils.MyLog;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by superxlcr on 2017/1/13.
 * 房间列表主界面
 */

public class MainActivity extends AppCompatActivity {

    private static String TAG = MainActivity.class.getSimpleName();

    private static MyHandler handler = new MyHandler();

    private Button createRoomBtn;
    private Button updateRoomListBtn;

    private ListView roomListView;
    private List<Room> roomList;
    private MyAdapter adapter;

    private Dialog dialog; // 进度条
    private long time = 0; // 发送消息时间

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 设置昵称
        if (UserController.getInstance().getUser() == null) {
            // 没有user说明没有登录，重新进行登录
            MyLog.d(TAG, "获取登录用户失败！");
            showToast("您还没有进行登录，请进行登录");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        // 初始化Handler
        handler.setReference(new SoftReference<MainActivity>(this));

        // 初始化列表
        roomListView = (ListView) findViewById(R.id.room_list);
        roomList = new ArrayList<>();
        adapter = new MyAdapter(this, R.layout.item_room, roomList);
        roomListView.setAdapter(adapter);
        // listView item点击事件
        roomListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 进入房间
                Room room = roomList.get(position);
                time = System.currentTimeMillis();
                dialog = LoadingDialogUtils.showDialog(MainActivity.this, "正在加入房间...", true);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        // 关闭进度条
                        LoadingDialogUtils.closeDialog(dialog);
                        // 更新时间，使过去协议失效
                        time += 1;
                        // 退出房间
                        RoomController.getInstance().exitRoom(MainActivity.this, handler, System.currentTimeMillis());
                    }
                });
                RoomController.getInstance().joinRoom(MainActivity.this, handler, time, room.getId());
            }
        });

        // 初始化view
        createRoomBtn = (Button) findViewById(R.id.create_room);
        createRoomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建房间
                final EditText et = new EditText(MainActivity.this);
                new AlertDialog.Builder(MainActivity.this).setTitle("请输入房间名称").setView(et).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        if (et.getText().toString().isEmpty()) {
                            showToast("请输入房间名称");
                        } else {
                            // 创建房间
                            String roomName = et.getText().toString();
                            time = System.currentTimeMillis();
                            dialog = LoadingDialogUtils.showDialog(MainActivity.this, "正在创建房间...", true);
                            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialogInterface) {
                                    // 关闭进度条
                                    LoadingDialogUtils.closeDialog(dialog);
                                    // 更新时间，使过去协议失效
                                    time += 1;
                                    // 退出房间
                                    RoomController.getInstance().exitRoom(MainActivity.this, handler, System.currentTimeMillis());
                                }
                            });
                            if (!RoomController.getInstance().createRoom(MainActivity.this, handler, time, roomName)) {
                                // 没发送成功，关闭进度条
                                LoadingDialogUtils.closeDialog(dialog);
                            }
                        }
                    }
                }).setNegativeButton("取消", null).show();
            }
        });
        updateRoomListBtn = (Button) findViewById(R.id.update_room_list);
        updateRoomListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 更新房间列表
                updateRoomList();
            }
        });

        // 初次更新房间列表
        updateRoomList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        User user = UserController.getInstance().getUser();
        String username = user.getUsername();
        String nickname = user.getNickname();
        setTitle(nickname + "(" + username + ")");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_info: {
                // 编辑个人信息界面
                Intent intent = new Intent(MainActivity.this, EditInfoActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.logout: {
                // 登录注销
                // 关闭与服务器连接
                CommunicationController.getInstance(MainActivity.this).clearSocket();
                // 返回登录界面
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;
            }
        }
        return true;
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    // 获取房间列表
    private void updateRoomList() {
        time = System.currentTimeMillis();
        dialog = LoadingDialogUtils.showDialog(this, "正在获取房间列表...", true);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                // 关闭进度条
                LoadingDialogUtils.closeDialog(dialog);
                // 更新时间，使过去协议失效
                time += 1;
            }
        });
        RoomController.getInstance().getRoomList(this, handler, time);
    }

    static class MyHandler extends Handler {

        private SoftReference<MainActivity> reference;

        public MyHandler() {
        }

        public void setReference(SoftReference<MainActivity> reference) {
            this.reference = reference;
        }

        @Override
        public void handleMessage(Message msg) {
            // 处理指令
            MainActivity activity = reference.get();
            if (activity != null && msg != null && msg.obj != null && msg.obj instanceof Protocol) {
                Protocol protocol = (Protocol) msg.obj;
                int order = protocol.getOrder();
                long time = protocol.getTime();
                JSONArray content = protocol.getContent();
                if (time >= activity.time) { // 协议消息没有过期
                    // 关闭进度条
                    LoadingDialogUtils.closeDialog(activity.dialog);
                    try {
                        switch (order) {
                            case Protocol.GET_ROOM_LIST: { // 获取房间列表
                                List<Room> list = activity.roomList;
                                int index = 0;
                                int roomNumber = content.getInt(index++);
                                // 刷新房间列表
                                list.clear();
                                for (int i = 0; i < roomNumber; i++) {
                                    int roomId = content.getInt(index++);
                                    String roomName = content.getString(index++);
                                    int roomMemberNumber = content.getInt(index++);
                                    Room room = new Room(null, roomId, roomName);
                                    room.setRoomMemberNumber(roomMemberNumber);
                                    list.add(room);
                                }
                                // 更新listView
                                activity.adapter.notifyDataSetChanged();
                                // 更新RoomController
                                RoomController.getInstance().setList(new ArrayList<Room>(list));
                                // 显示Toast
                                showToast("房间列表更新成功");
                                break;
                            }
                            case Protocol.CREATE_ROOM: { // 创建房间
                                int roomId = content.getInt(0);
                                String roomName = content.getString(1);
                                // 保存房间
                                Room room = new Room(UserController.getInstance().getUser(), roomId, roomName);
                                RoomController.getInstance().setRoom(room);
                                showToast("房间创建成功");
                                // 跳转至房间界面
                                Intent intent = new Intent(activity, RoomActivity.class);
                                activity.startActivity(intent);
                                break;
                            }
                            case Protocol.JOIN_ROOM: { // 加入房间
                                int stateCode = content.getInt(0);
                                switch (stateCode) {
                                    case Protocol.JOIN_ROOM_ALREADY_IN: { // 用户已在该房间
                                        showToast("您已在该房间中");
                                        break;
                                    }
                                    case Protocol.JOIN_ROOM_INVALID_ROOMID: { // 房间id错误
                                        showToast("该房间已失效，请更新房间列表");
                                        // 更新房间列表
                                        activity.updateRoomList();
                                        break;
                                    }
                                    case Protocol.JOIN_ROOM_SUCCESS: { // 加入房间成功
                                        int roomId = content.getInt(1);
                                        String roomName = content.getString(2);
                                        // 保存房间
                                        Room room = new Room(UserController.getInstance().getUser(), roomId, roomName);
                                        RoomController.getInstance().setRoom(room);
                                        showToast("加入房间成功");
                                        // 跳转至房间界面
                                        Intent intent = new Intent(activity, RoomActivity.class);
                                        activity.startActivity(intent);
                                        break;
                                    }
                                    case Protocol.JOIN_ROOM_UNKNOW_PRO: { // 未知错误
                                        showToast("出现未知错误");
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                    } catch (JSONException e) {
                        MyLog.e(TAG, Log.getStackTraceString(e));
                        showToast("协议内容解析错误");
                    }
                }
            }
        }

        private void showToast(String msg) {
            if (reference != null && reference.get() != null) {
                Toast.makeText(reference.get(), msg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    static class MyAdapter extends ArrayAdapter<Room> {

        private LayoutInflater inflater;
        private int resourceId;
        private List<Room> list;

        MyAdapter(Context context, int resource, List<Room> objects) {
            super(context, resource, objects);
            inflater = LayoutInflater.from(context);
            resourceId = resource;
            list = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            TextView roomIdTV;
            TextView roomNameTV;
            TextView roomMemberNumberTV;
            // 初始化view
            if (convertView == null) {
                view = inflater.inflate(resourceId, parent, false);
                roomIdTV = (TextView) view.findViewById(R.id.room_id);
                roomNameTV = (TextView) view.findViewById(R.id.room_name);
                roomMemberNumberTV = (TextView) view.findViewById(R.id.room_member_number);
                view.setTag(new ViewHolder(roomIdTV, roomNameTV, roomMemberNumberTV));
            } else {
                view = convertView;
                ViewHolder holder = (ViewHolder) view.getTag();
                roomIdTV = holder.roomIdTV;
                roomNameTV = holder.roomNameTV;
                roomMemberNumberTV = holder.roomMemberNumberTV;
            }
            // 设置内容
            Room room = list.get(position);
            roomIdTV.setText(room.getId() + "");
            roomNameTV.setText(room.getRoomName());
            roomMemberNumberTV.setText(room.getRoomMemberNumber() + "");
            return view;
        }

        class ViewHolder {
            TextView roomIdTV;
            TextView roomNameTV;
            TextView roomMemberNumberTV;

            ViewHolder(TextView roomIdTV, TextView roomNameTV, TextView roomMemberNumberTV) {
                this.roomIdTV = roomIdTV;
                this.roomNameTV = roomNameTV;
                this.roomMemberNumberTV = roomMemberNumberTV;
            }
        }
    }
}
