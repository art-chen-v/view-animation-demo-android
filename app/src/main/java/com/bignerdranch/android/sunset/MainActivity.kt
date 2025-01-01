package com.bignerdranch.android.sunset

import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.addListener
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.doOnLayout
import com.bignerdranch.android.sunset.databinding.ActivityMainBinding

private const val TAG = "MainActivity"

enum class SunMovement {
    SUNSET, SUNRISE
}

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var sunMovement = SunMovement.SUNSET

    private var sunToSeaHalfwayPoint: Int? = null

    private val sunMovementDuration: Long = 3000

    private var animatedCompletionFraction: Float = 0f

    private val brightSunColor: Int by lazy {
        ContextCompat.getColor(this, R.color.bright_sun)
    }
    private val blueSkyColor: Int by lazy {
        ContextCompat.getColor(this, R.color.blue_sky)
    }
    private val sunsetSkyColor: Int by lazy {
        ContextCompat.getColor(this, R.color.sunset_sky)
    }
    private val nightSkyColor: Int by lazy {
        ContextCompat.getColor(this, R.color.night_sky)
    }
    private val sunHeatColor: Int by lazy {
        ContextCompat.getColor(this, R.color.sun_heat)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startPulsating()

        binding.scene.doOnLayout {
            sunToSeaHalfwayPoint = (binding.sun.bottom +
                    (binding.sky.bottom - binding.sun.bottom) / 2)
        }

        binding.scene.setOnClickListener {
            sunToSeaHalfwayPoint?.let {
                when (sunMovement) {
                    SunMovement.SUNSET -> {
                        if (binding.sun.translationY == 0f) {
                            sunMovement = SunMovement.SUNRISE
                            startSunset()
                        } else if (binding.sun.y + binding.sun.height < it) {
                            sunMovement = SunMovement.SUNRISE
                            reverseSunrise()
                        }
                    }
                    SunMovement.SUNRISE -> {
                        if (binding.sun.y == binding.sky.bottom.toFloat()) {
                            sunMovement = SunMovement.SUNSET
                            startSunrise()
                        } else if (binding.sun.y > it) {
                            sunMovement = SunMovement.SUNSET
                            reverseSunset()
                        }
                    }
                }
            }
        }
    }

    private fun reverseSunset() {
        val sunYStart = binding.sun.y
        val sunYEnd = binding.sun.top.toFloat()

        val reverseAnimator = ObjectAnimator
            .ofFloat(binding.sun, "y", sunYStart, sunYEnd)
            .setDuration((sunMovementDuration * animatedCompletionFraction).toLong())

        reverseAnimator.addUpdateListener {
            animatedCompletionFraction = it.animatedFraction
        }

        reverseAnimator.start()
    }

    private fun reverseSunrise() {
        val sunYStart = binding.sun.y
        val sunYEnd = binding.sky.height.toFloat()

        val reverseAnimator = ObjectAnimator
            .ofFloat(binding.sun, "y", sunYStart, sunYEnd)
            .setDuration((sunMovementDuration * animatedCompletionFraction).toLong())

        reverseAnimator.addUpdateListener {
            animatedCompletionFraction = it.animatedFraction
        }

        reverseAnimator.start()
    }

    private fun startPulsating() {
        val pulsateAnimator = ObjectAnimator
            .ofArgb(brightSunColor, sunHeatColor)
            .setDuration(500)

        pulsateAnimator.addUpdateListener {
            binding.sun.drawable.colorFilter =
                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    it.animatedValue as Int,
                    BlendModeCompat.SRC_ATOP
                )
        }

        pulsateAnimator.repeatCount = ValueAnimator.INFINITE
        pulsateAnimator.start()
    }

    private fun startSunset() {
        val sunYStart = binding.sun.top.toFloat()
        val sunYEnd = binding.sky.height.toFloat()

        val heightAnimator = ObjectAnimator
            .ofFloat(binding.sun, "y", sunYStart, sunYEnd)
            .setDuration(sunMovementDuration)
        heightAnimator.interpolator = AccelerateInterpolator()

        heightAnimator.addUpdateListener {
            animatedCompletionFraction = it.animatedFraction
        }

        val sunsetSkyAnimator = ObjectAnimator
            .ofInt(binding.sky, "backgroundColor", blueSkyColor, sunsetSkyColor)
            .setDuration(sunMovementDuration)
        sunsetSkyAnimator.setEvaluator(ArgbEvaluator())

        val nightSkyAnimator = ObjectAnimator
            .ofInt(binding.sky, "backgroundColor", sunsetSkyColor, nightSkyColor)
            .setDuration(1500)
        nightSkyAnimator.setEvaluator(ArgbEvaluator())

        val reflectionAnimator = ObjectAnimator
            .ofFloat(binding.sunReflection, "alpha", 1f, 0f)
            .setDuration(sunMovementDuration)

        val animatorSet = AnimatorSet()

        animatorSet.play(heightAnimator)
            .with(sunsetSkyAnimator)
            .with(reflectionAnimator)
            .before(nightSkyAnimator)

        animatorSet.start()
    }

    private fun startSunrise() {
        val sunYStart = binding.sun.top.toFloat()
        val sunYEnd = binding.sky.height.toFloat()

        val heightAnimator = ObjectAnimator
            .ofFloat(binding.sun, "y", sunYEnd, sunYStart)
            .setDuration(sunMovementDuration)
        heightAnimator.interpolator = AccelerateInterpolator()

        heightAnimator.addUpdateListener {
            animatedCompletionFraction = it.animatedFraction
        }

        val sunriseSkyAnimator = ObjectAnimator
            .ofInt(binding.sky, "backgroundColor", sunsetSkyColor, blueSkyColor)
            .setDuration(sunMovementDuration)

        sunriseSkyAnimator.setEvaluator(ArgbEvaluator())

        val reflectionAnimator = ObjectAnimator
            .ofFloat(binding.sunReflection, "alpha", 0f, 1f)
            .setDuration(sunMovementDuration)

        val sunsetSkyAnimator = ObjectAnimator
            .ofInt(binding.sky, "backgroundColor", nightSkyColor, sunsetSkyColor)
            .setDuration(1500)
        sunsetSkyAnimator.setEvaluator(ArgbEvaluator())

        val animatorSet = AnimatorSet()

        animatorSet.play(heightAnimator)
            .with(sunriseSkyAnimator)
            .with(reflectionAnimator)
            .after(sunsetSkyAnimator)
        animatorSet.start()
    }
}