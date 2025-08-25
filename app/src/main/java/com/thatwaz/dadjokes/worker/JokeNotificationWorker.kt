package com.thatwaz.dadjokes.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.thatwaz.dadjokes.data.repository.JokeRepository
import com.thatwaz.dadjokes.domain.model.Joke
import com.thatwaz.dadjokes.notification.JokeNotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class JokeNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: JokeRepository
) : Worker(context, params) {

    override fun doWork(): Result {
        val joke = getRandomJoke() ?: return Result.failure()
        val fullJoke = "${joke.setup} ${joke.punchline}"
        showNotification(fullJoke)
        return Result.success()
    }

    private fun getRandomJoke(): Joke? {
        val allJokes = repository.getAllJokesBlocking()
        return allJokes.randomOrNull()
    }

    private fun showNotification(joke: String) {
        JokeNotificationHelper.showNotification(applicationContext, joke)
    }
}


