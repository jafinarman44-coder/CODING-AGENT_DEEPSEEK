// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.lifeeasy.agent.core.data.local.DatabaseFactory
import com.lifeeasy.agent.core.data.repository.EncryptedMemoryRepository
import com.lifeeasy.agent.core.designsystem.theme.LifeEasyTheme
import com.lifeeasy.agent.feature.memory.MemoryScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = DatabaseFactory.get(applicationContext)
        val memoryRepository = EncryptedMemoryRepository(database.encryptedMemoryDao())
        setContent {
            LifeEasyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground
                ) {
                    MemoryScreen(repository = memoryRepository)
                }
            }
        }
    }
}
