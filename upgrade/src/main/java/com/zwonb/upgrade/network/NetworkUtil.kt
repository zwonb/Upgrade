package com.zwonb.upgrade.network

import com.zwonb.upgrade.smartCrateNewFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Streaming
import retrofit2.http.Url
import java.io.File
import java.io.RandomAccessFile

/**
 * 网络工具
 * @author: zwonb
 * @date: 2020/8/12
 */
object NetworkUtil {

    private var timeMillis = System.currentTimeMillis()

    val downloadRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://127.0.0.1")
            .client(OkHttpClient())
            .build()
    }

    suspend fun downloadFile(
        url: String, saveFile: File, listener: DownloadListener.() -> Unit
    ) {
        val callbackDSL = DownloadListener().apply(listener)
        try {
            withContext(Dispatchers.IO) {
                val tmpFilePath = tmpFilePath(saveFile)
                val tmpFile = File(tmpFilePath)
                val range = if (tmpFile.exists()) tmpFile.length() else 0
                val response =
                    downloadRetrofit.create<DownloadApi>().download(url, range.toDownloadRange())
                writeFile(response, saveFile, range, callbackDSL)
            }
        } catch (e: Exception) {
            callbackDSL.errorBlock(e)
        }
    }

    private suspend fun writeFile(
        response: ResponseBody,
        saveFile: File,
        range: Long,
        callbackDSL: DownloadListener
    ) {
        val tmpFile = createTmpFile(saveFile, range)
        val bean = DownloadBean(range + response.contentLength())
        bean.bytesLoaded = range

        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val output = response.byteStream()
        var read = output.read(buffer)
        while (read > 0) {
            tmpFile.write(buffer, 0, read)
            bean.bytesLoaded += read

            // 防止频繁刷新
            val currentTimeMillis = System.currentTimeMillis()
            if (currentTimeMillis - timeMillis >= 50) {
                timeMillis = currentTimeMillis
                withContext(Dispatchers.Main) {
                    callbackDSL.downloadBlock(bean)
                }
            }

            read = output.read(buffer)
        }
        // 避免最后一次没刷新
        withContext(Dispatchers.Main) {
            callbackDSL.downloadBlock(bean)
        }

        File(tmpFilePath(saveFile)).renameTo(saveFile)
        tmpFile.close()
        withContext(Dispatchers.Main) {
            callbackDSL.completeBlock(bean)
        }
    }

    private fun createTmpFile(saveFile: File, range: Long): RandomAccessFile {
        val tmpFilePath = tmpFilePath(saveFile)
        File(tmpFilePath).apply {
            if (!exists()) smartCrateNewFile()
        }
        val tmpFile = RandomAccessFile(tmpFilePath, "rwd")
        if (range > 0) {
            tmpFile.seek(range)
        }
        return tmpFile
    }

    private fun tmpFilePath(file: File) = "${file.path}.tmp"
}


interface DownloadApi {

    @Streaming
    @GET
    suspend fun download(
        @Url url: String,
        @Header("Range") range: String = "bytes=0-"
    ): ResponseBody
}

fun Long.toDownloadRange() = "bytes=$this-"