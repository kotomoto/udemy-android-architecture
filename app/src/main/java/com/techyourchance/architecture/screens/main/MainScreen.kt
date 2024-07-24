package com.techyourchance.architecture.screens.main

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.techyourchance.architecture.common.database.FavoriteQuestionDao
import com.techyourchance.architecture.common.networking.StackoverflowApi
import com.techyourchance.architecture.screens.Route
import com.techyourchance.architecture.screens.ScreensNavigator
import com.techyourchance.architecture.screens.favoritequestions.FavoriteQuestionsPresenter
import com.techyourchance.architecture.screens.favoritequestions.FavoriteQuestionsScreen
import com.techyourchance.architecture.screens.questiondetails.QuestionDetailsScreen
import com.techyourchance.architecture.screens.questiondetails.QuestionDetailsViewModel
import com.techyourchance.architecture.screens.questionslist.QuestionsListScreen
import kotlinx.coroutines.flow.map

@Composable
fun MainScreen(
    stackoverflowApi: StackoverflowApi,
    favoriteQuestionDao: FavoriteQuestionDao,
) {
    val screensNavigator = remember() {
        ScreensNavigator()
    }

    val currentBottomTab = screensNavigator.currentBottomTab.collectAsState()

    val currentRoute = screensNavigator.currentRoute.collectAsState()

    val isRootRoute = screensNavigator.isRootRoute.collectAsState()

    val isShowFavoriteButton = screensNavigator.currentRoute.map { route ->
        route is Route.QuestionDetailsScreen
    }.collectAsState(false)

    val questionIdAndTitle = remember(currentRoute.value) {
        if (currentRoute.value is Route.QuestionDetailsScreen) {
            Pair(
                (currentRoute.value as Route.QuestionDetailsScreen).questionId,
                (currentRoute.value as Route.QuestionDetailsScreen).questionTitle,
            )
        } else {
            Pair("", "")
        }
    }

    var isFavoriteQuestion by remember { mutableStateOf(false) }

    if (isShowFavoriteButton.value && questionIdAndTitle.first.isNotEmpty()) {
        // Since collectAsState can't be conditionally called, use LaunchedEffect for conditional logic
        LaunchedEffect(questionIdAndTitle) {
            favoriteQuestionDao.observeById(questionIdAndTitle.first).collect { favoriteQuestion ->
                isFavoriteQuestion = favoriteQuestion != null
            }
        }
    }

    Scaffold(
        topBar = {
            MyTopAppBar(
                favoriteQuestionDao = favoriteQuestionDao,
                isRootRoute = isRootRoute.value,
                isShowFavoriteButton = isShowFavoriteButton.value,
                isFavoriteQuestion = isFavoriteQuestion,
                questionIdAndTitle = questionIdAndTitle,
                onBackClicked = {
                    screensNavigator.navigateBack()
                }
            )
        },
        bottomBar = {
            BottomAppBar(modifier = Modifier) {
                MyBottomTabsBar(
                    bottomTabs = ScreensNavigator.BOTTOM_TABS,
                    currentBottomTab = currentBottomTab.value,
                    onTabClicked = { bottomTab ->
                        screensNavigator.toTab(bottomTab)
                    }
                )
            }
        },
        content = { padding ->
            MainScreenContent(
                padding = padding,
                screensNavigator = screensNavigator,
                stackoverflowApi = stackoverflowApi,
                favoriteQuestionDao = favoriteQuestionDao,
            )
        }
    )
}

@Composable
private fun MainScreenContent(
    padding: PaddingValues,
    screensNavigator: ScreensNavigator,
    stackoverflowApi: StackoverflowApi,
    favoriteQuestionDao: FavoriteQuestionDao,
) {
    val parentNavController = rememberNavController()
    screensNavigator.setParentNavController(parentNavController)

    val viewModelFactory = object : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(QuestionDetailsViewModel::class.java)) {
                return QuestionDetailsViewModel(stackoverflowApi, favoriteQuestionDao) as T
            }

            return super.create(modelClass)
        }
    }

    Surface(
        modifier = Modifier
            .padding(padding)
            .padding(horizontal = 12.dp),
    ) {
        val favoriteQuestionsPresenter = remember {
            FavoriteQuestionsPresenter(favoriteQuestionDao)
        }

        NavHost(
            modifier = Modifier.fillMaxSize(),
            navController = parentNavController,
            enterTransition = { fadeIn(animationSpec = tween(200)) },
            exitTransition = { fadeOut(animationSpec = tween(200)) },
            startDestination = Route.MainTab.routeName,
        ) {
            composable(route = Route.MainTab.routeName) {
                val mainNestedNavController = rememberNavController()
                screensNavigator.setNestedNavController(mainNestedNavController)
                NavHost(navController = mainNestedNavController, startDestination = Route.QuestionsListScreen.routeName) {
                    composable(route = Route.QuestionsListScreen.routeName) {
                        QuestionsListScreen(
                            onQuestionClicked = { clickedQuestionId, clickedQuestionTitle ->
                                screensNavigator.toRoute(Route.QuestionDetailsScreen(clickedQuestionId, clickedQuestionTitle))
                            },
                        )
                    }
                    composable(route = Route.QuestionDetailsScreen().routeName) {
                        val questionId = remember {
                            (screensNavigator.currentRoute.value as Route.QuestionDetailsScreen).questionId
                        }
                        QuestionDetailsScreen(
                            viewModelFactory = viewModelFactory,
                            questionId = questionId,
                            onError = {
                                screensNavigator.navigateBack()
                            },
                        )
                    }
                }
            }

            composable(route = Route.FavoritesTab.routeName) {
                val favoritesNestedNavController = rememberNavController()
                screensNavigator.setNestedNavController(favoritesNestedNavController)
                NavHost(navController = favoritesNestedNavController, startDestination = Route.FavoriteQuestionsScreen.routeName) {
                    composable(route = Route.FavoriteQuestionsScreen.routeName) {
                        FavoriteQuestionsScreen(
                            presenter = favoriteQuestionsPresenter,
                            onQuestionClicked = { favoriteQuestionId, favoriteQuestionTitle ->
                                screensNavigator.toRoute(Route.QuestionDetailsScreen(favoriteQuestionId, favoriteQuestionTitle))
                            },
                        )
                    }
                    composable(route = Route.QuestionDetailsScreen().routeName) {
                        val questionId = remember {
                            (screensNavigator.currentRoute.value as Route.QuestionDetailsScreen).questionId
                        }
                        QuestionDetailsScreen(
                            viewModelFactory = viewModelFactory,
                            questionId = questionId,
                            onError = {
                                screensNavigator.navigateBack()
                            },
                        )
                    }
                }
            }
        }
    }
}
