package com.thatwaz.dadjokes.di


import android.content.Context
import com.thatwaz.dadjokes.data.repository.PrefsRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PrefsModule {

    @Provides
    @Singleton
    fun providePrefsRepo(
        @ApplicationContext context: Context
    ): PrefsRepo = PrefsRepo(context)
}
