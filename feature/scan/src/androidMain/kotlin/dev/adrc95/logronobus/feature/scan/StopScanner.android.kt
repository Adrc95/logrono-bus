/*
 * Copyright 2026 adrc95 (Adrián Cabrera)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.adrc95.logronobus.feature.scan

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Rect
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dev.adrc95.logronobus.core.common.InputAction
import dev.adrc95.logronobus.core.common.platform.logDebug
import dev.adrc95.logronobus.core.device.AppPermissions
import dev.adrc95.logronobus.core.model.ScannedText
import dev.adrc95.logronobus.feature.scan.ScanConstants
import org.koin.compose.koinInject
import java.util.concurrent.Executors
import kotlin.math.abs

actual val isStopScanSupported: Boolean = true

@Composable
actual fun CameraTextScanner(
    modifier: Modifier,
    onPieces: InputAction<List<ScannedText>>,
) {
    val context = LocalContext.current
    val permissions = koinInject<AppPermissions>()
    var hasPermission by remember { mutableStateOf(hasCameraPermission(context)) }
    var denied by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            val granted = permissions.requestCamera()
            hasPermission = granted
            denied = !granted
        }
    }

    when {
        hasPermission -> {
            CameraPreviewWithAnalysis(modifier, onPieces)
        }

        denied -> {
            ScanMessage(
                title = "Camera permission needed",
                body = "Allow camera access to scan a stop's sign.",
                modifier = modifier,
            )
        }

        else -> {
            Box(modifier)
        }
    }
}

private fun hasCameraPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA,
    ) == PackageManager.PERMISSION_GRANTED

@Composable
private fun CameraPreviewWithAnalysis(
    modifier: Modifier,
    onPieces: InputAction<List<ScannedText>>,
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val currentOnPieces by rememberUpdatedState(onPieces)
    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            analysisExecutor.shutdown()
            recognizer.close()
        }
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val providerFuture = ProcessCameraProvider.getInstance(ctx)
            providerFuture.addListener({
                val provider = providerFuture.get()
                val preview =
                    Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                val analysis =
                    ImageAnalysis
                        .Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(
                                analysisExecutor,
                                TextAnalyzer(recognizer) { currentOnPieces(it) },
                            )
                        }
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis,
                )
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = modifier.fillMaxSize(),
    )
}

private class TextAnalyzer(
    private val recognizer: TextRecognizer,
    private val onPieces: InputAction<List<ScannedText>>,
) : ImageAnalysis.Analyzer {
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val bitmap = imageProxy.toBitmap().rotated(imageProxy.imageInfo.rotationDegrees)
        recognizer
            .process(image)
            .addOnSuccessListener { result ->
                val pieces =
                    result.textBlocks
                        .flatMap { block -> block.lines }
                        .flatMap { line -> line.elements }
                        .mapNotNull { element ->
                            val box = element.boundingBox ?: return@mapNotNull null
                            ScannedText(
                                text = element.text,
                                left = box.left.toFloat(),
                                top = box.top.toFloat(),
                                width = box.width().toFloat(),
                                height = box.height().toFloat(),
                                colour = bitmap.inkColour(box),
                                dot = bitmap.dotColour(box),
                            )
                        }
                if (pieces.isNotEmpty()) {
                    logDebug(
                        "scan piezas=" +
                            pieces.joinToString { piece ->
                                "${piece.text}@${piece.left.toInt()},${piece.top.toInt()}," +
                                    "${piece.width.toInt()}x${piece.height.toInt()}" +
                                    "c${piece.colour}p${piece.dot}"
                            },
                    )
                    onPieces(pieces)
                }
            }.addOnCompleteListener { imageProxy.close() }
    }
}

private fun Bitmap.rotated(degrees: Int): Bitmap {
    if (degrees == 0) return this
    val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

private fun Bitmap.inkColour(box: Rect): Int {
    val insetX = (box.width() * ScanConstants.INSET).toInt()
    val insetY = (box.height() * ScanConstants.INSET_Y).toInt()
    val left = (box.left + insetX).coerceIn(0, width - 1)
    val top = (box.top + insetY).coerceIn(0, height - 1)
    val right = (box.right - insetX).coerceIn(left + 1, width)
    val bottom = (box.bottom - insetY).coerceIn(top + 1, height)
    val counts = HashMap<Int, Int>()
    val pixels = ArrayList<Int>()
    for (y in top until bottom) {
        for (x in left until right) {
            val pixel = getPixel(x, y)
            pixels += pixel
            val key = (Color.red(pixel) shr 4 shl 8) or (Color.green(pixel) shr 4 shl 4) or (Color.blue(pixel) shr 4)
            counts[key] = (counts[key] ?: 0) + 1
        }
    }
    val background = counts.maxByOrNull { it.value }?.key ?: return 0
    val backgroundRed = (background shr 8 and 0xF) * 17
    val backgroundGreen = (background shr 4 and 0xF) * 17
    val backgroundBlue = (background and 0xF) * 17
    val ink = ArrayList<Int>()
    pixels.forEach { pixel ->
        val red = Color.red(pixel)
        val green = Color.green(pixel)
        val blue = Color.blue(pixel)
        val distance =
            abs(red - backgroundRed) +
                abs(green - backgroundGreen) +
                abs(blue - backgroundBlue)
        if (distance > ScanConstants.INK_DISTANCE) ink += maxOf(red, green, blue) - minOf(red, green, blue)
    }
    if (ink.size < ScanConstants.MIN_INK) return 0
    ink.sort()
    return ink[ink.size * ScanConstants.COLOUR_PERCENTILE / 100]
}

private fun Bitmap.dotColour(box: Rect): Int {
    val size = (box.height() * ScanConstants.DOT_SIZE).toInt()
    if (size < 4) return 0
    val centre = (box.left - box.height() * ScanConstants.DOT_GAP).toInt()
    val left = (centre - size / 2).coerceAtLeast(0)
    val top = (box.top + box.height() / 2 - size / 2).coerceAtLeast(0)
    val dot = Rect(left, top, (left + size).coerceAtMost(width), (top + size).coerceAtMost(height))
    if (dot.width() < 4 || dot.height() < 4) return 0
    return inkColour(dot)
}

private fun saturation(
    red: Int,
    green: Int,
    blue: Int,
): Int {
    val brightest = maxOf(red, green, blue)
    val darkest = minOf(red, green, blue)
    return if (brightest == 0) 0 else (brightest - darkest) * 100 / brightest
}

private fun hueBucket(
    red: Int,
    green: Int,
    blue: Int,
): Int {
    val brightest = maxOf(red, green, blue)
    val darkest = minOf(red, green, blue)
    val delta = (brightest - darkest).toFloat()
    if (delta == 0f) return 0
    val hue =
        when (brightest) {
            red -> 60f * (((green - blue) / delta) % 6f)
            green -> 60f * ((blue - red) / delta + 2f)
            else -> 60f * ((red - green) / delta + 4f)
        }
    val degrees = ((hue % 360f) + 360f) % 360f
    return (degrees / (360f / ScanConstants.HUE_BUCKETS)).toInt().coerceIn(0, ScanConstants.HUE_BUCKETS - 1)
}

@Composable
private fun ScanMessage(
    modifier: Modifier,
    title: String,
    body: String,
) {
    Column(
        modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
