package com.rongyan.appstore.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import com.rongyan.appstore.R;

/**
 * 自定义星星可滑动评分控件
 */

public class StarRatingView extends LinearLayout {
    private Drawable on,off;   //星星两种图片
    private boolean ratable;    //是否点击滑动
    private boolean isReturn;    //是否返回事件
    private float padding;  //星星之间间距
    private List<ImageView> list;   //存放五颗星星
    private float  points[] = new float[6];    //0-10分点的x坐标，index为分数，值为此分数为坐标
    private float starWidth;    //设置的星星图片宽度
    private int paddingLeft;       //控件左padding
    private OnRateChangeListener onRateChangeListener;

    public StarRatingView(Context context) {
        super(context);
    }

    public StarRatingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.StarRatingView);
        on = array.getDrawable(R.styleable.StarRatingView_stat_on);
        off = array.getDrawable(R.styleable.StarRatingView_stat_off);
        ratable = array.getBoolean(R.styleable.StarRatingView_ratable, false);
        starWidth = on.getIntrinsicWidth();
        padding = array.getDimension(R.styleable.StarRatingView_star_padding, starWidth / 3);
        paddingLeft = getPaddingLeft();
        list = new ArrayList<>();
        ImageView imageView;
        //初始化五个星星，并通过points数组把分数与坐标对应起来
        for(int i=0;i<5;i++){
            imageView = new ImageView(context);
            LinearLayout.LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
            if(i!=0&&i!=4) {
                //设置间距
                layoutParams.setMargins((int) padding / 2, 0, (int) padding / 2, 0);
                //分数点所在的x坐标
                points[i*1] = i*starWidth+i*padding;
            }
            //第一个左边和最后一个右边不设置间距
            if(i==0){
                layoutParams.setMargins(0, 0, (int) padding / 2, 0);
                points[0] = 0;
            }
            if(i==4){
                layoutParams.setMargins((int) padding / 2, 0, 0, 0);
                points[i*1] = i*starWidth+i*padding;
            }
            imageView.setLayoutParams(layoutParams);
            imageView.setImageDrawable(off);
            list.add(imageView);
            addView(list.get(i));
        }
        setOrientation(LinearLayout.HORIZONTAL);
        array.recycle();
    }

    /**
     *根据分数显示星星
     * @param rate
     */
    public void setRate(int rate){
        if(ratable) {
            removeAllViews();
            int count = rate;
            for (int i = 0; i < list.size(); i++) {
                if (i < count) {
                    list.get(i).setImageDrawable(on);
                } else {
                    list.get(i).setImageDrawable(off);
                }
                addView(list.get(i));
            }
        }
        if(isReturn&&onRateChangeListener!=null){
            onRateChangeListener.onRateChange(rate);
        }
    }

    /**
     *根据分数显示星星
     * @param rate
     */
    public void setRate2(int rate){
        removeAllViews();
        int count = rate;
        for (int i = 0; i < list.size(); i++) {
            if (i < count) {
                list.get(i).setImageDrawable(on);
            } else {
                list.get(i).setImageDrawable(off);
            }
            addView(list.get(i));
        }
    }

    public void setOnRateChangeListener(OnRateChangeListener l){
        onRateChangeListener = l;

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //设置不可以滑动则不处理事件
        if(!isReturn){
            return super.onTouchEvent(event);
        }
        setRate(calculateStar(event.getX()));
        return true;
    }

    /**
     * 根据坐标计算分数
     * @param x
     * @return
     */
    private int calculateStar(float x) {
        //减去控件左padding
        float realPosition = x-paddingLeft;
        for(int i = 0;i<points.length;i++){
            if(points[i]>realPosition){
                return i;
            }
        }
        //如果循环结束即为右滑超出最大值，返回5分
        return 5;
    }

    /**
     * 评分改变的回调
     */
    public interface OnRateChangeListener{
        void onRateChange(int rate);
    }

    public void setRatable(boolean ratable) {
        this.ratable = ratable;
    }

    public boolean isRatable() {
        return ratable;
    }

    public void setReturn(boolean aReturn) {
        isReturn = aReturn;
    }
}
