package com.ulu.repositories

import com.ulu.models.Attribute
import com.ulu.models.AttributeCategory
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface AttributeRepository : JpaRepository<Attribute,Long> {


}