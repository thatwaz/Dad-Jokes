package com.thatwaz.dadjokes.viewmodel
//
//
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import androidx.work.*
//import com.thatwaz.dadjokes.worker.JokeNotificationWorker
//
//import dagger.hilt.android.lifecycle.HiltViewModel
//import java.util.Calendar
//import java.util.concurrent.TimeUnit
//import javax.inject.Inject
//import kotlinx.coroutines.launch
//
//@HiltViewModel
//class NotificationViewModel @Inject constructor(
//    private val workManager: WorkManager
//) : ViewModel() {
//
//    fun scheduleDailyJoke(hour: Int, minute: Int) {
//        viewModelScope.launch {
//            val current = Calendar.getInstance()
//            val target = Calendar.getInstance().apply {
//                set(Calendar.HOUR_OF_DAY, hour)
//                set(Calendar.MINUTE, minute)
//                set(Calendar.SECOND, 0)
//                if (before(current)) add(Calendar.DAY_OF_YEAR, 1)
//            }
//
//            val delay = target.timeInMillis - current.timeInMillis
//
//            val request = PeriodicWorkRequestBuilder<JokeNotificationWorker>(1, TimeUnit.DAYS)
//                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
//                .build()
//
//            workManager.enqueueUniquePeriodicWork(
//                "dailyJoke",
//                ExistingPeriodicWorkPolicy.REPLACE,
//                request
//            )
//        }
//    }
//}
