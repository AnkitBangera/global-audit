package com.landmarkgroup.globalaudit.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthorizationResponse(
    @Json(name = "id") val id: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "authorizations") val authorizations: Authorizations?
)

@JsonClass(generateAdapter = true)
data class Authorizations(
    @Json(name = "id") val id: String?,
    @Json(name = "first_name") val firstName: String?,
    @Json(name = "last_name") val lastName: String?,
    @Json(name = "locations") val locations: List<String>?,
    @Json(name = "roles") val roles: List<String>?
)
