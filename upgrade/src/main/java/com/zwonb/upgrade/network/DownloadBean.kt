package com.zwonb.upgrade.network

/**
 * @author: zwonb
 * @date: 2020/8/13
 */
data class DownloadBean(val total: Long, var bytesLoaded: Long = 0L) {

    fun percent(): Int = (bytesLoaded * 100 / total).toInt()
}