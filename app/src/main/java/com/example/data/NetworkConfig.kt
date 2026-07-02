package com.example.data

import android.content.Context

/**
 * Central, non-secret backend configuration.
 *
 * There is no backend server wired into this app today (see README's
 * "Security & Backend" section). This object exists so that IF a backend is
 * added later (dynamic-QR redirect service, real authentication, etc.), the
 * app reads its base URL from `.env` — via the Secrets Gradle Plugin, which
 * is already configured in `app/build.gradle.kts` — instead of a hardcoded
 * string in source. `.env` is gitignored; only `.env.example` (blank
 * placeholders) is committed.
 *
 * Only put non-secret values here (base URLs, feature flags). Real
 * credentials for a backend — DB passwords, a Play Developer API
 * service-account key, a Stripe secret key, JWT signing secrets — must be
 * held by the server itself and never bundled into the app.
 */
object NetworkConfig {

    fun dynamicQrApiBaseUrl(context: Context): String? = readResourceOrNull(context, "DYNAMIC_QR_API_BASE_URL")

    fun authApiBaseUrl(context: Context): String? = readResourceOrNull(context, "AUTH_API_BASE_URL")

    private fun readResourceOrNull(context: Context, resName: String): String? {
        return try {
            val resId = context.resources.getIdentifier(resName, "string", context.packageName)
            if (resId == 0) return null
            val value = context.getString(resId)
            value.ifBlank { null }
        } catch (e: Exception) {
            null
        }
    }
}
