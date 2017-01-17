package com.app.superxlcr.mypaintboard.view;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.app.superxlcr.mypaintboard.R;
import com.app.superxlcr.mypaintboard.model.ChatMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by superxlcr on 2017/1/15.
 * 聊天 View
 */

public class MyChatView extends LinearLayout {

    private ListView chatListView;
    private EditText inputET;
    private Button sendBtn;

    // 聊天列表相关
    private MyAdapter adapter;
    private List<ChatMessage> myChatMessageList;

    private void init(Context context) {
        setOrientation(LinearLayout.VERTICAL);
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        layoutInflater.inflate(R.layout.view_my_chat, this);
        inputET = (EditText) findViewById(R.id.input);
        sendBtn = (Button) findViewById(R.id.send);
        // 列表初始化
        chatListView = (ListView) findViewById(R.id.chat_list_view);
        // 关闭点击事件
        chatListView.setClickable(false);
        chatListView.setLongClickable(false);
        myChatMessageList = new ArrayList<>();
        adapter = new MyAdapter(context, myChatMessageList);
        chatListView.setAdapter(adapter);
    }

    public ListView getChatListView() {
        return chatListView;
    }

    public EditText getInputET() {
        return inputET;
    }

    public Button getSendBtn() {
        return sendBtn;
    }

    public MyAdapter getAdapter() {
        return adapter;
    }

    public List<ChatMessage> getMyChatMessageList() {
        return myChatMessageList;
    }

    static class MyAdapter extends ArrayAdapter<ChatMessage> {

        private LayoutInflater inflater;
        private List<ChatMessage> list;
        private Animation hyperspaceJumpAnimation; // 旋转动画

        public MyAdapter(Context context, List<ChatMessage> objects) {
            super(context, 0, objects);
            inflater = LayoutInflater.from(context);
            list = objects;
            hyperspaceJumpAnimation = AnimationUtils.loadAnimation(context, R.anim.rotate_animation);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // 初始化view
            View view = null;
            if (convertView == null) {
                switch (getItemViewType(position)) {
                    case ChatMessage.SEND: {
                        view = inflater.inflate(R.layout.item_chat_right, parent, false);
                        TextView nicknameTV = (TextView) view.findViewById(R.id.nickname);
                        TextView messageTV = (TextView) view.findViewById(R.id.message);
                        ImageView imageView = (ImageView) view.findViewById(R.id.img);
                        TextView timeTV = (TextView) view.findViewById(R.id.time);
                        ViewHolder holder = new ViewHolder(nicknameTV, messageTV, imageView, timeTV);
                        view.setTag(holder);
                        break;
                    }
                    case ChatMessage.RECEIVE: {
                        view = inflater.inflate(R.layout.item_chat_left, parent, false);
                        TextView nicknameTV = (TextView) view.findViewById(R.id.nickname);
                        TextView messageTV = (TextView) view.findViewById(R.id.message);
                        ImageView imageView = (ImageView) view.findViewById(R.id.img);
                        TextView timeTV = (TextView) view.findViewById(R.id.time);
                        ViewHolder holder = new ViewHolder(nicknameTV, messageTV, imageView, timeTV);
                        view.setTag(holder);
                        break;
                    }
                }
            } else {
                view = convertView;
            }

            // 填充条目内容
            ViewHolder holder = (ViewHolder) (view.getTag());
            ChatMessage item = list.get(position);
            holder.nicknameTV.setText(item.getNickname());
            holder.messageTV.setText(item.getMessage());
            holder.timeTV.setText(item.getSimpleFormatTime());
            // TODO 发送失败打个叉
            // 是否显示正在发送动画
            if (item.isWaiting()) {
                holder.imageView.setVisibility(View.VISIBLE);
                holder.imageView.startAnimation(hyperspaceJumpAnimation);
            } else {
                holder.imageView.setVisibility(View.GONE);
                holder.imageView.clearAnimation();
            }
            return view;
        }

        @Override
        public int getItemViewType(int position) {
            // 获取当前条目类型
            return list.get(position).getState();
        }

        @Override
        public int getViewTypeCount() {
            // 界面总类型
            return 2;
        }
    }

    static class ViewHolder {

        TextView nicknameTV;
        TextView messageTV;
        ImageView imageView;
        TextView timeTV;

        public ViewHolder(TextView nicknameTV, TextView messageTV, ImageView imageView, TextView timeTV) {
            this.nicknameTV = nicknameTV;
            this.messageTV = messageTV;
            this.imageView = imageView;
            this.timeTV = timeTV;
        }
    }

    public MyChatView(Context context) {
        super(context);
        init(context);
    }

    public MyChatView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MyChatView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
}
