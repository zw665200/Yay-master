//package com.ql.recovery.yay.util
//
//import com.google.android.exoplayer2.upstream.DataSource
//import com.google.android.exoplayer2.upstream.cache.Cache
//
//class CustomLoaderFactory(
//    private val upstreamFactory: DataSource.Factory,
//    private val cache: Cache,
//    private val maxCacheDurationMs: Long
//) : DataSource.Factory {
//
//    override fun createDataSource(): DataSource {
//        return CustomLoader(upstreamFactory.createDataSource(), cache, maxCacheDurationMs)
//    }
//}
//
