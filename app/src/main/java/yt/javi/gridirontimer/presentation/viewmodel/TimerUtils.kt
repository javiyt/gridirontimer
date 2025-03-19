package yt.javi.gridirontimer.presentation.viewmodel

import android.content.Context
import android.content.Context.VIBRATOR_MANAGER_SERVICE
import android.content.Context.VIBRATOR_SERVICE
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.media.RingtoneManager.TYPE_NOTIFICATION
import android.os.Build
import android.os.VibrationEffect
import android.os.VibrationEffect.DEFAULT_AMPLITUDE
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.text.intl.Locale
import yt.javi.gridirontimer.presentation.viewmodel.TimerUtils.VibrationTimes.TWO

object TimerUtils {

    fun formatTime(time: Long): String {
        val minutes = (time / 1000) / 60
        val seconds = (time / 1000) % 60
        return String.format(Locale.current.platformLocale, "%02d:%02d", minutes, seconds)
    }

    fun formatSeconds(time: Long): String {
        val seconds = (time / 1000) % 60
        return String.format(Locale.current.platformLocale, "%02d", seconds)
    }

    fun vibrate(context: Context) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        vibrator.vibrate(VibrationEffect.createOneShot(1500, DEFAULT_AMPLITUDE))
    }

    fun playSound(context: Context) {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val ringtone = RingtoneManager.getRingtone(
            context,
            RingtoneManager.getDefaultUri(TYPE_NOTIFICATION)
        )
        ringtone.audioAttributes = audioAttributes

        if (audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION) != 0) {
            ringtone.play()
        }
    }

    enum class VibrationTimes {
        ONE,
        TWO
    }
}