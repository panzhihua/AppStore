package com.rongyan.appstore.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 基础adapter
 */

public abstract class RootAdapter<T> extends BaseAdapter {

    protected Context context;

    protected List<T> mList = new ArrayList<T>();

    public List<T> getList() {
        return mList;
    }

    public int mCount=0;

    public RootAdapter(Context context) {
        this.context = context;
    }

    public void remove(T t) {
        if (mList != null && mList.contains(t)) {
            mList.remove(t);
        }
        notifyDataSetChanged();
    }

    public void appendToList(List<T> list) {
        if (list == null) {
            return;
        }
        mList.addAll(list);
        notifyDataSetChanged();
    }

    public void setList(List<T> list) {
        if (list == null) {
            return;
        }
        mCount=0;
        this.mList = list;
        notifyDataSetChanged();
    }

    public void appendToTopList(List<T> list) {
        if (list == null) {
            return;
        }
        mList.addAll(0, list);
        notifyDataSetChanged();
    }

    public void clear() {
        mList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
    	if(mList!=null){
    		return mList.size();
    	}
    	return 0;
    }
    

    @Override
    public Object getItem(int position) {
        if (position > mList.size() - 1) {
            return null;
        }
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // if (position == getCount() - 1) {//滑动到底部
        // onReachBottom();
        // }
        return getExView(position, convertView, parent);
    }

    protected abstract View getExView(int position, View convertView, ViewGroup parent);

    /**
     * 添加到尾部
     * @param t
     */
    public void appendBottom(T t) {
        if (t == null) {
            return;
        }
        mList.add(t);
        notifyDataSetChanged();
    }
    /**
     * 添加到头部
     * @param t
     */
    public void appendTop(T t) {
        if (t == null) {
            return;
        }
        mList.add(0,t);
        notifyDataSetChanged();
    }
}
