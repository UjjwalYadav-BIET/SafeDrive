# 🚗 SafeDrive - AI Driver Drowsiness Detection System

**SafeDrive** is a life-saving Android application designed to prevent road accidents caused by driver fatigue. It uses **Artificial Intelligence (Google ML Kit)** to monitor the driver's eyes in real-time. If the driver closes their eyes for more than 1.5 seconds, the app triggers a loud alarm and visual warning to wake them up immediately.

Built entirely with **Kotlin** and **Jetpack Compose**, SafeDrive features a high-performance, real-time camera analyzer that works offline without an internet connection.

---

## 🚀 Key Features

### 👁️ Real-Time Eye Monitoring
* Continuously scans the driver's face using the front camera.
* Uses **Google ML Kit** (Face Detection API) to detect facial landmarks and eye states.

### 🧠 Intelligent Drowsiness Logic
* Unlike simple blink detectors, SafeDrive distinguishes between **normal blinking** and **falling asleep**.
* **Algorithm:** It calculates the `Eye Open Probability`. If probability drops below **0.5** for more than **1.5 seconds**, the system triggers an alert.

### 🚨 Instant Alarm System
* Triggers a loud, looping alarm sound (`MediaPlayer`) when drowsiness is detected.
* Stops automatically when the driver opens their eyes.

### 🖥️ Heads-Up Display (HUD)
* **Safe Mode (Green):** Displays "Status: SAFE" with a green border.
* **Danger Mode (Red):** Flashes a red border with a "WAKE UP!" warning and siren icon.
* **Live Metrics:** Shows a real-time "Alertness Level" percentage bar on the dashboard.

---

## 🛠️ Tech Stack

* **Language:** [Kotlin](https://kotlinlang.org/) (100%)
* **UI Toolkit:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Modern, Reactive UI)
* **AI/ML:** [Google ML Kit](https://developers.google.com/ml-kit/vision/face-detection) (Face Detection with Classification)
* **Camera:** [CameraX](https://developer.android.com/training/camerax) (ImageAnalysis Use Case)
* **Audio:** Android MediaPlayer
* **Architecture:** MVVM + Clean Architecture principles

---

## 🧠 How It Works

1.  **Capture:** The app captures video frames using **CameraX's ImageAnalysis**.
2.  **Analyze:** Each frame is passed to the `DrowsinessAnalyzer`.
3.  **Classify:** The AI calculates `leftEyeOpenProbability` and `rightEyeOpenProbability` (0.0 to 1.0).
4.  **Decide:**
    * If `(Left < 0.5)` AND `(Right < 0.5)` → **Eyes Closed**.
    * A timer starts. If the timer exceeds **1500ms (1.5s)** → **Status: DROWSY**.
5.  **Act:** The UI turns red, and the `AlarmManager` plays the emergency sound.

---

## ⚙️ Installation & Setup

1.  **Clone the Repository**
    ```bash
    git clone [https://github.com/YourUsername/SafeDrive.git](https://github.com/YourUsername/SafeDrive.git)
    ```
2.  **Open in Android Studio**
    * File > Open > Select the `SafeDrive` folder.
    * Let Gradle sync.
3.  **Hardware Requirement**
    * You MUST run this on a **Real Android Device**.
    * *Note: Most Android Emulators do not simulate camera inputs effectively.*
4.  **Permissions**
    * Grant **Camera Permissions** when the app launches.

---

## 🔮 Future Improvements
* **Yawn Detection:** Add mouth landmark detection to identify yawning patterns.
* **Night Mode:** Enhance low-light detection using brightness correction.
* **GPS Integration:** Automatically send the driver's location to emergency contacts if they don't wake up.

---

## 👨‍💻 Author
**Ujjwal Yadav**
* [GitHub Profile](https://github.com/UjjwalYadav-BIET)
* [LinkedIn](https://www.linkedin.com/in/ujjwalyadav5437/)

---
