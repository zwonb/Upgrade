package com.zwonb.upgrade

import java.io.File

/**
 * @author: zwonb
 * @date: 2020/8/12
 */

/**
 * 保证文件的父文件夹已经存在
 */
fun File.smartCrateNewFile(): Boolean {
    if (exists()) {
        return true
    }
    if (parentFile!!.exists()) {
        return createNewFile()
    } else {
        if (parentFile!!.mkdirs()) {
            return createNewFile()
        }
    }
    return false
}