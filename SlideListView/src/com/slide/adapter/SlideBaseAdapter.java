package com.slide.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.slide.view.SlideView;

/**
 * 
 * @author tangjiabing
 * 
 * @see 开源时间：2016年04月01日
 * 
 *      记得给我个star哦~
 * 
 */
public abstract class SlideBaseAdapter<T> extends BaseAdapter {

	private Context mContext = null;
	private SparseArray<SlideView> mSlideViewArray = null;
	private ArrayList<T> mDataList = null;
	private int mItemLayoutId = 0;
	private int mSlideLayoutId = 0;

	public SlideBaseAdapter(Context context, ArrayList<T> dataList,
			int itemLayoutId, int slideLayoutId) {
		mContext = context;
		mDataList = dataList;
		mItemLayoutId = itemLayoutId;
		mSlideLayoutId = slideLayoutId;
		mSlideViewArray = new SparseArray<SlideView>();
	}

	@Override
	public int getCount() {
		return mDataList.size();
	}

	@Override
	public Object getItem(int position) {
		return mSlideViewArray.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		SlideViewHolder holder = SlideViewHolder.getInstance(mContext,
				mItemLayoutId, mSlideLayoutId, convertView);
		SlideView slideView = holder.getConvertView();
		slideView.quickReset();
		mSlideViewArray.put(position, slideView);
		T bean = mDataList.get(position);
		convert(holder, bean, position);
		return slideView;
	}

	public abstract void convert(SlideViewHolder holder, T bean, int position);

}
