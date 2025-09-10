# 🔐 Biometric Login Feature Sample

This repository demonstrates a complete biometric authentication flow in Android using BiometricPrompt and Android Keystore. It includes data encryption, decryption, and UI integration across multiple screens following clean architecture principles.

# 🚀 Features

✅ BiometricPromptManager – central class handling:

Enable biometric authentication

Secure data encryption & storage

Biometric prompt for login

Data decryption

Key invalidation handling (when biometrics are added/removed)

✅ Reactive Result Handling

Uses Kotlin Channels & Flow for event-driven result propagation

Producer–consumer model: BiometricPromptManager produces results, screens consume them

✅ Screens

Settings Screen – toggle to enable/disable biometric login

Enable Biometric Screen – user enters credentials, data is encrypted, and biometric key is enrolled

Login Screen – user authenticates with biometrics; on success, decrypted credentials are displayed

🖼️ Flow Overview

Settings Screen

User toggles "Enable Biometric"

Navigates to Enable Biometric Screen

Enable Biometric Screen

User enters credentials

On success, credentials are encrypted with biometric-protected key

Navigates back → toggle is now enabled

Subsequent App Launch

Login Screen is shown

Biometric prompt is displayed

On success → credentials decrypted & displayed (email + password)

🔑 Security Details

AES / CBC / PKCS7 used for encryption

Initialization Vector (IV) is appended to ciphertext for proper decryption

Keys are stored securely in Android Keystore

.setInvalidatedByBiometricEnrollment(true) ensures keys are invalidated if biometrics are added/removed

Handles KeyPermanentlyInvalidatedException gracefully by regenerating keys

📂 Project Structure
biometric-sample/
│
├── biometric/  
│   └── BiometricPromptManager.kt   # Core biometric logic
│
├── ui/  
│   ├── SettingsScreen.kt           # Toggle biometric login
│   ├── EnableBiometricScreen.kt    # Collect credentials, enable biometric
│   └── LoginScreen.kt              # Prompt user with biometric login
│
└── ...

📸 Screens

Settings Screen – toggle biometric login

Enable Biometric Screen – credential input & enabling biometric

Login Screen – biometric authentication on app launch

🛠️ Tech Stack

Kotlin

Jetpack Compose (for UI)

AndroidX Biometric API

Kotlin Coroutines (Channel + Flow)

Android Keystore (AES/CBC/PKCS7)

🤝 Contributing

This repository is intended as a sample/demo project.
Feel free to fork, raise issues, or suggest improvements 🚀.
