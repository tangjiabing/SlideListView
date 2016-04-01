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
 * @see ��Դʱ�䣺2016��04��01��
 * 
 *      �ǵø��Ҹ�starŶ~
 * 
 */
public class SlideListView extends ListView {

	private SlideView mSlideView = null; // ��ǰ��item
	private SlideView mLastSlideView = null; // ��һ�ε�item
	private boolean mIsHandleFromActionDown = false; // �Ƿ�����֮��Ķ���
	private boolean mIsCallSuperMethod = false; // �Ƿ���ø���ķ�����onTouchEvent
	// item����click�¼�����Ҫ���������裺���º�̧�𡣵���������������Ϊtrueʱ���ɵ��item��������
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
	 * ����item�¼��������÷���������setOnItemClickListenerͬʱʹ��
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
	 * ����item�¼��������÷���������setOnItemLongClickListenerͬʱʹ��
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
	// ˽�з���

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
					if (slideView != null) // ������ת������ʹposition����Ҫ��slideViewҲ����Ϊnull
						slideView.setItemContentViewEnabled(true);
				}
			}

			if (mSlidePosition != AdapterView.INVALID_POSITION
					&& mSlidePosition > -1 && mSlidePosition < itemCount) {
				mLastSlidePosition = mSlidePosition;
				// ��ʹmSlidePosition����Ҫ��mSlideViewҲ����Ϊnull
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
					&& mIsCallSuperMethod == true) { // ��item�е�button�ؼ������ֺ�
				mSlideView.reset();
				mLastSlideView = null;
				mIsClickItemUpValid = false;
			}
		}
		mIsCallSuperMethod = true;
		// ����ȥ����ԭ������֮������recoverSlideView��������item�е�button�ؼ������ֺ�
		mIsHandleFromActionDown = false;
	}

	private void recoverSlideView() {
		mSlideView.quickReset();
		if (mSlidePosition != AdapterView.INVALID_POSITION
				&& mSlidePosition > -1 && mSlidePosition < getCount()) {
			mSlideView = (SlideView) getItemAtPosition(mSlidePosition);
			if (mSlideView != null) // ��ʹmSlidePosition����Ҫ�󣬵�mSlideViewҲ����Ϊnull
				mSlideView.setDownXY(mDownX, mDownY);
		} else {
			mIsHandleFromActionDown = false;
			mIsClickItemDownValid = false;
			mIsLongClickItemValid = false;
			mIsCallSuperMethod = true;
		}
	}

	// ***********************************************************************
	// �Զ������

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
			case SCROLL_STATE_IDLE: // ��ֹ״̬
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
			case SCROLL_STATE_TOUCH_SCROLL: // ��ָ����״̬
				break;
			case SCROLL_STATE_FLING: // ��ָ�����ˣ�������Ļ���ڹ���״̬
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
