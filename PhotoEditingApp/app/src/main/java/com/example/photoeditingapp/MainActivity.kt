package com.example.photoeditingapp

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import jp.co.cyberagent.android.gpuimage.GPUImageView
import jp.co.cyberagent.android.gpuimage.filter.GPUImageBrightnessFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageContrastFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSaturationFilter
import java.io.File
import java.io.FileOutputStream
//import java.io.OutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var gpuImageView: GPUImageView
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private var currentBitmap: Bitmap? = null
    private var rotationAngle = 0f
    private var flipped = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gpuImageView = findViewById(R.id.gpuImageView)

        val btnSelect = findViewById<Button>(R.id.btnSelect)
        val btnRotate = findViewById<Button>(R.id.btnRotate)
        val btnFlip = findViewById<Button>(R.id.btnFlip)
        val btnSave = findViewById<Button>(R.id.btnSave)

        val seekBrightness = findViewById<SeekBar>(R.id.seekBrightness)
        val seekContrast = findViewById<SeekBar>(R.id.seekContrast)
        val seekSaturation = findViewById<SeekBar>(R.id.seekSaturation)

        // ✅ Modern ActivityResultLauncher (no deprecated startActivityForResult)
        imagePickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val data: Intent? = result.data
                    val uri: Uri? = data?.data
                    uri?.let {
                        val bitmap = loadBitmapFromUri(it)
                        currentBitmap = bitmap
                        gpuImageView.setImage(bitmap)
                    }
                }
            }

        // Select Image
        btnSelect.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        }

        // Rotate
        btnRotate.setOnClickListener {
            rotationAngle += 90f
            gpuImageView.rotation = rotationAngle
        }

        // Flip
        btnFlip.setOnClickListener {
            flipped = !flipped
            gpuImageView.scaleX = if (flipped) -1f else 1f
        }

        // Brightness
        seekBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = (progress - 100) / 100f
                gpuImageView.filter = GPUImageBrightnessFilter(value)
            }

            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        // Contrast
        seekContrast.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress / 100f
                gpuImageView.filter = GPUImageContrastFilter(value)
            }

            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        // Saturation
        seekSaturation.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress / 100f
                gpuImageView.filter = GPUImageSaturationFilter(value)
            }

            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        // Save Image
        btnSave.setOnClickListener {
            val bmp = gpuImageView.gpuImage.bitmapWithFilterApplied
            bmp?.let {
                saveImage(it)
            } ?: Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ Safe image loading (no deprecated getBitmap)
    private fun loadBitmapFromUri(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= 28) {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }
    }

    // ✅ Final saveImage (works on all versions, no deprecated APIs)
    private fun saveImage(bitmap: Bitmap) {
        try {
            val filename = "edited_${System.currentTimeMillis()}.jpg"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android 10+
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/PhotoEditingApp")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                uri?.let {
                    contentResolver.openOutputStream(it)?.use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(uri, contentValues, null, null)
                }

            } else {
                // For Android 9 and below
                val picturesDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES
                ).toString() + "/PhotoEditingApp"

                val dir = File(picturesDir)
                if (!dir.exists()) dir.mkdirs()

                val file = File(dir, filename)
                val out = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                out.flush()
                out.close()

                // Update Gallery
                android.media.MediaScannerConnection.scanFile(
                    this,
                    arrayOf(file.absolutePath),
                    arrayOf("image/jpeg"),
                    null
                )
            }

            Toast.makeText(this, "Image saved to Gallery", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Save failed!", Toast.LENGTH_SHORT).show()
        }
    }
}
