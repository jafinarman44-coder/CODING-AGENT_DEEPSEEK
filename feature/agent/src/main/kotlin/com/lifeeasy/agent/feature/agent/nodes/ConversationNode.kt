// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.feature.agent.nodes

import com.lifeeasy.agent.feature.agent.model.AgentNodeId
import com.lifeeasy.agent.feature.agent.model.NodeExecutionContext
import com.lifeeasy.agent.feature.agent.model.NodeResult
import com.lifeeasy.agent.feature.agent.runtime.AgentNode

/** Safe offline fallback; no generative model is claimed or simulated. */
class ConversationNode : AgentNode {
    override val id = AgentNodeId.CONVERSATION
    override fun execute(context: NodeExecutionContext) = NodeResult(
        "I received your message: ${context.inputText.trim()}\n\n" +
            "The offline agent core is active, but a language-model provider is not connected yet. " +
            "I can currently route supported tasks such as summaries, meeting notes, task organization, and document outlines.",
        listOf("Local deterministic response; no LLM inference performed")
    )
}
