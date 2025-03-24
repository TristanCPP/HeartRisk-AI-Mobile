package com.example.ai_mobileapp_test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.*


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the composable UI content for the MainActivity
        setContent {
            MyApp()  // Launching the MyApp composable function
        }
    }
}

@Composable
fun MyApp() {
    // Initializing the navigation controller to manage screen transitions
    var currentID by remember { mutableStateOf<Int?>(null) } // Store current user ID
    val navController = rememberNavController()

    // Setting up a NavHost for navigation between screens
    // The startDestination is the initial screen to show when the app starts (SignInScreen)
    NavHost(navController, startDestination = "signIn") {
        composable("signIn") { SignInScreen(navController) { userID -> currentID = userID } }

        composable("RiskAssessment") {
            RiskAssessmentScreen(navController, currentID)
        }
        composable("PreviousAssessments") {
            PreviousAssessmentsScreen(navController, currentID)
        }

        // The 'signup' screen, which displays the SignUpScreen composable
        composable("signup") { SignUpScreen(navController) }

        // The 'dashboard' screen, which will display the DashboardScreen composable (currently a placeholder)
        composable("dashboard") { DashboardScreen(navController) }

        // The 'risk assessment' screen, which displays the RiskAssessment composable
        composable("PreviousAssessments") { PreviousAssessmentsScreen(navController, currentID) }

        // The 'risk assessment' screen, which displays the RiskAssessment composable
        composable("RiskAssessment") { RiskAssessmentScreen(navController, currentID) }

        composable("RiskResult/{riskScore}") { backStackEntry ->
            val riskScore = backStackEntry.arguments?.getString("riskScore") ?: "N/A"
            RiskResultScreen(navController, riskScore)
        }
    }
}