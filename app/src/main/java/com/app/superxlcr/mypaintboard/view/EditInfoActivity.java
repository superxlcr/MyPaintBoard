package com.app.superxlcr.mypaintboard.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.app.superxlcr.mypaintboard.R;
import com.app.superxlcr.mypaintboard.controller.UserController;
import com.app.superxlcr.mypaintboard.model.Protocol;
import com.app.superxlcr.mypaintboard.model.User;
import com.app.superxlcr.mypaintboard.tools.LoadingDialogUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.SoftReference;

/**
 * Created by superxlcr on 2017/1/14.
 * 编辑用户信息界面
 */

public class EditInfoActivity extends Activity {

    private static Handler handler;

    private TextView accountTV;
    private EditText passwordET;
    private EditText nicknameET;
    private Button confirmBtn;
    private Button cancelBtn;

    private Dialog dialog;
    private long time; // 发送协议时间

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_info);

        handler = new MyHandler(this);

        accountTV = (TextView) findViewById(R.id.account);
        accountTV.setText(UserController.getInstance().getUser().getUsername());
        passwordET = (EditText) findViewById(R.id.password);
        nicknameET = (EditText) findViewById(R.id.nickname);
        nicknameET.setText(UserController.getInstance().getUser().getNickname());
        confirmBtn = (Button) findViewById(R.id.confirm);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nicknameET.getText().toString().isEmpty()) {
                    Toast.makeText(EditInfoActivity.this, "昵称不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    time = System.currentTimeMillis();
                    dialog = LoadingDialogUtils.showDialog(EditInfoActivity.this, "正在更新用户信息...", true);
                    dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            time += 1; // 使协议过期
                            LoadingDialogUtils.closeDialog(dialog);
                        }
                    });
                    // 发送更新信息消息
                    String username = accountTV.getText().toString();
                    String password = passwordET.getText().toString();
                    String nickname = nicknameET.getText().toString();
                    UserController.getInstance().editInfo(EditInfoActivity.this, handler, time, username, password, nickname);
                }
            }
        });
        cancelBtn = (Button) findViewById(R.id.cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    static class MyHandler extends Handler {

        private SoftReference<EditInfoActivity> reference;

        public MyHandler(EditInfoActivity activity) {
            reference = new SoftReference<EditInfoActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            // 处理指令
            EditInfoActivity activity = reference.get();
            if (activity != null && msg != null && msg.obj != null && msg.obj instanceof Protocol) {
                Protocol protocol = (Protocol) msg.obj;
                if (protocol.getOrder() == Protocol.EDIT_INFO && protocol.getTime() >= activity.time) {
                    // 关闭进度条
                    LoadingDialogUtils.closeDialog(activity.dialog);
                    JSONArray content = protocol.getContent();
                    try {
                        int stateCode = content.getInt(0);
                        switch (stateCode) {
                            case Protocol.EDIT_INFO_REPEAT_NICKNAME: { // 昵称重复
                                showToast("您要修改的昵称已被使用");
                                break;
                            }
                            case Protocol.EDIT_INFO_SUCCESS: { // 编辑信息成功
                                showToast("更新用户信息成功");
                                // 保存新的用户信息
                                String username = activity.accountTV.getText().toString();
                                String nickname = activity.nicknameET.getText().toString();
                                User oldUser = UserController.getInstance().getUser();
                                User newUser = new User(oldUser.getId(), username, "*", nickname);
                                UserController.getInstance().setUser(newUser);
                                break;
                            }
                            case Protocol.EDIT_INFO_UNKNOW_PRO: { // 未知错误
                                showToast("出现未知错误");
                                break;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
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
}
