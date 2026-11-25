// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.feature.agent.model

/** User-originated task. The raw request is retained only for this execution. */
data class AgentRequest(
    val id: String,
    val text: String,
    val localeTag: String = "und",
    val createdAtEpochMillis: Long = System.currentTimeMillis()
) {
    init {
        require(id.isNotBlank()) { "Request id must not be blank" }
        require(text.isNotBlank()) { "Request text must not be blank" }
    }
}

enum class AgentIntent {
    CONVERSATION,
    SUMMARIZE,
    MEETING_NOTES,
    ORGANIZE_TASKS,
    DOCUMENT_OUTLINE,
    TRANSLATION,
    EXTERNAL_ACTION,
    UNKNOWN
}

enum class AgentRisk { LOW, ELEVATED, HIGH }

enum class AgentRunStatus {
    COMPLETED,
    AWAITING_CONFIRMATION,
    NEEDS_CLARIFICATION,
    REJECTED,
    FAILED,
    TIMED_OUT
}

enum class AgentNodeId {
    CONVERSATION,
    SUMMARIZER,
    MEETING_EXTRACTOR,
    TASK_ORGANIZER,
    DOCUMENT_OUTLINER,
    TRANSLATOR,
    CLARIFIER
}

data class PlannedStep(
    val nodeId: AgentNodeId,
    val purpose: String
)

data class AgentPlan(
    val requestId: String,
    val intent: AgentIntent,
    val risk: AgentRisk,
    val steps: List<PlannedStep>,
    val requiresConfirmation: Boolean,
    val explanation: String
)

data class NodeExecutionContext(
    val request: AgentRequest,
    val intent: AgentIntent,
    val previousOutput: String? = null
) {
    /** The first node receives the user's text; later nodes receive prior node output. */
    val inputText: String get() = previousOutput ?: request.text
}

data class NodeResult(
    val outputText: String,
    val notes: List<String> = emptyList()
)

data class AgentTraceEntry(
    val nodeId: AgentNodeId,
    val startedAtElapsedMillis: Long,
    val finishedAtElapsedMillis: Long,
    val succeeded: Boolean,
    val detail: String
)

data class AgentRunResult(
    val requestId: String,
    val status: AgentRunStatus,
    val intent: AgentIntent,
    val plan: AgentPlan?,
    val outputText: String? = null,
    val trace: List<AgentTraceEntry> = emptyList(),
    val message: String
)
