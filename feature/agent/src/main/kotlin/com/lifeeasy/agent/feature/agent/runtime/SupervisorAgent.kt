// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.feature.agent.runtime

import com.lifeeasy.agent.feature.agent.model.*

class SupervisorAgent(
    private val registry: AgentNodeRegistry,
    private val watchdog: WatchdogAgent = WatchdogAgent()
) {
    fun execute(
        request: AgentRequest,
        plan: AgentPlan,
        confirmationGranted: Boolean
    ): AgentRunResult {
        if (plan.requestId != request.id) return result(request, plan, AgentRunStatus.REJECTED,
            "Plan/request mismatch; execution rejected.")
        if (plan.steps.isEmpty() || !watchdog.validatePlan(plan.steps.size)) return result(
            request, plan, AgentRunStatus.REJECTED, "Plan exceeds the permitted execution-step limit."
        )
        if (plan.steps.any { !registry.contains(it.nodeId) }) return result(
            request, plan, AgentRunStatus.REJECTED, "Plan references an unavailable agent node."
        )
        if ((plan.requiresConfirmation || plan.risk == AgentRisk.HIGH) && !confirmationGranted) return result(
            request, plan, AgentRunStatus.AWAITING_CONFIRMATION,
            "This request could cause an external side effect. Confirm it explicitly before proceeding."
        )
        // The current STEP 8 contains no external-action adapter. Even after confirmation,
        // the request is not executed; this prevents a confirmation from pretending a provider exists.
        if (plan.intent == AgentIntent.EXTERNAL_ACTION) return result(
            request, plan, AgentRunStatus.REJECTED,
            "Confirmation was received, but no external-action provider is connected in this build. No action was taken."
        )

        val startedAt = watchdog.start()
        val trace = mutableListOf<AgentTraceEntry>()
        var previousOutput: String? = null
        for (step in plan.steps) {
            if (!watchdog.hasTimeRemaining(startedAt)) return AgentRunResult(
                request.id, AgentRunStatus.TIMED_OUT, plan.intent, plan, previousOutput,
                trace.toList(), "Agent execution budget exceeded; remaining steps were stopped."
            )
            val node = registry.resolve(step.nodeId) ?: return AgentRunResult(
                request.id, AgentRunStatus.REJECTED, plan.intent, plan, previousOutput,
                trace.toList(), "Agent node became unavailable; execution stopped."
            )
            val nodeStarted = watchdog.mark()
            try {
                val output = node.execute(NodeExecutionContext(request, plan.intent, previousOutput))
                previousOutput = output.outputText
                trace += AgentTraceEntry(step.nodeId, nodeStarted, watchdog.mark(), true,
                    output.notes.joinToString("; ").ifBlank { "Completed" })
            } catch (error: Exception) {
                trace += AgentTraceEntry(step.nodeId, nodeStarted, watchdog.mark(), false,
                    error.message?.take(180) ?: error::class.java.simpleName)
                return AgentRunResult(
                    request.id, AgentRunStatus.FAILED, plan.intent, plan, previousOutput,
                    trace.toList(), "A workflow node failed safely. No later steps were executed."
                )
            }
        }
        val status = if (plan.intent == AgentIntent.UNKNOWN) AgentRunStatus.NEEDS_CLARIFICATION
            else AgentRunStatus.COMPLETED
        return AgentRunResult(
            request.id, status, plan.intent, plan, previousOutput, trace.toList(),
            if (status == AgentRunStatus.NEEDS_CLARIFICATION) "Please clarify what you want the agent to do."
            else "Workflow completed."
        )
    }

    private fun result(request: AgentRequest, plan: AgentPlan, status: AgentRunStatus, message: String) =
        AgentRunResult(request.id, status, plan.intent, plan, message = message)
}
