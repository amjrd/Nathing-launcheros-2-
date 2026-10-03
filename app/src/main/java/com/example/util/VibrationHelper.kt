package com.example.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

object VibrationHelper {
  fun vibrateTouch(context: Context, isHeavy: Boolean = false) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
    if (!vibrator.hasVibrator()) return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      vibrator.vibrate(VibrationEffect.createOneShot(if (isHeavy) 45L else 20L, if (isHeavy) 180 else 80))
    } else {
      @Suppress("DEPRECATION")
      vibrator.vibrate(if (isHeavy) 45L else 20L)
    }
  }
}
