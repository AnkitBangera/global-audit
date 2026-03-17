package com.landmarkgroup.globalaudit.network

import com.landmarkgroup.globalaudit.data.model.ScanZoneRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit service for NGINX-backed endpoints (warehouse-ops, audit operations, etc.).
 *
 * Base URL is defined in NetworkModule as Constants.NGINX_URL:
 *   https://devapi1.landmarkgroup.com/warehouse-ops/
 */
interface AuditApiService {

    // POST /warehouse-ops/apps/digitalwms/warehouses/audit/scan-zone
    @POST("apps/digitalwms/warehouse/audit/scan-zone")
    suspend fun scanZone(@Body request: ScanZoneRequest): Response<Unit>
}
