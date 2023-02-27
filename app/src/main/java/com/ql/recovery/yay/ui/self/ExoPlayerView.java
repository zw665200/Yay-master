package com.ql.recovery.yay.ui.self;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.ql.recovery.yay.databinding.ItemFunAnchorBinding;
import com.ql.recovery.yay.util.JLog;

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
        setShutterBackgroundColor(Color.TRANSPARENT);
        setPlayer(mExoPlayer);
        mExoPlayer.setPlayWhenReady(false);
    }

    public void releasePlayer() {
        // 释放ExoPlayer资源
        if (mExoPlayer != null) {
            mExoPlayer.stop();
            mExoPlayer.release();
            mExoPlayer = null;
        }
    }

    public void setMediaSource(String url, ItemFunAnchorBinding itemBinding) {
        if (mExoPlayer != null) {
            // 创建 SimpleCache，指定缓存路径和缓存大小
//            SimpleCache simpleCache = new SimpleCache(new File(mContext.getExternalCacheDir(), "media"), new LeastRecentlyUsedCacheEvictor(1024 * 1024 * 3));
//            CacheDataSource cacheDataSource = new CacheDataSource.Factory().setCache(simpleCache).createDataSource();

            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(url)
                    .build();

//            CacheDataSourceFactory dataSourceFactory = new CacheDataSourceFactory(
//                    cache, new DefaultHttpDataSourceFactory(userAgent));
//            ProgressiveMediaExtractor progressiveMediaExtractor = new ProgressiveMediaExtractor(0, 3000);
//            DataSpec dataSpec = new DataSpec(uri);
//            CacheUtil.cache(dataSpec, dataSourceFactory, new SimpleCacheUtilListener(),
//                    new CachingCounters(), null, null, progressiveMediaExtractor);

            ProgressiveMediaSource mediaSource = new ProgressiveMediaSource
                    .Factory(new DefaultDataSourceFactory(mContext))
                    .createMediaSource(mediaItem);

            mExoPlayer.setMediaSource(mediaSource);
            mExoPlayer.prepare();

            mExoPlayer.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    Player.Listener.super.onPlaybackStateChanged(playbackState);
                    switch (playbackState) {
                        case Player.STATE_READY:
                            itemBinding.playerView.setVisibility(View.VISIBLE);
                            if (mExoPlayer != null) {
                                mExoPlayer.play();
                            }
                            break;
                        case Player.STATE_BUFFERING:
                            break;
                        case Player.STATE_ENDED:
                            break;
                        case Player.STATE_IDLE:
                            break;
                    }
                }

                @Override
                public void onRenderedFirstFrame() {
                    Player.Listener.super.onRenderedFirstFrame();
                    itemBinding.ivPhoto.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onPlayerError(@NonNull PlaybackException error) {
                    Player.Listener.super.onPlayerError(error);
                    JLog.i("onPlayerError : " + error);
                    JLog.i("onPlayerError code : " + error.errorCode);
                    JLog.i("onPlayerError url : " + url);

                    switch (error.errorCode) {
                        case ExoPlaybackException.TYPE_SOURCE:
                            JLog.i("TYPE_SOURCE: " + error.getMessage());
                            //Restart the playback
                            mExoPlayer.prepare();
                            break;
                    }
                }
            });
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
//                .setRenderersFactory(new DefaultRenderersFactory(mContext).setEnableDecoderFallback(true))
                .setMediaSourceFactory(new DefaultMediaSourceFactory(mContext))
                .setLoadControl(loadControl)
                .build();

        exoPlayer.setVolume(0);
        exoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);

        return exoPlayer;
    }


}
