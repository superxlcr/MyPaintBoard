package com.app.superxlcr.mypaintboard.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.util.Log;

import com.app.superxlcr.mypaintboard.controller.CommunicationController;
import com.app.superxlcr.mypaintboard.model.Protocol;

import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by superxlcr on 2017/3/10.
 * 上传文件的工具类
 */

public class UploadFileUtil {

    private static final String TAG = UploadFileUtil.class.getSimpleName();

    private UploadFileUtil() {
    }

    private static boolean flag;

    /**
     * 上传图片文件到服务器，自带阻塞与进度条
     *
     * @param context 上下文
     * @param pic     图片
     */
    public static void uploadPic(final Context context, final Uri pic) {
        flag = true;
        final Dialog dialog = LoadingDialogUtils.showDialog(context, "正在上传图片...", true);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                flag = false;
            }
        });
        // 子线程上传
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream is = context.getApplicationContext().getContentResolver().openInputStream(pic);
                    byte[] fileBytes = new byte[1024];
                    int len;
                    while ((len = is.read(fileBytes, 0, fileBytes.length)) > 0) {
                        if (!flag) { // 传输被终止
                            break;
                        }
                        JSONArray content = new JSONArray();
                        // stateCode + len + file
                        content.put(Protocol.UPLOAD_PIC_CONTINUE);
                        content.put(len);
                        String str = new String(fileBytes, 0, len, "ISO-8859-1");
                        content.put(str);
//                MyLog.d(TAG, "len : " + len + "fileBytesLen : " + fileBytes.length + "strLen : " + str.getBytes("ISO-8859-1").length);
                        Protocol protocol = new Protocol(Protocol.UPLOAD_PIC, System.currentTimeMillis(), content);
                        CommunicationController.getInstance(context).sendProtocol(protocol);
                    }
                    if (flag) { // 没用中止传输
                        // 发送finish信号
                        JSONArray content = new JSONArray();
                        // stateCode + len + file
                        content.put(Protocol.UPLOAD_PIC_FINISH);
                        Protocol protocol = new Protocol(Protocol.UPLOAD_PIC, System.currentTimeMillis(), content);
                        CommunicationController.getInstance(context).sendProtocol(protocol);
                    }
                } catch (IOException e) {
                    MyLog.e(TAG, Log.getStackTraceString(e));
                } finally {
                    flag = false;
                    LoadingDialogUtils.closeDialog(dialog);
                }
            }
        }).start();
    }
}
