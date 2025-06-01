package com.example.speakerrecoginitonfinal2.data.recorder

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WavAudioRecorder(private val context: Context, private val outputFile: File) {

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private val sampleRate = 44100
    private val channels = AudioFormat.CHANNEL_IN_MONO
    private val bitDepth = AudioFormat.ENCODING_PCM_16BIT

    fun startRecording() {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Permission to record audio is not granted", Toast.LENGTH_SHORT).show()
            return
        }

        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channels, bitDepth)
        audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channels, bitDepth, bufferSize)

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            Log.e("AudioRecord", "AudioRecord initialization failed.")
            return
        }

        audioRecord?.startRecording()
        isRecording = true
        Log.d("AudioRecord", "Recording started.")

        Thread {
            try {
                FileOutputStream(outputFile).use { output ->
                    val buffer = ByteArray(bufferSize)
                    while (isRecording) {
                        val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                        if (read > 0) {
                            output.write(buffer, 0, read)
                        } else if (read < 0) {
                            Log.e("AudioRecord", "Error reading audio data, read: $read")
                        }
                    }
                }
                Log.d("AudioRecord", "Recording to file: ${outputFile.absolutePath}, size: ${outputFile.length()} bytes")
                addWavHeader(outputFile) // WAV başlığı ekleniyor
                Log.d("AudioRecord", "Recording finished. WAV file saved.")
            } catch (e: Exception) {
                Log.e("AudioRecord", "Error in recording thread: ${e.message}", e)
            }
        }.start()

    }

    fun stopRecording(isCanceled: Boolean = false) {
        try {
            if (audioRecord != null && isRecording) {
                isRecording = false
                audioRecord?.stop()
                audioRecord?.release()
                audioRecord = null
                Log.d("AudioRecord", "AudioRecord stopped.")
            }

            if (!isCanceled) {
                Toast.makeText(context, "Recording stopped", Toast.LENGTH_SHORT).show()
            } else {
                outputFile.delete()
                Toast.makeText(context, "Recording canceled and file deleted", Toast.LENGTH_SHORT).show()
            }
        } catch (e: RuntimeException) {
            e.printStackTrace()
            Toast.makeText(context, "Error stopping the recording: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Reset recording
    fun resetRecording() {
        stopRecording(isCanceled = true)
    }

    fun getAudioFile(): File { // Yeni public metod
        return outputFile
    }

    private fun addWavHeader(file: File) {
        val pcmData = file.readBytes()
        val wavHeader = createWavHeader(pcmData.size, sampleRate, 1, 16)

        FileOutputStream(file).use { output ->
            output.write(wavHeader)
            output.write(pcmData)
        }
    }

    private fun createWavHeader(dataSize: Int, sampleRate: Int, channels: Int, bitDepth: Int): ByteArray {
        val totalDataLen = 36 + dataSize
        val byteRate = sampleRate * channels * bitDepth / 8
        val blockAlign = channels * bitDepth / 8

        return ByteBuffer.allocate(44).apply {
            order(ByteOrder.LITTLE_ENDIAN)
            put("RIFF".toByteArray()) // Chunk ID
            putInt(totalDataLen) // Chunk Size
            put("WAVE".toByteArray()) // Format
            put("fmt ".toByteArray()) // Subchunk1 ID
            putInt(16) // Subchunk1 Size (PCM)
            putShort(1) // Audio Format (PCM = 1)
            putShort(channels.toShort()) // Number of Channels
            putInt(sampleRate) // Sample Rate
            putInt(byteRate) // Byte Rate
            putShort(blockAlign.toShort()) // Block Align
            putShort(bitDepth.toShort()) // Bits Per Sample
            put("data".toByteArray()) // Subchunk2 ID
            putInt(dataSize) // Subchunk2 Size
        }.array()
    }
} 