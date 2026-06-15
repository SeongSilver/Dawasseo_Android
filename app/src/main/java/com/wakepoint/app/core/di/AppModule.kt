package com.wakepoint.app.core.di

import android.content.Context
import androidx.room.Room
import com.wakepoint.app.core.data.local.WakepointDatabase
import com.wakepoint.app.core.notification.AlarmNotificationManager
import com.wakepoint.app.data.alarm.AlarmRepository
import com.wakepoint.app.data.alarm.DefaultAlarmRepository
import com.wakepoint.app.data.alarm.local.AlarmDao
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
    ).build()

    @Provides
    fun provideAlarmDao(database: WakepointDatabase): AlarmDao = database.alarmDao()

    @Provides
    @Singleton
    fun provideAlarmRepository(repository: DefaultAlarmRepository): AlarmRepository = repository

    @Provides
    @Singleton
    fun provideAlarmNotificationManager(
        @ApplicationContext context: Context
    ): AlarmNotificationManager = AlarmNotificationManager(context)
}
