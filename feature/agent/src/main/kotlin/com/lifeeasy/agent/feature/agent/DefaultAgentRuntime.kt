// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.feature.agent

import com.lifeeasy.agent.feature.agent.classification.IntentClassifierNode
import com.lifeeasy.agent.feature.agent.nodes.*
import com.lifeeasy.agent.feature.agent.planning.PlannerNode
import com.lifeeasy.agent.feature.agent.runtime.AgentNodeRegistry
import com.lifeeasy.agent.feature.agent.runtime.SupervisorAgent
import com.lifeeasy.agent.feature.agent.runtime.WatchdogAgent

/** Creates the shipped deterministic runtime. Replace/inject adapters in later stages as capabilities are added. */
object DefaultAgentRuntime {
    fun createOrchestrator(watchdog: WatchdogAgent = WatchdogAgent()): AgentOrchestrator {
        val registry = AgentNodeRegistry(
            listOf(
                ConversationNode(),
                SummarizerNode(),
                MeetingNotesNode(),
                TaskOrganizerNode(),
                DocumentOutlinerNode(),
                TranslatorNode(),
                ClarifierNode()
            )
        )
        return AgentOrchestrator(
            classifier = IntentClassifierNode(),
            planner = PlannerNode(),
            supervisor = SupervisorAgent(registry, watchdog)
        )
    }
}
