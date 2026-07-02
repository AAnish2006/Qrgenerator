package com.example.util

object QrCodeParser {

    sealed class ParsedResult {
        data class Url(val url: String) : ParsedResult()
        data class Wifi(val ssid: String, val pass: String, val type: String) : ParsedResult()
        data class Email(val email: String, val subject: String?, val body: String?) : ParsedResult()
        data class Text(val text: String) : ParsedResult()
    }

    /**
     * Parses a raw scanned string into a typed result.
     */
    fun parse(raw: String): ParsedResult {
        val trimmed = raw.trim()
        return when {
            trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true) -> {
                ParsedResult.Url(trimmed)
            }
            trimmed.startsWith("WIFI:", ignoreCase = true) -> {
                // Parse WIFI:S:<ssid>;T:<type>;P:<password>;;
                var ssid = ""
                var pass = ""
                var type = "WPA"

                val parts = trimmed.substring(5).split(";")
                for (part in parts) {
                    if (part.startsWith("S:")) ssid = part.substring(2)
                    if (part.startsWith("P:")) pass = part.substring(2)
                    if (part.startsWith("T:")) type = part.substring(2)
                }
                ParsedResult.Wifi(ssid, pass, type)
            }
            trimmed.startsWith("mailto:", ignoreCase = true) -> {
                // mailto:abc@example.com?subject=Hello&body=World
                val emailUri = trimmed.substring(7)
                val splitMail = emailUri.split("?")
                val email = splitMail[0]
                var subject: String? = null
                var body: String? = null

                if (splitMail.size > 1) {
                    val queryParams = splitMail[1].split("&")
                    for (param in queryParams) {
                        val kv = param.split("=")
                        if (kv.size == 2) {
                            if (kv[0].equals("subject", ignoreCase = true)) subject = java.net.URLDecoder.decode(kv[1], "UTF-8")
                            if (kv[0].equals("body", ignoreCase = true)) body = java.net.URLDecoder.decode(kv[1], "UTF-8")
                        }
                    }
                }
                ParsedResult.Email(email, subject, body)
            }
            else -> {
                ParsedResult.Text(trimmed)
            }
        }
    }
}
