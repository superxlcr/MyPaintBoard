package com.app.superxlcr.mypaintboard.view;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
 * Created by superxlcr on 2017/1/10.
 * 注册界面
 */

public class RegisterActivity extends AppCompatActivity {

    private MyHandler handler;

    private EditText accountEt;
    private EditText passwordEt;
    private EditText nicknameEt;
    private Button confirmBtn;
    private Button cancelBtn;
    private Dialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        handler = new MyHandler(this);
        accountEt = (EditText) findViewById(R.id.account);
        passwordEt = (EditText) findViewById(R.id.password);
        nicknameEt = (EditText) findViewById(R.id.nickname);
        confirmBtn = (Button) findViewById(R.id.confirm);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 确认
                if (checkInput()) {
                    String username = accountEt.getText().toString();
                    String password = passwordEt.getText().toString();
                    String nickname = nicknameEt.getText().toString();
//                    if (UserController.getInstance().register(RegisterActivity.this, handler, username, password, nickname)) {
//                        // TODO 可以取消，新时间覆盖问题
//                        dialog = LoadingDialogUtils.showDialog(RegisterActivity.this, "注册中...", false);
//                    }
                }
            }
        });
        cancelBtn = (Button) findViewById(R.id.cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 取消，返回登录界面
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public Dialog getDialog() {
        return dialog;
    }

    private boolean checkInput() {
        if (accountEt != null && passwordEt != null && nicknameEt != null) {
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
            String nickname = nicknameEt.getText().toString();
            if (nickname.isEmpty()) {
                Toast.makeText(this, "请填入您的昵称", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }
        return false;
    }

    static class MyHandler extends Handler {

        private SoftReference<RegisterActivity> reference;

        public MyHandler(RegisterActivity activity) {
            reference = new SoftReference<RegisterActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            RegisterActivity activity = reference.get();
            if (activity != null && msg != null && msg.obj != null && msg.obj instanceof Protocol) {
                Protocol protocol = (Protocol)msg.obj;
                if (protocol.getOrder() == Protocol.REGISTER) { // 注册
                    JSONArray content = protocol.getContent();
                    // 关闭等待进度条
                    if (activity.getDialog() != null) {
                        LoadingDialogUtils.closeDialog(activity.getDialog());
                    }
                    // 处理注册信息
                    try {
                        int stateCode = content.getInt(0);
                        switch (stateCode) {
                            case Protocol.REGISTER_REPEAT_NICKNAME: { // 昵称重复
                                showToast("您的昵称已被使用");
                                break;
                            }
                            case Protocol.REGISTER_REPEAT_USERNAME: { // 用户名重复
                                showToast("您的用户名已被使用");
                                break;
                            }
                            case Protocol.REGISTER_SUCCESS: { // 注册成功
                                // 保存用户数据
                                String username = content.getString(1);
                                String nickname = content.getString(2);
                                UserController.getInstance().setUser(new User(username, "*", nickname));
                                // 显示信息
                                showToast("注册成功");
                                // TODO 界面跳转
                                activity.finish();
                            }
                            case Protocol.REGISTER_UNKNOW_PRO: { // 未知错误
                                showToast("遇到未知错误");
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
