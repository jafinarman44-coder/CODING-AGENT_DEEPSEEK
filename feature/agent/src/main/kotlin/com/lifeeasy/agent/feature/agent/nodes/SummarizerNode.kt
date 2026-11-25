// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.feature.agent.nodes

import com.lifeeasy.agent.feature.agent.model.AgentNodeId
import com.lifeeasy.agent.feature.agent.model.NodeExecutionContext
import com.lifeeasy.agent.feature.agent.model.NodeResult
import com.lifeeasy.agent.feature.agent.runtime.AgentNode

class SummarizerNode : AgentNode {
    override val id = AgentNodeId.SUMMARIZER
    override fun execute(context: NodeExecutionContext): NodeResult {
        val sentences = context.inputText
            .replace(Regex("\\s+"), " ")
            .split(Regex("(?<=[.!?।])\\s+|\\n+"))
            .map(String::trim)
            .filter(String::isNotBlank)
        val selected = sentences.take(5)
        val output = if (selected.isEmpty()) "No text was provided to summarize."
            else selected.mapIndexed { index, sentence -> "${index + 1}. $sentence" }.joinToString("\n")
        return NodeResult(
            "SUMMARY (extractive)\n$output\n\nNote: This is an extractive selection of the first key sentences, not an AI-generated semantic summary.",
            listOf("Extractive summary", "Source text preserved; no facts added")
        )
    }
}
