package com.wakepoint.app.data.sound

import android.net.Uri

interface SoundStorageRepository {
    /**
     * Uploads a local sound file to Supabase Storage bucket `alarm-sounds` under the current user's folder.
     * Returns the object path on success. The bucket must already exist; the upload is best-effort and
     * playback always uses the local URI.
     */
    suspend fun uploadSound(localUri: Uri, fileName: String): Result<String>
}
