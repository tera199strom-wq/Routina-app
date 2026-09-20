package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.ArrayDeque
import java.util.UUID
import kotlin.math.sqrt

object MascotImageHelper {

    fun removeBackground(
        context: Context,
        imageUri: String,
        onSuccess: (String) -> Unit,
        onError: () -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val uri = Uri.parse(imageUri)
                val inputStream = context.contentResolver.openInputStream(uri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (originalBitmap == null) {
                    withContext(Dispatchers.Main) { onError() }
                    return@launch
                }

                // Resize bitmap if excessively large for speed and memory efficiency
                val maxDim = 1200
                val bitmap = if (originalBitmap.width > maxDim || originalBitmap.height > maxDim) {
                    val ratio = maxDim.toFloat() / maxOf(originalBitmap.width, originalBitmap.height)
                    Bitmap.createScaledBitmap(
                        originalBitmap,
                        (originalBitmap.width * ratio).toInt(),
                        (originalBitmap.height * ratio).toInt(),
                        true
                    )
                } else {
                    originalBitmap
                }

                // Step 1: Try Google ML Kit Subject Segmentation (Works best for real humans/pets/objects)
                val options = SubjectSegmenterOptions.Builder()
                    .enableForegroundBitmap()
                    .build()

                val segmenter = SubjectSegmentation.getClient(options)
                val image = InputImage.fromBitmap(bitmap, 0)

                segmenter.process(image)
                    .addOnSuccessListener { result ->
                        val fgBitmap = result.foregroundBitmap
                        if (fgBitmap != null && isValidForeground(fgBitmap)) {
                            saveAndReturnBitmap(context, fgBitmap, onSuccess, onError)
                        } else {
                            // ML Kit could not detect real human/subject (common in anime, cartoons, and illustrations)
                            // Seamlessly fallback to Smart Edge-Connected Floodfill Removal
                            removeCartoonBackground(bitmap, context, onSuccess, onError)
                        }
                    }
                    .addOnFailureListener {
                        // On ML Kit failure, fallback to cartoon algorithm
                        removeCartoonBackground(bitmap, context, onSuccess, onError)
                    }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { onError() }
            }
        }
    }

    private fun isValidForeground(bitmap: Bitmap): Boolean {
        val sampleStepX = (bitmap.width / 25).coerceAtLeast(1)
        val sampleStepY = (bitmap.height / 25).coerceAtLeast(1)
        var visibleCount = 0
        var totalSampled = 0
        for (y in 0 until bitmap.height step sampleStepY) {
            for (x in 0 until bitmap.width step sampleStepX) {
                totalSampled++
                val pixel = bitmap.getPixel(x, y)
                if (Color.alpha(pixel) > 30) {
                    visibleCount++
                }
            }
        }
        return visibleCount > (totalSampled * 0.02) && visibleCount < (totalSampled * 0.98)
    }

    private fun saveAndReturnBitmap(
        context: Context,
        fgBitmap: Bitmap,
        onSuccess: (String) -> Unit,
        onError: () -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val imagesDir = File(context.filesDir, "mascot_images")
                if (!imagesDir.exists()) {
                    imagesDir.mkdirs()
                }
                val fileName = "avatar_bgrm_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.png"
                val destinationFile = File(imagesDir, fileName)

                FileOutputStream(destinationFile).use { output ->
                    fgBitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
                }
                withContext(Dispatchers.Main) {
                    onSuccess(Uri.fromFile(destinationFile).toString())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { onError() }
            }
        }
    }

    /**
     * Smart boundary-seeded flood-fill algorithm specifically designed for 2D anime, cartoons,
     * stickers, and drawings with solid or near-solid backgrounds (e.g. white, black, light gray, chroma).
     *
     * Only pixels connected to the outer perimeter within the color tolerance threshold are removed.
     * Inner white/light pixels (like cartoon eyes, teeth, clothes) remain intact.
     */
    private fun removeCartoonBackground(
        sourceBitmap: Bitmap,
        context: Context,
        onSuccess: (String) -> Unit,
        onError: () -> Unit
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val width = sourceBitmap.width
                val height = sourceBitmap.height
                val pixels = IntArray(width * height)
                sourceBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

                // Sample corners and perimeter edges to detect background color
                val cornerIndices = intArrayOf(
                    0,
                    width - 1,
                    (height - 1) * width,
                    height * width - 1,
                    width / 2, // top center
                    (height - 1) * width + width / 2 // bottom center
                )

                val cornerColors = cornerIndices.map { pixels[it] }
                val bgRed = cornerColors.map { Color.red(it) }.average().toInt()
                val bgGreen = cornerColors.map { Color.green(it) }.average().toInt()
                val bgBlue = cornerColors.map { Color.blue(it) }.average().toInt()

                fun colorDistance(c1R: Int, c1G: Int, c1B: Int, pixel: Int): Double {
                    val r = Color.red(pixel)
                    val g = Color.green(pixel)
                    val b = Color.blue(pixel)
                    val dr = (c1R - r).toDouble()
                    val dg = (c1G - g).toDouble()
                    val db = (c1B - b).toDouble()
                    return sqrt(dr * dr + dg * dg + db * db)
                }

                // Tolerance threshold to accommodate compression artifacts (e.g. JPEG white halos)
                val tolerance = 45.0

                val visited = BooleanArray(width * height)
                val queue = ArrayDeque<Int>(width * 4)

                // Seed queue with border pixels that match background color
                for (x in 0 until width) {
                    val topIdx = x
                    if (colorDistance(bgRed, bgGreen, bgBlue, pixels[topIdx]) <= tolerance) {
                        visited[topIdx] = true
                        queue.add(topIdx)
                    }
                    val bottomIdx = (height - 1) * width + x
                    if (colorDistance(bgRed, bgGreen, bgBlue, pixels[bottomIdx]) <= tolerance) {
                        visited[bottomIdx] = true
                        queue.add(bottomIdx)
                    }
                }

                for (y in 0 until height) {
                    val leftIdx = y * width
                    if (!visited[leftIdx] && colorDistance(bgRed, bgGreen, bgBlue, pixels[leftIdx]) <= tolerance) {
                        visited[leftIdx] = true
                        queue.add(leftIdx)
                    }
                    val rightIdx = y * width + (width - 1)
                    if (!visited[rightIdx] && colorDistance(bgRed, bgGreen, bgBlue, pixels[rightIdx]) <= tolerance) {
                        visited[rightIdx] = true
                        queue.add(rightIdx)
                    }
                }

                var removedCount = 0
                while (!queue.isEmpty()) {
                    val curr = queue.poll() ?: break
                    pixels[curr] = 0 // Fully transparent
                    removedCount++

                    val cx = curr % width
                    val cy = curr / width

                    val n1 = if (cx > 0) curr - 1 else -1
                    val n2 = if (cx < width - 1) curr + 1 else -1
                    val n3 = if (cy > 0) curr - width else -1
                    val n4 = if (cy < height - 1) curr + width else -1

                    val neighbors = intArrayOf(n1, n2, n3, n4)
                    for (next in neighbors) {
                        if (next != -1 && !visited[next]) {
                            if (colorDistance(bgRed, bgGreen, bgBlue, pixels[next]) <= tolerance) {
                                visited[next] = true
                                queue.add(next)
                            }
                        }
                    }
                }

                // Soft alpha feathering / anti-aliasing on outer boundary edges
                for (y in 1 until height - 1) {
                    for (x in 1 until width - 1) {
                        val idx = y * width + x
                        if (pixels[idx] != 0) {
                            val dist = colorDistance(bgRed, bgGreen, bgBlue, pixels[idx])
                            if (dist < tolerance + 20.0) {
                                val hasClearedNeighbor = pixels[idx - 1] == 0 ||
                                        pixels[idx + 1] == 0 ||
                                        pixels[idx - width] == 0 ||
                                        pixels[idx + width] == 0

                                if (hasClearedNeighbor) {
                                    val alphaRatio = ((dist - tolerance + 8.0) / 28.0).coerceIn(0.15, 1.0)
                                    val origAlpha = Color.alpha(pixels[idx])
                                    val newAlpha = (origAlpha * alphaRatio).toInt().coerceIn(0, 255)
                                    pixels[idx] = Color.argb(
                                        newAlpha,
                                        Color.red(pixels[idx]),
                                        Color.green(pixels[idx]),
                                        Color.blue(pixels[idx])
                                    )
                                }
                            }
                        }
                    }
                }

                val totalPixels = width * height
                if (removedCount > totalPixels * 0.01 && removedCount < totalPixels * 0.98) {
                    val outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    outputBitmap.setPixels(pixels, 0, width, 0, 0, width, height)

                    val imagesDir = File(context.filesDir, "mascot_images")
                    if (!imagesDir.exists()) imagesDir.mkdirs()
                    val fileName = "avatar_bgrm_cartoon_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.png"
                    val destinationFile = File(imagesDir, fileName)

                    FileOutputStream(destinationFile).use { output ->
                        outputBitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
                    }

                    withContext(Dispatchers.Main) {
                        onSuccess(Uri.fromFile(destinationFile).toString())
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onError()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onError()
                }
            }
        }
    }
}

