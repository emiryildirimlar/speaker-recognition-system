package com.example.speakerrecoginitonfinal2.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorResponse(
    val detail: String? = null,
    val message: String? = null
)
