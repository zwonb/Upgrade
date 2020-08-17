package com.zwonb.upgrade

import androidx.annotation.Keep
import java.io.Serializable

/**
 * @author: zwonb
 * @date: 2020/8/13
 */
@Keep
class UpgradeBean : Serializable {

    var versionCode = 0
    var versionName: String? = null
    var versionSize: String? = null
    var versionDate: String? = null
    var versionDownload: String? = null
    var versionBody: List<UpgradeListBean>? = null
}

@Keep
class UpgradeListBean : Serializable {
    var bodyIntroduct: String? = null
}
