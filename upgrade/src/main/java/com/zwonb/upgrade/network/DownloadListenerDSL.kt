package com.zwonb.upgrade.network

/**
 * @author: zwonb
 * @date: 2020/8/13
 */
class DownloadListener {

    internal lateinit var downloadBlock: suspend (DownloadBean) -> Unit
    internal lateinit var completeBlock: suspend (DownloadBean) -> Unit
    internal lateinit var errorBlock: suspend (Throwable) -> Unit

    fun onDownload(block: suspend (DownloadBean) -> Unit) {
        downloadBlock = block
    }

    fun onComplete(block: suspend (DownloadBean) -> Unit) {
        completeBlock = block
    }

    fun onError(block: suspend (e: Throwable) -> Unit) {
        errorBlock = block
    }
}