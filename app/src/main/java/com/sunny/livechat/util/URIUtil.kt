package com.sunny.livechat.util

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.loader.content.CursorLoader


/**
 * Desc
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/10/30 15:10
 */
object URIUtil {

    fun getRealPathFromUri(context: Context?, uri: Uri?): String {

        if (context == null || uri == null) {
            return ""
        }

        //content判断不完善需要增加条件区分API版本

        if ("file" == uri.scheme) {
            return getRealPathFromUriByFile(uri)
        } else if ("content" == uri.scheme && !uri.toString().contains("provider")) {
            return getRealPathFromUriApi11To18(context, uri)
        }
        return getRealPathFromUriAboveApi19(context, uri)
    }


    /**
     *
     * 适配api19以上,根据uri获取图片的绝对路径
     */

    private fun getRealPathFromUriByFile(uri: Uri): String {
        val uriStr = uri.toString()
        return uriStr.substring(uriStr.indexOf(":") + 3)

    }


    /**
     * //适配api11-api18,根据uri获取图片的绝对路径。
     * 针对图片URI格式为Uri:: content://media/external/images/media/1028
     */

    private fun getRealPathFromUriApi11To18(context: Context, uri: Uri): String {
        var filePath: String? = null
        val projection = arrayOf(MediaStore.Images.Media.DATA)

        val loader = CursorLoader(context, uri, projection, null,
                null, null)
        val cursor = loader.loadInBackground()

        if (cursor != null) {
            cursor.moveToFirst()
            filePath = cursor.getString(cursor.getColumnIndex(projection[0]))
            cursor.close()
        }
        return filePath ?: ""
    }


    /**
     * 适配api19以上,根据uri获取图片的绝对路径
     */

    private fun getRealPathFromUriAboveApi19(context: Context, uri: Uri): String {
        var filePath: String? = null
        val wholeID = DocumentsContract.getDocumentId(uri)

        // 使用':'分割
        val id = wholeID.split(":")[1]

        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val selection = MediaStore.Images.Media._ID + "=?"
        val selectionArgs = arrayOf(id)

        val cursor = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                selection, selectionArgs, null)
        cursor?.let {
            val columnIndex = it.getColumnIndex(projection[0]);

            if (it.moveToFirst()) {
                filePath = it.getString(columnIndex);
            }
            cursor.close()
        }

        return filePath ?: ""
    }
}