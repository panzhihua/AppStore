package com.rongyan.appstore.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rongyan.appstore.item.Apps;
import com.rongyan.appstore.utils.ApplicationUtils;
import com.rongyan.appstore.utils.LogUtils;
import com.rongyan.appstore.widget.AppView;

import com.rongyan.appstore.R;;

/**
 * 应用Adapter（竖屏）
 */

public class AppAdapter extends RootAdapter<Apps>{

    private final static String TAG="AppAdapter";

    private Object mObject;

    public AppAdapter(Object object,Context context) {
        super(context);
        this.mObject=object;
    }

    @Override
    protected View getExView(int position, View convertView, ViewGroup parent) {
        AppAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new AppAdapter.ViewHolder();
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.app_item, null);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (AppAdapter.ViewHolder) convertView.getTag();
        }
        viewHolder.appview = (AppView) convertView
                .findViewById(R.id.app_appview);
        Apps app=mList.get(position);
        if (app != null) {
            viewHolder.appview = (AppView) convertView
                    .findViewById(R.id.app_appview);
            viewHolder.appview.setView(mObject,app);
            ApplicationUtils.setAppState(viewHolder.appview,app);
        }
        return convertView;
    }


    static class ViewHolder {
        AppView appview;
    }
}
