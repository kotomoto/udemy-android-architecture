package com.techyourchance.architecture.question

import android.util.Log
import com.techyourchance.architecture.BuildConfig
import com.techyourchance.architecture.common.networking.StackoverflowApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class FetchQuestionsListUseCase {

    private val retrofit by lazy {
        val httpClient = OkHttpClient.Builder().run {
            addInterceptor(HttpLoggingInterceptor().apply {
                if (BuildConfig.DEBUG) {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            })
            build()
        }

        Retrofit.Builder()
            .baseUrl("http://api.stackexchange.com/2.3/")
            .addConverterFactory(MoshiConverterFactory.create())
            .client(httpClient)
            .build()
    }

    private val stackoverflowApi by lazy {
        retrofit.create(StackoverflowApi::class.java)
    }

    private var lastNetworkRequestNano = 0L

    private var questions: List<QuestionSchema> = emptyList()

    suspend fun fetchLastActiveQuestions(forceUpdate: Boolean = false): List<QuestionSchema> {
        return withContext(Dispatchers.IO) {
            if (forceUpdate || hasEnoughTimePassed()) {
                Log.i("QuestionsListViewModel", "launched fetchLastActiveQuestions() request")
                lastNetworkRequestNano = System.nanoTime()
                questions = stackoverflowApi.fetchLastActiveQuestions(20)!!.questions
                questions
            } else {
                questions
            }
        }
    }

    private fun hasEnoughTimePassed(): Boolean {
        return System.nanoTime() - lastNetworkRequestNano > THROTTLING_MS * 1_000_000
    }

    companion object {
        const val THROTTLING_MS = 5_000L
    }
}
