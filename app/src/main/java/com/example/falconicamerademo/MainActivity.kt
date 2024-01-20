package com.example.falconicamerademo

import android.app.Activity
import android.content.ContentResolver
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
import java.io.File
import java.io.FileOutputStream
import java.util.*

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.camera_button)


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
    }

    private fun createImage(): Uri? {
        val resolver = this.contentResolver
        var imageUri: Uri? = null
        imageUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val imageName = System.currentTimeMillis().toString()
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "$imageName.jpg")
        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures" + "/FalconI")
        val finalUri = resolver.insert(imageUri, contentValues)
        return finalUri
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


}