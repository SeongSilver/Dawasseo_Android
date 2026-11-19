package com.wakepoint.app.data.sound

import org.junit.Assert.assertEquals
import org.junit.Test

class SoundStoragePathTest {
    @Test
    fun buildsUserScopedPath() {
        assertEquals("user-1/clip.m4a", buildSoundObjectPath("user-1", "clip.m4a"))
    }

    @Test
    fun stripsDirectoriesAndUnsafeCharacters() {
        assertEquals("u/my_clip_1.m4a", buildSoundObjectPath("u", "/tmp/my clip (1).m4a"))
    }

    @Test
    fun fallsBackWhenNameIsEmpty() {
        assertEquals("sound", sanitizeSoundFileName("   "))
        assertEquals("sound", sanitizeSoundFileName("___"))
    }
}
