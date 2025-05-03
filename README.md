# AI-Powered Heart Disease Risk Prediction - Mobile Application (Frontend)

![Project Badge](https://img.shields.io/badge/Frontend-Kotlin%2FAndroidStudio-blue)

## Overview

This is the mobile frontend of our AI-Powered Heart Disease Risk Prediction system. The app allows users to input their health information, sends it to a machine learning backend for processing, and displays the predicted risk of Coronary Heart Disease (CHD) along with lifestyle recommendations.

The application is built using **Kotlin** in **Android Studio**, communicates with a Flask backend over XML, and uses **SQLite** for local data storage and previous assessment tracking.

---

## Features

- **User Authentication**: Sign Up, Login, and local account management.
- **Risk Prediction Form**: Users enter health metrics used to calculate CHD risk.
- **XML-based Communication**: Sends health data as XML to the backend server via HTTP.
- **Results Screen**: Displays CHD risk score, color-coded risk category, and personalized recommendations.
- **Previous Assessments**: Stores and displays user's past risk evaluations using SQLite.
- **Fully Responsive UI**: Designed with Jetpack Compose for modern and responsive UI on Android devices.

---

## Technologies Used

- **Language**: Kotlin
- **IDE**: Android Studio (Electric Eel or newer)
- **UI**: Jetpack Compose
- **Local Storage**: SQLite (Android Room not used for simplicity)
- **XML Format**: Used to structure requests/responses between mobile and backend

---

## Setup Instructions

### Prerequisites

- Android Studio installed
- An Android emulator or physical device running Android 7.0 (API 24) or newer
- Ensure the backend server is running on the **same local network** or deployed to a remote host

### How to Run

1. Clone this repository into Android Studio.
2. Open the project and let it sync Gradle dependencies.
3. Connect a device or start the emulator.
4. Update the IP address of the backend server in `RiskAssessment.kt`:

```kotlin
val apiUrl = "http://192.168.X.X:5000/predict"
```

5. Run the application.

---

## Project Structure

```
frontend/
│
├── MainActivity.kt                 # Root navigation host
├── screens/
│   ├── SignIn.kt                   # Sign-in screen
│   ├── SignUp.kt                   # Registration screen
│   ├── Dashboard.kt                # Home dashboard
│   ├── RiskAssessment.kt           # Risk assessment form and request logic
│   ├── RiskResult.kt               # Results display screen
│   ├── PreviousAssessments.kt      # View previous risk history
│
├── database/
│   └── DatabaseHelper.kt           # SQLite helper class for storing users and assessments
│
├── res/
│   ├── drawable/                   # Image assets (e.g., heart image)
│   ├── values/                     # Colors, themes, styles
│
└── AndroidManifest.xml             # Application manifest
```

---

## Notes

- **Device Permissions**: No special permissions are required.
- **Network Configuration**: Cleartext traffic (`HTTP`) is allowed only for local testing. Use `HTTPS` in production.
- **XML Format Example**:

```xml
<HeartRiskRequest>
    <Age>45</Age>
    <Sex>M</Sex>
    <ChestPainType>ASY</ChestPainType>
    <RestingBP>130</RestingBP>
    <Cholesterol>210</Cholesterol>
    <MaxHR>150</MaxHR>
    <ExerciseAngina>Y</ExerciseAngina>
    <Oldpeak>1.2</Oldpeak>
    <ST_Slope>Up</ST_Slope>
</HeartRiskRequest>
```

---

## Future Improvements

- Migrate to **HTTPS** for production deployment.
- Improve input validation and error handling.
- Consider implementing **Room** or **Jetpack DataStore** for more scalable local storage.
- Integrate charts for visualizing past risk scores.


---

## Contributors
**Team Members**: Tristan Garner, Muhsen AbuMuhsen, Chase Lillard  
**Advisor**: Dr. Ahmad Al-Shami
