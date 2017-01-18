package com.app.superxlcr.mypaintboard.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.superxlcr.mypaintboard.R;

/**
 * Created by superxlcr on 2017/1/8.
 * 等待进度条工具
 */

public class LoadingDialogUtils {

    /**
     * 显示进度条
     *
     * @param context      上下文
     * @param msg          显示内容
     * @param isCancelable 是否可以点击取消
     * @return
     */
    public static Dialog showDialog(Context context, String msg, boolean isCancelable) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.dialog_loading, null);             // 得到加载view
        RelativeLayout layout = (RelativeLayout) v.findViewById(R.id.dialog_view);// 加载布局

        // main.xml中的ImageView
        ImageView spaceshipImage = (ImageView) v.findViewById(R.id.img);
        TextView tipTextView = (TextView) v.findViewById(R.id.tipTextView);   // 提示文字
        // 加载旋转动画
        Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(context, R.anim.rotate_animation);
        // 使用ImageView显示动画
        spaceshipImage.startAnimation(hyperspaceJumpAnimation);
        tipTextView.setText(msg);// 设置加载信息

        Dialog loadingDialog = new Dialog(context, R.style.WhiteDialogStyle);    // 创建自定义样式dialog
        loadingDialog.setContentView(layout);
        loadingDialog.setCancelable(isCancelable);
        loadingDialog.setCanceledOnTouchOutside(false);

        Window window = loadingDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = DensityUtil.dip2px(context, 165); // 165dp
        window.setGravity(Gravity.CENTER);
        window.setAttributes(lp);
        window.setWindowAnimations(R.style.PopWindowAnimStyle);
        loadingDialog.show();
        return loadingDialog;
    }

    /**
     * 关闭进度条
     *
     * @param mDialog
     *
     * @return 是否关闭成功
     */
    public static boolean closeDialog(Dialog mDialog) {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            return true;
        }
        return false;
    }

    private LoadingDialogUtils() {
    }

}
