package com.clickretina.brewkery.di

import com.clickretina.brewkery.data.remote.BrewkeryApi
import com.clickretina.brewkery.data.repository.CartRepository
import com.clickretina.brewkery.data.repository.MenuRepositoryImpl
import com.clickretina.brewkery.data.repository.OrderRepository
import com.clickretina.brewkery.domain.repository.MenuRepository
import com.clickretina.brewkery.domain.usecase.CalculateOrderSummary
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

interface AppContainer {
    val menuRepository: MenuRepository
    val cartRepository: CartRepository
    val orderRepository: OrderRepository
    val calculateOrderSummary: CalculateOrderSummary
}

class DefaultAppContainer : AppContainer {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BrewkeryApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    private val brewkeryApi: BrewkeryApi by lazy {
        retrofit.create(BrewkeryApi::class.java)
    }

    override val menuRepository: MenuRepository by lazy {
        MenuRepositoryImpl(brewkeryApi)
    }

    override val cartRepository: CartRepository by lazy {
        CartRepository()
    }

    override val orderRepository: OrderRepository by lazy {
        OrderRepository()
    }

    override val calculateOrderSummary: CalculateOrderSummary by lazy {
        CalculateOrderSummary()
    }
}
