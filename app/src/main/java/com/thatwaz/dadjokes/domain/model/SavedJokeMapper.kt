package com.thatwaz.dadjokes.domain.model

import com.thatwaz.dadjokes.data.db.SavedJokeEntity

fun SavedJokeEntity.toDomain(): SavedJokeDelivery = SavedJokeDelivery(
    id = id,
    setup = setup,
    punchline = punchline,
    personName = personName,
    hasBeenTold = hasBeenTold,
    reactionRating = reactionRating,
    dateSaved = dateSaved
)

fun SavedJokeDelivery.toEntity(): SavedJokeEntity = SavedJokeEntity(
    id = id,
    setup = setup,
    punchline = punchline,
    personName = personName,
    hasBeenTold = hasBeenTold,
    reactionRating = reactionRating,
    dateSaved = dateSaved
)

