package com.tingtingfm.cbb.ui.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * 公共的ViewHolder，大多数情况下，可共用
 * @author lqsir
 *
 */
public class ViewHolder {
	private View contentView;
	private SparseArray<View> mViews;
	private int position;

	private ViewHolder(Context context, ViewGroup parent, int layoutId, int position) {
		this.position = position;
		mViews = new SparseArray<View>();
		contentView = LayoutInflater.from(context).inflate(layoutId, parent, false);
		contentView.setTag(this);
	}

	public static ViewHolder getViewHolder(Context context, View convertView, ViewGroup parent, int layoutId, int position) {
		if (convertView == null) {
			return new ViewHolder(context, parent, layoutId, position);
		} else {
			ViewHolder holder = (ViewHolder) convertView.getTag();
			holder.position = position;
			return holder;
		}
	}

	public View getContentView() {
		return contentView;
	}
	
	/**
	 * 根据viewId获取当前View
	 * @param viewId
	 * @return 缓存控件T
	 */
	public <T extends View> T getView(int viewId) {
		View view = mViews.get(viewId);
		if (view == null) {
			view = contentView.findViewById(viewId);
			mViews.put(viewId, view);
		}
		
		return (T) view;
	}
	
	/**
	 * 返回当前View处于列表的位置
	 * @return 返回索引位置
	 */
	public int getPosition() {
		return position;
	}
	
	/**
	 * 设置当前TextView显示文字
	 * @param viewId 控件Id
	 * @param text 显示文字
	 */
	public void setText(int viewId, String text) {
		TextView tv = getView(viewId);
		tv.setText(text);
	}
	
	/**
	 * 设置当前View的可见性
	 * @param viewId 控件ID
	 * @param visibility 可见性
	 */
	public void setViewVisibility(int viewId, int visibility) {
		getView(viewId).setVisibility(visibility);
	}

	/**
	 * 设置当前TextView 的背影
	 * @param viewId
	 * @param resid
	 */
	public void setTextBackground(int viewId, int resid) {
		TextView tv = getView(viewId);
		tv.setBackgroundResource(resid);
	}

	/**
	 * 设置TextView的字体颜色
	 * @param viewId
	 * @param resid
	 */
	public void setTextColor(int viewId, int resid) {
		TextView tv = getView(viewId);
		tv.setTextColor(resid);
	}
}
