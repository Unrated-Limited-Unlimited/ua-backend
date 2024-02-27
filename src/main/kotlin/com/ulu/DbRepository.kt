package com.ulu


import com.ulu.models.Rating
import com.ulu.models.UserData
import com.ulu.models.Whiskey
import jakarta.inject.Singleton

/***
 * Mocking a DB/repository until JPA is fully working
 */

@Singleton
class DbRepository {

    fun findAllUsers(): List<UserData> {
        return users
    }


    fun findAllWhiskeys() : List<Whiskey>{

        // Calculate average rating
        whiskeys.forEach { whiskey: Whiskey ->
            var totalRating = 0f
            var count = 0
            if (whiskey.ratings != null){
                whiskey.ratings.forEach { rating: Rating ->
                    totalRating += rating.rating
                    count++;
                }
                whiskey.rating = if (count > 0) totalRating / count else 0f
            }
        }

        return whiskeys
    }

    companion object {
        // Create some testing data
        private val user1 = UserData(1,"Jeff","jeff@bank.no","123","")
        private val user2 = UserData(2,"Paul","pauling@gmail.com","42","")
        private val users = listOf(user1,user2)

        private val whiskey1 = Whiskey(1,"test.com/img","Test",199.6f,"its a whiskey",1.5f,99.9f)
        private val whiskey2 = Whiskey(2,"test2.com/img","Test2",5f,"its another whiskey",0.4f,21f,
            ratings = listOf(Rating(body = "test", rating = 2f, title = "its drinkable", user = user1, whiskey = whiskey1))
        )

        private val whiskeys = listOf(whiskey1, whiskey2)
    }
}