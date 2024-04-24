package com.ulu

import com.ulu.services.UploadService
import io.micronaut.http.MediaType
import io.micronaut.http.multipart.CompletedFileUpload
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.*
import javax.imageio.ImageIO

@MicronautTest
class UploadServiceTest(private val uploadService: UploadService) {
    private class FakeCompletedFileUpload(
        private val contentType: MediaType,
        private val name: String,
        private val filename: String,
        private val size: Long,
        private val data: ByteArray,
    ) : CompletedFileUpload {
        override fun getContentType() = Optional.of(contentType)

        override fun getName() = name

        override fun getFilename() = filename

        override fun getSize() = size

        override fun getDefinedSize(): Long = 0

        override fun isComplete(): Boolean = true

        override fun getBytes() = data

        override fun getByteBuffer(): ByteBuffer = ByteBuffer.wrap(data)

        override fun getInputStream() = ByteArrayInputStream(data)
    }

    @Test
    fun testValidImageType() {
        val fileUpload =
            FakeCompletedFileUpload(
                MediaType("image/jpeg"),
                "test.jpeg",
                "test.jpeg",
                1234L,
                byteArrayOf(1, 2, 3),
            )
        val result = uploadService.isUploadedFileImage(fileUpload)
        assertTrue(result)
    }

    @Test
    fun testInvalidImageType() {
        val fileUpload =
            FakeCompletedFileUpload(
                MediaType("text/plain"),
                "test.txt",
                "test.txt",
                1234L,
                byteArrayOf(1, 2, 3),
            )
        val result = uploadService.isUploadedFileImage(fileUpload)
        assertFalse(result)
    }

    @Test
    fun testValidImage() {
        val originalImage = BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB)
        originalImage.graphics.apply {
            fillRect(0, 0, 100, 100)
            dispose()
        }
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(originalImage, "png", outputStream)
        val imageBytes = outputStream.toByteArray()

        val result = uploadService.imageToJpegByteArray(imageBytes)
        assertNotNull(result)
        assertTrue(result!!.isNotEmpty())
    }

    @Test
    fun testInvalidImage() {
        val result = uploadService.imageToJpegByteArray(byteArrayOf())
        assertNull(result)
    }
}
