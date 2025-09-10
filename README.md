# 🔐 Biometric Login Feature Sample

This repository demonstrates a complete biometric authentication flow in Android using BiometricPrompt and Android Keystore. It includes data encryption, decryption, and UI integration across multiple screens following clean architecture principles.

# 🖼️ Flow Overview

https://github.com/user-attachments/assets/b9a02cc8-65d6-4ccb-8206-4e0e1c67ba3e

# 🚀 Features

✅ BiometricPromptManager – central class handling:

Enable biometric authentication

Secure data encryption & storage

Biometric prompt for login

Data decryption

Key invalidation handling (when biometrics are added/removed)

# 🔑 Security Details

AES / CBC / PKCS7 used for encryption

Initialization Vector (IV) is appended to ciphertext for proper decryption

Keys are stored securely in Android Keystore

ensures keys are invalidated if biometrics are added/removed

Handles KeyPermanentlyInvalidatedException by regenerating key
