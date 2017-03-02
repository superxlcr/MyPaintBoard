package com.app.superxlcr.mypaintboard.view;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by superxlcr on 2017/3/2.
 * 基础Activity提供Activity管理功能
 */

public class BaseActivity extends AppCompatActivity {

    private static List<Activity> activityList = new LinkedList<>();

    /**
     * 关闭所有已开启的activity
     */
    public static void removeAllActivity() {
        for (Activity activity : activityList) {
            activity.finish();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityList.add(this);
    }

    @Override
    protected void onDestroy() {
        activityList.remove(this);
        super.onDestroy();
    }
}
