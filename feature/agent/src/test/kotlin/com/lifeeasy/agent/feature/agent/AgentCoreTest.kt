// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.feature.agent

import com.lifeeasy.agent.feature.agent.classification.IntentClassifierNode
import com.lifeeasy.agent.feature.agent.model.*
import com.lifeeasy.agent.feature.agent.nodes.*
import com.lifeeasy.agent.feature.agent.planning.PlannerNode
import com.lifeeasy.agent.feature.agent.runtime.*
import org.junit.Assert.*
import org.junit.Test

class AgentCoreTest {
    private fun registry() = AgentNodeRegistry(listOf(
        ConversationNode(), SummarizerNode(), MeetingNotesNode(), TaskOrganizerNode(),
        DocumentOutlinerNode(), TranslatorNode(), ClarifierNode()
    ))

    private fun orchestrator(watchdog: WatchdogAgent = WatchdogAgent()) = AgentOrchestrator(
        IntentClassifierNode(), PlannerNode(), SupervisorAgent(registry(), watchdog)
    )

    @Test fun bengaliMeetingRequestIsRoutedToMeetingWorkflow() {
        val result = orchestrator().run(AgentRequest("r1", "এই কথাগুলো দিয়ে মিটিং নোট তৈরি করো\nকাজটি আগামীকাল করতে হবে"))
        assertEquals(AgentIntent.MEETING_NOTES, result.intent)
        assertEquals(AgentRunStatus.COMPLETED, result.status)
        assertEquals(2, result.trace.size)
        assertTrue(result.outputText!!.contains("ACTION ITEMS"))
    }

    @Test fun summaryIsExplicitlyExtractive() {
        val result = orchestrator().run(AgentRequest("r2", "Summarize: First point. Second point. Third point."))
        assertEquals(AgentIntent.SUMMARIZE, result.intent)
        assertTrue(result.outputText!!.contains("SUMMARY (extractive)"))
        assertTrue(result.outputText!!.contains("First point."))
    }

    @Test fun externalActionWaitsForConfirmation() {
        val result = orchestrator().run(AgentRequest("r3", "Send email to the client"))
        assertEquals(AgentRunStatus.AWAITING_CONFIRMATION, result.status)
        assertTrue(result.trace.isEmpty())
    }

    @Test fun confirmationDoesNotPretendAnExternalProviderExists() {
        val result = orchestrator().run(AgentRequest("r4", "Send email to the client"), confirmationGranted = true)
        assertEquals(AgentRunStatus.REJECTED, result.status)
        assertTrue(result.message.contains("No action was taken"))
    }

    @Test fun generalConversationIsRoutedWithoutPretendingAnLlmIsConnected() {
        val result = orchestrator().run(AgentRequest("r5", "Hello, what can you do?"))
        assertEquals(AgentIntent.CONVERSATION, result.intent)
        assertEquals(AgentRunStatus.COMPLETED, result.status)
        assertTrue(result.outputText!!.contains("language-model provider is not connected"))
    }

    @Test fun watchdogStopsExecutionWhenBudgetIsExceeded() {
        var time = 0L
        val clock = ElapsedClock { time += 10L; time }
        val watchdog = WatchdogAgent(clock = clock, maxDurationMillis = 15L)
        val result = orchestrator(watchdog).run(AgentRequest("r6", "meeting notes: topic one\nwill follow up"))
        assertEquals(AgentRunStatus.TIMED_OUT, result.status)
        assertTrue(result.message.contains("budget exceeded"))
    }

    @Test fun nodeFailureStopsRemainingSteps() {
        val broken = object : AgentNode {
            override val id = AgentNodeId.MEETING_EXTRACTOR
            override fun execute(context: NodeExecutionContext): NodeResult = error("controlled test failure")
        }
        val nodes = AgentNodeRegistry(listOf(broken, TaskOrganizerNode()))
        val supervisor = SupervisorAgent(nodes)
        val request = AgentRequest("r7", "meeting")
        val plan = AgentPlan("r7", AgentIntent.MEETING_NOTES, AgentRisk.LOW,
            listOf(PlannedStep(AgentNodeId.MEETING_EXTRACTOR, "extract"), PlannedStep(AgentNodeId.TASK_ORGANIZER, "organize")),
            false, "test")
        val result = supervisor.execute(request, plan, false)
        assertEquals(AgentRunStatus.FAILED, result.status)
        assertEquals(1, result.trace.size)
        assertFalse(result.trace.single().succeeded)
    }

    @Test fun supervisorRejectsPlanForDifferentRequest() {
        val request = AgentRequest("r8", "hello")
        val plan = PlannerNode().createPlan(AgentRequest("other", "hello"), AgentIntent.CONVERSATION)
        val result = SupervisorAgent(registry()).execute(request, plan, false)
        assertEquals(AgentRunStatus.REJECTED, result.status)
    }
}
