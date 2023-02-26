package com.ql.recovery.yay.ui.self;

import android.content.Context;
import android.util.AttributeSet;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerView;

public class ExoPlayerView extends PlayerView {
    private ExoPlayer mExoPlayer;
    private Context mContext;

    public ExoPlayerView(Context context) {
        this(context, null);
    }

    public ExoPlayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExoPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public void initPlayer() {
        // 创建ExoPlayer实例
        mExoPlayer = getPlayer();
        setPlayer(mExoPlayer);
        mExoPlayer.setPlayWhenReady(true);
    }

    public void releasePlayer() {
        // 释放ExoPlayer资源
        if (mExoPlayer != null) {
            mExoPlayer.stop();
            mExoPlayer.release();
            mExoPlayer = null;
        }
    }

    public void setMediaSource(MediaSource mediaSource) {
        if (mExoPlayer != null) {
            mExoPlayer.setMediaSource(mediaSource);
            mExoPlayer.prepare();
        }
    }

    public ExoPlayer getPlayer() {
        DefaultLoadControl loadControl = new DefaultLoadControl.Builder()
                .setBufferDurationsMs(3000, 4000, 250, 500)
                .build();

//        val mediaSourceFactory = DefaultMediaSourceFactory(requireContext())
//            .setDataSourceFactory(cacheDataSourceFactory)
//            .setLocalAdInsertionComponents(adsLoaderProvider, playerView)

        ExoPlayer exoPlayer = new ExoPlayer.Builder(mContext)
                .setRenderersFactory(new DefaultRenderersFactory(mContext).setEnableDecoderFallback(true))
                .setMediaSourceFactory(new DefaultMediaSourceFactory(mContext))
                .setLoadControl(loadControl)
                .build();

        exoPlayer.setVolume(0);
        exoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);

        return exoPlayer;
    }


}
