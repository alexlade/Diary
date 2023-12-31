package com.alexlade.diaryapp.navigation

import com.alexlade.diaryapp.util.Constants.WRITE_SCREEN_ARGUMENT_KEY

sealed class Screen(val route: String) {
    object Login: Screen(route = "login_screen")
    object Home: Screen(route = "home_screen")
    object Write: Screen(
        route = "write_screen?$WRITE_SCREEN_ARGUMENT_KEY={$WRITE_SCREEN_ARGUMENT_KEY}"
    ) {
        fun passDiaryId(diaryId: String) = "write_screen?$WRITE_SCREEN_ARGUMENT_KEY=$diaryId"
    }
}
