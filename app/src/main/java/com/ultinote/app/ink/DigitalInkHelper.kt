package com.ultinote.app.ink

import com.google.android.gms.tasks.Tasks
import com.google.mlkit.common.MlKitException
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognition
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognitionModel
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognitionModelIdentifier
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognizerOptions
import com.google.mlkit.vision.digitalink.recognition.Ink
import com.ultinote.app.data.model.DrawingStroke
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * On-device handwriting recognition. Language packs (~20MB each) download once
 * on demand; recognition itself is fully offline afterwards. Every failure
 * degrades to null so the UI can toast instead of crash. Original ink is never
 * touched — callers decide what to do with the text.
 */
object DigitalInkHelper {

    /** Null when the tag has no model (safe to treat as "unsupported on this device"). */
    fun modelFor(tag: String): DigitalInkRecognitionModel? {
        return try {
            val id: DigitalInkRecognitionModelIdentifier? =
                DigitalInkRecognitionModelIdentifier.fromLanguageTag(tag)
            if (id == null) null else DigitalInkRecognitionModel.builder(id).build()
        } catch (_: MlKitException) {
            null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun isDownloaded(tag: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val model = modelFor(tag) ?: return@withContext false
            Tasks.await(RemoteModelManager.getInstance().isModelDownloaded(model))
        } catch (_: Exception) {
            false
        }
    }

    /** Downloads the pack if needed. False = no network / Play Services issue; caller toasts. */
    suspend fun ensureDownloaded(tag: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val model = modelFor(tag) ?: return@withContext false
            val manager = RemoteModelManager.getInstance()
            if (Tasks.await(manager.isModelDownloaded(model))) return@withContext true
            Tasks.await(manager.download(model, DownloadConditions.Builder().build()))
            true
        } catch (_: Exception) {
            false
        }
    }

    /** Best-guess text for the given strokes, or null when anything goes wrong. */
    suspend fun recognize(strokes: List<DrawingStroke>, tag: String): String? =
        withContext(Dispatchers.IO) {
            try {
                val model = modelFor(tag) ?: return@withContext null
                val inkBuilder = Ink.builder()
                // Normalize timestamps to start at 0 — recognizers care about deltas, not epochs.
                val t0 = strokes.flatMap { it.points }.minOfOrNull { it.timestamp } ?: 0L
                for (stroke in strokes) {
                    if (stroke.points.size < 2) continue
                    val sb = Ink.Stroke.builder()
                    for (p in stroke.points) {
                        sb.addPoint(Ink.Point.create(p.x, p.y, p.timestamp - t0))
                    }
                    inkBuilder.addStroke(sb.build())
                }
                val ink = inkBuilder.build()
                val recognizer = DigitalInkRecognition.getClient(
                    DigitalInkRecognizerOptions.builder(model).build()
                )
                try {
                    val result = Tasks.await(recognizer.recognize(ink))
                    result.candidates.firstOrNull()?.text
                } finally {
                    try {
                        recognizer.close()
                    } catch (_: Exception) {
                    }
                }
            } catch (_: Exception) {
                null
            }
        }
}
