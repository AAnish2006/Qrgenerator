package com.example.util

import org.junit.Assert.assertEquals
import org.junit.Test

class QrCodeParserTest {

    @Test
    fun `parses http url`() {
        val result = QrCodeParser.parse("http://example.com")
        assertEquals(QrCodeParser.ParsedResult.Url("http://example.com"), result)
    }

    @Test
    fun `parses https url case-insensitively`() {
        val result = QrCodeParser.parse("HTTPS://Example.com/path")
        assertEquals(QrCodeParser.ParsedResult.Url("HTTPS://Example.com/path"), result)
    }

    @Test
    fun `parses wifi payload`() {
        val result = QrCodeParser.parse("WIFI:S:HomeNet;T:WPA;P:secret123;;")
        assertEquals(QrCodeParser.ParsedResult.Wifi("HomeNet", "secret123", "WPA"), result)
    }

    @Test
    fun `parses mailto with subject and body`() {
        val result = QrCodeParser.parse("mailto:someone@example.com?subject=Hi%20there&body=Hello%20World")
        assertEquals(
            QrCodeParser.ParsedResult.Email("someone@example.com", "Hi there", "Hello World"),
            result
        )
    }

    @Test
    fun `parses mailto without query params`() {
        val result = QrCodeParser.parse("mailto:someone@example.com")
        assertEquals(QrCodeParser.ParsedResult.Email("someone@example.com", null, null), result)
    }

    @Test
    fun `falls back to plain text`() {
        val result = QrCodeParser.parse("  just some text  ")
        assertEquals(QrCodeParser.ParsedResult.Text("just some text"), result)
    }
}
