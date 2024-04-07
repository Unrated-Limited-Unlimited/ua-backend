package com.ulu.services

import com.ulu.models.*
import com.ulu.repositories.*
import jakarta.inject.Singleton

/**
 * Service for accessing multiple of the repositories functions in the same file.
 * Mostly used for creating/setting up testing data; when instantiating multiple objects.
 * */
@Singleton
class DatabaseService(
    private val userDataRepository: UserDataRepository,
    private val whiskeyRepository: WhiskeyRepository,
    private val ratingRepository: RatingRepository,
    private val thumbRepository: ThumbRepository,
    private val attributeRepository: AttributeRepository,
    private val attributeCategoryRepository: AttributeCategoryRepository
) {
    private val noMatchingRepositoryErrorMsg = "No repository matching parsed type!"

    fun <T> save(obj : T) : T {
        if (obj is Whiskey){
            return whiskeyRepository.save(obj)
        }
        if (obj is UserData){
            return userDataRepository.save(obj)
        }
        if (obj is Rating){
            return ratingRepository.save(obj)
        }
        if (obj is Thumb){
            return thumbRepository.save(obj)
        }
        if (obj is Attribute){
            return attributeRepository.save(obj)
        }
        if (obj is AttributeCategory){
            return attributeCategoryRepository.save(obj)
        }
        throw Exception(noMatchingRepositoryErrorMsg)
    }

    fun <T> delete(obj : T) {
        if (obj is Whiskey){
            return whiskeyRepository.delete(obj)
        }
        if (obj is UserData){
            return userDataRepository.delete(obj)
        }
        if (obj is Rating){
            return ratingRepository.delete(obj)
        }
        if (obj is Thumb){
            return thumbRepository.delete(obj)
        }
        if (obj is Attribute){
            return attributeRepository.delete(obj)
        }
        if (obj is AttributeCategory){
            return attributeCategoryRepository.delete(obj)
        }
        throw Exception(noMatchingRepositoryErrorMsg)
    }

    fun <T> exists(obj : T) : Boolean {
        if (obj is Whiskey){
            return whiskeyRepository.existsById(obj.id)
        }
        if (obj is UserData){
            return userDataRepository.existsById(obj.id)
        }
        if (obj is Rating){
            return ratingRepository.existsById(obj.id)
        }
        if (obj is Thumb){
            return thumbRepository.existsById(obj.id)
        }
        if (obj is Attribute){
            return attributeRepository.existsById(obj.id)
        }
        if (obj is AttributeCategory){
            return attributeCategoryRepository.existsById(obj.id)
        }
        throw Exception(noMatchingRepositoryErrorMsg)
    }

    fun deleteAll(){
        thumbRepository.deleteAll()
        attributeRepository.deleteAll()
        attributeCategoryRepository.deleteAll()
        ratingRepository.deleteAll()

        whiskeyRepository.deleteAll()
        userDataRepository.deleteAll()
    }
}