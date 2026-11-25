// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.feature.agent

import com.lifeeasy.agent.feature.agent.classification.IntentClassifier
import com.lifeeasy.agent.feature.agent.model.AgentRequest
import com.lifeeasy.agent.feature.agent.model.AgentRunResult
import com.lifeeasy.agent.feature.agent.planning.PlannerNode
import com.lifeeasy.agent.feature.agent.runtime.SupervisorAgent

/** Coordinates classification, planning, supervision, and controlled execution. */
class AgentOrchestrator(
    private val classifier: IntentClassifier,
    private val planner: PlannerNode,
    private val supervisor: SupervisorAgent
) {
    fun run(request: AgentRequest, confirmationGranted: Boolean = false): AgentRunResult {
        val intent = classifier.classify(request.text)
        val plan = planner.createPlan(request, intent)
        return supervisor.execute(request, plan, confirmationGranted)
    }
}
