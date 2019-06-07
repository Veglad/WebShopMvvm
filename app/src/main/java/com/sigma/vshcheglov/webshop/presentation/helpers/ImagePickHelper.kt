package com.sigma.vshcheglov.webshop.presentation.helpers

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.sigma.vshcheglov.webshop.BuildConfig
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class ImagePickHelper(private val context: Context) {

    var imagePath: String? = null

    fun getPickImageIntent() = Intent(Intent.ACTION_PICK).also { imagePickIntent ->
        imagePath = imagePath ?: createImageFileName()
        imagePickIntent.type = "image/*"
        val mimeTypes = arrayOf("image/jpeg", "image/png")
        imagePickIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
    }

    fun getCaptureIntent(): Intent? {
        // Create the File where the photo should go
        val photoFile: File = try {
            imagePath = imagePath ?: createImageFileName()
            File(imagePath)
        } catch (ex: IOException) {
            null
        } ?: return null

        val photoURI: Uri = FileProvider.getUriForFile(
            context,
            BuildConfig.APPLICATION_ID + ".provider",
            photoFile
        )

        return Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(context.packageManager)?.also {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            }
        }
    }

    private fun createImageFileName(): String {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return "$storageDir/JPEG_${timeStamp}_.jpg"
    }

    fun saveImageToInternalStorage(imageUri: Uri?, isFromCamera: Boolean) {
        if (isFromCamera || imageUri == null || imagePath == null) {
            return
        }

        //Copy file from gallery to app external files dir
        context.contentResolver.openInputStream(imageUri).use { inputStream ->
            FileOutputStream(File(imagePath)).use { fileOutputStream ->
                copy(inputStream, fileOutputStream)
            }
        }
    }

    private fun copy(source: InputStream, sink: OutputStream) {
        val BUFFER_SIZE = 8192
        val buffer = ByteArray(BUFFER_SIZE)
        var byte = source.read(buffer)
        while (byte > 0) {
            sink.write(buffer, 0, byte)
            byte = source.read(buffer)
        }
    }

    fun getImageBitmap(imageUri: Uri?, isFromCamera: Boolean): Bitmap? {
        val selectedImageUri: Uri = when {
            isFromCamera -> {
                imagePath?.let {
                    Uri.parse("file://$imagePath")
                } ?: return null
            }
            imageUri != null -> imageUri
            else -> return null
        }
        return MediaStore.Images.Media.getBitmap(context.contentResolver, selectedImageUri)
    }
}
