// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent

import android.os.Bundle
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.app.Activity
import android.os.BatteryManager
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.android.filament.utils.Manipulator
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
import java.util.Locale
import java.io.File
import com.lifeeasy.agent.feature.docs.model.DocumentRequest
import com.lifeeasy.agent.feature.docs.model.DocumentTable
import com.lifeeasy.agent.feature.docs.pdf.PdfBuilder
import com.lifeeasy.agent.feature.docs.word.DocxBuilder
import com.lifeeasy.agent.feature.docs.presentation.PptxBuilder
import com.lifeeasy.agent.feature.docs.spreadsheet.XlsxBuilder

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
        // Keep Android system bars aligned with the app's fixed dark visual system.
        window.statusBarColor = android.graphics.Color.rgb(5, 7, 19)
        window.navigationBarColor = android.graphics.Color.rgb(5, 7, 19)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
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
        Column(Modifier.fillMaxSize()) {
            TopBar(onSettings = { selected = Destination.SETTINGS }, onSearch = { selected = Destination.EXPLORE })
            Box(Modifier.weight(1f).fillMaxWidth()) {
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
            BottomNavigationBar(selected = selected, onSelect = { selected = it })
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
private fun BottomNavigationBar(selected: Destination, onSelect: (Destination) -> Unit) {
    val items = listOf(
        Destination.HOME to Icons.Outlined.Home,
        Destination.CHAT to Icons.Outlined.ChatBubbleOutline,
        Destination.EXPLORE to Icons.Outlined.Explore,
        Destination.CREATE to Icons.Outlined.AutoAwesome,
        Destination.SETTINGS to Icons.Outlined.Settings
    )
    Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 7.dp)
        .clip(RoundedCornerShape(22.dp)).background(Color(0xEE0B1024))
        .border(1.dp, Violet.copy(.28f), RoundedCornerShape(22.dp)).padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
        items.forEach { (destination, icon) ->
            val active = selected == destination || (destination == Destination.EXPLORE && selected in listOf(Destination.LIBRARY, Destination.VOICE, Destination.MEMORY, Destination.MONITOR))
            Column(Modifier.weight(1f).clip(RoundedCornerShape(15.dp)).clickable { onSelect(destination) }.padding(vertical = 5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, contentDescription = destination.title, tint = if (active) Cyan else Muted, modifier = Modifier.size(20.dp))
                Text(destination.title, color = if (active) Color.White else Muted, fontSize = 9.sp, maxLines = 1)
            }
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
            Box(Modifier.fillMaxWidth().height(292.dp).clip(RoundedCornerShape(26.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF17113B), Color(0xFF09152C), Color(0xFF080B1A))))
                .border(1.dp, Violet.copy(.28f), RoundedCornerShape(26.dp))) {
                EarthGlobeScene(Modifier.align(Alignment.Center).fillMaxWidth(.82f).height(250.dp), modelScale = 1.42f, cameraDistance = 3.15f)
                Column(Modifier.align(Alignment.TopStart).padding(17.dp)) {
                    Text("YOUR PERSONAL AI", color = Cyan, fontSize = 9.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                    Text("Ideas into action.", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                }
                Surface(Modifier.align(Alignment.BottomCenter).padding(bottom = 13.dp), color = Color(0xCC11152D), shape = CircleShape, border = androidx.compose.foundation.BorderStroke(1.dp, Cyan.copy(.35f))) {
                    Text("✦  Ask LIFEEASY anything", color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
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
        item { StatusNote("One-step AI setup", "Add one Google AI Studio API key in Settings. Chat uses Gemini; built-in Android voice and document export work without another key. Ollama is optional.") }
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
    val mainLightNode = rememberMainLightNode(engine) { intensity = 150_000.0f }
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
                isOpaque = false,
                manipulator = remember { Manipulator.Builder().build(Manipulator.Mode.ORBIT) }
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
    val chatSpeechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
        if (spoken.isNotBlank()) input = listOf(input, spoken).filter { it.isNotBlank() }.joinToString(" ")
    }
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
            IconButton(onClick = {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to LIFEEASY")
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }
                runCatching { chatSpeechLauncher.launch(intent) }.onFailure { messages += ChatMessage("Speech recognition unavailable: ${it.message}", false, "Voice input") }
            }) { Icon(Icons.Outlined.Mic, null, tint = Cyan) }
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
        WorkspaceRow(Icons.Outlined.Mic, "Voice assistant", "Device speech recognition and no-key TTS", Cyan) { onSelect(Destination.VOICE) }
        WorkspaceRow(Icons.Outlined.FolderOpen, "Library", "Browse your saved work", Violet) { onSelect(Destination.LIBRARY) }
        WorkspaceRow(Icons.Outlined.Lock, "Private memory", "Encrypted notes and context", Cyan) { onSelect(Destination.MEMORY) }
        WorkspaceRow(Icons.Outlined.MonitorHeart, "System monitor", "Device and app diagnostics", Violet) { onSelect(Destination.MONITOR) }
        StatusNote("Capability notice", "Gemini chat uses one Google AI Studio API key. On-device voice and file export do not require another cloud key. Web search and image generation are separate integrations.")
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

private enum class ExportFormat(val label: String, val extension: String, val mime: String) {
    PDF("PDF", "pdf", "application/pdf"),
    WORD("Word", "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    POWERPOINT("PowerPoint", "pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"),
    EXCEL("Excel", "xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
}

@Composable
private fun CreateScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("My LIFEEASY Document") }
    var body by remember { mutableStateOf("") }
    var format by remember { mutableStateOf(ExportFormat.PDF) }
    var status by remember { mutableStateOf("Write your content, choose a format, then export the real file to your device.") }
    var busy by remember { mutableStateOf(false) }
    var pendingFile by remember { mutableStateOf<File?>(null) }

    val saveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri = result.data?.data
        val source = pendingFile
        if (result.resultCode == Activity.RESULT_OK && uri != null && source?.isFile == true) {
            busy = true
            scope.launch {
                val copied = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openOutputStream(uri, "w")?.use { output ->
                            source.inputStream().use { input -> input.copyTo(output) }
                        } ?: error("Android did not provide a destination stream")
                    }
                }
                status = copied.fold({ "Export saved successfully. You can open it from your device's file manager." }, { "Save failed: ${it.message}" })
                busy = false
            }
        } else {
            status = "Export cancelled. No file was saved."
        }
    }

    fun exportDocument() {
        if (title.isBlank() || body.isBlank()) {
            status = "Document title and content are required."
            return
        }
        if (busy) return
        busy = true
        status = "Preparing ${format.label} file…"
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val folder = File(context.cacheDir, "lifeasy_exports").apply { if (!exists() && !mkdirs()) error("Cannot create export folder") }
                    val safeName = title.trim().replace(Regex("[^A-Za-z0-9._-]+"), "_").trim('_').take(70).ifBlank { "LIFEEASY_Document" }
                    val destination = File(folder, "$safeName.${format.extension}")
                    val lines = body.replace("\r\n", "\n").split('\n').map { it.trimEnd() }.filter { it.isNotBlank() }
                    val table = if (format == ExportFormat.EXCEL) DocumentTable(listOf("Content"), lines.map { listOf(it) }) else null
                    val request = DocumentRequest(title = title.trim(), paragraphs = lines, table = table).validated()
                    val buildResult = when (format) {
                        ExportFormat.PDF -> PdfBuilder().build(request, destination)
                        ExportFormat.WORD -> DocxBuilder().build(request, destination)
                        ExportFormat.POWERPOINT -> PptxBuilder().build(request, destination)
                        ExportFormat.EXCEL -> XlsxBuilder().build(request, destination)
                    }
                    // Capture the nullable cross-module property once. Kotlin cannot
                    // smart-cast a public API property declared in another module.
                    val generatedFile = buildResult.file
                        ?: error(buildResult.error ?: "Builder did not produce a file")
                    if (!buildResult.success || !generatedFile.isFile || generatedFile.length() <= 0L) {
                        error(buildResult.error ?: "Builder did not produce a valid file")
                    }
                    generatedFile
                }
            }
            result.onSuccess { file ->
                pendingFile = file
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = format.mime
                    putExtra(Intent.EXTRA_TITLE, file.name)
                }
                busy = false
                status = "${format.label} file generated. Choose a folder in the Android save dialog."
                saveLauncher.launch(intent)
            }.onFailure { error ->
                status = "Export failed: ${error.message ?: error.javaClass.simpleName}"
                busy = false
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Create Studio", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Create a real file from your own content. Files are generated on-device; no cloud provider is required.", color = Muted, fontSize = 11.sp, lineHeight = 16.sp)
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Document title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("Content") }, placeholder = { Text("Write or paste your document content here. Each paragraph or line is included in the export.") }, modifier = Modifier.fillMaxWidth().heightIn(min = 170.dp), minLines = 7)
        Text("EXPORT FORMAT", color = Cyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ExportFormat.entries.forEach { item ->
                FilterChip(selected = format == item, onClick = { format = item }, label = { Text(item.label, fontSize = 10.sp) })
            }
        }
        Button(onClick = { exportDocument() }, enabled = !busy && title.isNotBlank() && body.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
            if (busy) CircularProgressIndicator(Modifier.size(17.dp), strokeWidth = 2.dp) else Icon(Icons.Outlined.FileDownload, null)
            Spacer(Modifier.width(8.dp)); Text(if (busy) "Preparing…" else "Generate & Save ${format.label}")
        }
        StatusNote("Export status", status)
        StatusNote("Current scope", "PDF, DOCX, PPTX and XLSX export are connected to the existing builders. This screen exports text content; advanced page layouts, embedded images, formulas, slide themes and print approval are separate enhancements.")
    }
}

@Composable
private fun VoiceScreen() {
    val context = LocalContext.current
    var spokenText by remember { mutableStateOf("") }
    var recognizedText by remember { mutableStateOf("") }
    var voiceStatus by remember { mutableStateOf("Preparing the device's built-in voice engine…") }
    var ttsReady by remember { mutableStateOf(false) }

    val tts = remember(context) {
        TextToSpeech(context) { result ->
            if (result == TextToSpeech.SUCCESS) {
                voiceStatus = "Android system speech engine initialized."
                ttsReady = true
            } else {
                voiceStatus = "No Android text-to-speech engine is available on this device."
                ttsReady = false
            }
        }
    }
    DisposableEffect(tts) {
        onDispose { tts.stop(); tts.shutdown() }
    }

    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        recognizedText = matches?.firstOrNull().orEmpty()
        if (recognizedText.isNotBlank()) {
            spokenText = recognizedText
            voiceStatus = "Speech recognized by the Android speech-recognition service."
        } else {
            voiceStatus = "No speech result returned. Check microphone permission and the installed recognition service."
        }
    }

    Column(Modifier.fillMaxSize().padding(18.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(104.dp).clip(CircleShape).background(Brush.radialGradient(listOf(Violet.copy(.55f), Panel, Ink))).border(1.dp, Cyan.copy(.45f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Mic, null, tint = Color.White, modifier = Modifier.size(42.dp))
        }
        Text("Voice Assistant", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
        Text("Automatic no-key voice mode uses the speech engine installed on this Android phone. ElevenLabs cloud voices require an ElevenLabs API key; this screen does not pretend otherwise.", color = Muted, fontSize = 11.sp, lineHeight = 16.sp)
        OutlinedTextField(value = spokenText, onValueChange = { spokenText = it }, label = { Text("Text to speak") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                val bn = tts.isLanguageAvailable(Locale("bn", "BD")) >= TextToSpeech.LANG_AVAILABLE
                val selectedLocale = if (bn) Locale("bn", "BD") else Locale.US
                val languageResult = tts.setLanguage(selectedLocale)
                voiceStatus = if (languageResult >= TextToSpeech.LANG_AVAILABLE) {
                    tts.speak(spokenText, TextToSpeech.QUEUE_FLUSH, null, "lifeeasy-voice-${System.currentTimeMillis()}")
                    "Speaking with ${if (bn) "Bangla" else "the device default English"} system voice."
                } else "The selected language voice is not installed. Install a TTS voice in Android settings."
            }, enabled = ttsReady && spokenText.isNotBlank(), modifier = Modifier.weight(1f)) {
                Icon(Icons.Outlined.VolumeUp, null); Spacer(Modifier.width(6.dp)); Text("Speak")
            }
            OutlinedButton(onClick = {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now")
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }
                runCatching { speechLauncher.launch(intent) }.onFailure {
                    voiceStatus = "Speech recognition is unavailable: ${it.message ?: it.javaClass.simpleName}"
                }
            }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Outlined.Mic, null); Spacer(Modifier.width(6.dp)); Text("Listen")
            }
        }
        if (recognizedText.isNotBlank()) {
            StatusNote("Recognized speech", recognizedText)
        }
        StatusNote("Voice engine", voiceStatus)
        StatusNote("Privacy and capability", "Text-to-speech runs through the Android TTS engine. Speech recognition is provided by an installed Android recognition service and may require internet, depending on the phone. No ElevenLabs API call is made in this no-key mode.")
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
        StatusLine("Document export", "PDF / DOCX / PPTX / XLSX builders connected", true)
        StatusLine("AI chat", if (AiProviderStore(context).load().isConfigured) "API key and model configured" else "API key not configured · open Settings", AiProviderStore(context).load().isConfigured)
        StatusLine("Voice", "Android system speech engine · no cloud key", true)
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
    val context = LocalContext.current
    var config by remember { mutableStateOf(providerStore.load()) }
    var status by remember { mutableStateOf(if (config.apiKey.isBlank()) "One-time setup: paste your Google AI Studio API key and save." else "Gemini settings loaded. Your key is stored encrypted on this phone.") }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val gemini = ProviderCatalog.providers.first { it.name == "Google Gemini" }
    fun openKeyPage() {
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(gemini.keyUrl))) }
            .onFailure { status = "Could not open browser: ${it.message}" }
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Your AI setup", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Text("Connect once. LIFEEASY will use Gemini for chat. No Ollama installation or PC setup is required.", color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Brush.linearGradient(listOf(Color(0xFF211542), Color(0xFF0B1830)))).border(1.dp, Violet.copy(.5f), RoundedCornerShape(18.dp)).padding(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.AutoAwesome, null, tint = Cyan); Spacer(Modifier.width(8.dp)); Text("GOOGLE GEMINI", color = Color.White, fontWeight = FontWeight.Bold) }
                Text("Default model: gemini-3.8-flash", color = Cyan, fontSize = 12.sp)
                Text("A Google AI Studio API key is required. It is not the same as your Google account password.", color = Muted, fontSize = 11.sp, lineHeight = 16.sp)
                TextButton(onClick = { openKeyPage() }) { Text("Get a free-tier API key  ↗", color = Cyan) }
            }
        }
        OutlinedTextField(value = config.apiKey, onValueChange = { config = config.copy(apiKey = it.trim(), providerName = "Google Gemini", baseUrl = gemini.baseUrl, model = "gemini-3.8-flash") }, label = { Text("Paste Gemini API key") }, placeholder = { Text("AIza…") }, modifier = Modifier.fillMaxWidth(), singleLine = true, visualTransformation = PasswordVisualTransformation())
        Button(onClick = {
            runCatching {
                require(config.apiKey.isNotBlank()) { "Paste your Google AI Studio API key first." }
                providerStore.save(config.copy(providerName = "Google Gemini", baseUrl = gemini.baseUrl, model = "gemini-3.8-flash"))
                config = providerStore.load()
                status = "Saved securely. Return to Chat and send a message to test the connection."
            }.onFailure { status = "Setup failed: ${it.message}" }
        }, enabled = config.apiKey.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Lock, null); Spacer(Modifier.width(8.dp)); Text("Save & connect Gemini") }
        OutlinedButton(onClick = {
            scope.launch {
                busy = true
                status = "Checking Gemini API key…"
                val check = runCatching { OpenAiCompatibleClient().listModels(config.copy(providerName = "Google Gemini", baseUrl = gemini.baseUrl)) }
                status = check.fold({ models -> if (models.isNotEmpty()) "Connection check passed. ${models.size} model IDs returned by Google." else "Google returned no model IDs. Try sending a chat message; model discovery may be unavailable for this key." }, { "Connection check failed: ${it.message}" })
                busy = false
            }
        }, enabled = config.apiKey.isNotBlank() && !busy, modifier = Modifier.fillMaxWidth()) { if (busy) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp) else Icon(Icons.Outlined.WifiTethering, null); Spacer(Modifier.width(8.dp)); Text(if (busy) "Checking…" else "Check API key") }
        StatusNote("Setup status", status)
        StatusLine("Voice", "Built-in Android voice · no API key", true)
        StatusLine("Document export", "PDF, Word, PowerPoint and Excel · offline", true)
        StatusLine("Local Ollama", "Optional only · not needed to use LIFEEASY", true)
        StatusNote("About 503 errors", "If Google is temporarily overloaded, LIFEEASY retries automatically. If the service remains unavailable, wait and retry; the app cannot control Google's capacity.")
        StatusNote("Privacy", "The API key is encrypted with Android Keystore on this device. Do not paste it into GitHub, screenshots or public messages.")
        OutlinedButton(onClick = { providerStore.clear(); config = providerStore.load(); status = "API key cleared from this device." }, modifier = Modifier.fillMaxWidth()) { Text("Remove saved API key") }
    }
}

@Composable
private fun ProviderKeyRow(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, accent: Color, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Panel).border(1.dp, Line, RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = accent)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) { Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold); Text(subtitle, color = Muted, fontSize = 10.sp) }
        Icon(Icons.Outlined.Launch, null, tint = accent)
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
