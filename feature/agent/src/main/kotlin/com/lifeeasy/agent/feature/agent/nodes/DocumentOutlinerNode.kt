// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.feature.agent.nodes

import com.lifeeasy.agent.feature.agent.model.AgentNodeId
import com.lifeeasy.agent.feature.agent.model.NodeExecutionContext
import com.lifeeasy.agent.feature.agent.model.NodeResult
import com.lifeeasy.agent.feature.agent.runtime.AgentNode

class DocumentOutlinerNode : AgentNode {
    override val id = AgentNodeId.DOCUMENT_OUTLINER
    override fun execute(context: NodeExecutionContext): NodeResult {
        val subject = context.inputText.trim().take(240)
        return NodeResult(
            "DOCUMENT DRAFT OUTLINE\n\nTitle\n${subject.lineSequence().firstOrNull().orEmpty()}\n\n" +
                "1. Purpose and background\n2. Key information\n3. Findings / main discussion\n4. Recommendations / next steps\n5. Conclusion\n\n" +
                "SOURCE MATERIAL\n$subject\n\n" +
                "Status: outline only. No DOCX/PDF file has been generated in STEP 8.",
            listOf("Structured outline generated from supplied request", "File export not implemented in this stage")
        )
    }
}
