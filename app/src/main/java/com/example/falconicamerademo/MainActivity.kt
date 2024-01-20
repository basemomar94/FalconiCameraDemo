package com.example.falconicamerademo

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import java.io.*

class MainActivity : AppCompatActivity() {
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as Bitmap?
                // Handle the captured image (e.g., display it in an ImageView)
                if (imageBitmap != null) {
                    saveImageToGallery(imageBitmap)
                }
            }
        }
    private val takeVideoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val videoUri: Uri? = result.data?.data
                // Handle the captured image (e.g., display it in an ImageView)
                if (videoUri != null) {
                    saveVideoToGallery(videoUri)
                }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.camera_button)
        val button2 = findViewById<Button>(R.id.video_button)



        button.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                val imagePath: Uri? = createImage()
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imagePath)
                takePictureLauncher.launch(takePictureIntent)
            } else {
                // Handle the case where camera permission is not granted
                // You may want to request the permission here
            }
        }
        button2.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val takePictureIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                val imagePath: Uri? = createVideo()
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imagePath)
                takeVideoLauncher.launch(takePictureIntent)
            } else {
                // Handle the case where camera permission is not granted
                // You may want to request the permission here
            }
        }
    }


    private fun createImage(): Uri? {
        val resolver = this.contentResolver
        var imageUri: Uri? = null
        imageUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val imageName = System.currentTimeMillis().toString()
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "$imageName.jpg")
        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures" + "/FalconI Pictures")
        val finalUri = resolver.insert(imageUri, contentValues)
        return finalUri
    }
    private fun createVideo(): Uri? {
        val resolver = contentResolver
        var videoUri: Uri? = null

        try {
            videoUri = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

            val videoName = System.currentTimeMillis().toString()
            val contentValues = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, "$videoName.mp4")
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/FalconI Videos")
            }

            return resolver.insert(videoUri, contentValues)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return videoUri
    }


    private fun saveImageToGallery(bitmap: Bitmap) {
        Log.d("save", "saving to gallery")
        val storageDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val fileName = System.currentTimeMillis().toString() + ".jpg"
        val imageFile = File(storageDir, fileName)
        val outPutStream = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outPutStream)
        outPutStream.flush()
        outPutStream.close()

        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        mediaScanIntent.data = Uri.fromFile(imageFile)
        sendBroadcast(mediaScanIntent)
    }
    private fun saveVideoToGallery(videoUri: Uri) {
        Log.d("save", "saving video to gallery")

        // Ensure you have the necessary permissions to write to external storage.
        // For simplicity, you may want to check for permissions before proceeding.

        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        val fileName = System.currentTimeMillis().toString() + ".mp4"
        val videoFile = File(storageDir, fileName)

        try {
            val inputStream: InputStream? = contentResolver.openInputStream(videoUri)
            val outputStream: OutputStream = FileOutputStream(videoFile)

            if (inputStream != null) {
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()

                // Notify the media scanner about the new video
                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                mediaScanIntent.data = Uri.fromFile(videoFile)
                sendBroadcast(mediaScanIntent)

                Log.d("save", "Video saved successfully to: ${videoFile.absolutePath}")
            } else {
                Log.e("save", "Failed to open InputStream for videoUri: $videoUri")
            }
        } catch (e: IOException) {
            Log.e("save", "Error saving video: ${e.message}")
            e.printStackTrace()
        }
    }



}