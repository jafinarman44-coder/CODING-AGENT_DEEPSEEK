// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.core.data.local

import android.content.Context
import androidx.room.Room

object DatabaseFactory {
    @Volatile private var instance: LifeEasyDatabase? = null

    fun get(context: Context): LifeEasyDatabase = instance ?: synchronized(this) {
        instance ?: Room.databaseBuilder(
            context.applicationContext,
            LifeEasyDatabase::class.java,
            DATABASE_NAME
        ).build().also { instance = it }
    }

    private const val DATABASE_NAME = "lifeeasy.db"
}
