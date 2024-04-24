package com.ulu

import com.ulu.models.Whiskey
import com.ulu.repositories.WhiskeyRepository
import com.ulu.services.UploadService
import io.micronaut.http.HttpResponse
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import java.net.URL

/**
 * Script for using whiskey.img with url from vinmonopolet and storing the images in FISO.
 * */
@MicronautTest(environments = ["prod"])
@Tag("manual")
class FisoPop(private val whiskeyRepository: WhiskeyRepository, private val uploadService: UploadService) {
    @Test
    fun testUpload() {
        val whiskeys: List<Whiskey> = whiskeyRepository.findAll()

        println(whiskeys)

        val requestList: MutableList<Mono<HttpResponse<String>>> = mutableListOf()
        whiskeys.forEach { whiskey: Whiskey ->
            val bytes: ByteArray = URL(whiskey.img).readBytes()

            val req = uploadService.upload(bytes, "w", whiskey.id.toString())
            requestList.add(req)
        }
        requestList.parallelStream().map {
            val res = it.block()
            println(res)
        }
    }
}
