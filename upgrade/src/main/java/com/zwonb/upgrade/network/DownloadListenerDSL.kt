package com.zwonb.upgrade.network

/**
 * @author: zwonb
 * @date: 2020/8/13
 */
class DownloadListener {

    internal lateinit var downloadBlock: (DownloadBean) -> Unit
    internal lateinit var completeBlock: (DownloadBean) -> Unit
    internal lateinit var errorBlock: (Throwable) -> Unit

    fun onDownload(block: (DownloadBean) -> Unit) {
        downloadBlock = block
    }

    fun onComplete(block: (DownloadBean) -> Unit) {
        completeBlock = block
    }

    fun onError(block: (e: Throwable) -> Unit) {
        errorBlock = block
    }
}