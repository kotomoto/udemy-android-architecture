package com.techyourchance.architecture.screens.questiondetails

import android.util.Log
import androidx.lifecycle.ViewModel
import com.techyourchance.architecture.common.database.FavoriteQuestionDao
import com.techyourchance.architecture.common.networking.StackoverflowApi
import com.techyourchance.architecture.question.ObserveQuestionDetailsUseCase
import com.techyourchance.architecture.question.QuestionWithBodySchema
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class QuestionDetailsViewModel(
    private val observeQuestionDetailsUseCase: ObserveQuestionDetailsUseCase,
) : ViewModel() {

    sealed class QuestionDetailsResult {
        data object None : QuestionDetailsResult()
        data class Success(val questionDetails: QuestionWithBodySchema, val isFavorite: Boolean) : QuestionDetailsResult()
        data object Error : QuestionDetailsResult()
    }

    val questionDetails = MutableStateFlow<QuestionDetailsResult>(QuestionDetailsResult.None)

    suspend fun observeQuestionDetails(questionId: String) {
        withContext(Dispatchers.Main.immediate) {
            observeQuestionDetailsUseCase
                .observeQuestionDetails(questionId)
                .collect { useCaseResult ->
                    val result = when (useCaseResult) {
                        is ObserveQuestionDetailsUseCase.QuestionDetailsResult.Success -> {
                            QuestionDetailsResult.Success(useCaseResult.questionDetails, useCaseResult.isFavorite)
                        }

                        is ObserveQuestionDetailsUseCase.QuestionDetailsResult.Error -> {
                            QuestionDetailsResult.Error
                        }

                        else -> {
                            QuestionDetailsResult.None
                        }
                    }
                    questionDetails.value = result
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("QuestionDetailsViewModel", "onCleared()")
    }
}
