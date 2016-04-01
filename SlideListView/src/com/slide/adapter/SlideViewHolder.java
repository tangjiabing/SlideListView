package com.slide.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;

import com.slide.view.SlideView;

/**
 * 
 * @author tangjiabing
 * 
 * @see ��Դʱ�䣺2016��04��01��
 * 
 *      �ǵø��Ҹ�starŶ~
 * 
 */
public class SlideViewHolder {

	private SlideView mConvertView = null;
	private SparseArray<View> mViewArray = null;

	private SlideViewHolder(Context context, int itemLayoutId, int slideLayoutId) {
		mViewArray = new SparseArray<View>();
		LayoutInflater inflater = LayoutInflater.from(context);
		mConvertView = new SlideView(context);
		View view1 = inflater.inflate(itemLayoutId, null);
		View view2 = inflater.inflate(slideLayoutId, null);
		mConvertView.setItemContentView(view1);
		mConvertView.setSlideContentView(view2);
		mConvertView.setTag(this);
	}

	// ********************************************************************
	// ��������

	protected static SlideViewHolder getInstance(Context context,
			int itemLayoutId, int slideLayoutId, View convertView) {
		if (convertView == null)
			return new SlideViewHolder(context, itemLayoutId, slideLayoutId);
		else
			return (SlideViewHolder) convertView.getTag();
	}

	// ********************************************************************
	// ���з���

	public SlideView getConvertView() {
		return mConvertView;
	}

	public <T extends View> T getView(int viewId) {
		View view = mViewArray.get(viewId);
		if (view == null) {
			view = mConvertView.findViewById(viewId);
			mViewArray.put(viewId, view);
		}
		return (T) view;
	}

}
