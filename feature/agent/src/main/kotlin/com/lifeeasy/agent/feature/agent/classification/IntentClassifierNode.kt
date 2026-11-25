// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.feature.agent.classification

import com.lifeeasy.agent.feature.agent.model.AgentIntent
import java.util.Locale

/** Deterministic, offline intent routing. This is not represented as an LLM. */
fun interface IntentClassifier {
    fun classify(text: String): AgentIntent
}

class IntentClassifierNode : IntentClassifier {
    override fun classify(text: String): AgentIntent {
        val value = normalize(text)
        if (value.isBlank()) return AgentIntent.UNKNOWN

        // External side effects are routed to a confirmation-gated intent first.
        if (containsAny(value, listOf(
                "send email", "send message", "send it", "post this", "publish",
                "delete file", "delete all", "make payment", "pay bill", "purchase",
                "buy this", "book appointment", "call this number", "share publicly",
                "ইমেইল পাঠাও", "মেসেজ পাঠাও", "পোস্ট কর", "প্রকাশ কর", "ডিলিট কর",
                "মুছে ফেল", "পেমেন্ট কর", "টাকা পাঠাও", "কিনে দাও", "বুক কর", "কল কর"
            ))) return AgentIntent.EXTERNAL_ACTION

        if (containsAny(value, listOf(
                "meeting minutes", "meeting notes", "meeting transcript", "minutes of meeting",
                "মিটিং নোট", "মিটিং মিনিটস", "সভা কার্যবিবরণী", "মিটিংয়ের কথা", "মিটিং এর কথা"
            ))) return AgentIntent.MEETING_NOTES

        if (containsAny(value, listOf(
                "summarize", "summary", "short summary", "সংক্ষেপ", "সারাংশ", "সারমর্ম", "সংক্ষেপে"
            ))) return AgentIntent.SUMMARIZE

        if (containsAny(value, listOf(
                "organize tasks", "make a task list", "action items", "to do list", "todo",
                "কাজগুলো সাজাও", "কাজের তালিকা", "টাস্ক লিস্ট", "করণীয়", "করণীয়"
            ))) return AgentIntent.ORGANIZE_TASKS

        if (containsAny(value, listOf(
                "document outline", "write a report", "create a report", "proposal",
                "প্রতিবেদন তৈরি", "রিপোর্ট তৈরি", "প্রস্তাবনা", "ডকুমেন্ট তৈরি", "ডকুমেন্টের খসড়া"
            ))) return AgentIntent.DOCUMENT_OUTLINE

        if (containsAny(value, listOf(
                "translate", "translation", "অনুবাদ", "ইংরেজিতে কর", "বাংলায় কর", "বাংলায় কর"
            ))) return AgentIntent.TRANSLATION

        return AgentIntent.CONVERSATION
    }

    private fun normalize(text: String): String = text
        .lowercase(Locale.ROOT)
        .replace(Regex("\\s+"), " ")
        .trim()

    private fun containsAny(text: String, phrases: List<String>): Boolean =
        phrases.any { text.contains(it.lowercase(Locale.ROOT)) }
}
