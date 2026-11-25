// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeeasy.agent.core.data.local.DatabaseFactory
import com.lifeeasy.agent.core.data.repository.EncryptedMemoryRepository
import com.lifeeasy.agent.core.designsystem.theme.LifeEasyTheme
import com.lifeeasy.agent.feature.agent.DefaultAgentRuntime
import com.lifeeasy.agent.feature.agent.model.AgentRequest
import com.lifeeasy.agent.feature.memory.MemoryScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

private val Ink = Color(0xFF050713)
private val Panel = Color(0xFF0D1227)
private val Panel2 = Color(0xFF111A34)
private val Violet = Color(0xFF9B5CFF)
private val Cyan = Color(0xFF55E7FF)
private val Muted = Color(0xFFA9B4D2)
private val Line = Color(0xFF27365C)

private enum class Destination(val title: String) {
    HOME("Home"), CHAT("Chat"), EXPLORE("Explore"), LIBRARY("Library"), CREATE("Create"),
    VOICE("Voice"), MEMORY("Memory"), MONITOR("Monitor"), SETTINGS("Settings")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = DatabaseFactory.get(applicationContext)
        val memoryRepository = EncryptedMemoryRepository(db.encryptedMemoryDao())
        setContent {
            LifeEasyTheme {
                LifeEasyShell(memoryRepository)
            }
        }
    }
}

@Composable
private fun LifeEasyShell(memoryRepository: EncryptedMemoryRepository) {
    var selected by remember { mutableStateOf(Destination.HOME) }
    Box(Modifier.fillMaxSize().background(Ink)) {
        StarField(Modifier.fillMaxSize())
        Row(Modifier.fillMaxSize()) {
            NavigationRail(selected = selected, onSelect = { selected = it })
            Column(Modifier.fillMaxSize()) {
                TopBar(onSettings = { selected = Destination.SETTINGS }, onSearch = { selected = Destination.EXPLORE })
                when (selected) {
                    Destination.HOME -> HomeScreen(onSelect = { selected = it })
                    Destination.CHAT -> ChatScreen()
                    Destination.EXPLORE -> ExploreScreen(onSelect = { selected = it })
                    Destination.LIBRARY -> LibraryScreen(onCreate = { selected = Destination.CREATE })
                    Destination.CREATE -> CreateScreen()
                    Destination.VOICE -> VoiceScreen()
                    Destination.MEMORY -> MemoryScreen(repository = memoryRepository)
                    Destination.MONITOR -> MonitorScreen()
                    Destination.SETTINGS -> SettingsScreen()
                }
            }
        }
    }
}

@Composable
private fun StarField(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val points = listOf(
            Offset(size.width * .08f, size.height * .10f), Offset(size.width * .92f, size.height * .14f),
            Offset(size.width * .18f, size.height * .42f), Offset(size.width * .88f, size.height * .58f),
            Offset(size.width * .10f, size.height * .78f), Offset(size.width * .75f, size.height * .91f)
        )
        points.forEachIndexed { index, p ->
            drawCircle(if (index % 2 == 0) Violet.copy(alpha = .65f) else Cyan.copy(alpha = .55f),
                radius = if (index % 2 == 0) 2.2.dp.toPx() else 1.5.dp.toPx(), center = p)
        }
        drawRect(Brush.verticalGradient(listOf(Color.Transparent, Violet.copy(alpha = .035f), Color.Transparent)))
    }
}

@Composable
private fun NavigationRail(selected: Destination, onSelect: (Destination) -> Unit) {
    val destinations = listOf(
        Destination.HOME to Icons.Outlined.Home, Destination.CHAT to Icons.Outlined.ChatBubbleOutline,
        Destination.EXPLORE to Icons.Outlined.Explore, Destination.LIBRARY to Icons.Outlined.FolderOpen,
        Destination.CREATE to Icons.Outlined.AutoAwesome, Destination.VOICE to Icons.Outlined.Mic,
        Destination.MEMORY to Icons.Outlined.Lock, Destination.MONITOR to Icons.Outlined.MonitorHeart
    )
    Column(
        Modifier.width(68.dp).fillMaxHeight().padding(start = 7.dp, top = 10.dp, bottom = 10.dp)
            .clip(RoundedCornerShape(24.dp)).background(Color(0xCC090E22))
            .border(1.dp, Violet.copy(alpha = .30f), RoundedCornerShape(24.dp)).padding(vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text("✦", color = Cyan, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 5.dp))
        destinations.forEach { (destination, icon) ->
            val active = selected == destination
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 5.dp).clip(RoundedCornerShape(15.dp))
                    .background(if (active) Brush.verticalGradient(listOf(Violet.copy(.38f), Cyan.copy(.12f))) else Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent)))
                    .clickable { onSelect(destination) }.padding(vertical = 9.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(icon, contentDescription = destination.title, tint = if (active) Color.White else Muted, modifier = Modifier.size(21.dp))
                Text(destination.title, fontSize = 9.sp, color = if (active) Color.White else Muted, maxLines = 1)
            }
        }
        Spacer(Modifier.weight(1f))
        IconButton(onClick = { onSelect(Destination.SETTINGS) }) {
            Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = if (selected == Destination.SETTINGS) Cyan else Muted)
        }
    }
}

@Composable
private fun TopBar(onSettings: () -> Unit, onSearch: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("LIFEEASY", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 3.sp)
            Text("YOUR AI COMPANION", color = Muted, fontSize = 8.sp, letterSpacing = 2.5.sp)
        }
        IconButton(onClick = onSearch) { Icon(Icons.Outlined.Search, null, tint = Color.White) }
        IconButton(onClick = onSettings) { Icon(Icons.Outlined.NotificationsNone, null, tint = Color.White) }
        Box(Modifier.size(34.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Violet, Cyan))), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Person, null, tint = Color.White)
        }
    }
}

@Composable
private fun HomeScreen(onSelect: (Destination) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Box(Modifier.fillMaxWidth().height(252.dp)) {
                HologramOrb(Modifier.align(Alignment.CenterStart).fillMaxWidth(.74f).height(245.dp))
                Column(Modifier.align(Alignment.CenterEnd).width(135.dp).padding(end = 2.dp)) {
                    Text("Hello,", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Light)
                    Text("Welcome", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold, lineHeight = 29.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("How can I help you today?", color = Muted, fontSize = 12.sp, lineHeight = 17.sp)
                    Spacer(Modifier.height(10.dp))
                    Text("✦  LIFEEASY", color = Cyan, fontSize = 10.sp, letterSpacing = 1.sp)
                }
            }
        }
        item { Text("QUICK ACCESS", color = Muted, fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold) }
        item {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                QuickCard("Get Ideas", "Creative planning", Icons.Outlined.Lightbulb, Violet) { onSelect(Destination.CHAT) }
                QuickCard("Summarize", "Text & meeting notes", Icons.Outlined.Description, Cyan) { onSelect(Destination.CHAT) }
                QuickCard("Create", "Documents & files", Icons.Outlined.Image, Violet) { onSelect(Destination.CREATE) }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Your workspace", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                Text("VIEW ALL", color = Cyan, fontSize = 9.sp, modifier = Modifier.clickable { onSelect(Destination.LIBRARY) })
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                WorkspaceRow(Icons.Outlined.Chat, "AI Conversation", "Chat with the local workflow assistant", Cyan) { onSelect(Destination.CHAT) }
                WorkspaceRow(Icons.Outlined.Folder, "Document Studio", "PDF, Word, PowerPoint and Excel tools", Violet) { onSelect(Destination.CREATE) }
                WorkspaceRow(Icons.Outlined.Lock, "Private Memory", "Encrypted details stored on this device", Cyan) { onSelect(Destination.MEMORY) }
            }
        }
        item { StatusNote("Offline workflow engine", "No cloud LLM is connected in this build. Supported local text workflows are available in Chat.") }
    }
}

@Composable
private fun HologramOrb(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val c = Offset(size.width * .48f, size.height * .46f)
        val r = size.minDimension * .34f
        drawCircle(Brush.radialGradient(listOf(Cyan.copy(.32f), Violet.copy(.18f), Color.Transparent), c, r * 1.55f), r * 1.55f, c)
        drawCircle(Brush.radialGradient(listOf(Color(0xFF182B70), Color(0xFF0A0D26), Color(0xFF030611)), c - Offset(r * .18f, r * .18f), r), r, c)
        drawCircle(Brush.sweepGradient(listOf(Violet, Cyan, Color(0xFF3549B9), Violet, Cyan)), r, c, style = Stroke(width = 3.dp.toPx()))
        drawOval(Brush.linearGradient(listOf(Violet.copy(.9f), Cyan.copy(.85f), Color.White.copy(.8f))),
            topLeft = Offset(c.x - r * .96f, c.y - r * .28f), size = Size(r * 1.92f, r * .48f), style = Stroke(width = 5.dp.toPx()))
        drawOval(Color.White.copy(.55f), topLeft = Offset(c.x - r * 1.25f, c.y + r * 1.12f), size = Size(r * 2.5f, r * .28f), style = Stroke(width = 2.dp.toPx()))
        drawCircle(Color.White.copy(.9f), 3.dp.toPx(), Offset(c.x, c.y + r * 1.18f))
        repeat(3) { i ->
            val angle = Math.toRadians((i * 120.0) - 20.0)
            val p = Offset(c.x + kotlin.math.cos(angle).toFloat() * r * 1.35f, c.y + kotlin.math.sin(angle).toFloat() * r * .78f)
            drawCircle(if (i % 2 == 0) Violet else Cyan, 5.dp.toPx(), p)
            drawCircle(Color.White.copy(.6f), 1.5.dp.toPx(), p)
        }
    }
}

@Composable
private fun QuickCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, accent: Color, onClick: () -> Unit) {
    Column(Modifier.width(150.dp).height(128.dp).clip(RoundedCornerShape(19.dp))
        .background(Brush.linearGradient(listOf(Color(0xFF151A38), Color(0xFF080D20))))
        .border(1.dp, Brush.linearGradient(listOf(accent.copy(.8f), Cyan.copy(.25f))), RoundedCornerShape(19.dp))
        .clickable(onClick = onClick).padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(24.dp))
            Spacer(Modifier.weight(1f))
            Icon(Icons.Outlined.ChevronRight, null, tint = Muted)
        }
        Column {
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Muted, fontSize = 10.sp, maxLines = 2)
        }
    }
}

@Composable
private fun WorkspaceRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, description: String, accent: Color, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Panel.copy(.94f))
        .border(1.dp, Line, RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(accent.copy(.13f)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = accent) }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) { Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold); Text(description, color = Muted, fontSize = 10.sp) }
        Icon(Icons.Outlined.ChevronRight, null, tint = Muted)
    }
}

private data class ChatMessage(val text: String, val fromUser: Boolean, val note: String? = null)

@Composable
private fun ChatScreen() {
    val scope = rememberCoroutineScope()
    val orchestrator = remember { DefaultAgentRuntime.createOrchestrator() }
    var input by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    Column(Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text("AI Conversation", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold); Text("Local workflow mode · no cloud model", color = Muted, fontSize = 10.sp) }
            Surface(color = Color(0xFF142B34), shape = CircleShape) { Text("● LOCAL", color = Cyan, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) }
        }
        if (messages.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    HologramOrb(Modifier.size(170.dp))
                    Text("What would you like to work on?", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    Text("Try a summary, meeting notes, task list or document outline.", color = Muted, fontSize = 11.sp)
                }
            }
        } else {
            LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(messages) { message ->
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = if (message.fromUser) Alignment.End else Alignment.Start) {
                        Box(Modifier.widthIn(max = 290.dp).clip(RoundedCornerShape(17.dp))
                            .background(if (message.fromUser) Brush.linearGradient(listOf(Color(0xFF39226C), Color(0xFF172C59))) else Brush.linearGradient(listOf(Panel2, Color(0xFF10182A))))
                            .border(1.dp, if (message.fromUser) Violet.copy(.5f) else Cyan.copy(.24f), RoundedCornerShape(17.dp)).padding(13.dp)) {
                            Column { Text(message.text, color = Color.White, fontSize = 12.sp, lineHeight = 18.sp); message.note?.let { Spacer(Modifier.height(7.dp)); Text(it, color = Cyan, fontSize = 9.sp) } }
                        }
                    }
                }
                if (busy) item { Text("LIFEEASY is processing locally…", color = Cyan, fontSize = 11.sp) }
            }
        }
        Row(Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(24.dp)).background(Panel2)
            .border(1.dp, Violet.copy(.55f), RoundedCornerShape(24.dp)).padding(start = 15.dp, end = 7.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(value = input, onValueChange = { input = it }, modifier = Modifier.weight(1f), textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 13.sp), cursorBrush = SolidColor(Cyan), decorationBox = { inner -> Box { if (input.isBlank()) Text("Message LIFEEASY…", color = Muted, fontSize = 13.sp); inner() } })
            IconButton(onClick = { /* microphone provider is not connected yet */ }, enabled = false) { Icon(Icons.Outlined.Mic, null, tint = Muted) }
            IconButton(enabled = input.isNotBlank() && !busy, onClick = {
                val prompt = input.trim(); input = ""; messages += ChatMessage(prompt, true); busy = true
                scope.launch {
                    val result = withContext(Dispatchers.Default) { orchestrator.run(AgentRequest(UUID.randomUUID().toString(), prompt)) }
                    messages += ChatMessage(result.outputText ?: result.message, false, "${result.intent} · ${result.status} · deterministic local workflow")
                    busy = false
                }
            }) { Icon(Icons.Outlined.ArrowUpward, null, tint = if (input.isNotBlank() && !busy) Cyan else Muted) }
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf("Summarize: ", "Meeting notes: ", "Organize tasks: ", "Document outline: ").forEach { chip ->
                AssistChip(onClick = { input = chip }, label = { Text(chip.trim(), fontSize = 9.sp) }, colors = AssistChipDefaults.assistChipColors(labelColor = Muted))
            }
        }
    }
}

@Composable
private fun ExploreScreen(onSelect: (Destination) -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Explore tools", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
        Text("Choose a workflow. Availability is clearly marked.", color = Muted, fontSize = 11.sp)
        WorkspaceRow(Icons.Outlined.Summarize, "Text summarizer", "Extractive summary from text you provide", Cyan) { onSelect(Destination.CHAT) }
        WorkspaceRow(Icons.Outlined.Groups, "Meeting organizer", "Structure discussion lines and possible action items", Violet) { onSelect(Destination.CHAT) }
        WorkspaceRow(Icons.Outlined.Translate, "Translation", "Translation engine is not connected", Cyan) { onSelect(Destination.CHAT) }
        WorkspaceRow(Icons.Outlined.Description, "Document outline", "Create a structured outline from a prompt", Violet) { onSelect(Destination.CHAT) }
        WorkspaceRow(Icons.Outlined.Mic, "Voice assistant", "Voice capture and speech output not connected", Cyan) { onSelect(Destination.VOICE) }
        StatusNote("Capability notice", "Web search, image generation and cloud AI require provider integrations and are not active in this offline build.")
    }
}

@Composable
private fun LibraryScreen(onCreate: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Library", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Your generated and saved files will appear here.", color = Muted, fontSize = 11.sp)
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Panel).border(1.dp, Line, RoundedCornerShape(20.dp)).padding(25.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.FolderOpen, null, tint = Violet, modifier = Modifier.size(42.dp))
                Spacer(Modifier.height(10.dp)); Text("Your library is empty", color = Color.White, fontWeight = FontWeight.SemiBold)
                Text("No exported files have been saved yet.", color = Muted, fontSize = 11.sp)
                Spacer(Modifier.height(14.dp)); Button(onClick = onCreate) { Text("Open Create Studio") }
            }
        }
    }
}

@Composable
private fun CreateScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Create Studio", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Document export components are present in the project. The current UI does not yet connect the builders to editable document content.", color = Muted, fontSize = 11.sp, lineHeight = 16.sp)
        WorkspaceRow(Icons.Outlined.PictureAsPdf, "PDF document", "PDF builder module · UI export flow pending", Violet) { }
        WorkspaceRow(Icons.Outlined.Description, "Word document", "DOCX builder module · UI export flow pending", Cyan) { }
        WorkspaceRow(Icons.Outlined.Slideshow, "Presentation", "PPTX builder module · UI export flow pending", Violet) { }
        WorkspaceRow(Icons.Outlined.TableChart, "Spreadsheet", "XLSX builder module · UI export flow pending", Cyan) { }
        StatusNote("Not yet wired", "These cards describe existing code modules; they do not claim that a file has been generated or saved.")
    }
}

@Composable
private fun VoiceScreen() {
    Column(Modifier.fillMaxSize().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(Modifier.size(126.dp).clip(CircleShape).background(Brush.radialGradient(listOf(Violet.copy(.55f), Panel, Ink))).border(1.dp, Cyan.copy(.45f), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Mic, null, tint = Color.White, modifier = Modifier.size(48.dp)) }
        Spacer(Modifier.height(18.dp)); Text("Voice Assistant", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
        Text("Voice capture, speech recognition and ElevenLabs output are not connected in this build.", color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
        Spacer(Modifier.height(12.dp)); StatusNote("Voice status", "Voice security/profile components exist, but no live microphone or speech provider is active.")
    }
}

@Composable
private fun MonitorScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("System Monitor", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Visible module status based on this build's connected components.", color = Muted, fontSize = 11.sp)
        StatusLine("Encrypted memory", "Connected", true)
        StatusLine("Offline workflow engine", "Connected", true)
        StatusLine("Document builders", "Code present · UI not connected", false)
        StatusLine("Cloud LLM provider", "Not configured", false)
        StatusLine("Voice / speech provider", "Not configured", false)
        StatusLine("Web search / image generation", "Not configured", false)
    }
}

@Composable
private fun SettingsScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Settings", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        StatusLine("Application", "LIFEEASY 1.0.0", true)
        StatusLine("Data storage", "Encrypted on this device", true)
        StatusLine("AI provider", "No provider configured", false)
        StatusLine("Voice provider", "No provider configured", false)
        StatusNote("Privacy", "Memory records are stored locally using the encrypted repository. Cloud processing is not active in this build.")
    }
}

@Composable
private fun StatusLine(title: String, value: String, ready: Boolean) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Panel).border(1.dp, Line, RoundedCornerShape(14.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(if (ready) Color(0xFF57DCA6) else Color(0xFFFFC56B)))
        Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold); Text(value, color = Muted, fontSize = 10.sp) }
    }
}

@Composable
private fun StatusNote(title: String, body: String) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(Color(0xFF111A32)).border(1.dp, Cyan.copy(.25f), RoundedCornerShape(15.dp)).padding(13.dp)) {
        Text(title, color = Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp)); Text(body, color = Muted, fontSize = 10.sp, lineHeight = 15.sp)
    }
}
