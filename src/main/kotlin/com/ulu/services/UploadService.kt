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

@Singleton
class UploadService {
    private val maxImageSizeMb = 10 // Set upload limit.
    private val maxImageSizeByte = maxImageSizeMb * 1024 * 1024

    @Inject
    @field:Client("http://localhost:8001")
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
     * Verify upload attempt by checking size and type is jpeg.
     * */
    fun verifyUpload(fileUpload: CompletedFileUpload): HttpResponse<String>? {
        val imageBytes = fileUpload.bytes
        if (fileUpload.contentType.get().name != MediaType.IMAGE_JPEG) {
            return HttpResponse.badRequest("MediaType must be of ${MediaType.IMAGE_JPEG}")
        }
        println(fileUpload.filename)

        if (!fileUpload.filename.endsWith(".jpeg", ignoreCase = true) && !fileUpload.filename.endsWith(".jpg", ignoreCase = true)) {
            return HttpResponse.badRequest("Uploaded file must be of filetype jpeg/jpg!")
        }
        if (verifySize(imageBytes)) {
            return HttpResponse.badRequest("Image size exceeds the maximum allowed limit of ${maxImageSizeMb}MB")
        }
        return null
    }

    /**
     * Limit size of upload.
     * */
    private fun verifySize(imageBytes: ByteArray): Boolean {
        return imageBytes.size > maxImageSizeByte
    }
}
