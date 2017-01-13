package com.app.superxlcr.mypaintboard.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.superxlcr.mypaintboard.R;
import com.app.superxlcr.mypaintboard.controller.UserController;
import com.app.superxlcr.mypaintboard.model.Room;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by superxlcr on 2017/1/13.
 * 房间列表主界面
 */

public class MainActivity extends AppCompatActivity {

    private static MyHandler handler;

    private TextView usernameTV;
    private ImageView editInfoIV;
    private ImageView logoutIV;
    private Button createRoomBtn;
    private Button updateRoomListBtn;

    private ListView roomListView;
    private List<Room> roomList;
    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 设置昵称
        usernameTV = (TextView) findViewById(R.id.username);
        if (UserController.getInstance().getUser() != null) {
            usernameTV.setText(UserController.getInstance().getUser().getNickname());
        } else {
            // 没有user说明没有登录，重新进行登录
            showToast("您还没有进行登录，请进行登录");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        // 初始化Handler
        handler = new MyHandler(this);

        // 初始化列表
        roomListView = (ListView) findViewById(R.id.room_list);
        roomList = new ArrayList<>();
        adapter = new MyAdapter(this, R.layout.item_room, roomList);
        roomListView.setAdapter(adapter);

        // 初始化view
        editInfoIV = (ImageView) findViewById(R.id.edit_info);
        editInfoIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO 编辑个人信息
            }
        });
        logoutIV = (ImageView) findViewById(R.id.logout);
        logoutIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO 登录注销
            }
        });
        createRoomBtn = (Button) findViewById(R.id.create_room);
        createRoomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO 创建房间
            }
        });
        updateRoomListBtn = (Button) findViewById(R.id.update_room_list);
        updateRoomListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO 更新房间列表
            }
        });
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    static class MyHandler extends Handler {

        private SoftReference<MainActivity> reference;

        public MyHandler(MainActivity activity) {
            reference = new SoftReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            // TODO 处理指令
        }
    }

    static class MyAdapter extends ArrayAdapter<Room> {

        private LayoutInflater inflater;
        private int resourceId;
        private List<Room> list;

        MyAdapter(Context context, int resource, List<Room> objects) {
            super(context, resource, objects);
            inflater = LayoutInflater.from(context);
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
                view = inflater.inflate(resourceId, parent);
                roomIdTV = (TextView)view.findViewById(R.id.room_id);
                roomNameTV = (TextView)view.findViewById(R.id.room_name);
                roomMemberNumberTV = (TextView)view.findViewById(R.id.room_member_number);
                view.setTag(new ViewHolder(roomIdTV, roomNameTV, roomMemberNumberTV));
            } else {
                view = convertView;
                ViewHolder holder = (ViewHolder)view.getTag();
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
