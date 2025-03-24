@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.ai_mobileapp_test

import android.annotation.SuppressLint
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController

@SuppressLint("DefaultLocale")
@Composable
fun RiskResultScreen(navController: NavController, riskScore: String) {
    // Extract numerical value safely
    val riskScoreValue = riskScore.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0
    val formattedRiskScore = String.format("%.2f", riskScoreValue) + "%"
    val riskCategory = getRiskCategory(riskScoreValue)
    val recommendations = getRecommendations(riskCategory)
    val riskColor = getRiskColor(riskCategory)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Risk Assessment Result", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = riskColor)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()) // Enable scrolling
                .padding(horizontal = 24.dp, vertical = 16.dp), // Adjusted padding for balance
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp)) // Small top spacing

            // CHD Risk Score Header
            Text(
                text = "CHD Risk Score:",
                style = MaterialTheme.typography.headlineMedium, // Medium-sized heading
                color = Color.Black,
                modifier = Modifier.padding(bottom = 4.dp) // Small space below header
            )

            // Risk Score Value with background color for emphasis
            Text(
                text = formattedRiskScore,
                style = MaterialTheme.typography.displayMedium.copy(fontSize = 48.sp), // Large emphasis text
                color = riskColor,
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .background(riskColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)) // Soft background color
                    .padding(16.dp) // Padding inside the background box
            )

            // **Risk Category - Enlarged for visibility**
            Text(
                text = "Risk Category: $riskCategory",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp), // **Increased font size**
                color = riskColor,
                modifier = Modifier.padding(bottom = 20.dp) // **Slightly more spacing**
            )

            Divider(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), color = Color.Gray) // Subtle separator

            // Recommendations Section - Box with rounded corners and background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color(0xFFF1F1F1), RoundedCornerShape(16.dp))  // Rounded corners for the box
                    .padding(16.dp) // Padding inside the box
            ) {
                Column {
                    Text(
                        text = "Recommendations:",
                        style = MaterialTheme.typography.titleMedium, // Slightly larger subheading
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    recommendations.forEach { recommendation ->
                        Text(
                            text = "• $recommendation",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f)) // Pushes the button to the bottom

            // Home Button with rounded corners and background color
            Button(
                onClick = { navController.navigate("dashboard") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1290D5)),
                modifier = Modifier
                    .fillMaxWidth(0.6f) // Make button a bit wider
                    .padding(bottom = 24.dp) // Adds bottom padding
                    .height(60.dp) // Adjust height for better aesthetics
            ) {
                Text("Return to Home", color = Color.White, fontSize = 18.sp)
            }
        }
    }
}

fun getRiskCategory(score: Double): String {
    return when {
        score < 20 -> "Low Risk"
        score < 40 -> "Slight Risk"
        score < 60 -> "Moderate Risk"
        score < 80 -> "High Risk"
        else -> "Extreme Risk"
    }
}

fun getRecommendations(category: String): List<String> {
    return when (category) {
        "Low Risk" -> listOf(
            "Maintain a balanced diet with plenty of fruits, vegetables, and lean proteins.",
            "Stay active with at least 150 minutes of moderate exercise per week.",
            "Continue regular health checkups to monitor heart health."
        )
        "Slight Risk" -> listOf(
            "Monitor your cholesterol and blood pressure levels regularly.",
            "Exercise at least 30 minutes a day, 5 times a week (walking, jogging, or cycling).",
            "Limit processed foods and reduce sugar and salt intake."
        )
        "Moderate Risk" -> listOf(
            "Schedule a consultation with your doctor for further evaluation.",
            "Reduce consumption of high-fat and fried foods; focus on heart-healthy fats (olive oil, nuts, fish).",
            "Incorporate stress management techniques such as meditation or yoga.",
            "Increase your daily activity level; aim for at least 45 minutes of exercise."
        )
        "High Risk" -> listOf(
            "See a cardiologist for a comprehensive heart health assessment.",
            "Consider medication as prescribed by a healthcare professional to manage cholesterol or blood pressure.",
            "Adopt a heart-healthy diet: avoid red meat, high sodium, and sugary drinks.",
            "Monitor symptoms closely, such as chest pain or shortness of breath, and seek medical attention if they worsen."
        )
        "Extreme Risk" -> listOf(
            "Seek **immediate medical attention!**",
            "Emergency consultation is required to assess the risk of heart complications.",
            "Follow your doctor’s instructions strictly and consider hospitalization if necessary.",
            "Avoid any strenuous physical activity until cleared by a medical professional."
        )
        else -> listOf("No recommendations available.")
    }
}


fun getRiskColor(category: String): Color {
    return when (category) {
        "Low Risk" -> Color(0xFF228B22) // Dark Green
        "Slight Risk" -> Color(0xFFFFD700) // Gold (More distinct from Orange)
        "Moderate Risk" -> Color(0xFFFF8C00) // Darker Orange (More distinct from Yellow)
        "High Risk" -> Color(0xFFD50606) // Crimson Red (Less bright, more readable)
        "Extreme Risk" -> Color(0xFF8B0000) // Dark Red (No Change)
        else -> Color.Gray
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewRiskResultScreen() {
    val navController = rememberNavController()
    RiskResultScreen(navController = navController, riskScore = "45")
}


