# FinTrack - Android Finance Tracker App

<div align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.webp" alt="FinTrack Logo" width="150"/>
</div>
<br>

**FinTrack** is a comprehensive and modern Android application designed to help users manage their personal finances with ease. Built with Java and integrated with Firebase, it offers a secure, real-time, and intuitive platform for tracking expenses, managing budgets, and achieving savings goals.

---

## 📋 Features

FinTrack comes packed with features to provide a complete financial management experience:

* **Authentication**: Secure user registration and login using Firebase Authentication (Email & Password).
* **Dashboard**: An insightful home screen that provides a quick overview of your finances, including:
    * Total balance across all accounts.
    * Monthly income vs. expenses summary.
    * Interactive Pie Chart visualizing expenses by category.
    * Bar Chart comparing total monthly income and expenses.
    * A list of recent transactions.
* **Transaction Management**: Easily add, view, edit, and delete income or expense transactions.
* **Budgeting**: Set monthly budgets for different expense categories and track your spending against them. The progress bar color changes as you approach your limit (green -> yellow -> red).
* **Savings Goals**: Create and manage savings goals. Track your progress and get motivational suggestions as you save more.
* **Account & Category Management**: Add, edit, and delete custom accounts (e.g., Bank, Cash) and expense/income categories.
* **User Profile**: A dedicated profile section to manage user details, security settings, and app data.
* **Security**: Enhance security with a 6-digit PIN lock and Biometric (fingerprint/face) authentication to protect your financial data.
* **Data Export**: Export your transaction data to a CSV file for offline analysis or record-keeping.

---

## 🛠️ Technologies & Libraries Used

This project is built using modern Android development practices and a robust set of libraries:

* **Programming Language**: **Java**
* **Backend & Database**: **Firebase**
    * **Firebase Authentication**: For secure user management.
    * **Cloud Firestore**: As the real-time, NoSQL database for storing all user data.
* **UI & Design**:
    * **Material Design 3**: For modern and intuitive user interface components.
    * **MPAndroidChart**: For creating beautiful and interactive charts (Pie & Bar charts) on the dashboard.
* **Security**:
    * **AndroidX Biometric**: For fingerprint and face authentication.

---

## 🚀 Getting Started

Follow these instructions to get a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

* **Android Studio**: Make sure you have the latest version of Android Studio installed.
* **Firebase Project**: You'll need a Firebase project to connect the app to a backend.

### Setup and Installation

1.  **Clone the Repository**
    ```bash
    git clone [https://github.com/Viranya2006/FinTrack.git](https://github.com/Viranya2006/FinTrack.git)
    ```

2.  **Open in Android Studio**
    * Open Android Studio.
    * Click on `File > Open` and navigate to the cloned repository directory.
    * Let Android Studio sync the project dependencies.

3.  **Connect to Firebase**
    * Go to the [Firebase Console](https://console.firebase.google.com/) and create a new project.
    * Add a new Android app to your Firebase project with the package name `com.example.fintrack`.
    * Follow the setup steps to download the `google-services.json` file.
    * **Important**: Place the downloaded `google-services.json` file into the `FinTrack/app/` directory of the project.

4.  **Enable Firebase Services**
    * In the Firebase Console, navigate to the **Authentication** section and enable the **Email/Password** sign-in method.
    * Navigate to the **Firestore Database** section and create a database. Start in **test mode** for easy setup (you can configure security rules later).

5.  **Run the Application**
    * Connect an Android device or start an emulator.
    * Click the `Run 'app'` button in Android Studio.

The app should now build and run on your device/emulator. You can create an account and start using FinTrack!
