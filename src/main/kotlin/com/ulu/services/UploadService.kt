package com.ulu.services

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.multipart.CompletedFileUpload
import jakarta.inject.Inject
import jakarta.inject.Singleton
import reactor.core.publisher.Mono
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

@Singleton
class UploadService {
    @Inject
    @field:Client("http://localhost:8001") // Base url for file storage server.
    lateinit var client: HttpClient

    /**
     * Upload image to file storage server
     * */
    fun upload(
        imageBytes: ByteArray,
        prefix: String,
        id: String,
    ): Mono<HttpResponse<String>> {
        val request = HttpRequest.POST("/api/img/${prefix}$id", imageBytes).contentType(MediaType.IMAGE_JPEG)
        return Mono.from(client.exchange(request, String::class.java))
    }

    /**
     * Verify upload attempt by checking size and type is image.
     * */
    fun isUploadedFileImage(fileUpload: CompletedFileUpload): Boolean {
        return !(!fileUpload.contentType.isPresent || !fileUpload.contentType.get().name.startsWith("image/"))
    }

    /**
     * Convert the uploaded image file to jpeg byte array.
     * */
    fun imageToJpegByteArray(imageBytes: ByteArray): ByteArray? {
        // Convert ByteArray to BufferedImage
        val inputStream = ByteArrayInputStream(imageBytes)
        val image: BufferedImage? = ImageIO.read(inputStream)
        if (image == null) {
            println("Image buffer empty")
            return null
        }

        // Prepare output stream to capture the output bytes
        val outputStream = ByteArrayOutputStream()

        // Write the image as a JPEG
        val writer = ImageIO.write(convertToJpeg(image), "JPEG", outputStream)
        if (!writer) {
            println("No writer found.")
            return null
        }

        // Convert ByteArrayOutputStream to ByteArray
        return outputStream.toByteArray()
    }

    /**
     * Remove transparent features from image.
     * */
    private fun convertToJpeg(inputImage: BufferedImage): BufferedImage {
        // Create a new RGB buffered image
        val newImage = BufferedImage(inputImage.width, inputImage.height, BufferedImage.TYPE_INT_RGB)
        // Draw the input image on the new image, handling transparency
        newImage.createGraphics().apply {
            drawImage(inputImage, 0, 0, Color.WHITE, null)
            dispose()
        }
        return newImage
    }
}
