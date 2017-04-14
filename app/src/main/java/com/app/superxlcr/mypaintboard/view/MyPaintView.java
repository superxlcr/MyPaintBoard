package com.app.superxlcr.mypaintboard.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.app.superxlcr.mypaintboard.controller.DrawController;
import com.app.superxlcr.mypaintboard.model.Line;
import com.app.superxlcr.mypaintboard.model.Point;
import com.app.superxlcr.mypaintboard.utils.MyLog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by superxlcr on 2017/1/22.
 * 绘制画板View
 */

public class MyPaintView extends View {

    private static final String TAG = MyPaintView.class.getSimpleName();

    private static final int POINT_ARRAY_SIZE = 5; // 每组发送的点数大小
    private static final int REPEAT_TIMES = 5; // 发送失败重新发送次数
    private static final int FAIL_RELAX_TIME = 100; // 发送失败间隔时间
    private static final int ERASER_WIDTH = 40; // 橡皮擦宽度

    // 画板大小
    private int width = 0, height = 0;

    // 画笔颜色
    private int paintColor;
    // 画笔宽度
    private float paintWidth;

    // 图片缓冲内存
    private Bitmap cacheBitmap;
    // 画布缓冲区
    private Canvas cacheCanvas;

    // 本地未确认线段缓存区
    private Map<Long, Line> localLineList;
    // 图片缓冲内存
    private Bitmap localLineCacheBitmap;
    // 画布缓冲区
    private Canvas localLineCacheCanvas;

    // 是否擦除模式
    private boolean isEraser;

    // 点列表
    private List<Point> pointList;
    // 房间id
    private int roomId;
    // 用于回调消息
    private Handler handler = null;

    /**
     * 清空屏幕
     */
    public void clearDraw() {
        // 设置背景为白色
        setBackgroundColor(Color.WHITE);
        // 清空笔画
        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        cacheCanvas.drawPaint(paint);
        // 清空本地未确认线段
        localLineList.clear();
        // 刷新画面
        invalidate();
    }

    /**
     * 绘制线段
     *
     * @param line 线段
     */
    public void drawLine(Line line) {
        MyLog.d(TAG, "draw line to cache!");
        Paint linePaint = new Paint(Paint.DITHER_FLAG);
        linePaint.setColor(line.getColor()); // 颜色
        linePaint.setStyle(Paint.Style.STROKE); // 实心
        linePaint.setStrokeWidth((float) line.getPaintWidth()); // 长度
        // 反锯齿
        linePaint.setAntiAlias(true);
        linePaint.setDither(true);
        if (line.isEraser()) {
            // 透明色，橡皮擦固定大小
            linePaint.setColor(Color.TRANSPARENT);
            linePaint.setStrokeWidth(ERASER_WIDTH);
            linePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }
        List<Point> list = line.getPointList();
        Path linePath = new Path();
        int lineWidth = line.getWidth();
        int lineHeight = line.getHeight();
        float lineLastX = ((float) list.get(0).getX() * width) / lineWidth;
        float lineLastY = ((float) list.get(0).getY() * height) / lineHeight;
        linePath.moveTo(lineLastX, lineLastY);
        for (int i = 1; i < list.size(); i++) {
            float x = ((float) list.get(i).getX() * width) / lineWidth;
            float y = ((float) list.get(i).getY() * height) / lineHeight;
            linePath.quadTo(lineLastX, lineLastY, x, y);
            lineLastX = x;
            lineLastY = y;
        }
        // 绘制到缓冲区
        cacheCanvas.drawPath(linePath, linePaint);
        // 刷新视图
        invalidate();
    }

    /**
     * 设置画笔颜色
     *
     * @param color 颜色
     */
    public void setPaintColor(int color) {
        paintColor = color;
    }

    /**
     * 设置画笔宽度
     *
     * @param width 宽度
     */
    public void setPaintWidth(float width) {
        paintWidth = width;
    }

    /**
     * 确认本地线段
     *
     * @param time 线段协议发送时间
     * @param draw 是否绘制到缓存区
     */
    public void confirmLocalLine(long time, boolean draw) {
        List<Long> deleteTimeList = new ArrayList<>();
        for (Long keyTime : localLineList.keySet()) {
            if (keyTime <= time) {
                // 绘制到缓存区
                if (draw) {
                    drawLine(localLineList.get(keyTime));
                }
                // 已确认线段
                deleteTimeList.add(keyTime);
            }
        }
        // 移除相应线段
        for (Long ketTime : deleteTimeList) {
            localLineList.remove(ketTime);
        }
    }

    public boolean isEraser() {
        return isEraser;
    }

    public void setEraser(boolean eraser) {
        isEraser = eraser;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    @Override
    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (cacheBitmap == null) {
            // 设置白色背景
            setBackgroundColor(Color.WHITE);
            // 创建view大小的画布
            width = right - left;
            height = bottom - top;
            cacheBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            // 初始化画布
            cacheCanvas = new Canvas();
            cacheCanvas.setBitmap(cacheBitmap);
            // 初始化本地线段有序map
            localLineList = new LinkedHashMap<>();
            // 初始化本地线段画布
            localLineCacheBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            localLineCacheCanvas = new Canvas();
            localLineCacheCanvas.setBitmap(localLineCacheBitmap);
            // 初始化画笔
            paintColor = Color.BLACK;
            paintWidth = 1;
            // 画笔模式
            isEraser = false;
            // 初始化点数列表
            pointList = new ArrayList<>();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX(), y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: { // 第一次点击用于记录坐标
                // 清空并添加新的点
                pointList.clear();
                pointList.add(new Point(x, y));
                break;
            }
            case MotionEvent.ACTION_MOVE: { // 移动的同时绘制线段
                // 添加新的点，判断是否需要发送
                pointList.add(new Point(x, y));
                // 发送绘制线段
                if (pointList.size() == POINT_ARRAY_SIZE) {
                    if (handler == null) {
                        Toast.makeText(getContext(), "还没有设置Handler!", Toast.LENGTH_SHORT).show();
                    }
                    Point points[] = new Point[POINT_ARRAY_SIZE];
                    for (int i = 0; i < pointList.size(); i++) {
                        points[i] = pointList.get(i);
                    }
                    Line line = new Line(points, paintColor, paintWidth, isEraser, width, height);
                    // 断线重发机制
                    int counter = 0;
                    long time = System.currentTimeMillis();
                    while (!DrawController.getInstance().sendDraw(getContext(), handler, time, roomId, line) && counter < REPEAT_TIMES) {
                        counter++;
                        try {
                            Thread.sleep(FAIL_RELAX_TIME);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    // 列表只留下最后一个点
                    for (int i = 0; i < POINT_ARRAY_SIZE - 1; i++) {
                        pointList.remove(0);
                    }
                    // 本地线段添加
                    localLineList.put(time, line);
                    // 刷新界面
                    invalidate();
                }
                break;
            }
            case MotionEvent.ACTION_UP: { // 把线段绘制到缓存中
                // 绘制剩余的线段
                if (handler == null) {
                    Toast.makeText(getContext(), "还没有设置Handler!", Toast.LENGTH_SHORT).show();
                }
                Point points[] = new Point[pointList.size()];
                for (int i = 0; i < pointList.size(); i++) {
                    points[i] = pointList.get(i);
                }
                Line line = new Line(points, paintColor, paintWidth, isEraser, width, height);
                // 断线重发机制
                int counter = 0;
                long time = System.currentTimeMillis();
                while (!DrawController.getInstance().sendDraw(getContext(), handler, time, roomId, line) && counter < REPEAT_TIMES) {
                    counter++;
                    try {
                        Thread.sleep(FAIL_RELAX_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // 清空列表
                pointList.clear();
                // 本地线段添加
                localLineList.put(time, line);
                // 刷新界面
                invalidate();
                break;
            }
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 清空本地线段缓存
        Paint clearPaint = new Paint();
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        localLineCacheCanvas.drawPaint(clearPaint);
        // 绘制本地未确认线段
        for (Line line : localLineList.values()) {
            Paint linePaint = new Paint(Paint.DITHER_FLAG);
            linePaint.setColor(line.getColor()); // 颜色
            linePaint.setStyle(Paint.Style.STROKE); // 实心
            linePaint.setStrokeWidth((float) line.getPaintWidth()); // 长度
            // 反锯齿
            linePaint.setAntiAlias(true);
            linePaint.setDither(true);
            if (line.isEraser()) {
                linePaint.setColor(Color.TRANSPARENT);
                linePaint.setStrokeWidth(ERASER_WIDTH);
                linePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            }
            List<Point> list = line.getPointList();
            Path linePath = new Path();
            // 本地线段不需要缩放
            float lineLastX = (float) list.get(0).getX();
            float lineLastY = (float) list.get(0).getY();
            linePath.moveTo(lineLastX, lineLastY);
            for (int i = 1; i < list.size(); i++) {
                float x = (float) list.get(i).getX();
                float y = (float) list.get(i).getY();
                linePath.quadTo(lineLastX, lineLastY, x, y);
                lineLastX = x;
                lineLastY = y;
            }
            // 绘制到缓冲区
            localLineCacheCanvas.drawPath(linePath, linePaint);
        }
        Paint bmpPaint = new Paint();
        // 绘制之前的缓存轨迹
        canvas.drawBitmap(cacheBitmap, 0, 0, bmpPaint);
        // 绘制本地线段缓存
        canvas.drawBitmap(localLineCacheBitmap, 0, 0, bmpPaint);
    }

    public MyPaintView(Context context) {
        super(context);
    }

    public MyPaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyPaintView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
