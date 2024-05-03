package com.ulu.controllers

import com.ulu.repositories.UserDataRepository
import com.ulu.repositories.WhiskeyRepository
import com.ulu.services.RequestValidatorService
import com.ulu.services.UploadService
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.multipart.CompletedFileUpload
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.micronaut.security.utils.DefaultSecurityService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses

/**
 * REST endpoints for uploading images to the file storage microservice.
 *
 * Users must be logged in to upload images.
 * They can upload a profile image, and it will be stored in the file storage as a jpg:
 * "/api/img/{prefix}{user.id}" where prefix is "p" for profile image and "w" for whiskey.
 *
 * Only admins can upload Whiskey images.
 * */
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller
class ImageController(
    private val securityService: DefaultSecurityService,
    private val userDataRepository: UserDataRepository,
    private val whiskeyRepository: WhiskeyRepository,
    private val requestValidatorService: RequestValidatorService,
    private val uploadService: UploadService,
) {
    @Post("/img/whiskey/{id}", consumes = [MediaType.MULTIPART_FORM_DATA], produces = [MediaType.TEXT_PLAIN])
    @Operation(summary = "Upload Whiskey Image", description = "Uploads an image for a specific whiskey. Only accessible by admins.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Image successfully uploaded."),
        ApiResponse(responseCode = "401", description = "Unauthorized access, admin user required."),
        ApiResponse(responseCode = "400", description = "Invalid file size or type for file upload."),
        ApiResponse(responseCode = "500", description = "Internal server error; file storage server might be unreachable."),
    )
    fun whiskeyImagePost(
        @PathVariable id: String,
        @Part("file") fileUpload: CompletedFileUpload,
    ): HttpResponse<String> {
        // Only admins can upload whiskey photos
        if (!requestValidatorService.isAdmin(securityService)) {
            return HttpResponse.unauthorized()
        }
        // Verify type of upload.
        if (!uploadService.isUploadedFileImage(fileUpload)) {
            return HttpResponse.badRequest("File must be an image")
        }

        val whiskey = whiskeyRepository.findById(id.toLong())
        if (whiskey.isEmpty) {
            return HttpResponse.badRequest("No whiskey with id: $id")
        }

        // Convert file to jpg byte array
        val byteArray =
            uploadService.imageToJpegByteArray(fileUpload.bytes)
                ?: return HttpResponse.serverError("Could not convert file to jpg.")

        try {
            val response =
                uploadService.upload(byteArray, "w", id).block()
                    ?: return HttpResponse.serverError("Response is null from file server.")

            if (response.code() != 200) {
                return HttpResponse.serverError("Could not upload image to file storage ${response.code()}")
            }
            whiskey.get().img = "w$id"
            whiskeyRepository.update(whiskey.get())

            return HttpResponse.ok("Image successfully uploaded with status: ${response.status}")
        } catch (e: Exception) {
            println("Error uploading:")
            println(e)
            return HttpResponse.serverError("Error uploading whiskey image to file storage.")
        }
    }

    @Secured(SecurityRule.IS_AUTHENTICATED)
    @Post("/img/profile", consumes = [MediaType.MULTIPART_FORM_DATA], produces = [MediaType.TEXT_PLAIN])
    @Operation(summary = "Upload Profile Image", description = "Uploads a profile image for the authenticated user.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Profile image successfully uploaded to file storage."),
        ApiResponse(responseCode = "400", description = "Invalid file size or type for file upload."),
        ApiResponse(responseCode = "500", description = "Internal server error; file storage server might be unreachable."),
    )
    fun profileImagePost(
        @Part("file") fileUpload: CompletedFileUpload,
    ): HttpResponse<String> {
        // Verify type of upload.
        if (!uploadService.isUploadedFileImage(fileUpload)) {
            return HttpResponse.badRequest("File must be an image")
        }

        // Convert file to jpg byte array
        val jpgBytes =
            uploadService.imageToJpegByteArray(fileUpload.bytes)
                ?: return HttpResponse.serverError("Could not convert file to jpg.")

        try {
            val username = securityService.authentication.get().name
            val user = userDataRepository.getUserDataByName(username)

            return if (user == null) {
                HttpResponse.badRequest("No user found for the provided credentials.")
            } else {
                val id = user.id.toString()
                val response =
                    uploadService.upload(jpgBytes, "p", id).block()
                        ?: return HttpResponse.serverError("Response is null from file server.")

                if (response.code() != 200) {
                    return HttpResponse.serverError("Could not upload image to file storage ${response.code()}")
                }
                // Update user image
                user.img = "p$id"
                userDataRepository.update(user)

                HttpResponse.ok("Image successfully uploaded with status: ${response.status}")
            }
        } catch (e: Exception) {
            println("Error uploading:")
            println(e)
            return HttpResponse.serverError("Error uploading profile image to file storage.")
        }
    }
}
