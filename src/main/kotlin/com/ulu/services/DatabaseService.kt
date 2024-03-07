package com.ulu.services

import com.ulu.models.Rating
import com.ulu.models.Thumb
import com.ulu.models.UserData
import com.ulu.models.Whiskey
import com.ulu.repositories.RatingRepository
import com.ulu.repositories.ThumbRepository
import com.ulu.repositories.UserDataRepository
import com.ulu.repositories.WhiskeyRepository
import jakarta.inject.Singleton

@Singleton
class DatabaseService(
    private val userDataRepository: UserDataRepository,
    private val whiskeyRepository: WhiskeyRepository,
    private val ratingRepository: RatingRepository,
    private val thumbRepository: ThumbRepository
) {
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
        throw Exception("No repository matching parsed type!")
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
        throw Exception("No repository matching parsed type!")
    }

    fun <T> existsById(obj : T) : Boolean {
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
        throw Exception("No repository matching parsed type!")
    }
}