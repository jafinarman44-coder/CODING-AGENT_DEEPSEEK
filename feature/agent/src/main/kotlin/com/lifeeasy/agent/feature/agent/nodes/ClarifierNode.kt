// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.feature.agent.nodes

import com.lifeeasy.agent.feature.agent.model.AgentNodeId
import com.lifeeasy.agent.feature.agent.model.NodeExecutionContext
import com.lifeeasy.agent.feature.agent.model.NodeResult
import com.lifeeasy.agent.feature.agent.runtime.AgentNode

class ClarifierNode : AgentNode {
    override val id = AgentNodeId.CLARIFIER
    override fun execute(context: NodeExecutionContext) = NodeResult(
        "I need a little more detail before I can safely route this request.\n\nRequest received: ${context.inputText}\n\nPlease specify the exact task and, for any external action, the target and final content. No external action has been performed.",
        listOf("Clarification required", "No external side effect")
    )
}
