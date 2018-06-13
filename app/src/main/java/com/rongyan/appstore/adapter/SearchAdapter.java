package com.rongyan.appstore.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rongyan.appstore.activity.port.HomePageActivity;
import com.rongyan.appstore.item.Apps;
import com.rongyan.appstore.utils.ApplicationUtils;
import com.rongyan.appstore.widget.AppView;

import com.rongyan.appstore.R;;

/**
 * 搜索应用adapter
 */

public class SearchAdapter extends RootAdapter<Apps>{

    private Activity activity;

    public SearchAdapter(Context context, Activity activity) {
        super(context);
        this.activity=activity;
    }

    @Override
    protected View getExView(int position, View convertView, ViewGroup parent) {
        SearchAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new SearchAdapter.ViewHolder();
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.update_item, null);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (SearchAdapter.ViewHolder) convertView.getTag();
        }
        Apps mAppItem = mList
                .get(position);
        viewHolder.appview = (AppView) convertView
                .findViewById(R.id.update_appview);
        if (mAppItem != null) {
            viewHolder.appview.setView(activity,mAppItem);
            ApplicationUtils.setAppState(viewHolder.appview,mAppItem);
        }
        return convertView;
    }

    static class ViewHolder {
        AppView appview;
    }
}
