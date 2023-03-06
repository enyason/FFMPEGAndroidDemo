package com.enyason.ffmpegkitdemo

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegSession
import com.arthenica.ffmpegkit.FFmpegSessionCompleteCallback
import com.arthenica.ffmpegkit.ReturnCode
import java.io.File
import java.io.FileInputStream
import kotlin.random.Random


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val contract =
            ActivityResultContracts.GetContent() // define the kind of intent with this contract

        // ActivityResultCallback is a SAM interface, so we've used a lamda approach. The method is
        // triggered when there is a result from the action
        val callback = ActivityResultCallback<Uri?> { uri ->
            uri?.let {
                val inputVideoPath = FileHelper.getRealPathFromURI(uri, this)
                inputVideoPath?.let {
                    executeCommand(inputVideoPath)
                }
            }
        }

        val launcher = registerForActivityResult(contract, callback)


        findViewById<Button>(R.id.selectVideo).setOnClickListener {
            // permission

            val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
            val permissionCheck = ContextCompat.checkSelfPermission(this, permission)

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), PERMISSION_CODE)
            } else {
                // Your code to write file to the external storage
                launcher.launch(MIME_TYPE)
            }

        }


    }

    private fun shareGif(file: File) {
        val gifUri = FileProvider.getUriForFile(this, "com.example.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/gif"
            putExtra(Intent.EXTRA_STREAM, gifUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(shareIntent)
    }



    private fun executeCommand(inputFilePath: String) {

        println("FFMPEG demo input file path $inputFilePath")

        val num = Random.nextInt(4600)
        val outputFile = File(getExternalFilesDir(null), "output-tweaked$num.gif")


        val outPutFilePath = outputFile.absolutePath
        val ffmpegCommand = "-i $inputFilePath -t 10 -r 24 $outPutFilePath"
//        val ffmpegCommand = "-y -i $inputPath -vf scale=320:-1 -t 4 -r 10 $outPutFilePath"


        println("FFMPEG demo output path $outPutFilePath")


        FFmpegKit.executeAsync(ffmpegCommand) { session ->

            if (ReturnCode.isSuccess(session.returnCode)) {

                shareGif(outputFile)

            } else {

                // FAILURE
                Log.d(
                    TAG,
                    String.format(
                        "Command failed with state %s and rc %s.%s",
                        session.state,
                        session.returnCode,
                        session.failStackTrace
                    )
                );

            }

        }
    }

    companion object {
        const val MIME_TYPE = "video/*"
        const val TAG = "FFMPEG_TAG"
        const val PERMISSION_CODE = 1
        val PERMISSIONS_ALL = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    }
}
