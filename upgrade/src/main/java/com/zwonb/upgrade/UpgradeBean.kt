package com.zwonb.upgrade

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep

/**
 * @author: zwonb
 * @date: 2020/8/13
 */
@Keep
class UpgradeBean() : Parcelable {

    var versionCode: String? = "0"
    var versionName: String? = null
    var versionSize: String? = null
    var versionDate: String? = null
    var versionDownload: String? = null
    var versionBody: List<UpgradeListBean>? = null

    constructor(parcel: Parcel) : this() {
        versionDate = parcel.readString()
        versionSize = parcel.readString()
        versionDownload = parcel.readString()
        versionCode = parcel.readString()
        versionName = parcel.readString()
        versionBody = parcel.createTypedArrayList(UpgradeListBean)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(versionDate)
        parcel.writeString(versionSize)
        parcel.writeString(versionDownload)
        parcel.writeString(versionCode)
        parcel.writeString(versionName)
        parcel.writeTypedList(versionBody)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UpgradeBean> {
        override fun createFromParcel(parcel: Parcel): UpgradeBean {
            return UpgradeBean(parcel)
        }

        override fun newArray(size: Int): Array<UpgradeBean?> {
            return arrayOfNulls(size)
        }
    }
}

@Keep
class UpgradeListBean() : Parcelable {
    var bodyIntroduct: String? = null

    constructor(parcel: Parcel) : this() {
        bodyIntroduct = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(bodyIntroduct)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UpgradeListBean> {
        override fun createFromParcel(parcel: Parcel): UpgradeListBean {
            return UpgradeListBean(parcel)
        }

        override fun newArray(size: Int): Array<UpgradeListBean?> {
            return arrayOfNulls(size)
        }
    }
}
