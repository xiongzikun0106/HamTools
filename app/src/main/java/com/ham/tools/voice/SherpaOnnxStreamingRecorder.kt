package com.ham.tools.voice

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.k2fsa.sherpa.onnx.OnlineRecognizer
import com.k2fsa.sherpa.onnx.OnlineRecognizerConfig
import com.k2fsa.sherpa.onnx.getEndpointConfig
import com.k2fsa.sherpa.onnx.getFeatureConfig
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Sherpa-ONNX 流式识别（Paraformer 中英双语），16kHz。
 */
class SherpaOnnxStreamingRecorder(
    modelsRoot: File
) {
    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private val modelConfig = SherpaOnnxPaths.absoluteParaformerConfig(modelsRoot)

    @SuppressLint("MissingPermission")
    fun recordUntilStopped(
        isRecording: AtomicBoolean,
        onPartial: (String) -> Unit
    ): String {
        val config = OnlineRecognizerConfig(
            featConfig = getFeatureConfig(sampleRate = sampleRate, featureDim = 80),
            modelConfig = modelConfig,
            endpointConfig = getEndpointConfig(),
            enableEndpoint = true
        )
        val recognizer = OnlineRecognizer(assetManager = null, config = config)
        val stream = recognizer.createStream()

        val minBytes = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            minBytes * 2
        )
        audioRecord.startRecording()

        val interval = 0.1
        val bufferSize = (interval * sampleRate).toInt()
        val buffer = ShortArray(bufferSize)

        val segments = mutableListOf<String>()

        try {
            while (isRecording.get()) {
                val ret = audioRecord.read(buffer, 0, buffer.size)
                if (ret > 0) {
                    val samples = FloatArray(ret) { buffer[it] / 32768.0f }
                    stream.acceptWaveform(samples, sampleRate)
                    while (recognizer.isReady(stream)) {
                        recognizer.decode(stream)
                    }
                    val isEndpoint = recognizer.isEndpoint(stream)
                    var text = recognizer.getResult(stream).text
                    if (isEndpoint && recognizer.config.modelConfig.paraformer.encoder.isNotBlank()) {
                        val tailPaddings = FloatArray((0.8 * sampleRate).toInt())
                        stream.acceptWaveform(tailPaddings, sampleRate)
                        while (recognizer.isReady(stream)) {
                            recognizer.decode(stream)
                        }
                        text = recognizer.getResult(stream).text
                    }
                    if (isEndpoint) {
                        recognizer.reset(stream)
                        if (text.isNotBlank()) {
                            segments.add(text)
                            onPartial(segments.joinToString("\n"))
                        }
                    }
                }
            }
            stream.inputFinished()
            while (recognizer.isReady(stream)) {
                recognizer.decode(stream)
            }
            val tail = recognizer.getResult(stream).text
            if (tail.isNotBlank()) {
                segments.add(tail)
            }
            return segments.joinToString("\n").trim()
        } finally {
            try {
                audioRecord.stop()
            } catch (_: Exception) {
            }
            audioRecord.release()
            stream.release()
            recognizer.release()
        }
    }
}
