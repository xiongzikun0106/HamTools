package com.ham.tools.data.remote.qrz

import com.ham.tools.data.model.Mode
import com.ham.tools.data.model.QsoLog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.regex.Pattern

/**
 * 将 [QsoLog] 转为 QRZ INSERT 所需的 ADIF 片段（以 `<eor>` 结尾）。
 */
object QrzAdifBuilder {

    private val utcDate = SimpleDateFormat("yyyyMMdd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val utcTimeHm = SimpleDateFormat("HHmm", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private val freqNumber = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)")

    fun buildInsertAdif(
        log: QsoLog,
        stationCallsign: String
    ): String {
        val station = stationCallsign.ifBlank { "NOCALL" }.uppercase(Locale.US)
        val call = log.callsign.uppercase(Locale.US)
        val dateStr = utcDate.format(Date(log.timestamp))
        val timeStr = utcTimeHm.format(Date(log.timestamp))
        val band = frequencyToAdifBand(log.frequency)
        val mode = modeToAdifSubMode(log.mode)

        return buildString {
            appendField("station_callsign", station)
            appendField("call", call)
            appendField("qso_date", dateStr)
            appendField("time_on", timeStr)
            appendField("band", band)
            appendField("mode", mode)
            appendField("rst_sent", log.rstSent.ifBlank { "59" })
            appendField("rst_rcvd", log.rstRcvd.ifBlank { "59" })
            log.qth?.takeIf { it.isNotBlank() }?.let { appendField("qth", it) }
            log.gridLocator?.takeIf { it.isNotBlank() }?.let { appendField("gridsquare", it) }
            parseFrequencyMhz(log.frequency)?.let { mhz ->
                appendField("freq", String.format(Locale.US, "%.6f", mhz))
            }
            append("<eor>")
        }
    }

    private fun StringBuilder.appendField(tag: String, value: String) {
        val v = value.ifEmpty { " " }
        append('<').append(tag).append(':').append(v.length).append('>').append(v)
    }

    internal fun parseFrequencyMhz(frequency: String): Double? {
        val m = freqNumber.matcher(frequency.replace(",", "."))
        if (!m.find()) return null
        return m.group(1)?.toDoubleOrNull()
    }

    /**
     * ADIF `BAND` 常用字符串（如 20m、80m）。
     */
    fun frequencyToAdifBand(frequency: String): String {
        val mhz = parseFrequencyMhz(frequency) ?: return "20m"
        return when {
            mhz in 0.135..0.138 -> "2200m"
            mhz in 0.472..0.479 -> "630m"
            mhz in 1.8..2.0 -> "160m"
            mhz in 3.5..4.0 -> "80m"
            mhz in 5.0..5.5 -> "60m"
            mhz in 7.0..7.3 -> "40m"
            mhz in 10.1..10.15 -> "30m"
            mhz in 14.0..14.35 -> "20m"
            mhz in 18.068..18.168 -> "17m"
            mhz in 21.0..21.45 -> "15m"
            mhz in 24.89..24.99 -> "12m"
            mhz in 28.0..29.7 -> "10m"
            mhz in 50.0..54.0 -> "6m"
            mhz in 144.0..148.0 -> "2m"
            mhz in 222.0..225.0 -> "1.25m"
            mhz in 420.0..450.0 -> "70cm"
            mhz in 902.0..928.0 -> "33cm"
            mhz in 1240.0..1300.0 -> "23cm"
            else -> "${mhz}MHz"
        }
    }

    private fun modeToAdifSubMode(mode: Mode): String = when (mode) {
        Mode.CW -> "CW"
        Mode.FM -> "FM"
        Mode.FT8 -> "FT8"
        Mode.SSB -> "SSB"
    }
}
