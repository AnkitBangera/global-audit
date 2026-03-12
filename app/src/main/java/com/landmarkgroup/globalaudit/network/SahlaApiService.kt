package com.landmarkgroup.globalaudit.network

import com.landmarkgroup.globalaudit.data.model.AuthorizationResponse
import retrofit2.http.GET

interface SahlaApiService {
    @GET("/landmarkgroup/retail-sit/v2/users/me")
    suspend fun authorize(): AuthorizationResponse
}
