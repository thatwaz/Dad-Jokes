package com.thatwaz.dadjokes.di

import android.content.Context
import androidx.room.Room
import com.thatwaz.dadjokes.data.db.JokeDao
import com.thatwaz.dadjokes.data.db.JokeDatabase
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
    fun provideDatabase(
        @ApplicationContext appContext: Context
    ): JokeDatabase {
        return Room.databaseBuilder(
            appContext,
            JokeDatabase::class.java,
            "joke_database"
        )
            .fallbackToDestructiveMigration() // <- This line added
            .build()
    }

    @Provides
    fun provideJokeDao(db: JokeDatabase): JokeDao = db.jokeDao()
}



