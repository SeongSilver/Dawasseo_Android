package com.wakepoint.app.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wakepoint.app.data.alarm.local.AlarmDao
import com.wakepoint.app.data.alarm.local.AlarmEntity

@Database(
    entities = [AlarmEntity::class],
    version = 1,
    exportSchema = true
)
abstract class WakepointDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
}
