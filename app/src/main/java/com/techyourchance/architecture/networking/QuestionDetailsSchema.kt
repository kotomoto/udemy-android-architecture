package com.techyourchance.architecture.networking

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.techyourchance.architecture.question.QuestionWithBodySchema

@JsonClass(generateAdapter = true)
data class QuestionDetailsSchema (
    @Json(name = "items") val questions: List<QuestionWithBodySchema>,
)