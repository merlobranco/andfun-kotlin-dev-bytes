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

package com.example.android.devbyteviewer.repository

import com.example.android.devbyteviewer.database.VideosDatabase
import com.example.android.devbyteviewer.network.Network
import com.example.android.devbyteviewer.network.asDatabaseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for fetching devbyte videos from the network and storing them on disk.
 */
class VideosRepository(private val database: VideosDatabase) {

    // Refreshing the offline cache
    // It's a suspend function because will be called from a co-routine
    suspend fun refreshVideos() {
        // Forcing the Kotlin co-routine to switch to the IO dispatcher
        // In this way refreshVideos() is safety called from any dispatcher, even the main thread
        withContext(Dispatchers.IO) {
            // With await we are telling the co-routine to suspend until the data is available
            val playlist = Network.devbytes.getPlaylist().await()
            // Note the asterisk * is the spread operator. It allows us to pass in an array to a function that expects varargs.
            database.videoDao.insertAll(*playlist.asDatabaseModel())
        }
    }

}