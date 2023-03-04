package com.ql.recovery.yay.util;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.ql.recovery.yay.R;
import com.yanzhenjie.album.AlbumFile;
import com.yanzhenjie.album.AlbumLoader;

/**
 * @author Herr_Z
 * @description: 搭配相册库使用
 * @date : 2023/3/2 15:30
 */
public class MediaLoader implements AlbumLoader {

    @Override
    public void load(ImageView imageView, AlbumFile albumFile) {
        load(imageView, albumFile.getPath());
    }

    @Override
    public void load(ImageView imageView, String url) {
        Glide.with(imageView.getContext())
                .load(url)
                .error(R.drawable.placeholder)
                .placeholder(R.drawable.placeholder)
                .into(imageView);
    }
}
