package com.sahayak.android.ui.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Base64
import java.io.File

/**
 * Manages audio recording via [MediaRecorder], live amplitude sampling
 * for waveform visualisation, and playback of both local files and
 * base64-encoded audio responses via [MediaPlayer].
 */
class AudioRecorderManager(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var outputFile: File? = null

    val isRecording: Boolean get() = recorder != null
    val isPlaying: Boolean get() = player?.isPlaying == true

    /** The last recorded file, kept so the user can preview before sending. */
    var lastRecordedFile: File? = null
        private set

    // ── Amplitude sampling ───────────────────

    private val handler = Handler(Looper.getMainLooper())
    private var amplitudeCallback: ((Float) -> Unit)? = null
    private val amplitudeSampler = object : Runnable {
        override fun run() {
            recorder?.let { rec ->
                // maxAmplitude returns 0..32767; normalise to 0f..1f
                val amp = rec.maxAmplitude / 32767f
                amplitudeCallback?.invoke(amp)
            }
            handler.postDelayed(this, AMPLITUDE_SAMPLE_MS)
        }
    }

    /**
     * Register a callback that receives normalised amplitude values
     * (0 f–1 f) at ~60 ms intervals while recording.
     */
    fun onAmplitude(callback: (Float) -> Unit) {
        amplitudeCallback = callback
    }

    // ── Recording ────────────────────────────

    fun startRecording(): File {
        stopRecording()                               // safety: release any lingering recorder
        val file = File(context.cacheDir, "sahayak_voice_${System.currentTimeMillis()}.m4a")
        outputFile = file
        lastRecordedFile = null                       // clear previous

        recorder = createRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128_000)
            setAudioSamplingRate(44_100)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        // Start sampling amplitude for waveform
        handler.post(amplitudeSampler)
        return file
    }

    /**
     * Stops the current recording and returns the audio [File], or null
     * if nothing was being recorded.  The file is also kept in
     * [lastRecordedFile] for preview playback.
     */
    fun stopRecording(): File? {
        handler.removeCallbacks(amplitudeSampler)
        return try {
            recorder?.apply { stop(); release() }
            recorder = null
            lastRecordedFile = outputFile
            outputFile
        } catch (e: Exception) {
            recorder?.release()
            recorder = null
            null
        }
    }

    // ── Playback — local file (preview) ──────

    /**
     * Play a local audio file (e.g. the just-recorded preview).
     */
    fun playFile(file: File, onComplete: () -> Unit = {}) {
        stopPlayback()
        player = MediaPlayer().apply {
            setDataSource(file.absolutePath)
            setOnCompletionListener {
                it.release()
                player = null
                onComplete()
            }
            prepare()
            start()
        }
    }

    // ── Playback — base64 audio from backend TTS ──

    fun playBase64Audio(base64Audio: String, onComplete: () -> Unit = {}) {
        stopPlayback()

        val bytes = Base64.decode(base64Audio, Base64.DEFAULT)
        val tmpFile = File(context.cacheDir, "sahayak_tts_${System.currentTimeMillis()}.mp3")
        tmpFile.writeBytes(bytes)

        player = MediaPlayer().apply {
            setDataSource(tmpFile.absolutePath)
            setOnCompletionListener {
                it.release()
                player = null
                tmpFile.delete()
                onComplete()
            }
            prepare()
            start()
        }
    }

    fun stopPlayback() {
        player?.apply {
            if (isPlaying) stop()
            release()
        }
        player = null
    }

    /** Delete the cached preview file. */
    fun discardRecording() {
        lastRecordedFile?.delete()
        lastRecordedFile = null
    }

    // ── Cleanup ──────────────────────────────

    fun release() {
        stopRecording()
        stopPlayback()
        amplitudeCallback = null
    }

    // ── Helpers ──────────────────────────────

    @Suppress("DEPRECATION")
    private fun createRecorder(): MediaRecorder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            MediaRecorder(context)
        else
            MediaRecorder()

    companion object {
        private const val AMPLITUDE_SAMPLE_MS = 60L
    }
}
