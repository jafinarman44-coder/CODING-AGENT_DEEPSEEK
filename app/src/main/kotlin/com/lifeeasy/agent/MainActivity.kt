// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent

import android.os.Bundle
import android.app.ActivityManager
import android.content.Context
import android.os.BatteryManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeeasy.agent.core.data.local.DatabaseFactory
import com.lifeeasy.agent.core.data.repository.EncryptedMemoryRepository
import com.lifeeasy.agent.core.designsystem.theme.LifeEasyTheme
import com.lifeeasy.agent.feature.agent.DefaultAgentRuntime
import com.lifeeasy.agent.feature.agent.model.AgentRequest
import com.lifeeasy.agent.feature.memory.MemoryScreen
import com.lifeeasy.agent.feature.monitor.MonitorEventStore
import io.github.sceneview.Scene
import io.github.sceneview.SurfaceType
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironment
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
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
    val context = LocalContext.current
    val providerStore = remember(context) { AiProviderStore(context) }
    Box(Modifier.fillMaxSize().background(Ink)) {
        StarField(Modifier.fillMaxSize())
        Row(Modifier.fillMaxSize()) {
            NavigationRail(selected = selected, onSelect = { selected = it })
            Column(Modifier.fillMaxSize()) {
                TopBar(onSettings = { selected = Destination.SETTINGS }, onSearch = { selected = Destination.EXPLORE })
                when (selected) {
                    Destination.HOME -> HomeScreen(onSelect = { selected = it })
                    Destination.CHAT -> ChatScreen(providerStore)
                    Destination.EXPLORE -> ExploreScreen(onSelect = { selected = it })
                    Destination.LIBRARY -> LibraryScreen(onCreate = { selected = Destination.CREATE })
                    Destination.CREATE -> CreateScreen()
                    Destination.VOICE -> VoiceScreen()
                    Destination.MEMORY -> MemoryScreen(repository = memoryRepository)
                    Destination.MONITOR -> MonitorScreen()
                    Destination.SETTINGS -> SettingsScreen(providerStore)
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
                EarthGlobeScene(Modifier.align(Alignment.CenterStart).fillMaxWidth(.54f).height(220.dp), modelScale = 1.35f, cameraDistance = 3.25f)
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

/**
 * Renders the user's supplied animated GLB with Filament/SceneView.
 * The model is bundled locally; no network request is needed at runtime.
 */
@Composable
private fun EarthGlobeScene(modifier: Modifier = Modifier, modelScale: Float = 1.2f, cameraDistance: Float = 3.0f) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val environmentLoader = rememberEnvironmentLoader(engine)
    // IBL is required for the supplied PBR materials; TextureSurface is required to composite
    // the 3D viewport inline with Compose instead of punching a black SurfaceView hole.
    val environment = rememberEnvironment(environmentLoader, isOpaque = false)
    val cameraNode = rememberCameraNode(engine) {
        position = Position(z = cameraDistance)
        lookAt(Position(0f, 0f, 0f))
    }
    val mainLightNode = rememberMainLightNode(engine) { intensity = 55_000.0f }
    val modelLoadResult = remember(modelLoader) {
        runCatching { modelLoader.createModelInstance("models/earth_globe_sci-fi_look.glb") }
    }
    val modelInstance = modelLoadResult.getOrNull()
    val globeNodes = rememberNodes {
        modelInstance?.let { instance ->
            add(ModelNode(modelInstance = instance, scaleToUnits = modelScale, autoAnimate = true))
        }
    }
    Box(modifier = modifier.background(Color.Transparent), contentAlignment = Alignment.Center) {
        if (modelInstance != null && globeNodes.isNotEmpty()) {
            Scene(
                modifier = Modifier.fillMaxSize(),
                surfaceType = SurfaceType.TextureSurface,
                engine = engine,
                modelLoader = modelLoader,
                environmentLoader = environmentLoader,
                environment = environment,
                cameraNode = cameraNode,
                mainLightNode = mainLightNode,
                childNodes = globeNodes,
                isOpaque = false
            )
        } else {
            val reason = modelLoadResult.exceptionOrNull()?.message?.take(90) ?: "GLB is loading or failed to load"
            Text("3D Earth unavailable: $reason", color = Muted, fontSize = 9.sp)
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
private fun ChatScreen(providerStore: AiProviderStore) {
    val scope = rememberCoroutineScope()
    val providerClient = remember { OpenAiCompatibleClient() }
    var providerConfig by remember { mutableStateOf(providerStore.load()) }
    val orchestrator = remember { DefaultAgentRuntime.createOrchestrator() }
    var input by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    Column(Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text("AI Conversation", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold); Text(if (providerConfig.isConfigured) "${providerConfig.providerName} · ${providerConfig.model}" else "No language model connected · configure in Settings", color = Muted, fontSize = 10.sp) }
            Surface(color = Color(0xFF142B34), shape = CircleShape) { Text(if (providerConfig.isConfigured) "● AI READY" else "● OFFLINE", color = Cyan, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) }
        }
        if (messages.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    EarthGlobeScene(Modifier.size(170.dp), modelScale = 1.0f, cameraDistance = 2.8f)
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
                    runCatching {
                        if (providerConfig.isConfigured) {
                            val history = messages.map { message -> (if (message.fromUser) "user" else "assistant") to message.text }
                            providerClient.reply(providerConfig, history)
                        } else {
                            // Keep the supported offline task router, but never present its echo as generative AI.
                            val local = withContext(Dispatchers.Default) {
                                orchestrator.run(AgentRequest(UUID.randomUUID().toString(), prompt))
                            }
                            if (local.intent.name == "CONVERSATION") {
                                throw IllegalStateException("No AI model is connected. Open Settings → AI Provider and configure an OpenAI-compatible API or Ollama endpoint. Offline workflow tasks remain available.")
                            }
                            local.outputText ?: local.message
                        }
                    }.onSuccess { answer ->
                        messages += ChatMessage(answer, false, if (providerConfig.isConfigured) "${providerConfig.providerName} · ${providerConfig.model}" else "Offline task workflow · not an LLM response")
                        MonitorEventStore.record("Chat response completed", if (providerConfig.isConfigured) providerConfig.model else "offline workflow", true)
                    }.onFailure { error ->
                        messages += ChatMessage(error.message ?: error.javaClass.simpleName, false, "Provider/workflow error")
                        MonitorEventStore.record("Chat request failed", error.message ?: error.javaClass.simpleName, false)
                    }
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
    val context = LocalContext.current
    var refresh by remember { mutableIntStateOf(0) }
    val activityManager = remember(context) { context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager }
    val memoryInfo = remember(refresh) { ActivityManager.MemoryInfo().also(activityManager::getMemoryInfo) }
    val runtime = Runtime.getRuntime()
    val batteryManager = remember(context) { context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager }
    val battery = remember(refresh) { batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).takeIf { it in 0..100 } }
    val usedHeapMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
    val maxHeapMb = runtime.maxMemory() / (1024 * 1024)
    val availableRamMb = memoryInfo.availMem / (1024 * 1024)
    val totalRamMb = memoryInfo.totalMem / (1024 * 1024)
    val appVersion = remember(context) {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: "unknown"
    }
    val events by MonitorEventStore.events.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(11.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("System Monitor", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Live values from this Android device and recent agent runs", color = Muted, fontSize = 10.sp)
            }
            TextButton(onClick = { refresh++ }) { Icon(Icons.Outlined.Refresh, null, tint = Cyan); Text("Refresh", color = Cyan) }
        }
        StatusLine("Application", "LIFEEASY $appVersion · Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})", true)
        StatusLine("Device RAM available", "$availableRamMb MB / $totalRamMb MB", availableRamMb > 256)
        StatusLine("App heap", "$usedHeapMb MB used / $maxHeapMb MB max", usedHeapMb < maxHeapMb * 0.9)
        StatusLine("Battery", battery?.let { "$it%" } ?: "Unavailable on this device", (battery ?: 0) > 10)
        StatusLine("Encrypted memory", "Repository connected", true)
        StatusLine("Offline workflow engine", "Connected · ${events.size} recent run(s)", true)
        StatusLine("Document export UI", "Not connected to editable export workflow", false)
        StatusLine("Cloud LLM / Voice / Web search", "Not configured in this build", false)
        Text("RECENT AGENT ACTIVITY", color = Muted, fontSize = 10.sp, letterSpacing = 1.6.sp, fontWeight = FontWeight.Bold)
        if (events.isEmpty()) {
            StatusNote("No agent runs recorded", "Send a message from Chat. Successes and runtime failures will appear here.")
        } else {
            events.forEach { event ->
                StatusLine(event.title, "${event.detail} · ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(event.timestamp))}", event.success)
            }
        }
    }
}

@Composable
private fun SettingsScreen(providerStore: AiProviderStore) {
    var config by remember { mutableStateOf(providerStore.load()) }
    var status by remember { mutableStateOf("API key is encrypted with Android Keystore when saved. Use HTTPS for cloud endpoints.") }
    var saved by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Settings", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        StatusLine("Application", "LIFEEASY 1.0.0", true)
        StatusLine("Data storage", "Encrypted on this device", true)
        Text("AI PROVIDER", color = Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
        OutlinedTextField(value = config.providerName, onValueChange = { config = config.copy(providerName = it) }, label = { Text("Provider name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = config.baseUrl, onValueChange = { config = config.copy(baseUrl = it) }, label = { Text("API base URL") }, modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("https://api.openai.com/v1") })
        OutlinedTextField(value = config.model, onValueChange = { config = config.copy(model = it) }, label = { Text("Model ID") }, modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("gpt-4o-mini or llama3.1") })
        OutlinedTextField(value = config.apiKey, onValueChange = { config = config.copy(apiKey = it) }, label = { Text("API key (leave blank for local Ollama)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, visualTransformation = PasswordVisualTransformation())
        Button(onClick = {
            runCatching { providerStore.save(config); config = providerStore.load(); saved = true; status = "Provider settings saved securely. Return to Chat to send a real model request." }
                .onFailure { saved = false; status = "Could not save: ${it.message}" }
        }, modifier = Modifier.fillMaxWidth()) { Text("Save AI Provider") }
        OutlinedButton(onClick = { providerStore.clear(); config = providerStore.load(); saved = false; status = "Provider configuration cleared." }, modifier = Modifier.fillMaxWidth()) { Text("Disconnect provider") }
        StatusNote(if (saved) "Provider saved" else "Provider setup", status)
        StatusNote("Ollama on your phone", "For a PC-hosted Ollama server, set the base URL to http://YOUR-PC-LAN-IP:11434/v1, enable OLLAMA_HOST=0.0.0.0 on the PC, and allow port 11434 through the PC firewall. Do not use localhost on the phone.")
        StatusNote("Privacy", "API keys are encrypted at rest with Android Keystore AES-GCM. Requests are sent to the endpoint you configure. Do not enter a key unless you trust that provider.")
        StatusLine("Voice provider", "Not configured", false)
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
