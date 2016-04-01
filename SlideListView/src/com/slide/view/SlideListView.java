package com.slide.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * 
 * @author tangjiabing
 * 
 * @see 开源时间：2016年04月01日
 * 
 *      记得给我个star哦~
 * 
 */
public class SlideListView extends ListView {

	private SlideView mSlideView = null; // 当前的item
	private SlideView mLastSlideView = null; // 上一次的item
	private boolean mIsHandleFromActionDown = false; // 是否处理按下之后的动作
	private boolean mIsCallSuperMethod = false; // 是否调用父类的方法：onTouchEvent
	// item引起click事件，需要有两个步骤：按下和抬起。当下面两个变量都为true时，可点击item，否则不行
	private boolean mIsClickItemDownValid = false;
	private boolean mIsClickItemUpValid = false;
	private int mLastSlidePosition = 0;
	private boolean mIsLongClickItemValid = false;
	private OnSlideItemClickListener mSlideItemClickListener = null;
	private OnSlideItemLongClickListener mSlideItemLongClickListener = null;
	private int mSlidePosition = 0;
	private int mActionDownFirstVisiblePosition = 0;
	private int mDownX = 0;
	private int mDownY = 0;

	public SlideListView(Context context) {
		this(context, null);
	}

	public SlideListView(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.listViewStyle);
	}

	public SlideListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		addOnScrollListener();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mDownX = (int) event.getX();
			mDownY = (int) event.getY();
			mSlidePosition = pointToPosition(mDownX, mDownY);
			mActionDownFirstVisiblePosition = getFirstVisiblePosition();
			actionDown(event);
			break;
		case MotionEvent.ACTION_MOVE:
			actionMove(event);
			break;
		case MotionEvent.ACTION_UP:
			actionUp(event);
			break;
		}

		if (mIsCallSuperMethod == true)
			return super.onTouchEvent(event);
		else
			return true;
	}

	/**
	 * 单击item事件监听，该方法不能与setOnItemClickListener同时使用
	 * 
	 * @param listener
	 */
	public void setOnSlideItemClickListener(OnSlideItemClickListener listener) {
		mSlideItemClickListener = listener;
		if (listener != null)
			setOnItemClickListener(new ListItemClickListener());
		else
			setOnItemClickListener(null);
	}

	/**
	 * 长按item事件监听，该方法不能与setOnItemLongClickListener同时使用
	 * 
	 * @param listener
	 */
	public void setOnSlideItemLongClickListener(
			OnSlideItemLongClickListener listener) {
		mSlideItemLongClickListener = listener;
		if (listener != null)
			setOnItemLongClickListener(new ListItemLongClickListener());
		else
			setOnItemLongClickListener(null);
	}

	// ***********************************************************************
	// 私有方法

	private void addOnScrollListener() {
		this.setOnScrollListener(new ListScrollListener());
	}

	private void actionDown(MotionEvent event) {
		int itemCount = getCount();

		if (mLastSlideView == null
				&& (mSlidePosition == AdapterView.INVALID_POSITION
						|| mSlidePosition < 0 || mSlidePosition >= itemCount)) {
			mIsHandleFromActionDown = false;
			mIsClickItemDownValid = false;
			mIsLongClickItemValid = false;
			mIsCallSuperMethod = true;
		} else {

			for (int i = -1; i <= 1; i++) {
				int position = mLastSlidePosition + i;
				if (position != AdapterView.INVALID_POSITION && position > -1
						&& position < itemCount) {
					SlideView slideView = (SlideView) getItemAtPosition(position);
					if (slideView != null) // 横竖屏转换，即使position符合要求，slideView也可能为null
						slideView.setItemContentViewEnabled(true);
				}
			}

			if (mSlidePosition != AdapterView.INVALID_POSITION
					&& mSlidePosition > -1 && mSlidePosition < itemCount) {
				mLastSlidePosition = mSlidePosition;
				// 即使mSlidePosition符合要求，mSlideView也可能为null
				mSlideView = (SlideView) getItemAtPosition(mSlidePosition);
			}

			if (mLastSlideView != null) {
				if (mLastSlideView != mSlideView) {
					mLastSlideView.reset();
					mLastSlideView.onHandleTouchEvent(event);
					mIsHandleFromActionDown = false;
					mLastSlideView = null;
					if (mSlideView != null)
						mSlideView.setItemContentViewEnabled(false);
				} else {
					mSlideView.onHandleTouchEvent(event);
					mIsHandleFromActionDown = true;
				}
				mIsClickItemDownValid = false;
				mIsLongClickItemValid = false;
				mIsCallSuperMethod = false;
			} else {
				if (mSlideView != null) {
					mSlideView.onHandleTouchEvent(event);
					mIsHandleFromActionDown = true;
					mIsClickItemDownValid = true;
					mIsLongClickItemValid = true;
				} else {
					mIsHandleFromActionDown = false;
					mIsClickItemDownValid = false;
					mIsLongClickItemValid = false;
				}
				mIsCallSuperMethod = true;
			}
		}
	}

	private void actionMove(MotionEvent event) {
		if (mIsHandleFromActionDown == true) {
			mSlideView.onHandleTouchEvent(event);
			if (mSlideView.isScrolling() || mLastSlideView == mSlideView)
				mIsCallSuperMethod = false;
			else if (mSlideView.isNotScroll())
				mIsCallSuperMethod = true;
			else
				mIsCallSuperMethod = false;

			if (mSlideView.isScrolling()) {
				mIsLongClickItemValid = false;
				mSlideView.setItemContentViewPressed(false);
			} else
				mIsLongClickItemValid = true;
		}
	}

	private void actionUp(MotionEvent event) {
		if (mIsHandleFromActionDown == true) {
			if (mSlideView == mLastSlideView
					&& (mSlideView.isNotScroll() || mSlideView.isScrollOut())) {
				mSlideView.reset();
				mLastSlideView = null;
				mIsClickItemUpValid = false;
			} else {
				if ((mSlideView.isNotScroll() == false
						&& mSlideView.isScrolling() == false && mSlideView
						.isScrollOut() == false)
						|| mSlideView.isCanLeftSlide() == false)
					mIsClickItemUpValid = true;
				else
					mIsClickItemUpValid = false;

				mSlideView.onHandleTouchEvent(event);
				if (mSlideView.isScrollOut())
					mLastSlideView = mSlideView;
				else
					mLastSlideView = null;
			}

			if (mIsClickItemUpValid == false)
				mSlideView.setItemContentViewEnabled(false);

		} else {
			if (mSlideView != null && mSlideView.isScrollOut()
					&& mIsCallSuperMethod == true) { // 按item中的button控件，松手后
				mSlideView.reset();
				mLastSlideView = null;
				mIsClickItemUpValid = false;
			}
		}
		mIsCallSuperMethod = true;
		// 不能去掉的原因：在这之后会调用recoverSlideView方法（按item中的button控件，松手后）
		mIsHandleFromActionDown = false;
	}

	private void recoverSlideView() {
		mSlideView.quickReset();
		if (mSlidePosition != AdapterView.INVALID_POSITION
				&& mSlidePosition > -1 && mSlidePosition < getCount()) {
			mSlideView = (SlideView) getItemAtPosition(mSlidePosition);
			if (mSlideView != null) // 即使mSlidePosition符合要求，但mSlideView也可能为null
				mSlideView.setDownXY(mDownX, mDownY);
		} else {
			mIsHandleFromActionDown = false;
			mIsClickItemDownValid = false;
			mIsLongClickItemValid = false;
			mIsCallSuperMethod = true;
		}
	}

	// ***********************************************************************
	// 自定义的类

	private class ListItemClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if (mSlideItemClickListener != null) {
				if (mIsClickItemDownValid == true
						&& mIsClickItemUpValid == true)
					mSlideItemClickListener.onItemClick(parent, view, position,
							id);
			}
			if (mSlideView != null)
				mSlideView.setItemContentViewEnabled(true);
		}
	}

	private class ListItemLongClickListener implements OnItemLongClickListener {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			if (mSlideItemLongClickListener != null) {
				if (mIsLongClickItemValid == true)
					mSlideItemLongClickListener.onItemLongClick(parent, view,
							position, id);
			}
			return true;
		}
	}

	private class ListScrollListener implements OnScrollListener {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			switch (scrollState) {
			case SCROLL_STATE_IDLE: // 静止状态
				int firstVisiblePosition = view.getFirstVisiblePosition();
				if (mActionDownFirstVisiblePosition != firstVisiblePosition) {
					if (firstVisiblePosition > mActionDownFirstVisiblePosition)
						mSlidePosition = mSlidePosition + firstVisiblePosition
								- mActionDownFirstVisiblePosition;
					else
						mSlidePosition = mSlidePosition
								- mActionDownFirstVisiblePosition
								+ firstVisiblePosition;
					recoverSlideView();
					mActionDownFirstVisiblePosition = firstVisiblePosition;
				}
				break;
			case SCROLL_STATE_TOUCH_SCROLL: // 手指滚动状态
				break;
			case SCROLL_STATE_FLING: // 手指不动了，但是屏幕还在滚动状态
				break;
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
		}
	}

	public interface OnSlideItemClickListener {
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id);
	}

	public interface OnSlideItemLongClickListener {
		public void onItemLongClick(AdapterView<?> parent, View view,
				int position, long id);
	}

}
