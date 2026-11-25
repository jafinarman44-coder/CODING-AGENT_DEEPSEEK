// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.feature.agent.nodes

import com.lifeeasy.agent.feature.agent.model.AgentNodeId
import com.lifeeasy.agent.feature.agent.model.NodeExecutionContext
import com.lifeeasy.agent.feature.agent.model.NodeResult
import com.lifeeasy.agent.feature.agent.runtime.AgentNode

/** Explicitly reports missing translation capability instead of returning a fabricated translation. */
class TranslatorNode : AgentNode {
    override val id = AgentNodeId.TRANSLATOR
    override fun execute(context: NodeExecutionContext) = NodeResult(
        "Translation is not available in this build because no translation model or service is connected. Your source text has not been changed.\n\nSOURCE\n${context.inputText}",
        listOf("No translation engine connected")
    )
}
