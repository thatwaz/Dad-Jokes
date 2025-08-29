package com.thatwaz.dadjokes.data.db

import com.thatwaz.dadjokes.domain.model.PersonSummary


data class PersonSummaryDb(
    val personName: String,
    val untoldCount: Int,
    val toldCount: Int
)



fun PersonSummaryDb.toDomain(): PersonSummary =
    PersonSummary(personName, untoldCount, toldCount)
