package com.borg.budget.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.borg.budget.data.models.Receipt
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ReceiptScanner(context: Context) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val parser = ReceiptParser(context)

    suspend fun scanImage(bitmap: Bitmap): Receipt? = suspendCancellableCoroutine { continuation ->
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            recognizer.process(image)
                .addOnSuccessListener { result ->
                    Log.d("ReceiptScanner", "Raw Text: ${result.text}")
                    if (result.text.isBlank()) {
                        continuation.resume(null)
                    } else {
                        continuation.resume(parser.parse(result.text))
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ReceiptScanner", "Error scanning receipt", e)
                    continuation.resumeWithException(e)
                }
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }
}
