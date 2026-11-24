// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.feature.memory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lifeeasy.agent.core.domain.memory.MemoryItem
import com.lifeeasy.agent.core.domain.memory.MemoryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date
import java.util.UUID

/** Memory management UI. All persistence is delegated to the encrypted domain repository. */
@Composable
fun MemoryScreen(repository: MemoryRepository) {
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var memories by remember { mutableStateOf<List<MemoryItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    var editorVisible by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<MemoryItem?>(null) }
    var draft by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<MemoryItem?>(null) }
    var confirmClearAll by remember { mutableStateOf(false) }

    LaunchedEffect(repository) {
        try {
            repository.observeMemories().collect { result ->
                memories = result
                loading = false
                loadError = null
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            loading = false
            loadError = error.message ?: "Could not decrypt or load saved memories."
        }
    }

    val filtered = remember(memories, query) {
        if (query.isBlank()) memories else memories.filter { it.content.contains(query, ignoreCase = true) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editing = null
                draft = ""
                editorVisible = true
            }) { Icon(Icons.Outlined.Add, contentDescription = "Add memory") }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Memory", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                "Your saved details stay encrypted on this device.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    Column(Modifier.weight(1f)) {
                        Text("Encrypted storage", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text("AES-GCM via Android Keystore", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                    Text("${memories.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Search your memories") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) }
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("SAVED MEMORIES", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (memories.isNotEmpty()) TextButton(onClick = { confirmClearAll = true }) { Text("Clear all") }
            }
            when {
                loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                loadError != null -> Text("Memory unavailable: $loadError", color = MaterialTheme.colorScheme.error)
                filtered.isEmpty() -> Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (query.isBlank()) "No memories yet" else "No matching memories", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(6.dp))
                        Text(if (query.isBlank()) "Tap + to save your first memory." else "Try a different search term.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filtered, key = { it.id }) { item ->
                        MemoryCard(
                            item = item,
                            onEdit = { editing = item; draft = item.content; editorVisible = true },
                            onDelete = { deleteTarget = item }
                        )
                    }
                }
            }
        }
    }

    if (editorVisible) {
        AlertDialog(
            onDismissRequest = { editorVisible = false },
            title = { Text(if (editing == null) "Save a memory" else "Edit memory") },
            text = {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 8,
                    placeholder = { Text("Write something you want LIFEEASY to remember…") }
                )
            },
            confirmButton = {
                TextButton(enabled = draft.isNotBlank(), onClick = {
                    val id = editing?.id ?: UUID.randomUUID().toString()
                    val content = draft.trim()
                    scope.launch {
                        try {
                            repository.save(id, content)
                            editorVisible = false
                        } catch (error: Exception) {
                            snackbar.showSnackbar(error.message ?: "Could not save memory")
                        }
                    }
                }) { Text("Save encrypted") }
            },
            dismissButton = { TextButton(onClick = { editorVisible = false }) { Text("Cancel") } }
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete this memory?") },
            text = { Text("This permanently removes the selected saved memory from this device.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        try {
                            repository.delete(target.id)
                            deleteTarget = null
                        } catch (error: Exception) {
                            snackbar.showSnackbar(error.message ?: "Could not delete memory")
                        }
                    }
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel") } }
        )
    }

    if (confirmClearAll) {
        AlertDialog(
            onDismissRequest = { confirmClearAll = false },
            title = { Text("Clear all memories?") },
            text = { Text("This permanently deletes every saved memory on this device. This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        try {
                            repository.deleteAll()
                            confirmClearAll = false
                        } catch (error: Exception) {
                            snackbar.showSnackbar(error.message ?: "Could not clear memories")
                        }
                    }
                }) { Text("Delete all", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmClearAll = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun MemoryCard(item: MemoryItem, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        onClick = onEdit
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    item.content,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                IconButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, contentDescription = "Delete memory", tint = MaterialTheme.colorScheme.error) }
            }
            Text(
                "Updated ${DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(item.updatedAtEpochMillis))}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
