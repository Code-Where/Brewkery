package com.clickretina.brewkery.domain.repository

import com.clickretina.brewkery.domain.model.MenuData
import com.clickretina.brewkery.domain.model.MenuItem
import com.clickretina.brewkery.util.Resource

interface MenuRepository {
    suspend fun getMenu(): Resource<MenuData>
    suspend fun getItem(id: Int): Resource<MenuItem>
    fun getCachedItem(id: Int): MenuItem?
}
