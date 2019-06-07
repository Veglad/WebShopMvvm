package com.sigma.vshcheglov.webshop

import android.animation.AnimatorSet
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sigma.vshcheglov.webshop.data.DataProvider
import com.sigma.vshcheglov.webshop.presentation.helpers.Router
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.*
import javax.inject.Inject
import android.animation.ObjectAnimator
import android.os.Build
import android.view.animation.DecelerateInterpolator
import androidx.annotation.RequiresApi


class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var dataProvider: DataProvider

    private val job: Job = Job()
    private val uiCoroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main + job)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        App.appComponent.inject(this)

        uiCoroutineScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                animateTextView()
            }

            delay(1000)

            resolveStartScreen()
        }
    }

    private suspend fun resolveStartScreen() {
        val isStartLogin = try {
            val user = withContext(Dispatchers.IO) { dataProvider.getCurrentUser() }
            user.email == null
        } catch (ex: Exception) {
            true
        }

        if (isStartLogin) {
            Router.showLogin(this@SplashActivity)
        } else {
            Router.showMain(this@SplashActivity)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun animateTextView() {
        val animationX = ObjectAnimator.ofFloat(splashTextView, "scaleX", 0.4F, 1F)
        val animationY = ObjectAnimator.ofFloat(splashTextView, "scaleY", 0.4F, 1F)
        val set = AnimatorSet()
        set.play(animationX)
            .with(animationY)
        set.duration = 1000
        set.interpolator = DecelerateInterpolator()
        set.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
