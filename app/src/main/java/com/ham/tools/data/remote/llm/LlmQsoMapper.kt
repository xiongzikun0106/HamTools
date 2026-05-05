package com.ham.tools.data.remote.llm

import com.ham.tools.data.model.Mode
import com.ham.tools.data.model.PropagationMode
import com.ham.tools.data.model.QslStatus
import com.ham.tools.data.model.QsoLog

internal object LlmQsoMapper {

    private fun String?.orDash(): String {
        val t = this?.trim() ?: return "-"
        return if (t.isEmpty()) "-" else t
    }

    fun toQsoLogs(
        parsed: LlmParsedQsoFile,
        defaultTimestampBase: Long
    ): List<QsoLog> {
        return parsed.qsos.mapIndexed { index, row ->
            val callsign = row.callsign.orDash().uppercase()
            val frequency = row.frequency.orDash()
            val mode = parseMode(row.mode)
            val rstSent = row.rstSent.orDash().ifBlank { "59" }
            val rstRcvd = row.rstRcvd.orDash().ifBlank { "59" }
            // 必填项允许 "-" 占位；可选项若为 "-" 则存 null，避免破坏原有列表卡片展示逻辑
            val opt = { s: String? ->
                when (val v = s?.trim().orEmpty()) {
                    "", "-" -> null
                    else -> v
                }
            }
            QsoLog(
                callsign = callsign,
                frequency = frequency,
                mode = mode,
                rstSent = rstSent,
                rstRcvd = rstRcvd,
                timestamp = defaultTimestampBase + index * 1000L,
                opName = opt(row.opName),
                qth = opt(row.qth),
                gridLocator = opt(row.gridLocator)?.uppercase(),
                qslInfo = opt(row.qslInfo),
                txPower = opt(row.txPower),
                rig = opt(row.rig),
                myGridLocator = opt(row.myGridLocator)?.uppercase(),
                propagation = parsePropagation(row.propagation),
                qslStatus = parseQsl(row.qslStatus),
                remarks = opt(row.remarks)
            )
        }
    }

    private fun parseMode(raw: String?): Mode {
        val x = raw?.trim() ?: return Mode.SSB
        Mode.entries.forEach { m ->
            if (m.name.equals(x, ignoreCase = true)) return m
            if (m.displayName.equals(x, ignoreCase = true)) return m
        }
        return Mode.SSB
    }

    private fun parsePropagation(raw: String?): PropagationMode {
        val x = raw?.trim() ?: return PropagationMode.UNKNOWN
        PropagationMode.entries.forEach { p ->
            if (p.name.equals(x, ignoreCase = true)) return p
            if (p.displayName.equals(x, ignoreCase = true)) return p
        }
        return PropagationMode.UNKNOWN
    }

    private fun parseQsl(raw: String?): QslStatus {
        val x = raw?.trim() ?: return QslStatus.NOT_SENT
        QslStatus.entries.forEach { q ->
            if (q.name.equals(x, ignoreCase = true)) return q
        }
        return QslStatus.NOT_SENT
    }
}
