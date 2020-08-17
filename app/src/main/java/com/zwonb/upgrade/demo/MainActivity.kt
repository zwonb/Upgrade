package com.zwonb.upgrade.demo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.zwonb.upgrade.UpgradeBean
import com.zwonb.upgrade.UpgradeDialog
import com.zwonb.upgrade.UpgradeListBean

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.text).setOnClickListener {
            request()
        }

    }

    private fun request() {
        UpgradeDialog.getInstance(UpgradeBean().apply {
            versionCode = 1
            versionName = "1.0.0"
            versionSize = "9.9 MB"
            versionDate = "2020-08-14"
            versionDownload = "https://imtt.dd.qq.com/16891/apk/236597FDF9DD65D8ABA087D815FDA597.apk?fsname=com.devuni.flashlight_10.10.27_20200609.apk&csr=1bbd"
            versionBody = mutableListOf<UpgradeListBean>().apply {
                add(UpgradeListBean().apply {
                    bodyIntroduct = "修复若干bug"
                })
                add(UpgradeListBean().apply {
                    bodyIntroduct = "增加App稳定性增加App稳定性增加App稳定性增加App稳定性增加App稳定性增加App稳定性"
                })
                for (i in 0..7) {
                    add(UpgradeListBean().apply {
                        bodyIntroduct = "增加App稳定性增加App稳定性增加App稳定性$i"
                    })
                }
            }
        }).showUpgrade(supportFragmentManager)
    }
}