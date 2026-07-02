package com.example.di

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

/**
 * Shared networking primitives. This app has no backend today (see
 * NetworkConfig.kt and the README's "Security & Backend" section) — QR
 * generate/scan run entirely on-device via ZXing. This module exists so
 * that when a backend is added (dynamic-QR redirects, real auth), a
 * per-feature Retrofit instance can be built with
 * `Retrofit.Builder().baseUrl(NetworkConfig.dynamicQrApiBaseUrl(context)).client(okHttpClient)...`
 * without duplicating client/logging setup. There's no eager Retrofit
 * @Provides here on purpose: NetworkConfig's base URLs are optional and
 * blank by default, and Retrofit requires a non-null baseUrl at build time.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(
                    HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
                )
            }
        }
        .build()

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
}
