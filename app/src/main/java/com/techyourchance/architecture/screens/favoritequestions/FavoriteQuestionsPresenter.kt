package com.techyourchance.architecture.screens.favoritequestions

import com.techyourchance.architecture.common.database.FavoriteQuestionDao

class FavoriteQuestionsPresenter(
    private val favoriteQuestionDao: FavoriteQuestionDao,
) {

    val favorites = favoriteQuestionDao.observe()
}
