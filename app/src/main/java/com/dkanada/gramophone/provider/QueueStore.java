/*
 * Copyright (C) 2014 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dkanada.gramophone.provider;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dkanada.gramophone.App;
import com.dkanada.gramophone.model.Song;
import com.dkanada.gramophone.util.PreferenceUtil;

import java.util.List;

public class QueueStore {
    @Nullable
    private static QueueStore instance = null;

    private final Context context;

    private QueueStore(final Context context) {
        this.context = context;
    }

    @NonNull
    public static synchronized QueueStore getInstance(@NonNull final Context context) {
        if (instance == null) {
            instance = new QueueStore(context.getApplicationContext());
        }

        return instance;
    }

    public synchronized void saveQueues(@NonNull final List<Song> playingQueue, @NonNull final List<Song> originalPlayingQueue) {
        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(App.getInstance());

        preferenceUtil.savePlayingQueue(playingQueue);
        preferenceUtil.saveOriginalPlayingQueue(originalPlayingQueue);
    }

    @NonNull
    public List<Song> getSavedPlayingQueue() {
        return PreferenceUtil.getInstance(App.getInstance()).getPlayingQueue();
    }

    @NonNull
    public List<Song> getSavedOriginalPlayingQueue() {
        return PreferenceUtil.getInstance(App.getInstance()).getOriginalPlayingQueue();
    }
}
