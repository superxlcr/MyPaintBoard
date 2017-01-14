package com.app.superxlcr.mypaintboard.view;

import android.app.Application;

import com.app.superxlcr.mypaintboard.controller.CommunicationController;

/**
 * Created by superxlcr on 2017/1/14.
 */

public class MyApplication extends Application {

    @Override
    public void onTerminate() {
        // 关闭应用时断开连接
        CommunicationController.getInstance(this).clearSocket();
    }
}
