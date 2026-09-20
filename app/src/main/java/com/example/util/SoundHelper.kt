package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream

object SoundHelper {
    private var mediaPlayer: MediaPlayer? = null

    val PRESETS = listOf(
        "preset_meow" to "Meow Kucing",
        "preset_chime" to "Chime Notifikasi",
        "preset_pop" to "Pop Balon",
        "preset_bell" to "Lonceng Kemenangan",
        "preset_none" to "Tanpa Suara"
    )

    fun getSoundDisplayName(soundUri: String?, soundName: String?): String {
        if (!soundName.isNullOrBlank()) return soundName
        if (soundUri.isNullOrBlank() || soundUri == "preset_meow") return "Meow Kucing"
        val matched = PRESETS.firstOrNull { it.first == soundUri }
        if (matched != null) return matched.second
        return "Audio Kustom"
    }

    fun playSound(context: Context, soundUri: String?) {
        try {
            stop()
            if (soundUri == "preset_none") return

            if (soundUri.isNullOrBlank() || soundUri == "preset_meow") {
                val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
                toneGen.startTone(ToneGenerator.TONE_PROP_PROMPT, 250)
                return
            }

            when (soundUri) {
                "preset_bell" -> {
                    val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 90)
                    toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 300)
                }
                "preset_pop" -> {
                    val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
                    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
                }
                "preset_chime" -> {
                    try {
                        val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                        val ringtone = RingtoneManager.getRingtone(context, notificationUri)
                        ringtone?.play()
                    } catch (e: Exception) {
                        val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
                        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 220)
                    }
                }
                else -> {
                    val uri = Uri.parse(soundUri)
                    val player = MediaPlayer().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                                .build()
                        )
                        setDataSource(context, uri)
                        prepare()
                        start()
                        setOnCompletionListener {
                            it.release()
                            if (mediaPlayer == it) {
                                mediaPlayer = null
                            }
                        }
                    }
                    mediaPlayer = player
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback tone on any audio failure
            try {
                val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
            } catch (_: Exception) {}
        }
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveAudioToInternalStorage(context: Context, sourceUri: Uri): Pair<String, String>? {
        return try {
            val contentResolver = context.contentResolver
            val soundsDir = File(context.filesDir, "mascot_sounds")
            if (!soundsDir.exists()) {
                soundsDir.mkdirs()
            }

            var displayName = "audio_kustom_${System.currentTimeMillis()}.mp3"
            contentResolver.query(sourceUri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        val name = cursor.getString(nameIndex)
                        if (!name.isNullOrBlank()) {
                            displayName = name
                        }
                    }
                }
            }

            val cleanFileName = displayName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
            val destFile = File(soundsDir, "sound_${System.currentTimeMillis()}_$cleanFileName")

            contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return null

            Pair(Uri.fromFile(destFile).toString(), displayName)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
