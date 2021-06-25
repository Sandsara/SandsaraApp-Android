package com.ht117.sandsara.data

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import kotlinx.serialization.json.Json
import okhttp3.logging.HttpLoggingInterceptor

object NetworkService {

    // here you have to set the endpoint of your data base
    private const val MainHost = "databaseEndpoint"

    // here you have to use your Apikey
    private const val API_KEY = "key"

    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    /**
     * Default client
     * For basic request with server
     */
    fun defaultClient() = HttpClient(OkHttp) {
        followRedirects = true
        defaultRequest {
            host = MainHost
        }

        engine {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.HEADERS
            addInterceptor(loggingInterceptor)

            addInterceptor {
                val request = it.request().newBuilder()
                        .addHeader("Authorization", "Bearer $API_KEY")
                        .build()

                it.proceed(request)
            }
        }
        install(JsonFeature) {
            serializer = KotlinxSerializer(json)
        }
    }

    /**
     * Custom client
     * For some special request to end host binary
     */
    fun customClient() = HttpClient(OkHttp) {
        followRedirects = true

        engine {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.HEADERS
            addInterceptor(loggingInterceptor)

            addInterceptor {
                val request = it.request().newBuilder()
                    .addHeader("Authorization", "Bearer $API_KEY")
                    .build()

                it.proceed(request)
            }
        }
        install(JsonFeature) {
            serializer = KotlinxSerializer(json)
        }
    }
}