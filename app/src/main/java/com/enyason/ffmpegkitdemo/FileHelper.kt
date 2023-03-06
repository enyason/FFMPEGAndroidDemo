package com.enyason.ffmpegkitdemo

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


fun Uri?.getAbsoluteFilePath(context: Context): String? {
    this ?: return null
    val docFile = DocumentFile.fromSingleUri(context,this)
    return docFile?.uri?.path
//    return context.contentResolver.query(this, null, null, null, null)
//        ?.use {
//            val columnIndex = it.getColumnIndex(MediaStore.Images.Media.DATA)
//            if (it.moveToFirst()) it.getString(columnIndex)
//            else null
//        }
}


object FileHelper {


    fun getRealPathFromURI(uri: Uri, context: Context): String? {
        val returnCursor = context.contentResolver.query(uri, null, null, null, null)
        val nameIndex = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        val size = returnCursor.getLong(sizeIndex).toString()
        val file = File(context.filesDir, "temp-file")
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            var read = 0
            val maxBufferSize = 1 * 1024 * 1024
            val bytesAvailable: Int = inputStream?.available() ?: 0
            //int bufferSize = 1024;
            val bufferSize = Math.min(bytesAvailable, maxBufferSize)
            val buffers = ByteArray(bufferSize)
            while (inputStream?.read(buffers).also {
                    if (it != null) {
                        read = it
                    }
                } != -1) {
                outputStream.write(buffers, 0, read)
            }
            Log.e("File Size", "Size " + file.length())
            inputStream?.close()
            outputStream.close()
            Log.e("File Path", "Path " + file.path)

        } catch (e: java.lang.Exception) {
            Log.e("Exception", e.message!!)
        }
        returnCursor.close()
        return file.path
    }


    fun getStoragePath(directoryName: String): String {

        var file: File? = null

        val mainPath = File.separator + "VideoConverter" + File.separator + directoryName

        val sdPath = "/storage/emulated/0/"

        if (isAboveQ()) {
            file = File(sdPath + Environment.DIRECTORY_DOWNLOADS + mainPath)

            if (!file.exists()) {
                file.mkdirs()
            } else {
                file = File(
                    Environment.getExternalStorageState(),
                    File.separator + Environment.DIRECTORY_DOWNLOADS + mainPath
                )

                if (!file.exists()) {
                    file.mkdirs()
                }

            }
        }


        return file?.absolutePath ?: sdPath

    }

    fun isAboveQ(): Boolean {
        return SDK_INT >= Build.VERSION_CODES.Q
    }

}
