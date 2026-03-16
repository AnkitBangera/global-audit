package com.landmarkgroup.globalaudit.network

import com.landmarkgroup.globalaudit.utils.Constants
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // IBM API Gateway Retrofit (authorization)
    private val ibmRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(Constants.IBM_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    // NGINX Retrofit (warehouse-ops and other non-IBM endpoints)
    private val nginxRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(Constants.NGINX_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    // IBM-based service for authorization and user profile
    val sahlaApiService: SahlaApiService = ibmRetrofit.create(SahlaApiService::class.java)

    // NGINX-based service placeholder for audit/warehouse operations
    val auditApiService: AuditApiService = nginxRetrofit.create(AuditApiService::class.java)
}
