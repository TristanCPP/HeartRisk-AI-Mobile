@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ai_mobileapp_test

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

@Composable
fun RiskAssessmentScreen(navController: NavController, currentID: Int?) {
    val context = LocalContext.current
    val dbHelper = DatabaseHelper(context)

    // Fetch user details (Age & Sex)
    var age by remember { mutableStateOf<Int?>(null) }
    var sex by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentID) {
        if (currentID != null) {
            val userDetails = dbHelper.getUserDetails(currentID)
            userDetails?.let {
                age = it.first
                sex = it.second
            }
        }
    }

    var restingBloodPressure by remember { mutableStateOf(TextFieldValue()) }
    var cholesterol by remember { mutableStateOf(TextFieldValue()) }
    var maxHeartRate by remember { mutableStateOf(TextFieldValue()) }
    var chestPainType by remember { mutableStateOf("") }
    var exerciseAngina by remember { mutableStateOf("") }
    var stSlope by remember { mutableStateOf("") }

    // Dropdown options
    val chestPainOptions = listOf("ATA", "TA", "NAP", "ASY")
    val exerciseAnginaOptions = listOf("Y", "N")
    val stSlopeOptions = listOf("Up", "Down", "Flat")

    // Oldpeak conversion
    val fatigueLevels = (1..10).toList()
    val oldpeakMapping = mapOf(
        1 to 0.0, 2 to 0.4, 3 to 0.8, 4 to 1.2, 5 to 1.5,
        6 to 1.8, 7 to 2.2, 8 to 2.6, 9 to 3.0, 10 to 4.0
    )
    var selectedFatigueLevel by remember { mutableIntStateOf(1) }

    var errorMessage by remember { mutableStateOf<String?>(null) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Risk Assessment", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("dashboard") }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1290D5))
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp) .verticalScroll(rememberScrollState()) )// Enable scrolling
        {

            FeatureInputField("Resting Blood Pressure", restingBloodPressure, "Your blood pressure when at rest.\nIdeal: 90-120 mmHg (systolic)."
            ) { restingBloodPressure = it }
            FeatureInputField("Cholesterol", cholesterol, "The level of fat in your blood. \nIdeal: Less than 200 mg/dL.") { cholesterol = it }
            FeatureInputField("Max Heart Rate", maxHeartRate, "The highest your heart rate reaches during exercise. \nIdeal: 220 - your age.") { maxHeartRate = it }
            Spacer(modifier = Modifier.height(16.dp))

            DropdownMenuWithInfo("Chest Pain Type", "Select the type of chest pain you experience:\n\n" +
                    "- ATA (Atypical Angina): Mild discomfort, not typical heart-related pain\n\n" +
                    "- TA (Typical Angina): Classic heart pain, worsens with activity, relieved by rest\n\n" +
                    "- NAP (Non-Anginal Pain): Chest pain unrelated to the heart (e.g., muscle strain)\n\n" +
                    "- ASY (Asymptomatic): No chest pain"

                , chestPainType, chestPainOptions) { chestPainType = it }
            Spacer(modifier = Modifier.height(16.dp))

            DropdownMenuWithInfo("Exercise Angina", "Do you experience chest pain during exercise? (Y/N)", exerciseAngina, exerciseAnginaOptions) { exerciseAngina = it }
            Spacer(modifier = Modifier.height(16.dp))

            DropdownMenuWithInfo("ST Slope", "How does your chest feel during or after physical activity?\n\n" +
                    "- (Up) No Chest Pain, or Improves with activity (e.g., discomfort fades as you move) → Healthy response \n\n" +
                    "- (Down) Worsens with activity (e.g., pain increases with exertion) → Higher risk \n\n" +
                    "- (Flat) Stays the same (e.g., no change in discomfort) → Could indicate a concern", stSlope, stSlopeOptions) { stSlope = it }
            Spacer(modifier = Modifier.height(16.dp))

            OldpeakDropdownMenu(fatigueLevels, selectedFatigueLevel) { selectedFatigueLevel = it }
            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    if (age == null || sex == null || currentID == null) {
                        Toast.makeText(context, "User details are missing!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (restingBloodPressure.text.isEmpty() || cholesterol.text.isEmpty() || maxHeartRate.text.isEmpty()) {
                        Toast.makeText(context, "Please fill in all input fields.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (chestPainType.isEmpty() || exerciseAngina.isEmpty() || stSlope.isEmpty()) {
                        Toast.makeText(context, "Please select all dropdown options.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val oldpeakValue = oldpeakMapping[selectedFatigueLevel] ?: 0.0
                    sendRiskAssessmentRequest(
                        age!!,
                        sex!!,
                        chestPainType,
                        restingBloodPressure.text,
                        cholesterol.text,
                        maxHeartRate.text,
                        exerciseAngina,
                        oldpeakValue.toString(),
                        stSlope, context
                    ) { response ->
                        val riskScoreValue = parseRiskScore(response)
                        val success = dbHelper.insertRiskAssessment(
                            userID = currentID, chestPainType, restingBloodPressure.text.toIntOrNull() ?: 0,
                            cholesterol.text.toIntOrNull() ?: 0, maxHeartRate.text.toIntOrNull() ?: 0,
                            exerciseAngina, oldpeakValue, stSlope, riskScoreValue
                        )
                        if (success) {
                            Toast.makeText(context, "Assessment saved!", Toast.LENGTH_SHORT).show()
                            navController.navigate("RiskResult/$riskScoreValue")
                        } else {
                            Toast.makeText(context, "Failed to save assessment.", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1290D5),
                    contentColor = Color.White
                )
            ) {
                Text("Submit")
            }

        }
    }
}
fun parseRiskScore(response: String): Double {
    val regex = "<RiskScore>(.*?)</RiskScore>".toRegex()
    return regex.find(response)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
}

@Composable
fun DropdownMenuWithInfo(label: String, info: String, selectedValue: String, options: List<String>, onValueChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(imageVector = Icons.Default.Info, contentDescription = "$label Info", tint = Color.Gray, modifier = Modifier.size(18.dp).clickable { showDialog = true })
        }
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(if (selectedValue.isEmpty()) "Click here for options" else selectedValue, color = Color(0xFF1290D5)) // Change to desired color
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = {
                    onValueChange(option)
                    expanded = false
                })
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Information") },
            text = { Text(info) },
            confirmButton = { TextButton(onClick = { showDialog = false }) { Text("OK") } }
        )
    }
}

@Composable
fun FeatureInputField(label: String, textState: TextFieldValue, info: String, onValueChange: (TextFieldValue) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(onClick = { showDialog = true }, modifier = Modifier.size(18.dp)) {
                Icon(imageVector = Icons.Default.Info, contentDescription = "Info", tint = Color.Gray)
            }
        }
        OutlinedTextField(
            value = textState,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Information") },
            text = { Text(info) },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun DropdownMenuBox(options: List<Int>, selectedOption: Int, onOptionSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(if (selectedOption == 0) "Click here for options" else "Fatigue Level: $selectedOption", color = Color(0xFF1290D5))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option.toString()) }, onClick = {
                    onOptionSelected(option)
                    expanded = false
                })
            }
        }
    }
}

@Composable
fun OldpeakDropdownMenu(fatigueLevels: List<Int>, selectedFatigueLevel: Int, onSelectionChanged: (Int) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Oldpeak", style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Oldpeak Info",
                tint = Color.Gray,
                modifier = Modifier
                    .size(18.dp)
                    .clickable { showDialog = true }
            )
        }

        DropdownMenuBox(
            options = fatigueLevels,
            selectedOption = selectedFatigueLevel,
            onOptionSelected = onSelectionChanged
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Information") },
            text = {
                Text(
                    "How much do you struggle with physical activity (e.g., exercise, climbing stairs)?\n\n" +
                            "- (0-1): No difficulty\n\n" +
                            "- (2-4): Mild difficulty, shortness of breath\n\n" +
                            "- (5-7): Significant difficulty, may need to stop activity\n\n" +
                            "- (8-10): Extreme Difficulty, cannot continue activity"
                )
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}


fun sendRiskAssessmentRequest(
    age: Int, sex: String,
    chestPainType: String,
    restingBP: String,
    cholesterol: String,
    maxHR: String,
    exerciseAngina: String,
    oldpeak: String,
    stSlope: String,
    context: android.content.Context,
    onResponse: (String) -> Unit
) {
    val apiUrl = "http://192.168.1.70:5000/predict"

    val xmlRequest = """
        <HeartRiskRequest>
            <Age>$age</Age>
            <Sex>$sex</Sex>
            <ChestPainType>$chestPainType</ChestPainType>
            <RestingBP>$restingBP</RestingBP>
            <Cholesterol>$cholesterol</Cholesterol>
            <MaxHR>$maxHR</MaxHR>
            <ExerciseAngina>$exerciseAngina</ExerciseAngina>
            <Oldpeak>$oldpeak</Oldpeak>
            <ST_Slope>$stSlope</ST_Slope>
        </HeartRiskRequest>
    """.trimIndent()

    Log.d("XML_REQUEST", xmlRequest)

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val client = HttpClient()
            val response: HttpResponse = client.post(apiUrl) {
                contentType(ContentType.Application.Xml)
                setBody(xmlRequest.toByteArray(Charsets.UTF_8))
            }

            val responseBody = response.bodyAsText()

            withContext(Dispatchers.Main) {
                onResponse(responseBody)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRiskAssessmentScreen() {
    RiskAssessmentScreen(navController = NavController(LocalContext.current), currentID = null)
}