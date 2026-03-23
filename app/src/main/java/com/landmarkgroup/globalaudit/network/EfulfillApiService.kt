package com.landmarkgroup.globalaudit.network

import com.landmarkgroup.globalaudit.data.model.WarehouseFeature
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit service for eFulfill endpoints hosted on devapi3.landmarkgroup.com.
 * Uses the shared AuthInterceptor so the Authorization header is added automatically.
 */
interface EfulfillApiService {
    // GET https://devapi3.landmarkgroup.com/apps/efulfill/ui/feature/list/warehouses/{location}
    @GET("apps/efulfill/ui/feature/list/warehouses/{location}")
    suspend fun getWarehouseFeatures(@Path("location") location: String): List<WarehouseFeature>
}
