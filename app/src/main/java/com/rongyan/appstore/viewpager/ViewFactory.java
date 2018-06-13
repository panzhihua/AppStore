package com.rongyan.appstore.viewpager;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.rongyan.appstore.widget.GlideRoundTransform;

import com.rongyan.appstore.R;;

/**
 * ImageView创建工厂
 */
public class ViewFactory {

	/**
	 * 获取ImageView视图的同时加载显示url
	 */
	public static ImageView getImageView(Context context, final String url) {
		final ImageView imageView = (ImageView)LayoutInflater.from(context).inflate(
				R.layout.view_banner, null);
		Glide.with(context)
				.load(url)
				.placeholder(R.drawable.banner_default)
				.error(R.drawable.banner_default)
				.transform(new GlideRoundTransform(context, 10))
				.into(imageView);
		return imageView;
	}
}
