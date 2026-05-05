package com.ham.tools.voice

import com.k2fsa.sherpa.onnx.OnlineModelConfig
import com.k2fsa.sherpa.onnx.OnlineParaformerModelConfig
import java.io.File

internal object SherpaOnnxPaths {
    const val MODEL_SUBDIR = "sherpa-onnx-streaming-paraformer-bilingual-zh-en"
    const val MARKER_FILE = "tokens.txt"

    fun modelMarker(modelsRoot: File): File = File(File(modelsRoot, MODEL_SUBDIR), MARKER_FILE)

    fun isModelReady(modelsRoot: File): Boolean = modelMarker(modelsRoot).exists()

    fun absoluteParaformerConfig(modelsRoot: File): OnlineModelConfig {
        val dir = File(modelsRoot, MODEL_SUBDIR)
        return OnlineModelConfig(
            paraformer = OnlineParaformerModelConfig(
                encoder = File(dir, "encoder.int8.onnx").absolutePath,
                decoder = File(dir, "decoder.int8.onnx").absolutePath
            ),
            tokens = File(dir, "tokens.txt").absolutePath,
            modelType = "paraformer",
            numThreads = 2
        )
    }
}
