package com.ham.tools.data.remote.llm

/**
 * 与云端 LLM 一并上传的提示词工程：定义 QsoLog 字段、取值约束与 JSON 输出格式。
 * 保持与 [com.ham.tools.data.model.QsoLog] / Logbook 表单一致。
 */
object QsoLlmPromptText {

    const val SYSTEM =
        "你是业余无线电（HAM）通联日志结构化助手。" +
            "你只根据用户给出的「语音识别文本」与「字段说明」推断通联内容；不得编造未在语音中出现的呼号或通联事实。" +
            "若信息不确定，对应字段用半角减号 \"-\" 填充。" +
            "输出必须是单个 JSON 对象，不要 Markdown，不要解释性文字。"

    fun buildUserMessage(
        profileCallsign: String,
        profileGrid: String,
        profileQth: String,
        transcript: String,
        specBlock: String = FIELD_SPEC_AND_JSON_RULES
    ): String = buildString {
        appendLine("【以下为提示词工程 / 字段规格与输出规则】")
        appendLine()
        appendLine(specBlock.trim())
        appendLine()
        appendLine("【操作员档案（仅用于填写 myGridLocator 等我方字段；若语音未提及则填 \"-\"）】")
        appendLine("- profileCallsign: ${profileCallsign.ifBlank { "-" }}")
        appendLine("- profileGrid: ${profileGrid.ifBlank { "-" }}")
        appendLine("- profileQth: ${profileQth.ifBlank { "-" }}")
        appendLine()
        appendLine("【Sherpa-ONNX 语音识别原文（中英可能夹杂；一次录音可能含多段与多个不同友台通联）】")
        appendLine(transcript.trim().ifBlank { "-" })
    }

    /**
     * 与手工录入表单对齐的规格说明；随请求发送给 LLM。
     */
    const val FIELD_SPEC_AND_JSON_RULES = """
字段说明（与 App 内「通联日志」一致）：
- callsign：对方呼号，大写字母与数字，如 BG1ABC、W1AW。每出现一个新呼号通常表示一次独立通联。
- frequency：频率字符串，如 "14.200 MHz"、"145.500"、"7.050"。未知填 "-"。
- mode：只能是以下之一（英文大写枚举名）：SSB、CW、FM、FT8。
- rstSent / rstRcvd：信号报告，常为两位数如 59、599；未知填 "-"。
- opName, qth, gridLocator, qslInfo, txPower, rig, remarks：可选文本；未知填 "-"。
- myGridLocator：我方网格；若语音未说我方信息则用档案中的 profileGrid，否则 "-"。
- propagation：只能是以下之一（英文枚举名）：
  UNKNOWN, GROUND_WAVE, SKYWAVE, F2, SPORADIC_E, AURORA, AURORA_E, TROPO, METEOR_SCATTER, EME, SATELLITE, TEP, NVIS, INTERNET, REPEATER
  未知填 UNKNOWN。
- qslStatus：只能是以下之一（英文枚举名）：
  NOT_SENT, SENT, RECEIVED, LOTW_UPLOADED, LOTW_CONFIRMED, EQSL_SENT, EQSL_CONFIRMED, CLUBLOG_UPLOADED, CLUBLOG_CONFIRMED
  无法判断用 NOT_SENT。

输出 JSON 格式（仅输出该对象，不要其它内容）：
{
  "qsos": [
    {
      "callsign": "…",
      "frequency": "…",
      "mode": "SSB",
      "rstSent": "59",
      "rstRcvd": "59",
      "opName": "-",
      "qth": "-",
      "gridLocator": "-",
      "qslInfo": "-",
      "txPower": "-",
      "rig": "-",
      "myGridLocator": "-",
      "propagation": "UNKNOWN",
      "qslStatus": "NOT_SENT",
      "remarks": "-"
    }
  ]
}

规则：
1) 同一录音中若与多个不同呼号依次通联，必须为每个呼号各输出一条对象，按时间顺序排列在 qsos 数组中。
2) 任何无法从语音合理推断的字段必须用 "-"，不要用 null 或空字符串（remarks 等亦同）。
3) 禁止把未在语音中出现的呼号写成真实通联；若听不清呼号，该条 callsign 填 "-"。
"""
}
