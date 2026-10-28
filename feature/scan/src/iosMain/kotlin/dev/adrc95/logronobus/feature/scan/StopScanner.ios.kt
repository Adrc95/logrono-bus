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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import dev.adrc95.logronobus.core.common.InputAction
import dev.adrc95.logronobus.core.common.platform.logDebug
import dev.adrc95.logronobus.core.device.AppPermissions
import dev.adrc95.logronobus.core.model.ScannedText
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import org.koin.compose.koinInject
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetHigh
import platform.AVFoundation.AVCaptureVideoDataOutput
import platform.AVFoundation.AVCaptureVideoDataOutputSampleBufferDelegateProtocol
import platform.AVFoundation.AVCaptureVideoOrientationPortrait
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectZero
import platform.CoreMedia.CMSampleBufferGetImageBuffer
import platform.CoreMedia.CMSampleBufferRef
import platform.CoreVideo.CVPixelBufferGetHeight
import platform.CoreVideo.CVPixelBufferGetPixelFormatType
import platform.CoreVideo.CVPixelBufferGetWidth
import platform.CoreVideo.CVPixelBufferLockBaseAddress
import platform.CoreVideo.CVPixelBufferRef
import platform.CoreVideo.CVPixelBufferUnlockBaseAddress
import platform.CoreVideo.kCVPixelBufferLock_ReadOnly
import platform.CoreVideo.kCVPixelFormatType_32BGRA
import platform.Foundation.NSError
import platform.ImageIO.kCGImagePropertyOrientationRight
import platform.UIKit.UIView
import platform.Vision.VNImageRequestHandler
import platform.Vision.VNRecognizeTextRequest
import platform.Vision.VNRecognizedText
import platform.Vision.VNRecognizedTextObservation
import platform.Vision.VNRequest
import platform.Vision.VNRequestTextRecognitionLevelAccurate
import platform.darwin.NSObject
import platform.darwin.NSUInteger
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_queue_create

actual val isStopScanSupported: Boolean = true

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CameraTextScanner(
    modifier: Modifier,
    onPieces: InputAction<List<ScannedText>>,
) {
    var authorized by remember { mutableStateOf(cameraAuthorized()) }
    var denied by remember { mutableStateOf(cameraDenied()) }
    val permissions = koinInject<AppPermissions>()

    LaunchedEffect(Unit) {
        if (!authorized && !denied) {
            val granted = permissions.requestCamera()
            authorized = granted
            denied = !granted
        }
    }

    when {
        authorized -> {
            CameraPreview(modifier, onPieces)
        }

        denied -> {
            ScanMessage(
                title = "Camera permission needed",
                body = "Allow camera access to scan a stop's number.",
                modifier = modifier,
            )
        }

        else -> {
            Box(modifier)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
private fun CameraPreview(
    modifier: Modifier,
    onPieces: InputAction<List<ScannedText>>,
) {
    val currentOnPieces by rememberUpdatedState(onPieces)
    val controller = remember { ScannerController { currentOnPieces(it) } }

    DisposableEffect(Unit) {
        onDispose { controller.stop() }
    }

    UIKitView(
        factory = { controller.createView() },
        modifier = modifier.fillMaxSize(),
    )
}

private fun cameraAuthorized(): Boolean =
    AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo) == AVAuthorizationStatusAuthorized

private fun cameraDenied(): Boolean {
    val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
    return status == AVAuthorizationStatusDenied || status == AVAuthorizationStatusRestricted
}

@OptIn(ExperimentalForeignApi::class)
private class ScannerUIView : UIView(frame = CGRectZero.readValue()) {
    var previewLayer: AVCaptureVideoPreviewLayer? = null

    override fun layoutSubviews() {
        super.layoutSubviews()
        previewLayer?.setFrame(bounds)
    }
}

@OptIn(ExperimentalForeignApi::class)
private class ScannerController(
    onPieces: InputAction<List<ScannedText>>,
) {
    private val session = AVCaptureSession()
    private val queue = dispatch_queue_create("dev.adrc95.logronobus.feature.scanner", null)
    private val delegate = SampleDelegate(onPieces)
    private var configured = false

    fun createView(): UIView {
        configureIfNeeded()
        val view = ScannerUIView()
        val layer =
            AVCaptureVideoPreviewLayer(session = session).apply {
                videoGravity = AVLayerVideoGravityResizeAspectFill
                @Suppress("DEPRECATION")
                connection?.let { conn ->
                    if (conn.isVideoOrientationSupported()) {
                        conn.videoOrientation =
                            AVCaptureVideoOrientationPortrait
                    }
                }
            }
        view.previewLayer = layer
        view.layer.addSublayer(layer)
        dispatch_async(queue) { session.startRunning() }
        return view
    }

    private fun configureIfNeeded() {
        if (configured) return
        configured = true
        session.beginConfiguration()
        session.sessionPreset = AVCaptureSessionPresetHigh

        val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
        if (device != null) {
            val input = AVCaptureDeviceInput.deviceInputWithDevice(device, null)
            if (input != null && session.canAddInput(input)) session.addInput(input)
        }

        val output =
            AVCaptureVideoDataOutput().apply {
                alwaysDiscardsLateVideoFrames = true
                videoSettings =
                    mapOf<Any?, Any?>(
                        ScanConstants.PIXEL_FORMAT_KEY to kCVPixelFormatType_32BGRA,
                    )
            }
        output.setSampleBufferDelegate(delegate, queue)
        if (session.canAddOutput(output)) session.addOutput(output)

        session.commitConfiguration()
    }

    fun stop() {
        dispatch_async(queue) { session.stopRunning() }
    }
}

@OptIn(ExperimentalForeignApi::class)
private class SampleDelegate(
    private val onPieces: InputAction<List<ScannedText>>,
) : NSObject(),
    AVCaptureVideoDataOutputSampleBufferDelegateProtocol {
    private var buffer: CVPixelBufferRef? = null
    private var withoutText = 0
    private var formatLogged = false

    private val request =
        VNRecognizeTextRequest(completionHandler = { req, error -> read(req, error) }).apply {
            recognitionLevel = VNRequestTextRecognitionLevelAccurate
            usesLanguageCorrection = false
        }

    private fun read(
        request: VNRequest?,
        error: NSError?,
    ) {
        val frame = buffer ?: return
        if (!formatLogged) {
            formatLogged = true
            logDebug(
                "scan vision formato=${formatOf(frame)} " +
                    "${CVPixelBufferGetWidth(frame)}x${CVPixelBufferGetHeight(frame)}",
            )
        }
        if (error != null) {
            logDebug("scan vision error=${error.localizedDescription}")
            return
        }
        val pieces =
            request
                ?.results
                ?.flatMap { observation ->
                    (observation as? VNRecognizedTextObservation)?.pieces(frame).orEmpty()
                }.orEmpty()
        if (pieces.isEmpty()) {
            withoutText++
            if (withoutText % ScanConstants.LOG_EVERY_FRAMES == 0) {
                logDebug("scan vision sin texto en $withoutText fotogramas")
            }
            return
        }
        withoutText = 0
        logDebug(
            "scan piezas=" +
                pieces.joinToString { piece ->
                    "${piece.text}@${piece.left.toInt()},${piece.top.toInt()}," +
                        "${piece.width.toInt()}x${piece.height.toInt()}" +
                        "c${piece.colour}p${piece.dot}"
                },
        )
        dispatch_async(dispatch_get_main_queue()) { onPieces(pieces) }
    }

    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputSampleBuffer: CMSampleBufferRef?,
        fromConnection: AVCaptureConnection,
    ) {
        val frame = CMSampleBufferGetImageBuffer(didOutputSampleBuffer) ?: return
        buffer = frame
        CVPixelBufferLockBaseAddress(frame, kCVPixelBufferLock_ReadOnly)
        try {
            val handler =
                VNImageRequestHandler(
                    cVPixelBuffer = frame,
                    orientation = kCGImagePropertyOrientationRight,
                    options = emptyMap<Any?, Any?>(),
                )
            handler.performRequests(listOf(request), null)
        } finally {
            CVPixelBufferUnlockBaseAddress(frame, kCVPixelBufferLock_ReadOnly)
            buffer = null
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun formatOf(buffer: CVPixelBufferRef): String {
    val type = CVPixelBufferGetPixelFormatType(buffer)
    return byteArrayOf(
        (type shr 24).toByte(),
        (type shr 16).toByte(),
        (type shr 8).toByte(),
        type.toByte(),
    ).decodeToString()
}

@OptIn(ExperimentalForeignApi::class)
private fun VNRecognizedTextObservation.pieces(frame: CVPixelBufferRef): List<ScannedText> {
    val bufferWidth = CVPixelBufferGetWidth(frame).toInt()
    val bufferHeight = CVPixelBufferGetHeight(frame).toInt()
    if (bufferWidth <= 0 || bufferHeight <= 0) return emptyList()
    val box = boundingBox.toTextBox(bufferWidth, bufferHeight)
    val dotSize = box.height * ScanConstants.DOT_SIZE
    val dot =
        TextBox(
            left = box.left - box.height * ScanConstants.DOT_GAP - dotSize / 2f,
            top = box.top + box.height / 2f - dotSize / 2f,
            width = dotSize,
            height = dotSize,
        )
    val pixels = PixelFrame.of(frame)
    val ink =
        pixels?.let { sample -> box.toBuffer(bufferHeight).let { sample.inkColour(it[0], it[1], it[2], it[3]) } } ?: 0
    val dotInk =
        pixels?.let { sample -> dot.toBuffer(bufferHeight).let { sample.inkColour(it[0], it[1], it[2], it[3]) } } ?: 0
    return topCandidates(1.convert<NSUInteger>()).mapNotNull { candidate ->
        val text = (candidate as? VNRecognizedText)?.string ?: return@mapNotNull null
        ScannedText(
            text = text,
            left = box.left,
            top = box.top,
            width = box.width,
            height = box.height,
            colour = ink,
            dot = dotInk,
        )
    }
}

private data class TextBox(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float,
)

@OptIn(ExperimentalForeignApi::class)
private fun CValue<CGRect>.toTextBox(
    bufferWidth: Int,
    bufferHeight: Int,
): TextBox =
    useContents {
        val uprightWidth = bufferHeight.toFloat()
        val uprightHeight = bufferWidth.toFloat()
        TextBox(
            left = origin.x.toFloat() * uprightWidth,
            top = (1f - origin.y.toFloat() - size.height.toFloat()) * uprightHeight,
            width = size.width.toFloat() * uprightWidth,
            height = size.height.toFloat() * uprightHeight,
        )
    }

@OptIn(ExperimentalForeignApi::class)
private fun TextBox.toBuffer(bufferHeight: Int): IntArray {
    // Vision boxes are upright and bottom-left based; the camera buffer is landscape and rotated 90 degrees clockwise.
    val bufferLeft = top.toInt()
    val bufferTop = bufferHeight - 1 - (left + width).toInt()
    return intArrayOf(bufferLeft, bufferTop, height.toInt(), width.toInt())
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
