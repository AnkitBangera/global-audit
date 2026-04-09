package com.landmarkgroup.globalaudit.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Lightweight app-wide session event bus.
 * Used by non-Android layers (e.g., OkHttp interceptors) to signal session expiry.
 */
object SessionEvents {
    // Fire-and-forget signal; no payload needed
    private val _sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpired: SharedFlow<Unit> = _sessionExpired

    fun notifySessionExpired() {
        _sessionExpired.tryEmit(Unit)
    }
}
