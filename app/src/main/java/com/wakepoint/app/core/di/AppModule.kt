package com.wakepoint.app.core.di

import android.content.Context
import androidx.room.Room
import com.wakepoint.app.core.data.local.WakepointDatabase
import com.wakepoint.app.core.notification.AlarmNotificationManager
import com.wakepoint.app.core.supabase.SupabaseConfig
import com.wakepoint.app.data.alarm.AlarmRepository
import com.wakepoint.app.data.alarm.DefaultAlarmRepository
import com.wakepoint.app.data.alarm.local.AlarmDao
import com.wakepoint.app.data.auth.AuthRepository
import com.wakepoint.app.data.auth.DefaultAuthRepository
import com.wakepoint.app.data.friend.DefaultFriendRepository
import com.wakepoint.app.data.friend.FriendRepository
import com.wakepoint.app.data.friend.local.FriendDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideWakepointDatabase(
        @ApplicationContext context: Context
    ): WakepointDatabase = Room.databaseBuilder(
        context,
        WakepointDatabase::class.java,
        "wakepoint.db"
    )
        .addMigrations(WakepointDatabase.MIGRATION_1_2)
        .addMigrations(WakepointDatabase.MIGRATION_2_3)
        .addMigrations(WakepointDatabase.MIGRATION_3_4)
        .build()

    @Provides
    fun provideAlarmDao(database: WakepointDatabase): AlarmDao = database.alarmDao()

    @Provides
    fun provideFriendDao(database: WakepointDatabase): FriendDao = database.friendDao()

    @Provides
    @Singleton
    fun provideAlarmRepository(repository: DefaultAlarmRepository): AlarmRepository = repository

    @Provides
    @Singleton
    fun provideFriendRepository(repository: DefaultFriendRepository): FriendRepository = repository

    @Provides
    @Singleton
    fun provideAuthRepository(repository: DefaultAuthRepository): AuthRepository = repository

    @Provides
    @Singleton
    fun provideSupabaseConfig(): SupabaseConfig = SupabaseConfig()

    @Provides
    @Singleton
    fun provideAlarmNotificationManager(
        @ApplicationContext context: Context
    ): AlarmNotificationManager = AlarmNotificationManager(context)
}
