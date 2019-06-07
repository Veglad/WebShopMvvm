package com.sigma.vshcheglov.webshop.presentation.helpers

import android.content.Context
import androidx.work.*
import com.sigma.vshcheglov.webshop.presentation.main.helpers.AvatarWorker

object BackgroundHelper {
    fun saveUserAvatar(imagePath: String, context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val inputData = Data.Builder()
            .putString(AvatarWorker.KEY_AVATAR_WORKER_IMAGE_PATH, imagePath)
            .build()

        val avatarRequest = OneTimeWorkRequestBuilder<AvatarWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context)
            .enqueue(avatarRequest)
    }
}