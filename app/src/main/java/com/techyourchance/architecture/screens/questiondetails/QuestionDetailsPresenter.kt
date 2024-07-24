package com.techyourchance.architecture.screens.questiondetails

import com.techyourchance.architecture.common.database.FavoriteQuestionDao
import com.techyourchance.architecture.common.networking.StackoverflowApi
import com.techyourchance.architecture.question.QuestionWithBodySchema
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class QuestionDetailsPresenter(
    private val stackoverflowApi: StackoverflowApi,
    private val favoriteQuestionDao: FavoriteQuestionDao,
) {

    sealed class QuestionDetailsResult {
        data object None : QuestionDetailsResult()
        data class Success(val questionDetails: QuestionWithBodySchema, val isFavorite: Boolean) : QuestionDetailsResult()
        data object Error : QuestionDetailsResult()
    }

    val questionDetails = MutableStateFlow<QuestionDetailsResult>(QuestionDetailsResult.None)

    suspend fun fetchQuestionDetails(questionId: String) {
        withContext(Dispatchers.Main.immediate) {
            combine(
                flow = flow {
                    emit(stackoverflowApi.fetchQuestionDetails(questionId))
                },
                flow2 = favoriteQuestionDao.observeById(questionId),
            ) { questionDetails, favoriteQuestion ->
                if (questionDetails != null && questionDetails.questions.isNotEmpty()) {
                    QuestionDetailsResult.Success(
                        questionDetails = questionDetails.questions[0],
                        isFavorite = favoriteQuestion != null
                    )
                } else {
                    QuestionDetailsResult.Error
                }
            }.catch {
                QuestionDetailsResult.Error
            }.collect {
                questionDetails.value = it
            }
        }
    }
}
