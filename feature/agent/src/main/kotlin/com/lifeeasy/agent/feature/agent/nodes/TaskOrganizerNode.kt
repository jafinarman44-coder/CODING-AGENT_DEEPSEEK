// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.feature.agent.nodes

import com.lifeeasy.agent.feature.agent.model.AgentNodeId
import com.lifeeasy.agent.feature.agent.model.NodeExecutionContext
import com.lifeeasy.agent.feature.agent.model.NodeResult
import com.lifeeasy.agent.feature.agent.runtime.AgentNode

class TaskOrganizerNode : AgentNode {
    override val id = AgentNodeId.TASK_ORGANIZER
    override fun execute(context: NodeExecutionContext): NodeResult {
        val source = context.inputText
        val lines = source.lines().map(String::trim).filter(String::isNotBlank)
        val actionWords = listOf("todo", "action", "follow up", "follow-up", "need to", "must ", "will ",
            "করণীয়", "করতে হবে", "দায়িত্ব", "দায়িত্ব", "ফলোআপ")
        val tasks = lines.filter { line ->
            val normalized = line.lowercase()
            !normalized.startsWith("potential action items") &&
                !normalized.contains("no explicit action phrase detected") &&
                actionWords.any(normalized::contains)
        }
        val output = if (tasks.isEmpty()) {
            "ACTION ITEMS\n- No explicit task or owner could be reliably extracted.\n\nPlease review the source text and identify the responsible person and deadline manually."
        } else {
            "ACTION ITEMS\n" + tasks.mapIndexed { i, task -> "${i + 1}. $task" }.joinToString("\n") +
                "\n\nOwner and deadline are shown only when explicitly present in the source."
        }
        return NodeResult(output, listOf("Keyword-grounded task extraction", "No owners or deadlines fabricated"))
    }
}
