package com.rongyan.appstore.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.rongyan.appstore.item.CategoriesItem;
import com.rongyan.appstore.utils.ApplicationUtils;
import com.rongyan.appstore.utils.LogUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rongyan.appstore.R;;

/**
 * 横屏类别adapter
 */

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ViewHolder> implements View.OnClickListener{

    private List<CategoriesItem.Data.Categories> mCategoies; // 外面传入的数据

    private Context mContext;

    private OnItemClickListener mOnItemClickListener = null;

    private int lastPosition=0;

    private int lastId=0;//记录类别ID

    @Override
    public void onClick(final View v) {
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取position
            new Thread(){
                public void run() {
                    mOnItemClickListener.onItemClickListener(v,(int)v.getTag());
                }
            }.start();
            if(lastPosition!=(int)v.getTag()) {
                notifyDataSetChanged();
                lastPosition=(int)v.getTag();
                lastId=mCategoies.get(lastPosition).getId();
            }
        }
    }

    /**
     * Item的回调接口
     *
     */
    public interface OnItemClickListener {
        void onItemClickListener(View view, int position);
    }

    /**
     * 设置回调监听
     *
     * @param listener
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView adapter_Class_Img;

        TextView adapter_Class_Txt;

        FrameLayout adapter_Class_Fly;

        View adapter_Class_View;

        // TODO Auto-generated method stub
        public ViewHolder(View v) {
            super(v);
        }

    }

    public ClassAdapter(List<CategoriesItem.Data.Categories> categoies, Context context) {
        this.mCategoies = categoies;
        this.mContext = context;
    }

    public void setDatas(List<CategoriesItem.Data.Categories> categoies){
        this.mCategoies = categoies;
        boolean isLike=false;
        if(categoies!=null&&!categoies.isEmpty()){
            for(int i=0;i<categoies.size();i++){
                if(categoies.get(i).getId()==lastId){
                    lastPosition=i;
                    isLike=true;
                    break;
                }
            }
        }
        if(!isLike){
            lastPosition=0;
        }
    }

    /**
     * 获取总的条目数量
     */
    @Override
    public int getItemCount() {
        // TODO Auto-generated method stub
        return mCategoies.size();
    }

    /**
     * 创建ViewHolder
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // TODO Auto-generated method stub
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_class, parent, false);
        ViewHolder holder = new ViewHolder(v);
        holder.adapter_Class_Img = (ImageView) v.findViewById(R.id.adapter_class_img);
        holder.adapter_Class_Txt = (TextView) v.findViewById(R.id.adapter_class_txt);
        holder.adapter_Class_Fly = (FrameLayout) v.findViewById(R.id.adapter_class_fly);
        holder.adapter_Class_View = v.findViewById(R.id.adapter_class_view);
        //将创建的View注册点击事件
        v.setOnClickListener(this);
        return holder;
    }

    /**
     * 将数据绑定到ViewHolder上
     */
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        CategoriesItem.Data.Categories categoies=mCategoies.get(position);
        if(categoies!=null) {
            if(categoies.getIcon_url()!=null) {
                Glide.with(mContext)
                        .load(categoies.getIcon_url())
                        .placeholder(R.drawable.acquiescence)
                        .error(R.drawable.acquiescence)
                        .into(holder.adapter_Class_Img);
            }else{
                Glide.with(mContext)
                        .load(R.drawable.home_icon)
                        .placeholder(R.drawable.acquiescence)
                        .error(R.drawable.acquiescence)
                        .into(holder.adapter_Class_Img);
            }
            holder.adapter_Class_Txt.setText(categoies.getName());
            holder.itemView.setTag(position);
            if(position==lastPosition){
                holder.adapter_Class_Fly.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white_10));
                if(ApplicationUtils.getmBROKER().equals("KB")) {
                    holder.adapter_Class_View.setBackgroundResource(R.drawable.line_short_orangle);
                }else{
                    holder.adapter_Class_View.setBackgroundResource(R.drawable.line_short_red);
                }
                holder.adapter_Class_Txt.setTextAppearance(mContext, R.style.red_28_10);
            }else{
                if(holder.adapter_Class_View.getBackground()!=mContext.getDrawable(R.drawable.line_short_gray)) {
                    holder.adapter_Class_Fly.setBackgroundColor(ContextCompat.getColor(mContext, R.color.grayF2_10));
                    holder.adapter_Class_View.setBackground(mContext.getDrawable(R.drawable.line_short_gray));
                    holder.adapter_Class_Txt.setTextAppearance(mContext, R.style.gray37_24_10);
                }
            }
        }

    }

    public int getLastPosition() {
        return lastPosition;
    }
}
