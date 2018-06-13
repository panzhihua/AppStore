package com.rongyan.appstore.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rongyan.appstore.fragment.land.RecommendFragment;
import com.rongyan.appstore.item.Apps;
import com.rongyan.appstore.item.LeftOrRightAppsItem;
import com.rongyan.appstore.utils.ApplicationUtils;
import com.rongyan.appstore.widget.AppView;

import com.rongyan.appstore.R;;

/**
 * 最新最热Adapter
 */

public class RecommendAdapter extends RootAdapter<LeftOrRightAppsItem>{

    private RecommendFragment mRecommendFragment;

    public RecommendAdapter(RecommendFragment recommendFragment, Context context) {
        super(context);
        mRecommendFragment=recommendFragment;
    }

    @Override
    protected View getExView(int position, View convertView, ViewGroup parent) {

        RecommendAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new RecommendAdapter.ViewHolder();
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.apps_item, null);
            viewHolder.leftAppview = (AppView) convertView
                    .findViewById(R.id.app_left_appview);
            viewHolder.rightAppView = (AppView) convertView
                    .findViewById(R.id.app_right_appview);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (RecommendAdapter.ViewHolder) convertView.getTag();
        }
        if (position == 0) {
            mCount++;
        } else {
            mCount = 0;
        }
        if (mCount > 1) {
            return convertView;
        }
        Apps mLeftItem=null;
        Apps mRightItem=null;
        if(!mList.isEmpty()) {
            mLeftItem = mList
                    .get(position).getLeftApps();
            mRightItem = mList
                    .get(position).getRightApps();
        }
        if (mLeftItem != null) {
            viewHolder.leftAppview.setView(mRecommendFragment,mLeftItem);
            ApplicationUtils.setAppState(viewHolder.leftAppview,mLeftItem);
            viewHolder.leftAppview.setVisibility(View.VISIBLE);
        }else{
            viewHolder.leftAppview.setVisibility(View.INVISIBLE);
        }
        if (mRightItem != null) {
            viewHolder.rightAppView.setView(mRecommendFragment,mRightItem);
            ApplicationUtils.setAppState(viewHolder.rightAppView,mRightItem);
            viewHolder.rightAppView.setVisibility(View.VISIBLE);
        }else{
            viewHolder.rightAppView.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }


    static class ViewHolder {
        AppView leftAppview,rightAppView;
    }
}
