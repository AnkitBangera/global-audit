package com.landmarkgroup.globalaudit.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WarehouseFeature(
    @Json(name = "feature_name") val featureName: String,
    @Json(name = "feature_enabled_value") val featureEnabledValue: Boolean
)
