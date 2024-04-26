package com.ulu.repositories

import com.ulu.models.Whiskey
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable

@Repository
interface WhiskeyRepository : JpaRepository<Whiskey, Long> {
    fun listAll(pageable: Pageable): Page<Whiskey>
}
