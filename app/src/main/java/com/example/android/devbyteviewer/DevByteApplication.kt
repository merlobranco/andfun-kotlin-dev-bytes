/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.example.android.devbyteviewer

import android.app.Application
import android.os.Build
import androidx.work.*
import com.example.android.devbyteviewer.work.RefreshDataWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Override application to setup background work via WorkManager
 */
class DevByteApplication : Application() {
    /**
     * Let's take the extra initialization out
     */
    val applicationScope = CoroutineScope(Dispatchers.Default)

    /**
     * Creating an initialization function that does not block the main thread:
     *
     * It's important to note that WorkManager.initialize should be called from inside onCreate without using a background thread
     * to avoid issues caused when initialization happens after WorkManager is used [1]
     */
    private fun delayedInit() {
        applicationScope.launch {
            setupRecurringWork()
        }
    }

    private fun setupRecurringWork() {
        // Defining constraints (It is connected to WIFI, no data, battery and is charging)
        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED) // The user won't use their mobile data
                .setRequiresBatteryNotLow(true)
                .setRequiresCharging(true)
                .apply {
                    // For users of Android Marshmallow or above we could request that the device is idle
                    // meaning the user is not actively using the device
                    // and won't have their usage impacted by our background processing
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        setRequiresDeviceIdle(true)
                    }
                }.build()


        // Making a PeriodWorkRequest
        val repeatingRequest = PeriodicWorkRequestBuilder<RefreshDataWorker>(1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()



        // Scheduling the work as unique and periodically executed
        WorkManager.getInstance().enqueueUniquePeriodicWork(
                RefreshDataWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                repeatingRequest)
    }

    /**
     * onCreate is called before the first screen is shown to the user.
     *
     * Use it to setup any background tasks, running expensive setup operations in a background
     * thread to avoid delaying app start.
     */
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        delayedInit()

    }
}
