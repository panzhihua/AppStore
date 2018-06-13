package com.rongyan.appstore.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rongyan.appstore.fragment.land.UpdateFragment;
import com.rongyan.appstore.item.Apps;
import com.rongyan.appstore.utils.ApplicationUtils;
import com.rongyan.appstore.widget.AppView;

import com.rongyan.appstore.R;;

/**
 * 应用更新adapter
 */

public class UpdateAdapter extends RootAdapter<Apps>{

    public UpdateAdapter(Context context) {
        super(context);
    }

    @Override
    protected View getExView(int position, View convertView, ViewGroup parent) {
        UpdateAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new UpdateAdapter.ViewHolder();
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.update_item, null);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (UpdateAdapter.ViewHolder) convertView.getTag();
        }
        Apps mAppItem = mList
                .get(position);
        viewHolder.appview = (AppView) convertView
                .findViewById(R.id.update_appview);
        if (mAppItem != null) {
            viewHolder.appview.setView(null,mAppItem);
            ApplicationUtils.setAppState(viewHolder.appview,mAppItem);
        }
        return convertView;
    }

    static class ViewHolder {
        AppView appview;
    }
}
