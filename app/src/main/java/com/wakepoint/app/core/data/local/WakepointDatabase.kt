package com.wakepoint.app.core.data.local

import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.wakepoint.app.data.alarm.local.AlarmDao
import com.wakepoint.app.data.alarm.local.AlarmEntity
import com.wakepoint.app.data.friend.local.FriendDao
import com.wakepoint.app.data.friend.local.FriendEntity

@Database(
    entities = [AlarmEntity::class, FriendEntity::class],
    version = 7,
    exportSchema = true
)
abstract class WakepointDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun friendDao(): FriendDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS friends (
                        id TEXT NOT NULL PRIMARY KEY,
                        user_id TEXT NOT NULL,
                        friend_id TEXT NOT NULL,
                        nickname TEXT NOT NULL,
                        email TEXT NOT NULL,
                        avatar_url TEXT,
                        status TEXT NOT NULL,
                        permission_status TEXT NOT NULL,
                        created_at TEXT
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE friends ADD COLUMN permission_expires_at TEXT")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE alarms_new (
                        id TEXT NOT NULL PRIMARY KEY,
                        owner_id TEXT NOT NULL,
                        created_by TEXT NOT NULL,
                        label TEXT NOT NULL,
                        target_lat REAL NOT NULL,
                        target_lng REAL NOT NULL,
                        target_address TEXT NOT NULL,
                        radius_km REAL NOT NULL,
                        is_active INTEGER NOT NULL,
                        sound_type TEXT NOT NULL,
                        sound_uri TEXT
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO alarms_new (
                        id,
                        owner_id,
                        created_by,
                        label,
                        target_lat,
                        target_lng,
                        target_address,
                        radius_km,
                        is_active,
                        sound_type,
                        sound_uri
                    )
                    SELECT
                        id,
                        owner_id,
                        created_by,
                        label,
                        target_lat,
                        target_lng,
                        target_address,
                        radius_km,
                        is_active,
                        sound_type,
                        sound_uri
                    FROM alarms
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE alarms")
                db.execSQL("ALTER TABLE alarms_new RENAME TO alarms")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE alarms ADD COLUMN vibrate INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE alarms ADD COLUMN is_favorite INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE alarms ADD COLUMN sound_enabled INTEGER NOT NULL DEFAULT 1")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE alarms ADD COLUMN pending_sync INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
