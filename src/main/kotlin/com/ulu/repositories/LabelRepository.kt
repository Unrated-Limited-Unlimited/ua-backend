package com.ulu.repositories

import com.ulu.models.Label
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface LabelRepository : JpaRepository<Label,Long> {
    fun existsByName(name: String): Boolean

}