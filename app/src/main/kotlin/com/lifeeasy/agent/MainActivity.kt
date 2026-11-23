// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeeasy.agent.core.designsystem.component.LifeEasyCard
import com.lifeeasy.agent.core.designsystem.component.LifeEasySectionTitle
import com.lifeeasy.agent.core.designsystem.component.LifeEasyStatusChip
import com.lifeeasy.agent.core.designsystem.theme.LifeEasyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LifeEasyTheme { StartupScreen() } }
    }
}

@Composable
private fun StartupScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            LifeEasyStatusChip(label = "FOUNDATION BUILD")
            Spacer(Modifier.height(20.dp))
            Text("LIFEEASY", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            LifeEasySectionTitle(
                title = "Your personal AI workspace",
                subtitle = "A secure foundation for your everyday assistant."
            )
            Spacer(Modifier.height(24.dp))
            LifeEasyCard {
                Text("Design System · Step 3", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    "The application entry point and shared Material 3 UI foundation are now connected.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
