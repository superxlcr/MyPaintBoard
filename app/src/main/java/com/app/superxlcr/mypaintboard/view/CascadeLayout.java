package com.app.superxlcr.mypaintboard.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

/**
 * Created by superxlcr on 2017/1/19.
 * 层叠Layout
 */

public class CascadeLayout extends FrameLayout {

    public static final int CLOSE = 0;
    public static final int OPEN = 1;

    private View topView; // 最顶部的View
    private View triggerView; // 用于触发动画的View
    private int state;
    private int animatorTime; // 动画时间
    private boolean moving; // 是否正在执行动画
    private CascadeLayoutListener listener; // 监听器

    public void setTriggerView(View triggerView) {
        this.triggerView = triggerView;
        triggerView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!moving) {
                    executeMove();
                }
            }
        });
    }

    public View getTopView() {
        return topView;
    }

    public int getAnimatorTime() {
        return animatorTime;
    }

    public void setAnimatorTime(int animatorTime) {
        this.animatorTime = animatorTime;
    }

    public void setListener(CascadeLayoutListener listener) {
        this.listener = listener;
    }

    public int getState() {
        return state;
    }

    private void init() {
        topView = null;
        triggerView = null;
        state = CLOSE;
        animatorTime = 3000;
        moving = false;
        listener = null;
    }

    private void executeMove() {
        if (listener != null) {
            listener.onOpenLayout();
        }
        // 获取view与距离
        topView = getChildAt(getChildCount() - 1);
        int distance = topView.getWidth() - triggerView.getWidth();
        // 正在移动
        moving = true;

        ValueAnimator animator = ValueAnimator.ofInt();
        // 根据当前状态选择动画
        if (state == CLOSE) {
            animator.setIntValues(0, distance);
        } else if (state == OPEN) {
            animator.setIntValues(distance, 0);
        }
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(animatorTime);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                topView.scrollTo((Integer)animation.getAnimatedValue(), 0);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // 动画结束
                moving = false;
                // 更改状态
                if (state == CLOSE) {
                    state = OPEN;
                } else if (state == OPEN) {
                    state = CLOSE;
                }
            }
        });
        animator.start();
    }

    // 执行动画时拦截点击效果
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return moving;
    }

    public CascadeLayout(Context context) {
        super(context);
        init();
    }

    public CascadeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CascadeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    interface CascadeLayoutListener {

        void onOpenLayout();
    }

}
