package com.wakepoint.app.data.sound

/** Supabase Storage bucket that holds custom alarm sounds. The bucket must be created in the dashboard. */
const val ALARM_SOUND_BUCKET = "alarm-sounds"

/**
 * Builds the object path inside [ALARM_SOUND_BUCKET] for a user's sound file.
 * The file name is reduced to a safe character set so it can be used in a URL path.
 */
fun buildSoundObjectPath(userId: String, fileName: String): String {
    val safeName = sanitizeSoundFileName(fileName)
    return "${userId.trim()}/$safeName"
}

fun sanitizeSoundFileName(fileName: String): String {
    val trimmed = fileName.trim().substringAfterLast('/').substringAfterLast('\\')
    val replaced = trimmed.replace(Regex("[^A-Za-z0-9._-]"), "_")
    val collapsed = replaced
        .replace(Regex("_+"), "_")
        .replace(Regex("_+\\."), ".")
        .replace(Regex("\\._+"), ".")
        .trim('_')
    return collapsed.ifBlank { "sound" }
}
