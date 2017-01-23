package com.app.superxlcr.mypaintboard.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.app.superxlcr.mypaintboard.R;
import com.app.superxlcr.mypaintboard.controller.CommunicationController;
import com.app.superxlcr.mypaintboard.controller.UserController;
import com.app.superxlcr.mypaintboard.model.Protocol;
import com.app.superxlcr.mypaintboard.model.User;
import com.app.superxlcr.mypaintboard.utils.LoadingDialogUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.SoftReference;

/**
 * Created by superxlcr on 2017/1/10.
 * 登录界面
 */

public class LoginActivity extends AppCompatActivity {

    private static MyHandler handler = new MyHandler();

    private EditText accountEt;
    private EditText passwordEt;
    private Button loginBtn;
    private Button registerBtn;

    private Dialog dialog; // 等待进度条
    private long time = 0; // 最后发送消息的时间

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化服务器连接
        CommunicationController.getInstance(this).connectServer();

        handler.setReference(new SoftReference<LoginActivity>(this));
        accountEt = (EditText) findViewById(R.id.account);
        passwordEt = (EditText) findViewById(R.id.password);
        loginBtn = (Button) findViewById(R.id.login);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkAccountAndPassword()) {
                    // 登录
                    String username = accountEt.getText().toString();
                    String password = passwordEt.getText().toString();
                    // 更新发送时间
                    time = System.currentTimeMillis();
                    // 显示进度条，可以取消
                    dialog = LoadingDialogUtils.showDialog(LoginActivity.this, "登录中...", true);
                    dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            // 发送时间加一，导致遗留协议失效
                            time += 1;
                            LoadingDialogUtils.closeDialog(dialog);
                        }
                    });
                    if (!UserController.getInstance().login(LoginActivity.this, handler, time, username, password)) {
                        // 发送失败直接关闭进度条
                        LoadingDialogUtils.closeDialog(dialog);
                    }
                }
            }
        });
        registerBtn = (Button) findViewById(R.id.register);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 注册，打开注册界面
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private boolean checkAccountAndPassword() {
        if (accountEt != null && passwordEt != null) {
            String account = accountEt.getText().toString();
            if (account.isEmpty()) {
                Toast.makeText(this, "请填入您的账号", Toast.LENGTH_SHORT).show();
                return false;
            }
            String password = passwordEt.getText().toString();
            if (password.isEmpty()) {
                Toast.makeText(this, "请填入您的密码", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }
        return false;
    }

    static class MyHandler extends Handler {

        private SoftReference<LoginActivity> reference;

        public MyHandler() {
        }

        public void setReference(SoftReference<LoginActivity> reference) {
            this.reference = reference;
        }

        @Override
        public void handleMessage(Message msg) {
            LoginActivity activity = reference.get();
            if (activity != null && msg != null && msg.obj != null && msg.obj instanceof Protocol) {
                Protocol protocol = (Protocol)msg.obj;
                if (protocol.getOrder() == Protocol.LOGIN && activity.time <= protocol.getTime()) {
                    // 登录指令且消息时间有效
                    JSONArray content = protocol.getContent();
                    // 关闭等待进度条
                    LoadingDialogUtils.closeDialog(activity.dialog);
                    // 处理登录信息
                    try {
                        int stateCode = content.getInt(0);
                        switch (stateCode) {
                            case Protocol.LOGIN_ALREADY_LOGIN: { // 已登录
                                showToast("该用户已经登录");
                                break;
                            }
                            case Protocol.LOGIN_NO_USERNAME: { // 非法用户名
                                showToast("用户名不存在");
                                break;
                            }
                            case Protocol.LOGIN_SUCCESS: { // 登录成功
                                // 保存用户数据
                                int id = content.getInt(1);
                                String username = content.getString(2);
                                String nickname = content.getString(3);
                                UserController.getInstance().setUser(new User(id, username, "*", nickname));
                                // 显示信息
                                showToast("登录成功");
                                // 界面跳转
                                Intent intent = new Intent(activity, MainActivity.class);
                                activity.startActivity(intent);
                                activity.finish();
                                break;
                            }
                            case Protocol.LOGIN_UNKNOW_PRO: { // 未知错误
                                showToast("遇到未知错误");
                                break;
                            }
                            case Protocol.LOGIN_WRONG_PASSWORD: { // 密码错误
                                showToast("密码错误");
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
            Toast.makeText(reference.get(), msg, Toast.LENGTH_SHORT).show();
        }
    }
}