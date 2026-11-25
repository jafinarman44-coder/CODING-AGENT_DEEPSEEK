// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.feature.agent.runtime

import com.lifeeasy.agent.feature.agent.model.AgentNodeId
import com.lifeeasy.agent.feature.agent.model.NodeExecutionContext
import com.lifeeasy.agent.feature.agent.model.NodeResult

interface AgentNode {
    val id: AgentNodeId
    fun execute(context: NodeExecutionContext): NodeResult
}

class AgentNodeRegistry(nodes: Iterable<AgentNode>) {
    private val nodeList = nodes.toList()
    private val byId: Map<AgentNodeId, AgentNode> = nodeList.associateBy { it.id }

    init {
        require(byId.size == nodeList.size) { "Agent node identifiers must be unique" }
    }

    fun resolve(id: AgentNodeId): AgentNode? = byId[id]
    fun contains(id: AgentNodeId): Boolean = byId.containsKey(id)
}
