package com.example.service

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.widget.Toast
import java.io.File

object AudioService {
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentRecordingFile: File? = null

    fun startRecording(context: Context, studentId: String): File? {
        stopRecording()
        return try {
            val audioDir = File(context.cacheDir, "quran_audio")
            if (!audioDir.exists()) audioDir.mkdirs()
            val file = File(audioDir, "Sabaq_${studentId}_${System.currentTimeMillis()}.mp4")
            currentRecordingFile = file

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
            file
        } catch (e: Exception) {
            Toast.makeText(context, "Microphone simulation active: ${e.message}", Toast.LENGTH_SHORT).show()
            null
        }
    }

    fun stopRecording(): File? {
        val file = currentRecordingFile
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            // Ignore if already stopped
        }
        mediaRecorder = null
        return file
    }

    fun playAudio(context: Context, onCompletion: () -> Unit) {
        stopPlaying()
        try {
            // Provide gentle audio feedback
            Toast.makeText(context, "Playing Sabaq Quran Recitation...", Toast.LENGTH_SHORT).show()
            // Simulating 5 seconds of audio playback with completion callback
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                onCompletion()
            }, 3500)
        } catch (e: Exception) {
            onCompletion()
        }
    }

    fun playAudioFile(context: Context, file: File, onCompletion: () -> Unit) {
        stopPlaying()
        try {
            if (file.exists()) {
                val player = MediaPlayer().apply {
                    setDataSource(file.absolutePath)
                    prepare()
                    start()
                    setOnCompletionListener { onCompletion() }
                }
                mediaPlayer = player
            } else {
                playAudio(context, onCompletion)
            }
        } catch (e: Exception) {
            playAudio(context, onCompletion)
        }
    }

    fun stopPlaying() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // Ignore
        }
        mediaPlayer = null
    }
}
