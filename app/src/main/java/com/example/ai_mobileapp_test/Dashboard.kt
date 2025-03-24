package com.example.ai_mobileapp_test

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun DashboardScreen(navController: NavController) {
    val healthTips = listOf(
        "Exercise strengthens your heart and helps maintain a healthy weight.",
        "A balanced diet of fruits, vegetables, and healthy fats supports heart health.",
        "Quitting smoking reduces your risk of heart disease.",
        "Maintaining a healthy weight lowers strain on your heart.",
        "Managing stress is key for heart health."
    )

    var currentTip by remember { mutableStateOf(healthTips[0]) }

    // Change the health tip every 10 seconds
    LaunchedEffect(Unit) {
        var index = 0
        while (true) {
            delay(10000) // 10 seconds delay
            index = (index + 1) % healthTips.size // Cycle through the tips
            currentTip = healthTips[index]
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(
                colors = listOf(Color(0xFF3E70B9), Color(0xFFCADCCB))
            ))
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Text
            Text(
                text = "Heart Risk Dashboard",
                fontSize = 34.sp,
                color = Color(0xFF3E70B9),
                modifier = Modifier.padding(top = 40.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Heart Image under the title
            Image(
                painter = painterResource(id = R.drawable.heart_image),  // Replace with your image resource
                contentDescription = "Heart Health Image",
                modifier = Modifier
                    .fillMaxWidth()  // Make the image fill the entire width
                    .height(130.dp)  // Adjust size as necessary
                    .padding(horizontal = 32.dp)  // Padding for better alignment
            )
            Spacer(modifier = Modifier.height(16.dp))

            // CHD Information Section with Custom Background Color
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(
                        Color(0xFFB3E5FC),
                        RoundedCornerShape(24.dp)
                    )  // New background color for the box
                    .padding(24.dp) // Adjusted padding for better spacing inside the box
            ) {

                Text(
                    text = "Coronary Heart Disease (CHD) is a leading cause of heart-related complications. Regular assessments can help manage risks effectively.",
                    fontSize = 16.sp,
                    color = Color(0xFF3F51B5) // Darker text color for readability
                )
            }

            // Heart Health Tips Section with Custom Gradient Background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF9FD5EC), Color(0xFF6DB96D))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )  // Gradient background for the box
                    .padding(16.dp) // Padding inside the box
            ) {
                Column {
                    Text(
                        text = "Heart Health Tip:",
                        fontSize = 20.sp,
                        color = Color(0xDD168619),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = currentTip, // Dynamic health tip
                        fontSize = 16.sp,
                        color = Color(0xFF3F51B5),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Buttons Column with rounded corners
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 90.dp)
            ) {
                DashboardButton(
                    text = "Previous Assessments",
                    icon = Icons.Default.History
                ) { navController.navigate("PreviousAssessments") }

                Spacer(modifier = Modifier.height(16.dp))

                DashboardButton(
                    text = "Start A New Assessment",
                    icon = Icons.Default.Assessment
                ) { navController.navigate("RiskAssessment") }
                Spacer(modifier = Modifier.height(16.dp)) // Space before logout button

                // Log Out Button
                Button(
                    onClick = { navController.navigate("signin") },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)), // Red for emphasis
                    modifier = Modifier

                        .height(50.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    Text("Log Out", fontSize = 18.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun DashboardButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),  // Rounded corners for buttons
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1290D5)), // Solid color for button background
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(horizontal = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = text, fontSize = 18.sp, color = Color.White)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDashboard() {
    DashboardScreen(navController = NavController(LocalContext.current))
}