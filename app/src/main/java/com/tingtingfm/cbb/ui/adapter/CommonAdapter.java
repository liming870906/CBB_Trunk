package com.tingtingfm.cbb.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * 通用的Adapter
 * @author lqsir
 *
 */
public abstract class CommonAdapter<T> extends BaseAdapter {
	protected Context context;
	protected List<T> data;
	private int layoutId;
	private LayoutInflater inflater;
	
	public CommonAdapter(Context context, int layoutId) {
		this(context, layoutId, null);
	}
	
	public CommonAdapter(Context context, int layoutId, List<T> data) {
		this.context = context;
		this.data = data;
		this.layoutId = layoutId;
		inflater = LayoutInflater.from(context);
	}
	
	public void setData(List<T> data) {
		this.data = data;
	}
	
	@Override
	public int getCount() {
		if (data == null || data.size() == 0)
			return 0;
		
		return data.size();
	}

	@Override
	public T getItem(int position) {
		if (data == null || data.size() == 0)
			return null;
		
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		if (data == null || data.size() == 0)
			return 0;
		
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (data == null || data.size() == 0) {
			return null;
		}
		
		ViewHolder holder = ViewHolder.getViewHolder(context, convertView, parent, layoutId, position);
		covert(holder, getItem(position));
		return holder.getContentView();
	}
	
	/**
	 * 适配器中对显示控件进行处理
	 * @param holder holder对象
	 * @param t 显示Bean对象
	 */
	public abstract void covert(ViewHolder holder, T t);
	
	
	public String getStringForId(int resId, Object... items) {
		return context.getResources().getString(resId, items);
	}
}
