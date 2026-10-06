package com.clickretina.brewkery.data.remote

import com.clickretina.brewkery.data.remote.dto.ItemDto
import com.clickretina.brewkery.data.remote.dto.MenuResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

interface BrewkeryApi {

    companion object {
        const val BASE_URL = "https://raw.githubusercontent.com/VivekShah138/Brewkery/main/"
    }

    @GET("data.json")
    suspend fun getMenu(): MenuResponseDto

    @GET("api/items/{id}.json")
    suspend fun getItem(@Path("id") id: Int): ItemDto
}
