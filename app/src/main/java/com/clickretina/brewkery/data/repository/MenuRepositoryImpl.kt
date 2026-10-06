package com.clickretina.brewkery.data.repository

import com.clickretina.brewkery.data.mapper.toDomain
import com.clickretina.brewkery.data.remote.BrewkeryApi
import com.clickretina.brewkery.domain.model.MenuData
import com.clickretina.brewkery.domain.model.MenuItem
import com.clickretina.brewkery.domain.repository.MenuRepository
import com.clickretina.brewkery.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class MenuRepositoryImpl(
    private val api: BrewkeryApi
) : MenuRepository {

    private var cachedMenuData: MenuData? = null
    private val itemCache = mutableMapOf<Int, MenuItem>()

    override suspend fun getMenu(): Resource<MenuData> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMenu()
            val domainData = response.toDomain()
            cachedMenuData = domainData
            domainData.items.forEach { itemCache[it.id] = it }
            Resource.Success(domainData)
        } catch (e: IOException) {
            cachedMenuData?.let {
                Resource.Success(it)
            } ?: Resource.Error("Unable to connect to the server. Please check your internet connection.", e)
        } catch (e: HttpException) {
            cachedMenuData?.let {
                Resource.Success(it)
            } ?: Resource.Error("Server error (${e.code()}). Please try again later.", e)
        } catch (e: Exception) {
            cachedMenuData?.let {
                Resource.Success(it)
            } ?: Resource.Error("An unexpected error occurred: ${e.localizedMessage ?: "Unknown error"}", e)
        }
    }

    override suspend fun getItem(id: Int): Resource<MenuItem> = withContext(Dispatchers.IO) {
        try {
            val itemDto = api.getItem(id)
            val domainItem = itemDto.toDomain()
            itemCache[id] = domainItem
            Resource.Success(domainItem)
        } catch (e: Exception) {
            // Fallback to cache if available
            val cached = itemCache[id] ?: cachedMenuData?.items?.find { it.id == id }
            if (cached != null) {
                Resource.Success(cached)
            } else {
                val message = when (e) {
                    is IOException -> "Unable to load item details. Please check your network."
                    is HttpException -> "Failed to load item from server (${e.code()})."
                    else -> "Failed to load item: ${e.localizedMessage}"
                }
                Resource.Error(message, e)
            }
        }
    }

    override fun getCachedItem(id: Int): MenuItem? {
        return itemCache[id] ?: cachedMenuData?.items?.find { it.id == id }
    }
}
