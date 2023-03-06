package com.ql.recovery.yay.util

import android.net.Uri
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.TransferListener
import com.google.android.exoplayer2.upstream.cache.Cache

class CustomLoader(
    private val upstream: DataSource,
    private val cache: Cache,
    private val maxCacheDurationMs: Long
) : DataSource {

    private var cacheStarted = false
    private var cacheEndPositionUs = 0L
    private var dataSpec: DataSpec? = null

    override fun open(dataSpec: DataSpec): Long {
        this.dataSpec = dataSpec
        cacheStarted = false
        cacheEndPositionUs = 0L
        return upstream.open(dataSpec)
    }

    override fun read(buffer: ByteArray, offset: Int, readLength: Int): Int {
        val bytesRead = upstream.read(buffer, offset, readLength)
        if (!cacheStarted && bytesRead != C.RESULT_END_OF_INPUT) {
            cacheStarted = true
            cacheEndPositionUs = upstream.peekNextSample()?.timeUs ?: 0L + maxCacheDurationMs * 1000
            if (dataSpec != null && dataSpec!!.key != null) {
                cache.startFile(dataSpec!!.key!!, 0L, cacheEndPositionUs)
            }
        } else if (cacheStarted && bytesRead != C.RESULT_END_OF_INPUT) {
            cache.writeData(buffer, offset, bytesRead)
            cache.startReadWrite()
            if (upstream.peekNextSample()?.timeUs ?: 0L > cacheEndPositionUs) {
                cache.commitFile()
                cache.commitData()
                return bytesRead
            }
        } else if (bytesRead == C.RESULT_END_OF_INPUT && cacheStarted) {
            cache.commitData()
        }
        return bytesRead
    }

    override fun addTransferListener(transferListener: TransferListener) {
        upstream.addTransferListener(transferListener)
    }

    override fun getUri(): Uri? {
        return upstream.uri
    }

    override fun close() {
        upstream.close()
    }

}