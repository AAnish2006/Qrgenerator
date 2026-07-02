package com.example

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Instrumentation test runner that substitutes HiltTestApplication for the
 * real QrApplication so @HiltAndroidTest-annotated tests get an isolated,
 * replaceable Hilt component graph. Wired via
 * android.defaultConfig.testInstrumentationRunner in app/build.gradle.kts.
 */
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}
