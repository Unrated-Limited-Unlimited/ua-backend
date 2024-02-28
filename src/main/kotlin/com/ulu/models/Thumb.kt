package com.ulu.models

import jakarta.persistence.*

@Entity
class Thumb (
    @Id
    @GeneratedValue
    val id : Long? = null,

    @ManyToOne
    var user : UserData? = null,

    @ManyToOne
    var rating : Rating? = null,

    val thumb : Boolean
)