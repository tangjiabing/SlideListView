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
 * @see ��Դʱ�䣺2016��04��01��
 * 
 *      �ǵø��Ҹ�starŶ~
 * 
 */
public class SlideView extends LinearLayout {

	private static final int SCROLL_OUT = 0x11; // �ѻ�������
	private static final int SCROLLING = 0x12; // ���ڻ���
	private static final int NOT_SCROLL = 0x13; // ���ܻ���
	// �������ƻ����Ƕȣ������Ƕ�a��������ʱ�Ž��л�����tan a = deltaX / deltaY
	private static final int TAN_ANGLE = 50; // ���нǶ�
	private static final int QUICK_TAN_ANGLE = 70; // ���ٻ���ʱҪ������нǶ�
	private static final int QUICK_SNAP_VELOCITY = 900; // ���ٵ���ָ�����ٶ�
	private static final int NORMAL_SNAP_VELOCITY = 600; // ��ͨ����ָ�����ٶ�
	private static final int SLOW_SNAP_VELOCITY = 100; // ��������ָ�����ٶ�
	private int mTouchSlop = 0; // �û���������С����
	private double mTanValue = 0; // ����ֵ
	private double mQuickTanValue = 0; // ���ٻ���ʱҪ�������ֵ
	private int mLastX = 0; // �ϴλ�����x����
	private int mLastY = 0; // �ϴλ�����y����
	private int mDownX = 0; // ����ȥʱ��x����
	private int mDownY = 0; // ����ȥʱ��y����
	private int mScrollStatus = 0; // ����״̬
	private Scroller mScroller = null; // ���Ի��������ṩ���Ի���Ч��
	private FrameLayout mItemContentView = null; // ������������view������
	private FrameLayout mSlideContentView = null; // �������û���view������
	private int mSlideContentWidth = 0; // ����view�Ŀ��
	private VelocityTracker mVelocityTracker = null; // �ٶ�׷�ٶ���
	private boolean mIsCanLeftSlide = true; // SlideView�Ƿ�����󻬣�trueΪ����

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
	// ��������

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
					int direction = 0; // ��������
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
	// ˽�з���

	private void init(Context context) {
		mScroller = new Scroller(context); // ��ʼ�����Ի�������
		setOrientation(LinearLayout.HORIZONTAL); // �����䷽��Ϊ����
		// ���listview��item�Ľ���ռ������
		setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		View.inflate(context, R.layout.merge_slide_view, this); // ��merge_slide_view���ؽ���
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
	 * ��ȡX����Ļ����ٶȣ�����0���һ�������֮����
	 * 
	 * @return
	 */
	private int getScrollXVelocity() {
		mVelocityTracker.computeCurrentVelocity(1000);
		int velocity = (int) mVelocityTracker.getXVelocity();
		return velocity;
	}

	/**
	 * ��ȡY����Ļ����ٶȣ�����0���»�������֮����
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
