package com.example.parkfynder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.parkfynder.ui.theme.ParkFynderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ParkFynderTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "loginScreen") {

        composable("loginScreen") { LoginScreen(navController) }

        composable("signupScreen") { SignupScreen(navController) }

        composable("forgotPasswordScreen") { ForgotPasswordScreen(navController) }

        composable(
            "mainScreen/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            MainScreen(navController, userId)
        }

        composable(
            "parkingSlotsScreen/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            ParkingSlotsScreen(navController, userId)
        }

        composable(
            "paymentScreen/{userId}/{hours}/{cost}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("hours") { type = NavType.StringType },
                navArgument("cost") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val hours = backStackEntry.arguments?.getString("hours") ?: "0"
            val cost = backStackEntry.arguments?.getString("cost") ?: "0"
            PaymentScreen(navController, userId, hours, cost)
        }
    }
}
