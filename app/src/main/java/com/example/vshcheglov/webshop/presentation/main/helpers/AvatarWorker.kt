package com.example.vshcheglov.webshop.presentation.main.helpers

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.vshcheglov.webshop.data.DataProvider
import java.io.File
import java.util.*

class AvatarWorker(context: Context, workerParameters: WorkerParameters, val dataProvider: DataProvider) :
    Worker(context, workerParameters) {

    companion object {
        const val KEY_AVATAR_WORKER_IMAGE_PATH = "KEY_AVATAR_WORKER_IMAGE_PATH"
        val TAG = AvatarWorker.javaClass.canonicalName
    }

    override fun doWork(): Result {
        Log.d(TAG, "doWork start")
        val imagePath = inputData.getString(KEY_AVATAR_WORKER_IMAGE_PATH)
        val imageUri = Uri.parse("file://$imagePath")
        val bitmap = MediaStore.Images.Media.getBitmap(applicationContext.contentResolver, imageUri)

        if (bitmap != null) {
            dataProvider.saveUserProfilePhoto(bitmap, "JPEG_" + UUID.randomUUID())
        } else {
            Log.e(TAG, "doWork failure")
            return Result.failure()
        }

        //Delete file from local storage after uploading to the server
        val imageFile = File(imagePath)
        imageFile.delete()

        Log.d(TAG, "doWork success")
        return Result.success()
    }
}

class AvatarWorkerFactory(private val dataProvider: DataProvider) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        Log.d(AvatarWorker.TAG, "createWorker start")
        return AvatarWorker(appContext, workerParameters, dataProvider)
    }
}