// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.feature.agent.nodes

import com.lifeeasy.agent.feature.agent.model.AgentNodeId
import com.lifeeasy.agent.feature.agent.model.NodeExecutionContext
import com.lifeeasy.agent.feature.agent.model.NodeResult
import com.lifeeasy.agent.feature.agent.runtime.AgentNode

class MeetingNotesNode : AgentNode {
    override val id = AgentNodeId.MEETING_EXTRACTOR
    override fun execute(context: NodeExecutionContext): NodeResult {
        val lines = context.inputText.lines().map(String::trim).filter(String::isNotBlank)
        val topics = lines.filterNot { isActionLine(it) }
        val actions = lines.filter(::isActionLine)
        val output = buildString {
            appendLine("MEETING NOTES")
            appendLine("\nDISCUSSION / TOPICS")
            if (topics.isEmpty()) appendLine("- No separate discussion topics identified.")
            else topics.forEach { appendLine("- $it") }
            appendLine("\nPOTENTIAL ACTION ITEMS (keyword-based; verify manually)")
            if (actions.isEmpty()) appendLine("- No explicit action phrase detected.")
            else actions.forEach { appendLine("- $it") }
            append("\nUnspecified details (such as date, attendees, owners, and decisions) have not been invented.")
        }
        return NodeResult(output, listOf("Line-based meeting structuring", "No speaker attribution inferred"))
    }

    private fun isActionLine(line: String): Boolean {
        val lower = line.lowercase()
        return listOf("todo", "action", "follow up", "follow-up", "need to", "must ", "will ",
            "করণীয়", "করতে হবে", "দায়িত্ব", "দায়িত্ব", "ফলোআপ").any(lower::contains)
    }
}
