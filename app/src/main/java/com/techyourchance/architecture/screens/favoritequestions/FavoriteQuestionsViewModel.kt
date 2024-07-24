package com.techyourchance.architecture.screens.favoritequestions

import android.util.Log
import androidx.lifecycle.ViewModel
import com.techyourchance.architecture.common.database.FavoriteQuestionDao

class FavoriteQuestionsViewModel(
    private val favoriteQuestionDao: FavoriteQuestionDao,
) : ViewModel() {

    val favorites = favoriteQuestionDao.observe()

    override fun onCleared() {
        super.onCleared()
        Log.i("FavoriteQuestionsViewModel", "onCleared()")
    }
}
