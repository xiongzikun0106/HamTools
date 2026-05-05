package com.ham.tools.voice

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 从 GitHub asr-models 发布页下载并解压 Sherpa-ONNX 中英双语 Paraformer 流式模型。
 */
@Singleton
class SherpaModelDownloader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient
) {
    private val downloadClient: OkHttpClient = okHttpClient.newBuilder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .writeTimeout(300, TimeUnit.SECONDS)
        .build()

    private val modelsRoot: File
        get() = File(context.filesDir, "sherpa_models").apply { mkdirs() }

    /** Sherpa-ONNX Paraformer 模型根目录（[isReady] / 识别器共用路径）。 */
    fun modelsRootDirectory(): File = modelsRoot

    fun isReady(): Boolean = SherpaOnnxPaths.isModelReady(modelsRoot)

    suspend fun downloadIfNeeded(
        onProgress: (downloaded: Long, total: Long?) -> Unit = { _, _ -> }
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (isReady()) return@withContext Result.success(Unit)
        runCatching {
            val url =
                "https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/" +
                    "sherpa-onnx-streaming-paraformer-bilingual-zh-en.tar.bz2"
            val tmp = File.createTempFile("sherpa-asr-", ".tar.bz2", context.cacheDir)
            try {
                val req = Request.Builder().url(url).build()
                downloadClient.newCall(req).execute().use { resp ->
                    require(resp.isSuccessful) { "HTTP ${resp.code}" }
                    val body = resp.body ?: error("empty body")
                    val total = body.contentLength().takeIf { it > 0 }
                    body.byteStream().use { input ->
                        tmp.outputStream().use { out ->
                            val buf = ByteArray(8192)
                            var read: Int
                            var done = 0L
                            while (input.read(buf).also { read = it } != -1) {
                                out.write(buf, 0, read)
                                done += read
                                onProgress(done, total)
                            }
                        }
                    }
                }
                extractTarBz2(tmp, modelsRoot)
            } finally {
                tmp.delete()
            }
            require(isReady()) { "model marker missing after extract" }
        }
    }

    private fun extractTarBz2(archive: File, dest: File) {
        dest.mkdirs()
        FileInputStream(archive).use { fis ->
            BufferedInputStream(fis).use { bis ->
                BZip2CompressorInputStream(bis).use { bz ->
                    TarArchiveInputStream(bz).use { tis ->
                        var entry = tis.nextEntry
                        while (entry != null) {
                            val tarEntry = entry as? TarArchiveEntry
                                ?: error("unexpected tar entry type")
                            val name = tarEntry.name.trimStart('/')
                            require(!name.contains("..")) { "bad path" }
                            if (tarEntry.isDirectory) {
                                File(dest, name).mkdirs()
                            } else {
                                val outFile = File(dest, name)
                                outFile.parentFile?.mkdirs()
                                val size = tarEntry.size
                                require(size >= 0) { "tar entry without size: $name" }
                                var remaining: Long = size
                                outFile.outputStream().use { os ->
                                    val buf = ByteArray(8192)
                                    while (remaining > 0) {
                                        val chunk = minOf(buf.size.toLong(), remaining).toInt()
                                        val n = tis.read(buf, 0, chunk)
                                        if (n <= 0) break
                                        os.write(buf, 0, n)
                                        remaining -= n
                                    }
                                }
                            }
                            entry = tis.nextEntry
                        }
                    }
                }
            }
        }
    }
}
