package com.slide.demo;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.slide.adapter.SlideBaseAdapter;
import com.slide.adapter.SlideViewHolder;
import com.slide.view.SlideListView;
import com.slide.view.SlideListView.OnSlideItemClickListener;
import com.slide.view.SlideListView.OnSlideItemLongClickListener;

public class MainActivity extends Activity {

	private SlideListView mListView = null;
	private ListBaseAdapter mListAdapter = null;
	private ArrayList<ItemBean> mDataList = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		findView();
		init();
		registerListener();
	}

	// ***************************************************************
	// findView，init，registerListener

	private void findView() {
		mListView = (SlideListView) findViewById(R.id.listView);
	}

	private void init() {
		mDataList = new ArrayList<ItemBean>();
		testInitDataList();
		mListAdapter = new ListBaseAdapter(this, mDataList,
				R.layout.listview_item, R.layout.slide_content);
		mListView.setAdapter(mListAdapter);
	}

	private void registerListener() {
		mListView.setOnSlideItemClickListener(new SlideItemClickListener());
		mListView
				.setOnSlideItemLongClickListener(new SlideItemLongClickListener());
	}

	// ***************************************************************
	// 私有方法

	private void testInitDataList() {
		for (int i = 0; i < 40; i++) {
			ItemBean bean = new ItemBean("title" + i);
			mDataList.add(bean);
		}
	}

	private void toast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	// ***************************************************************
	// 自定义的类

	private class ListBaseAdapter extends SlideBaseAdapter<ItemBean> {

		public ListBaseAdapter(Context context, ArrayList<ItemBean> dataList,
				int itemLayoutId, int slideLayoutId) {
			super(context, dataList, itemLayoutId, slideLayoutId);
		}

		@Override
		public void convert(SlideViewHolder holder, ItemBean bean, int position) {

			TextView titleText = holder.getView(R.id.titleText);
			Button button = holder.getView(R.id.button);
			Button deleteButton = holder.getView(R.id.deleteButton);

			titleText.setText(bean.getTitle());
			button.setOnClickListener(new ButtonClickListener(position));
			deleteButton.setOnClickListener(new ButtonClickListener(position));

			holder.getConvertView().setCanLeftSlide(true);

		}

		class ButtonClickListener implements OnClickListener {

			private int position = 0;

			public ButtonClickListener(int position) {
				this.position = position;
			}

			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.button:
					toast("单击Button按钮，第" + position + "个");
					break;
				case R.id.deleteButton:
					toast("单击删除按钮，第" + position + "个");
					break;
				}
			}
		}

	}

	private class SlideItemClickListener implements OnSlideItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			toast("单击了第" + position + "个item");
		}
	}

	private class SlideItemLongClickListener implements
			OnSlideItemLongClickListener {

		@Override
		public void onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			toast("长按了第" + position + "个item");
		}

	}

}
