package com.theappcapital.siriusrating

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class UserAction(val appVersion: String, @Contextual val date: Instant)