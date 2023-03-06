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
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.ql.recovery.yay.databinding.ItemFunAnchorBinding;
import com.ql.recovery.yay.util.JLog;

public class ExoPlayerView extends PlayerView {
    private ExoPlayer mExoPlayer;
    private Context mContext;
    private SimpleCache simpleCache;

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

    public void initPlayer(String url) {
        // 创建ExoPlayer实例
        mExoPlayer = getPlayer(url);
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

            // 创建 CacheDataSink 对象
//            CacheDataSink.Factory cacheDataSinkFactory = new CacheDataSink.Factory()
//                    .setCache(simpleCache)
//                    .setFragmentSize(CacheDataSink.DEFAULT_FRAGMENT_SIZE)
//                    .setBufferSize(CacheDataSink.DEFAULT_BUFFER_SIZE);

//            SimpleCache cache = new SimpleCache(new File(mContext.getExternalCacheDir(), "media/" + AppUtil.md5Encode(url)), new LeastRecentlyUsedCacheEvictor(3 * 1024 * 1024));
//
//            DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(mContext, "exoplayer-codelab");
//
//            DefaultDataSourceFactory upstreamFactory = new DefaultDataSourceFactory(mContext, "exoplayer-codelab");
//
//            CustomLoaderFactory customLoaderFactory = new CustomLoaderFactory(
//                    upstreamFactory,
//                    cache,
//                    3 * 1000
//            );
//
//            DataSource.Factory cacheDataSource = () -> {
//                CacheDataSource cacheDataSource1 = new CacheDataSource(
//                        cache,
//                        customLoaderFactory.createDataSource(),
//                        new FileDataSource(),
//                        new CacheDataSink(cache, CacheDataSink.DEFAULT_FRAGMENT_SIZE),
//                        CacheDataSource.FLAG_BLOCK_ON_CACHE,
//                        null
//                );
//                return cacheDataSource1;
//            };

            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(url)
                    .build();

            DataSource.Factory dataSourceFactory = new FileDataSource.Factory();

            ProgressiveMediaSource mediaSource = new ProgressiveMediaSource
//                    .Factory((DataSource.Factory) () -> new DefaultHttpDataSource.Factory().createDataSource())
                    .Factory(dataSourceFactory)
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
                                // 视频已准备就绪
                                // 等待前三秒的数据缓存完毕
//                                while (mExoPlayer.getCurrentPosition() < 3000) {
//                                    try {
//                                        Thread.sleep(200);
//                                    } catch (InterruptedException e) {
//                                        e.printStackTrace();
//                                    }
//                                }

                                // 前三秒的数据已经缓存完毕
                                // 可以使用ExoPlayer的`getBufferedPosition`方法获取当前已缓存的视频长度
//                                long startPosition = 0;
//                                long endPosition = Math.min(mExoPlayer.getBufferedPosition(), 3000);
//                                Cache cache = new SimpleCache(new File(mContext.getCacheDir(), "exoplayer-cache"), new NoOpCacheEvictor());
//                                CacheDataSourceFactory cacheDataSourceFactory = new CacheDataSourceFactory(cache, dataSourceFactory);
//                                DataSpec dataSpec = new DataSpec(Uri.parse(url), startPosition, endPosition, null);


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

    public ExoPlayer getPlayer(String url) {
        DefaultLoadControl loadControl = new DefaultLoadControl.Builder()
                .setBufferDurationsMs(3000, 4000, 250, 500)
                .build();

//        File file = new File(mContext.getExternalCacheDir(), "media/" + AppUtil.md5Encode(url));

        // 创建 SimpleCache，指定缓存路径和缓存大小
//        if (simpleCache == null) {
//            simpleCache = new SimpleCache(file, new LeastRecentlyUsedCacheEvictor(1024 * 1024 * 3));
//        }

        DataSource.Factory cacheDataSourceFactory = new CacheDataSource.Factory()
                .setCache(simpleCache)
                .setUpstreamDataSourceFactory(new DefaultHttpDataSource.Factory())
                .setEventListener(new CacheDataSource.EventListener() {
                    @Override
                    public void onCachedBytesRead(long cacheSizeBytes, long cachedBytesRead) {
                        JLog.i("cacheSizeBytes = " + cacheSizeBytes);
                        JLog.i("cachedBytesRead = " + cachedBytesRead);
                    }

                    @Override
                    public void onCacheIgnored(int reason) {
                        JLog.i("reason = " + reason);
                    }
                });

        DataSource.Factory dataSourceFactory = new FileDataSource.Factory();

        MediaSource.Factory mediaSourceFactory = new DefaultMediaSourceFactory(mContext)
                .setDataSourceFactory(dataSourceFactory);

        ExoPlayer exoPlayer = new ExoPlayer.Builder(mContext)
                .setRenderersFactory(new DefaultRenderersFactory(mContext).setEnableDecoderFallback(true))
                .setMediaSourceFactory(mediaSourceFactory)
                .setLoadControl(loadControl)
                .build();

        exoPlayer.setVolume(0);
        exoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);

        return exoPlayer;
    }


}
