package com.animeai.app.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import android.util.Log
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class ImageProcessing @Inject constructor() {
    
    /**
     * Download image from URL as byte array
     */
    suspend fun downloadImageAsBytes(imageUrl: String): ByteArray {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(imageUrl)
                val connection = url.openConnection()
                connection.connect()
                
                val inputStream = connection.getInputStream()
                inputStream.readBytes().also {
                    inputStream.close()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error downloading image", e)
                ByteArray(0)
            }
        }
    }
    
    /**
     * Convert ImageProxy from CameraX to Bitmap
     */
    fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
        val buffer = imageProxy.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
    
    /**
     * Convert YUV_420_888 format image from CameraX to JPEG byte array
     */
    fun imageProxyToJpegByteArray(imageProxy: ImageProxy): ByteArray {
        val yBuffer = imageProxy.planes[0].buffer
        val uBuffer = imageProxy.planes[1].buffer
        val vBuffer = imageProxy.planes[2].buffer
        
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()
        
        val nv21 = ByteArray(ySize + uSize + vSize)
        
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)
        
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, imageProxy.width, imageProxy.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, imageProxy.width, imageProxy.height), 80, out)
        
        return out.toByteArray()
    }
    
    /**
     * Resize bitmap to target size
     */
    fun resizeBitmap(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }
    
    /**
     * Extract facial features from image
     * This is a stub - in a real implementation this would use ML Kit or similar
     */
    suspend fun extractFacialFeatures(imageData: ByteArray): Map<String, List<Float>> {
        return withContext(Dispatchers.Default) {
            // In a real app, this would use ML Kit, Firebase ML, or TensorFlow Lite
            // to extract facial landmarks
            mapOf(
                "eyes" to listOf(0.3f, 0.4f, 0.7f, 0.4f),
                "mouth" to listOf(0.5f, 0.7f),
                "eyebrows" to listOf(0.3f, 0.35f, 0.7f, 0.35f)
            )
        }
    }
    
    companion object {
        private const val TAG = "ImageProcessing"
    }
}
