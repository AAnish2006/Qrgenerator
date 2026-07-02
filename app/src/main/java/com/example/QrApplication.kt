package com.example

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point for the Hilt dependency graph. Every
 * @AndroidEntryPoint (MainActivity) and @HiltViewModel (QrViewModel) in the
 * app resolves its dependencies from the graph rooted here.
 */
@HiltAndroidApp
class QrApplication : Application()
