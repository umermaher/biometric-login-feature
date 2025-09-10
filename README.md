# 🔐 Biometric Login Feature Sample

This repository demonstrates a complete biometric authentication flow in Android using BiometricPrompt and Android Keystore. It includes data encryption, decryption, and UI integration across multiple screens.

# 🖼️ Flow Overview

https://github.com/user-attachments/assets/b9a02cc8-65d6-4ccb-8206-4e0e1c67ba3e

# 🚀 Features

✅ BiometricPromptManager – central class handling:

🔐 Enable Biometric Authentication

🛡️ Secure Data Encryption & Storage – AES encryption with Android Keystore

👆 Biometric Login – seamless biometric authentication flow

🔓 Data Decryption – securely retrieve and decrypt stored credentials

🧩 Handling Multiple Use-Cases

# 🔑 Security Details

AES / CBC / PKCS7 used for encryption

Initialization Vector (IV) is appended to ciphertext for proper decryption

Keys are stored securely in Android Keystore

ensures keys are invalidated if biometrics are added/removed

Handles KeyPermanentlyInvalidatedException by regenerating key
