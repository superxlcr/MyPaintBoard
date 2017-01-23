package com.app.superxlcr.mypaintboard.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.app.superxlcr.mypaintboard.controller.DrawController;
import com.app.superxlcr.mypaintboard.model.Line;
import com.app.superxlcr.mypaintboard.model.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by superxlcr on 2017/1/22.
 * 绘制画板View
 */

public class MyPaintView extends View {

    // TODO 貌似越大越好
    private static final int POINT_ARRAY_SIZE = 15; // 每组发送的点数大小

    // 点击坐标
    private float lastX = 0, lastY = 0;
    // 画板大小
    private int width = 0, height = 0;
    // 绘制路径
    private Path path;
    // 画笔
    private Paint paint;
    // 图片缓冲内存
    private Bitmap cacheBitmap;
    // 画布缓冲区
    private Canvas cacheCanvas;
    // 是否擦除模式
    private boolean isEraser;

    // 点列表
    private List<Point> pointList;
    // 房间id
    private int roomId;
    // 用于回调消息
    private Handler handler = null;

    /**
     * 绘制线段
     *
     * @param line 线段
     */
    public void drawLine(Line line) {
        Paint linePaint = new Paint(Paint.DITHER_FLAG);
        linePaint.setColor(line.getColor()); // 颜色
        linePaint.setStyle(Paint.Style.STROKE); // 实心
        linePaint.setStrokeWidth((float) line.getPaintWidth()); // 长度
        // 反锯齿
        linePaint.setAntiAlias(true);
        linePaint.setDither(true);
        // TODO 擦除模式
        if (line.isEraser()) {

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

    // TODO 设置线段颜色宽度擦除

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
            // 初始化路径
            path = new Path();
            // 初始化画笔
            paint = new Paint(Paint.DITHER_FLAG); // 防抖动
            paint.setColor(Color.BLACK); // 颜色
            paint.setStyle(Paint.Style.STROKE); // 实心
            paint.setStrokeWidth(3); // 长度
            // 反锯齿
            paint.setAntiAlias(true);
            paint.setDither(true);
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
                lastX = x;
                lastY = y;
                path.moveTo(x, y);
                // 清空并添加新的点
                pointList.clear();
                pointList.add(new Point(x, y));
                break;
            }
            case MotionEvent.ACTION_MOVE: { // 移动的同时绘制线段
                path.quadTo(lastX, lastY, x, y);
                lastX = x;
                lastY = y;
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
                    Line line = new Line(points, paint.getColor(), paint.getStrokeWidth(), isEraser, width, height);
                    // TODO 断线重发机制
                    DrawController.getInstance().sendDraw(getContext(), handler, System.currentTimeMillis(), roomId, line);
                    // 列表只留下最后一个点
                    for (int i = 0; i < POINT_ARRAY_SIZE - 1; i++) {
                        pointList.remove(0);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP: { // 把线段绘制到缓存中
                cacheCanvas.drawPath(path, paint);
                path.reset();
                // 绘制剩余的线段
                if (handler == null) {
                    Toast.makeText(getContext(), "还没有设置Handler!", Toast.LENGTH_SHORT).show();
                }
                Point points[] = new Point[pointList.size()];
                for (int i = 0; i < pointList.size(); i++) {
                    points[i] = pointList.get(i);
                }
                Line line = new Line(points, paint.getColor(), paint.getStrokeWidth(), isEraser, width, height);
                // TODO 断线重发机制
                DrawController.getInstance().sendDraw(getContext(), handler, System.currentTimeMillis(), roomId, line);
                // 清空列表
                pointList.clear();
                break;
            }
        }
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint bmpPaint = new Paint();
        // 绘制之前的缓存轨迹
        canvas.drawBitmap(cacheBitmap, 0, 0, bmpPaint);
        // 绘制当前的轨迹
        canvas.drawPath(path, paint);
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
