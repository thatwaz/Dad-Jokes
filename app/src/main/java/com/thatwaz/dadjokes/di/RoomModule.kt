package com.thatwaz.dadjokes.di

import android.content.Context
import androidx.room.Room
import com.thatwaz.dadjokes.data.db.CachedJokeDao
import com.thatwaz.dadjokes.data.db.JokeDao
import com.thatwaz.dadjokes.data.db.JokeDatabase
import com.thatwaz.dadjokes.data.db.SavedJokeDao
import com.thatwaz.dadjokes.data.db.SeenJokeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): JokeDatabase =
        Room.databaseBuilder(appContext, JokeDatabase::class.java, "joke_database")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideJokeDao(db: JokeDatabase): JokeDao = db.jokeDao()
    @Provides fun provideSavedJokeDao(db: JokeDatabase): SavedJokeDao = db.savedJokeDao()
    @Provides fun provideSeenJokeDao(db: JokeDatabase): SeenJokeDao = db.seenJokeDao()
    @Provides fun provideCachedJokeDao(db: JokeDatabase): CachedJokeDao = db.cachedJokeDao() // 👈 add
}



