package com.animeai.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.camera.core.ImageProxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for image processing operations
 */
@Singleton
class ImageUtil @Inject constructor(
    private val context: Context
) {
    private val TAG = "ImageUtil"
    
    /**
     * Download image from URL as byte array
     */
    suspend fun downloadImageAsBytes(imageUrl: String): ByteArray {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(imageUrl)
                val connection = url.openConnection()
                connection.connect()
                
                connection.getInputStream().use { inputStream ->
                    inputStream.readBytes()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error downloading image from $imageUrl", e)
                ByteArray(0)
            }
        }
    }
    
    /**
     * Download image from URL as bitmap
     */
    suspend fun downloadImageAsBitmap(imageUrl: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val imageBytes = downloadImageAsBytes(imageUrl)
                if (imageBytes.isNotEmpty()) {
                    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error converting image bytes to bitmap", e)
                null
            }
        }
    }
    
    /**
     * Convert CameraX ImageProxy to Bitmap
     */
    fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
        val buffer = imageProxy.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
    
    /**
     * Convert YUV_420_888 format from CameraX to JPEG byte array
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
     * Convert bitmap to Base64 string
     */
    fun bitmapToBase64(bitmap: Bitmap, quality: Int = 70): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
    
    /**
     * Resize bitmap to target size
     */
    fun resizeBitmap(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }
    
    /**
     * Save bitmap to app's cache directory
     */
    suspend fun saveBitmapToCache(bitmap: Bitmap, fileName: String): Uri {
        return withContext(Dispatchers.IO) {
            try {
                val cachePath = File(context.cacheDir, "images")
                cachePath.mkdirs()
                
                val file = File(cachePath, fileName)
                
                FileOutputStream(file).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.flush()
                }
                
                Uri.fromFile(file)
            } catch (e: Exception) {
                Log.e(TAG, "Error saving bitmap to cache", e)
                throw e
            }
        }
    }
}