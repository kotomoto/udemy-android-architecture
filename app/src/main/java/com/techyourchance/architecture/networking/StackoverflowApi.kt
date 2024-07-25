package com.techyourchance.architecture.networking

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface StackoverflowApi {

    @GET("/questions?key=$STACKOVERFLOW_API_KEY&sort=activity&order=desc&site=stackoverflow")
    suspend fun fetchLastActiveQuestions(@Query("pagesize") pageSize: Int?): QuestionsListSchema?

    @GET("/questions/{questionId}?key=$STACKOVERFLOW_API_KEY&site=stackoverflow&filter=withbody")
    suspend fun fetchQuestionDetails(@Path("questionId") questionId: String?): QuestionDetailsSchema?

    companion object {
        const val STACKOVERFLOW_API_KEY = "f)yov8mEGrYZa1dJDb2gpg(("
    }
}