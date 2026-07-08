package com.example.ui

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.*

object SirenPlayer {
    private var audioTrack: AudioTrack? = null
    private var playJob: Job? = null

    fun startSiren() {
        if (playJob != null) return
        playJob = CoroutineScope(Dispatchers.IO).launch {
            val sampleRate = 44100
            val numSamples = 22050 // 0.5s chunks at 44100Hz
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            
            try {
                val track = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize.coerceAtLeast(numSamples * 2),
                    AudioTrack.MODE_STREAM
                )
                audioTrack = track
                track.play()

                val samples = ShortArray(numSamples)
                var angle = 0.0
                var cycleCount = 0

                while (isActive) {
                    // Alternating siren sweep frequency between 600Hz and 1100Hz
                    val freq = if (cycleCount % 2 == 0) 650.0 else 950.0
                    cycleCount++
                    
                    for (i in 0 until numSamples) {
                        samples[i] = (Math.sin(angle) * Short.MAX_VALUE * 0.8).toInt().toShort() // 80% volume
                        angle += 2.0 * Math.PI * freq / sampleRate
                        if (angle > 2.0 * Math.PI) {
                            angle -= 2.0 * Math.PI
                        }
                    }
                    track.write(samples, 0, numSamples)
                    delay(500)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stopSiren() {
        playJob?.cancel()
        playJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            // ignore
        }
        audioTrack = null
    }
}
