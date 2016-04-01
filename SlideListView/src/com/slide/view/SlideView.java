package com.slide.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Scroller;

import com.slide.demo.R;

/**
 * 
 * @author tangjiabing
 * 
 * @see 开源时间：2016年04月01日
 * 
 *      记得给我个star哦~
 * 
 */
public class SlideView extends LinearLayout {

	private static final int SCROLL_OUT = 0x11; // 已滑出来了
	private static final int SCROLLING = 0x12; // 正在滑动
	private static final int NOT_SCROLL = 0x13; // 不能滑动
	// 用来控制滑动角度，仅当角度a满足条件时才进行滑动：tan a = deltaX / deltaY
	private static final int TAN_ANGLE = 50; // 正切角度
	private static final int QUICK_TAN_ANGLE = 70; // 快速滑动时要求的正切角度
	private static final int QUICK_SNAP_VELOCITY = 900; // 快速的手指滑动速度
	private static final int NORMAL_SNAP_VELOCITY = 600; // 普通的手指滑动速度
	private static final int SLOW_SNAP_VELOCITY = 100; // 缓慢的手指滑动速度
	private int mTouchSlop = 0; // 用户滑动的最小距离
	private double mTanValue = 0; // 正切值
	private double mQuickTanValue = 0; // 快速滑动时要求的正切值
	private int mLastX = 0; // 上次滑动的x坐标
	private int mLastY = 0; // 上次滑动的y坐标
	private int mDownX = 0; // 按下去时的x坐标
	private int mDownY = 0; // 按下去时的y坐标
	private int mScrollStatus = 0; // 滑动状态
	private Scroller mScroller = null; // 弹性滑动对象，提供弹性滑动效果
	private FrameLayout mItemContentView = null; // 用来放置所有view的容器
	private FrameLayout mSlideContentView = null; // 用来放置滑动view的容器
	private int mSlideContentWidth = 0; // 滑动view的宽度
	private VelocityTracker mVelocityTracker = null; // 速度追踪对象
	private boolean mIsCanLeftSlide = true; // SlideView是否可以左滑，true为可以

	public SlideView(Context context) {
		this(context, null);
	}

	public SlideView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SlideView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public void setItemContentView(View v) {
		mItemContentView.addView(v);
	}

	public void setSlideContentView(View v) {
		mSlideContentView.addView(v);
	}

	public void quickReset() {
		if (!mScroller.isFinished())
			mScroller.abortAnimation();
		scrollTo(0, 0);
		mScrollStatus = 0;
		recycleVelocityTracker();
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		}
	}

	public void setCanLeftSlide(boolean flag) {
		mIsCanLeftSlide = flag;
	}

	// ***********************************************************************
	// 保护方法

	protected void onHandleTouchEvent(MotionEvent event) {
		if (mIsCanLeftSlide == true) {
			int x = (int) event.getX();
			int y = (int) event.getY();
			int scrollX = getScrollX();
			addVelocityTracker(event);

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mDownX = x;
				mDownY = y;
				break;
			case MotionEvent.ACTION_MOVE:
				int deltaX = x - mLastX;
				int deltaY = y - mLastY;
				int xVelocity = 0;
				int yVelocity = 0;

				if (mScrollStatus == 0 || mScrollStatus == SCROLL_OUT) {
					xVelocity = Math.abs(getScrollXVelocity());
					yVelocity = Math.abs(getScrollYVelocity());

					if (xVelocity > SLOW_SNAP_VELOCITY
							|| Math.abs(x - mDownX) > mTouchSlop) {
						double tan = 0;
						if (xVelocity < QUICK_SNAP_VELOCITY)
							tan = mTanValue;
						else
							tan = mQuickTanValue;
						if (Math.abs(deltaX) >= Math.abs(deltaY) * tan)
							mScrollStatus = SCROLLING;
						else
							mScrollStatus = NOT_SCROLL;
					} else if (yVelocity > SLOW_SNAP_VELOCITY
							|| Math.abs(y - mDownY) > mTouchSlop)
						mScrollStatus = NOT_SCROLL;
				}

				if (mScrollStatus == SCROLLING) {
					if (deltaX != 0) {
						int newScrollX = scrollX - deltaX;
						if (newScrollX < 0)
							newScrollX = 0;
						else if (newScrollX > mSlideContentWidth)
							newScrollX = mSlideContentWidth;
						scrollTo(newScrollX, 0);
					}
				}

				break;
			case MotionEvent.ACTION_UP:
				if (mScrollStatus == SCROLLING) {
					int direction = 0; // 滑动方向
					int left = -1;
					int right = 1;
					xVelocity = getScrollXVelocity();
					if (xVelocity > 0)
						direction = right;
					else
						direction = left;

					mScrollStatus = 0;
					int newScrollX = 0;
					if (Math.abs(xVelocity) > NORMAL_SNAP_VELOCITY) {
						if (direction == left) {
							mScrollStatus = SCROLL_OUT;
							newScrollX = mSlideContentWidth;
						}
					} else {
						double scale = 0;
						if (direction == left)
							scale = 0.4;
						else
							scale = 0.6;

						if (scrollX > mSlideContentWidth * scale) {
							mScrollStatus = SCROLL_OUT;
							newScrollX = mSlideContentWidth;
						}
					}
					smoothScrollTo(newScrollX, 0);
				} else
					mScrollStatus = 0;
				recycleVelocityTracker();
				break;
			}

			mLastX = x;
			mLastY = y;
		} else
			mScrollStatus = NOT_SCROLL;
	}

	protected void reset() {
		smoothScrollTo(0, 0);
		mScrollStatus = 0;
		recycleVelocityTracker();
	}

	protected boolean isScrollOut() {
		if (mScrollStatus == SCROLL_OUT)
			return true;
		else
			return false;
	}

	protected boolean isScrolling() {
		if (mScrollStatus == SCROLLING)
			return true;
		else
			return false;
	}

	protected boolean isNotScroll() {
		if (mScrollStatus == NOT_SCROLL)
			return true;
		else
			return false;
	}

	protected boolean isCanLeftSlide() {
		return mIsCanLeftSlide;
	}

	protected void setItemContentViewPressed(boolean flag) {
		setPressed(mItemContentView, flag);
	}

	protected void setItemContentViewEnabled(boolean flag) {
		setEnabled(mItemContentView, flag);
	}

	protected void setDownXY(int downX, int downY) {
		mDownX = downX;
		mDownY = downY;
		mLastX = downX;
		mLastY = downY;
	}

	// ***********************************************************************
	// 私有方法

	private void init(Context context) {
		mScroller = new Scroller(context); // 初始化弹性滑动对象
		setOrientation(LinearLayout.HORIZONTAL); // 设置其方向为横向
		// 解决listview中item的焦点占用问题
		setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		View.inflate(context, R.layout.merge_slide_view, this); // 将merge_slide_view加载进来
		mItemContentView = (FrameLayout) findViewById(R.id.itemContentView);
		mSlideContentView = (FrameLayout) findViewById(R.id.slideContentView);
		mItemContentView.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.selector_slide_listview_item));
		setBackgroundColor(getResources().getColor(R.color.slideview_bg));
		mSlideContentWidth = Math.round(getResources().getDimension(
				R.dimen.slide_content_width));
		mTanValue = Math.tan(Math.toRadians(TAN_ANGLE));
		mQuickTanValue = Math.tan(Math.toRadians(QUICK_TAN_ANGLE));
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
	}

	private void smoothScrollTo(int destX, int destY) {
		int scrollX = getScrollX();
		int delta = destX - scrollX;
		mScroller.startScroll(scrollX, 0, delta, 0, Math.abs(delta) * 3);
		invalidate();
	}

	private void addVelocityTracker(MotionEvent event) {
		if (mVelocityTracker == null)
			mVelocityTracker = VelocityTracker.obtain();
		mVelocityTracker.addMovement(event);
	}

	private void recycleVelocityTracker() {
		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}

	/**
	 * 获取X方向的滑动速度，大于0向右滑动，反之向左
	 * 
	 * @return
	 */
	private int getScrollXVelocity() {
		mVelocityTracker.computeCurrentVelocity(1000);
		int velocity = (int) mVelocityTracker.getXVelocity();
		return velocity;
	}

	/**
	 * 获取Y方向的滑动速度，大于0向下滑动，反之向上
	 * 
	 * @return
	 */
	private int getScrollYVelocity() {
		mVelocityTracker.computeCurrentVelocity(1000);
		int velocity = (int) mVelocityTracker.getYVelocity();
		return velocity;
	}

	private void setPressed(ViewGroup viewGroup, boolean flag) {
		viewGroup.setPressed(flag);
		for (int i = 0; i < viewGroup.getChildCount(); i++) {
			View childView = viewGroup.getChildAt(i);
			if (childView instanceof ViewGroup)
				setPressed((ViewGroup) childView, flag);
		}
	}

	private void setEnabled(ViewGroup viewGroup, boolean flag) {
		viewGroup.setEnabled(flag);
		for (int i = 0; i < viewGroup.getChildCount(); i++) {
			View childView = viewGroup.getChildAt(i);
			if (childView instanceof ViewGroup)
				setEnabled((ViewGroup) childView, flag);
		}
	}

}
