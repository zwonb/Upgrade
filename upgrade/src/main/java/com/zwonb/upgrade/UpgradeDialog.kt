package com.zwonb.upgrade

import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.core.content.contentValuesOf
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.zwonb.upgrade.network.NetworkUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


/**
 * @author: zwonb
 * @date: 2020/8/14
 */
class UpgradeDialog : DialogFragment() {

    private val bean by lazy(LazyThreadSafetyMode.NONE) {
        arguments?.getSerializable("bean") as? UpgradeBean
    }
    private val file by lazy(LazyThreadSafetyMode.NONE) {
        File(
            requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            "${requireContext().packageName}${bean?.versionName}.apk"
        )
    }
    private lateinit var downloadView: TextView
    private lateinit var progressBar: ProgressBar


    companion object {
        fun getInstance(bean: UpgradeBean): UpgradeDialog {
            val dialog = UpgradeDialog()
            dialog.arguments = bundleOf("bean" to bean)
            return dialog
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        initDialog()
        return inflater.inflate(R.layout.dialog_upgrade, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
    }

    private fun initView(rootView: View) {
        rootView.findViewById<TextView>(R.id.versionName).text =
            String.format("%s %s", getString(R.string.version_name), bean?.versionName)
        rootView.findViewById<TextView>(R.id.package_size).text =
            String.format("%s %s", getString(R.string.package_size), bean?.versionSize)
        rootView.findViewById<TextView>(R.id.upgrade_time).text =
            String.format("%s %s", getString(R.string.upgrade_time), bean?.versionDate)
        val sb = StringBuilder()
        bean?.versionBody?.forEachIndexed { index, listBean ->
            if (index != 0) {
                sb.append("\n")
            }
            sb.append(index + 1).append(".").append(listBean.bodyIntroduct)
        }
        rootView.findViewById<TextView>(R.id.upgrade_feature).text = sb

        downloadView = rootView.findViewById(R.id.download)
        progressBar = rootView.findViewById(R.id.upgrade_progress)
        setDownloadView()
    }

    private fun setDownloadView() {
        lifecycleScope.launch(Dispatchers.Main) {
            downloadView.isEnabled = true
            val hasApk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                hasApkQ(false)
            } else {
                file.exists()
            }
            if (hasApk) {
                progressBar.progress = 100
                downloadView.setText(R.string.install)
                downloadView.setOnClickListener {
                    installApk()
                }
            } else {
                downloadView.setText(R.string.download)
                downloadView.setOnClickListener {
                    it.isEnabled = false
                    requestDownload()
                }
            }
        }
    }

    private fun requestDownload() {
        val url = bean?.versionDownload
        if (url.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "url is null", Toast.LENGTH_SHORT).show()
            downloadView.isEnabled = true
            return
        }
        lifecycleScope.launch {
            NetworkUtil.downloadFile(url, file) {
                onDownload {
                    progressBar.progress = it.percent()
                    downloadView.text =
                        String.format(getString(R.string.updating), it.percent())
                }
                onComplete {
                    installApk()
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        setDownloadView()
                    }
                }
                onError {
                    setDownloadView()
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun installApk() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            installApkQ()
            return
        }
        if (!file.exists()) {
            Toast.makeText(requireContext(), "安装包不存在", Toast.LENGTH_SHORT).show()
            setDownloadView()
            return
        }
        val uri = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> FileProvider.getUriForFile(
                requireContext(), requireContext().packageName + ".file_provider", file
            )
            else -> Uri.fromFile(file)
        }
        startInstallApk(uri)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun installApkQ() {
        lifecycleScope.launch(Dispatchers.IO) {
            if (!hasApkQ(true)) {
                copyApkToDownloadQ()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun hasApkQ(install: Boolean): Boolean = withContext(Dispatchers.IO) {
        var hasApk = false
        val cursor = requireContext().contentResolver.query(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI, null, null, null,
            "${MediaStore.MediaColumns.DATE_ADDED} desc"
        )
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val name =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
                if (name == file.name) {
                    hasApk = true
                    val id =
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                    val apkUri = ContentUris.withAppendedId(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI, id
                    )
                    if (install) {
                        withContext(Dispatchers.Main) { startInstallApk(apkUri) }
                    }
                    break
                }
            }
        }
        cursor?.close()
        hasApk
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun copyApkToDownloadQ() {
        val values = contentValuesOf(
            MediaStore.Downloads.DISPLAY_NAME to file.name,
            MediaStore.Downloads.MIME_TYPE to "application/vnd.android.package-archive",
            MediaStore.Downloads.RELATIVE_PATH to Environment.DIRECTORY_DOWNLOADS + "/${requireContext().packageName}"
        )
        val resolver = requireContext().contentResolver
        val uri = resolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI, values
        )
        if (uri != null) {
            val out = resolver.openOutputStream(uri)
            if (out != null) {
                file.inputStream().copyTo(out)
                file.delete()
                setDownloadView()
                withContext(Dispatchers.Main) { startInstallApk(uri) }
            }
        }
    }

    private fun startInstallApk(apkUri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            setDataAndType(apkUri, "application/vnd.android.package-archive")
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun initDialog() {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setCancelable(false)
        dialog?.setOnKeyListener { _, keyCode, _ -> keyCode == KeyEvent.KEYCODE_BACK }
    }

    fun showUpgrade(manager: FragmentManager) {
        try {
            showNow(manager, "upgrade")
        } catch (e: Exception) {
        }
    }

}