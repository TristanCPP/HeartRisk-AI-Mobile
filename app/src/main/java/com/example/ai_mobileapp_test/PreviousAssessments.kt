package com.example.ai_mobileapp_test

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviousAssessmentsScreen(navController: NavController, currentID: Int?) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }
    var assessments by remember { mutableStateOf(emptyList<DatabaseHelper.RiskAssessment>()) }

    val oldpeakMapping = mapOf(
        1 to 0.0, 2 to 0.4, 3 to 0.8, 4 to 1.2, 5 to 1.5,
        6 to 1.8, 7 to 2.2, 8 to 2.6, 9 to 3.0, 10 to 4.0
    )

    fun convertOldpeakToNumber(oldpeak: Double): Int {
        return oldpeakMapping.minByOrNull { (_, value) -> kotlin.math.abs(value - oldpeak) }?.key ?: 1
    }

    fun formatDate(raw: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val date = inputFormat.parse(raw)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            raw.split(" ").firstOrNull() ?: raw
        }
    }

    fun getRiskColor(score: Double): Color {
        return when {
            score < 33.33 -> Color(0xFF228B22) // Dark Green for Low Risk
            score < 66.66 -> Color(0xFFFFA500) // Orange for Moderate Risk
            else -> Color(0xFFD50606) // Red for High Risk
        }
    }


    LaunchedEffect(currentID) {
        currentID?.let {
            assessments = dbHelper.getUserAssessments(it).reversed() // Most recent first
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Previous Assessments",color=Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("dashboard") }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back to Dashboard",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1290D5))
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (assessments.isEmpty()) {
                Text("No assessments found", modifier = Modifier.padding(16.dp))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    items(assessments) { assessment ->
                        val formattedDate = formatDate(assessment.date)
                        val riskColor = getRiskColor(assessment.riskScore)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = formattedDate,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.DarkGray,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = "Risk Score: ${assessment.riskScore}%",
                                    color = riskColor,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("Chest Pain Type: ${assessment.chestPainType}")
                                Text("Resting BP: ${assessment.restingBP}")
                                Text("Cholesterol: ${assessment.cholesterol}")
                                Text("Max Heart Rate: ${assessment.maxHeartRate}")
                                Text("Exercise Angina: ${assessment.exerciseAngina}")
                                Text("Oldpeak: ${convertOldpeakToNumber(assessment.oldpeak)}")
                                Text("ST Slope: ${assessment.stSlope}")
                            }
                        }
                    }
                }
            }
        }
    }
}
