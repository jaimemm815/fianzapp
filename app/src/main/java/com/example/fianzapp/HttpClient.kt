/*
 * Given by the skeleton. Leave it as it is.
 *
 * Every web call of the app goes through httpClient, so all of them carry the same
 * User-Agent: the name of the app, which public web services ask every app to send. It has
 * no personal data on purpose: do not add your name or your email to it.
 *
 * Use it in each Retrofit.Builder() of your app with .client(httpClient).
 */
package com.example.fianzapp

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

private const val USER_AGENT = "Fianzapp/1.0 (CUNEF SAP student project)"

/**
 * Stamps every outgoing request with the User-Agent header, as in Session 8.
 */
class UserAgentInterceptor(private val userAgent: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestWithUserAgent = chain.request().newBuilder()
            .header("User-Agent", userAgent)
            .build()
        return chain.proceed(requestWithUserAgent)
    }
}

val httpClient: OkHttpClient = OkHttpClient.Builder()
    .addInterceptor(UserAgentInterceptor(USER_AGENT))
    .build()
