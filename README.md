# 🔐 Biometric Login Feature Sample

This repository demonstrates a complete biometric authentication flow in Android using BiometricPrompt and Android Keystore. It includes data encryption, decryption, and UI integration across multiple screens following clean architecture principles.

# 🚀 Features

✅ BiometricPromptManager – central class handling:

Enable biometric authentication

Secure data encryption & storage

Biometric prompt for login

Data decryption

Key invalidation handling (when biometrics are added/removed)

# 🖼️ Flow Overview

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

# 🔑 Security Details

AES / CBC / PKCS7 used for encryption

Initialization Vector (IV) is appended to ciphertext for proper decryption

Keys are stored securely in Android Keystore

.setInvalidatedByBiometricEnrollment(true) ensures keys are invalidated if biometrics are added/removed

Handles KeyPermanentlyInvalidatedException gracefully by regenerating key
