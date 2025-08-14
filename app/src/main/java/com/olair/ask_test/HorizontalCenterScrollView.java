package com.olair.ask_test;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

/**
 * 可以横向滑动并自动居中子视图的自定义组件
 * 支持点击子视图自动居中，并提供居中事件回调
 */
public class HorizontalCenterScrollView extends HorizontalScrollView {
    
    private static final int SCROLL_DURATION = 300; // 滑动动画时长
    
    private LinearLayout mContentLayout;
    private OnCenterChangeListener mOnCenterChangeListener;
    private Runnable mScrollEndRunnable;
    private int mCurrentCenterIndex = -1;
    private boolean mIsScrolling = false;
    
    /**
     * 居中变化监听器
     */
    public interface OnCenterChangeListener {
        /**
         * 当子视图居中时调用
         * @param centerView 居中的视图
         * @param centerIndex 居中视图的索引
         */
        void onCenterChanged(View centerView, int centerIndex);
    }
    
    public HorizontalCenterScrollView(Context context) {
        super(context);
        init();
    }
    
    public HorizontalCenterScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public HorizontalCenterScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // 创建内容容器
        mContentLayout = new LinearLayout(getContext());
        mContentLayout.setOrientation(LinearLayout.HORIZONTAL);
        addView(mContentLayout, new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        
        // 滑动结束检测
        mScrollEndRunnable = new Runnable() {
            @Override
            public void run() {
                if (!mIsScrolling) {
                    centerToNearestChild();
                }
            }
        };
    }
    
    /**
     * 添加子视图
     * @param child 要添加的子视图
     */
    public void addChildView(View child) {
        addChildView(child, -1);
    }
    
    /**
     * 添加子视图到指定位置
     * @param child 要添加的子视图
     * @param index 插入位置，-1表示添加到末尾
     */
    public void addChildView(View child, int index) {
        // 为子视图添加点击监听器
        child.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                centerToChild(v, true);
            }
        });
        
        if (index == -1) {
            mContentLayout.addView(child);
        } else {
            mContentLayout.addView(child, index);
        }
    }
    
    /**
     * 移除子视图
     * @param child 要移除的子视图
     */
    public void removeChildView(View child) {
        mContentLayout.removeView(child);
    }
    
    /**
     * 移除指定位置的子视图
     * @param index 要移除的子视图索引
     */
    public void removeChildViewAt(int index) {
        if (index >= 0 && index < mContentLayout.getChildCount()) {
            mContentLayout.removeViewAt(index);
        }
    }
    
    /**
     * 获取子视图数量
     */
    public int getChildViewCount() {
        return mContentLayout.getChildCount();
    }
    
    /**
     * 获取指定位置的子视图
     */
    public View getChildViewAt(int index) {
        if (index >= 0 && index < mContentLayout.getChildCount()) {
            return mContentLayout.getChildAt(index);
        }
        return null;
    }
    
    /**
     * 设置居中变化监听器
     */
    public void setOnCenterChangeListener(OnCenterChangeListener listener) {
        mOnCenterChangeListener = listener;
    }
    
    /**
     * 将指定的子视图居中
     * @param child 要居中的子视图
     * @param smooth 是否使用平滑滚动
     */
    public void centerToChild(View child, boolean smooth) {
        int index = findChildIndex(child);
        if (index != -1) {
            centerToChildAt(index, smooth);
        }
    }
    
    /**
     * 将指定索引的子视图居中
     * @param index 子视图索引
     * @param smooth 是否使用平滑滚动
     */
    public void centerToChildAt(int index, boolean smooth) {
        if (index < 0 || index >= mContentLayout.getChildCount()) {
            return;
        }
        
        View child = mContentLayout.getChildAt(index);
        int childCenterX = child.getLeft() + child.getWidth() / 2;
        int scrollViewCenterX = getWidth() / 2;
        int targetScrollX = childCenterX - scrollViewCenterX;
        
        // 确保滚动范围在有效范围内
        targetScrollX = Math.max(0, Math.min(targetScrollX, 
                mContentLayout.getWidth() - getWidth()));
        
        if (smooth) {
            smoothScrollTo(targetScrollX, 0);
        } else {
            scrollTo(targetScrollX, 0);
        }
        
        // 更新当前居中的索引并触发回调
        updateCenterIndex(index);
    }
    
    /**
     * 自动居中到最近的子视图
     */
    private void centerToNearestChild() {
        if (mContentLayout.getChildCount() == 0) {
            return;
        }
        
        int scrollX = getScrollX();
        int scrollViewCenterX = scrollX + getWidth() / 2;
        int nearestIndex = 0;
        int minDistance = Integer.MAX_VALUE;
        
        // 找到距离中心最近的子视图
        for (int i = 0; i < mContentLayout.getChildCount(); i++) {
            View child = mContentLayout.getChildAt(i);
            int childCenterX = child.getLeft() + child.getWidth() / 2;
            int distance = Math.abs(childCenterX - scrollViewCenterX);
            
            if (distance < minDistance) {
                minDistance = distance;
                nearestIndex = i;
            }
        }
        
        centerToChildAt(nearestIndex, true);
    }
    
    /**
     * 查找子视图的索引
     */
    private int findChildIndex(View child) {
        for (int i = 0; i < mContentLayout.getChildCount(); i++) {
            if (mContentLayout.getChildAt(i) == child) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * 更新当前居中的索引并触发回调
     */
    private void updateCenterIndex(int newIndex) {
        if (mCurrentCenterIndex != newIndex) {
            mCurrentCenterIndex = newIndex;
            if (mOnCenterChangeListener != null && newIndex >= 0 && 
                    newIndex < mContentLayout.getChildCount()) {
                View centerView = mContentLayout.getChildAt(newIndex);
                mOnCenterChangeListener.onCenterChanged(centerView, newIndex);
            }
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsScrolling = true;
                removeCallbacks(mScrollEndRunnable);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsScrolling = false;
                // 延迟检测滚动结束，因为可能还有惯性滚动
                postDelayed(mScrollEndRunnable, 100);
                break;
        }
        return super.onTouchEvent(ev);
    }
    
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        
        // 如果不是主动滚动，则重新安排滚动结束检测
        if (!mIsScrolling) {
            removeCallbacks(mScrollEndRunnable);
            postDelayed(mScrollEndRunnable, 100);
        }
    }
    
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        
        // 布局完成后，如果还没有居中任何子视图，则居中第一个
        if (changed && mCurrentCenterIndex == -1 && mContentLayout.getChildCount() > 0) {
            post(new Runnable() {
                @Override
                public void run() {
                    centerToChildAt(0, false);
                }
            });
        }
    }
    
    /**
     * 获取当前居中的子视图索引
     */
    public int getCurrentCenterIndex() {
        return mCurrentCenterIndex;
    }
    
    /**
     * 获取当前居中的子视图
     */
    public View getCurrentCenterView() {
        if (mCurrentCenterIndex >= 0 && mCurrentCenterIndex < mContentLayout.getChildCount()) {
            return mContentLayout.getChildAt(mCurrentCenterIndex);
        }
        return null;
    }
}