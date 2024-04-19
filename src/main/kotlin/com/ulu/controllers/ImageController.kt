package com.ulu.controllers

import com.ulu.repositories.UserDataRepository
import com.ulu.services.RequestValidatorService
import com.ulu.services.UploadService
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.multipart.CompletedFileUpload
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.micronaut.security.utils.DefaultSecurityService

/**
 * REST endpoints for uploading images to the file storage microservice.
 *
 * Users must be logged in to upload images.
 * They can upload a profile image, and it will be stored in the file storage at:
 * "/api/img/{prefix}{user.id}" where prefix is "p" for profile image and "w" for whiskey.
 *
 * Only admins can upload Whiskey images.
 * */
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller
class ImageController(
    private val securityService: DefaultSecurityService,
    private val userDataRepository: UserDataRepository,
    private val requestValidatorService: RequestValidatorService,
    private val uploadService: UploadService,
) {
    @Post("/img/whiskey/{id}", consumes = [MediaType.MULTIPART_FORM_DATA], produces = [MediaType.TEXT_PLAIN])
    fun whiskeyImagePost(
        @PathVariable id: String,
        @Part("file") fileUpload: CompletedFileUpload,
    ): HttpResponse<String> {
        // Only admins can upload whiskey photos
        if (!requestValidatorService.isAdmin(securityService)) {
            return HttpResponse.unauthorized()
        }
        // Verify size and type of upload.
        val failedCheck = uploadService.verifyUpload(fileUpload)
        if (failedCheck != null) {
            return failedCheck
        }

        try {
            val response =
                uploadService.upload(fileUpload.bytes, "w", id).block()
                    ?: return HttpResponse.serverError("Response is null from file server.")

            if (response.code() != 200) {
                return HttpResponse.serverError("Could not upload image to file storage ${response.code()}")
            }
            return HttpResponse.ok("Image successfully uploaded with status: ${response.status}")
        } catch (e: Exception) {
            println("Error uploading:")
            println(e)
            return HttpResponse.serverError("Error uploading whiskey image to file storage.")
        }
    }

    @Post("/img/profile", consumes = [MediaType.MULTIPART_FORM_DATA], produces = [MediaType.TEXT_PLAIN])
    fun profileImagePost(
        @Part("file") fileUpload: CompletedFileUpload,
    ): HttpResponse<String> {
        // Verify size and type of upload.
        val failedCheck = uploadService.verifyUpload(fileUpload)
        if (failedCheck != null) {
            return failedCheck
        }

        try {
            val username = securityService.authentication.get().name
            val user = userDataRepository.getUserDataByName(username)
            return if (user == null) {
                HttpResponse.badRequest("No user found for the provided credentials.")
            } else {
                val id = user.id.toString()
                val response =
                    uploadService.upload(fileUpload.bytes, "p", id).block()
                        ?: return HttpResponse.serverError("Response is null from file server.")

                if (response.code() != 200) {
                    return HttpResponse.serverError("Could not upload image to file storage ${response.code()}")
                }
                HttpResponse.ok("Image successfully uploaded with status: ${response.status}")
            }
        } catch (e: Exception) {
            println("Error uploading:")
            println(e)
            return HttpResponse.serverError("Error uploading profile image to file storage.")
        }
    }
}
