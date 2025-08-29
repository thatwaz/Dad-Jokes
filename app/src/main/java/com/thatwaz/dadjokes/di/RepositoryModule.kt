package com.thatwaz.dadjokes.di

import com.thatwaz.dadjokes.data.db.SavedJokeDao
import com.thatwaz.dadjokes.data.repository.SavedJokeRepositoryImpl
import com.thatwaz.dadjokes.domain.repository.SavedJokeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideSavedJokeRepository(
        dao: SavedJokeDao
    ): SavedJokeRepository = SavedJokeRepositoryImpl(dao)
}
