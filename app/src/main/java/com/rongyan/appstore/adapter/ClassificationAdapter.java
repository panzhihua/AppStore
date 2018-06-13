package com.rongyan.appstore.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.rongyan.appstore.item.CategoriesItem;

import com.rongyan.appstore.R;;

/**
 * 竖屏类别adapter
 */

public class ClassificationAdapter extends RootAdapter<CategoriesItem.Data.Categories>{

    public ClassificationAdapter(Context context) {
        super(context);
    }

    @Override
    protected View getExView(int position, View convertView, ViewGroup parent) {
        ClassificationAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ClassificationAdapter.ViewHolder();
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.classification_item, null);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ClassificationAdapter.ViewHolder) convertView.getTag();
        }
        viewHolder.adapter_Classification_Img = (ImageView) convertView
                .findViewById(R.id.adapter_classification_img);
        viewHolder.adapter_Classification_Txt = (TextView) convertView
                .findViewById(R.id.adapter_classification_txt);
        CategoriesItem.Data.Categories categoies=mList.get(position);
        if(categoies!=null) {
            Glide.with(context)
                    .load(categoies.getIcon_url())
                    .placeholder(R.drawable.acquiescence)
                    .error(R.drawable.acquiescence)
                    .into(viewHolder.adapter_Classification_Img);
            viewHolder.adapter_Classification_Txt.setText(categoies.getName());
        }
        return convertView;
    }


    static class ViewHolder {
        TextView adapter_Classification_Txt;
        ImageView adapter_Classification_Img;
    }
}
