package com.ulu.repositories

import com.ulu.models.Whiskey
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface WhiskeyRepository : JpaRepository<Whiskey, Long>
