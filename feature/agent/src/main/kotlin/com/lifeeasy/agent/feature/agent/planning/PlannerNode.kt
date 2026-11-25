// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.feature.agent.planning

import com.lifeeasy.agent.feature.agent.model.*

class PlannerNode {
    fun createPlan(request: AgentRequest, intent: AgentIntent): AgentPlan {
        val (risk, confirmation, steps, explanation) = when (intent) {
            AgentIntent.CONVERSATION -> PlanParts(
                AgentRisk.LOW, false,
                listOf(PlannedStep(AgentNodeId.CONVERSATION, "Respond to the user's message")),
                "A conversational response can be prepared locally."
            )
            AgentIntent.SUMMARIZE -> PlanParts(
                AgentRisk.LOW, false,
                listOf(PlannedStep(AgentNodeId.SUMMARIZER, "Extract the main points from supplied text")),
                "Summarize only the text supplied in this request."
            )
            AgentIntent.MEETING_NOTES -> PlanParts(
                AgentRisk.LOW, false,
                listOf(
                    PlannedStep(AgentNodeId.MEETING_EXTRACTOR, "Separate topics, decisions, and discussion points"),
                    PlannedStep(AgentNodeId.TASK_ORGANIZER, "Extract owners, actions, and follow-ups where explicitly stated")
                ),
                "Structure the supplied meeting text without inventing missing speakers, dates, or decisions."
            )
            AgentIntent.ORGANIZE_TASKS -> PlanParts(
                AgentRisk.LOW, false,
                listOf(PlannedStep(AgentNodeId.TASK_ORGANIZER, "Convert supplied text into an actionable task list")),
                "Organize only tasks that can be grounded in the supplied text."
            )
            AgentIntent.DOCUMENT_OUTLINE -> PlanParts(
                AgentRisk.LOW, false,
                listOf(PlannedStep(AgentNodeId.DOCUMENT_OUTLINER, "Prepare a structured document outline")),
                "Prepare a draft outline; this step does not create or export a file."
            )
            AgentIntent.TRANSLATION -> PlanParts(
                AgentRisk.ELEVATED, false,
                listOf(PlannedStep(AgentNodeId.TRANSLATOR, "Check whether a translation engine is connected")),
                "No translation model is bundled in this step; the agent will not fabricate a translation."
            )
            AgentIntent.EXTERNAL_ACTION -> PlanParts(
                AgentRisk.HIGH, true,
                listOf(PlannedStep(AgentNodeId.CLARIFIER, "Describe the proposed external action and request explicit confirmation")),
                "External side effects must not run without explicit user confirmation and a connected action provider."
            )
            AgentIntent.UNKNOWN -> PlanParts(
                AgentRisk.LOW, false,
                listOf(PlannedStep(AgentNodeId.CLARIFIER, "Ask for a clearer task")),
                "The request could not be mapped confidently to a supported workflow."
            )
        }
        return AgentPlan(
            requestId = request.id,
            intent = intent,
            risk = risk,
            steps = steps,
            requiresConfirmation = confirmation,
            explanation = explanation
        )
    }

    private data class PlanParts(
        val risk: AgentRisk,
        val confirmation: Boolean,
        val steps: List<PlannedStep>,
        val explanation: String
    )
}
