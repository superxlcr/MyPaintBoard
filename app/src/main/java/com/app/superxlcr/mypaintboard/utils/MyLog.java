package com.app.superxlcr.mypaintboard.utils;

import android.util.Log;

/**
 * Created by superxlcr on 2017/2/27.
 * 自定义工具Log类
 */

public class MyLog {

    private static String MY_TAG = "MyPaintBoard";

    public static int v(String TAG, String msg) {
        return Log.v(MY_TAG, TAG + " | " + msg);
    }

    public static int d(String TAG, String msg) {
        return Log.d(MY_TAG, TAG + " | " + msg);
    }

    public static int i(String TAG, String msg) {
        return Log.i(MY_TAG, TAG + " | " + msg);
    }

    public static int w(String TAG, String msg) {
        return Log.w(MY_TAG, TAG + " | " + msg);
    }

    public static int e(String TAG, String msg) {
        return Log.e(MY_TAG, TAG + " | " + msg);
    }
}
