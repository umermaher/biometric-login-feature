package com.umermahar.biometriclogin.presentation

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import com.umermahar.biometriclogin.R
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

/**
 * Manages biometric authentication within the application.
 *
 * This class provides a centralized way to handle biometric prompt display,
 * cryptographic key management (encryption/decryption tied to biometrics),
 * and communication of authentication results. It leverages Android's
 * [BiometricPrompt] and Keystore system for secure biometric operations.
 *
 * Key functionalities include:
 * - Displaying biometric prompts for enabling authentication (encrypting data).
 * - Displaying biometric prompts for user sign-in (decrypting data).
 * - Checking if biometric authentication is available and configured on the device.
 * - Handling key invalidation due to changes in biometric enrollment (e.g., new fingerprint).
 * - Providing a reactive stream ([promptResult]) to observe authentication outcomes.
 *
 * It utilizes a [Channel] internally to communicate authentication results, which are
 * then exposed as a [kotlinx.coroutines.flow.Flow] via [promptResult]. This allows UI components
 * to reactively update based on the biometric authentication process.
 *
 * Cryptographic keys are stored in the Android Keystore under a specific alias ([KEY_ALIAS])
 * and are configured to be invalidated if the user's biometric information changes.
 * This enhances security by ensuring that previously encrypted data cannot be decrypted
 * if the biometric credentials used to encrypt it are no longer valid.
 *
 * @param activity The [AppCompatActivity] context required to display the [BiometricPrompt]
 *                 and access system services.
 */
class BiometricPromptManager(
    private val activity: AppCompatActivity,
) {

    /**
     * An instance of [BiometricManager] used to check for biometric capabilities
     * of the device (e.g., hardware availability, enrolled biometrics).
     */
    private val manager = BiometricManager.from(activity)

    /**
     * A [Channel] to send biometric authentication results.
     * This channel is used internally to communicate the outcome of biometric operations
     * to the [promptResult] flow, which can be observed by the UI.
     */
    private val resultChannel = Channel<BiometricResult>()
    val promptResult = resultChannel.receiveAsFlow()

    /**
     * [showBiometricPromptToEnableAuth] Displays a biometric prompt to the user for enabling biometric authentication.
     *
     * This function is typically used during the initial setup of biometric authentication.
     * It encrypts the provided [dataToEncrypt] using a hardware-backed key after successful
     * biometric authentication. The encrypted data, along with its initialization vector (IV),
     * is then Base64 encoded and sent through the [resultChannel] as a [BiometricResult.AuthenticationSuccess].
     *
     * If the user cancels or authentication fails, appropriate [BiometricResult] are sent.
     * This function also handles cases where the biometric key might have been invalidated
     * (e.g., due to new fingerprint enrollment) by deleting the old key and generating a new one.
     *
     * @param title The title to be displayed on the biometric prompt.
     * @param description The description or message to be displayed on the biometric prompt.
     * @param dataToEncrypt The string data that needs to be encrypted upon successful authentication.
     */
    fun showBiometricPromptToEnableAuth(
        title: String,
        description: String,
        dataToEncrypt: String
    ) {
        val promptInfo = buildPromptInfo(title, description)

        if (!canAuthenticate()) return

        val prompt = BiometricPrompt(
            activity,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    resultChannel.trySend(BiometricResult.AuthenticationError(errString.toString()))
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val cipherResult = result.cryptoObject?.cipher ?: return
                    val iv = cipherResult.iv ?: return
                    val encryptedBytes = cipherResult.doFinal(dataToEncrypt.toByteArray()) ?: return
                    val finalPayload = iv + encryptedBytes
                    val base64Payload = Base64.getEncoder().encodeToString(finalPayload)
                    resultChannel.trySend(BiometricResult.AuthenticationSuccess(base64Payload))
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    resultChannel.trySend(BiometricResult.AuthenticationFailed)
                }
            }
        )

        val cipher = getCipher()
        try {
            cipher.init(Cipher.ENCRYPT_MODE, getKey())
        } catch (e: KeyPermanentlyInvalidatedException) {
            Log.e("BiometricPromptManager", e.message.toString())
            // Key was invalidated (user added fingerprint)
            deleteKey() // delete old key
            cipher.init(Cipher.ENCRYPT_MODE, getKey()) //Re-init with new key
        }

        prompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
    }

    /**
     * Displays a biometric prompt for user authentication.
     *
     * This function is used when a user attempts to sign in using their biometric credentials.
     * It handles the decryption of a previously encrypted payload after successful authentication.
     *
     * @param title The title to be displayed on the biometric prompt.
     * @param description The description or message to be displayed on the biometric prompt.
     * @param encryptedPayloadBase64 A Base64 encoded string representing the encrypted data
     *                                 that needs to be decrypted upon successful biometric authentication.
     *                                 This payload typically contains an Initialization Vector (IV)
     *                                 prepended to the actual ciphertext.
     */
    fun showBiometricPrompt(
        title: String,
        description: String,
        encryptedPayloadBase64: String
    ) {

        val encryptedPayload = Base64.getDecoder().decode(encryptedPayloadBase64)

        val cipher = getCipher()
        // Separate IV and ciphertext
        val iv = encryptedPayload.copyOfRange(0, cipher.blockSize)
        val encryptedBytes = encryptedPayload.copyOfRange(cipher.blockSize, encryptedPayload.size)
        try {
            cipher.init(Cipher.DECRYPT_MODE, getKey(), IvParameterSpec(iv))
        } catch (e: KeyPermanentlyInvalidatedException) {
            Log.e("BiometricPromptManager", e.message.toString())
            // Key was invalidated (user added fingerprint)
            deleteKey() // delete old key
            resultChannel.trySend(BiometricResult.AuthenticationFailedDueToBiometricEnrollment)
            return
        }

        val promptInfo = buildPromptInfo(title, description)

        if (!canAuthenticate()) return

        val prompt = BiometricPrompt(
            activity,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val decryptedBytes = result.cryptoObject?.cipher?.doFinal(encryptedBytes)
                    val decryptedString = decryptedBytes?.decodeToString()
                    resultChannel.trySend(
                        if(decryptedString != null) {
                            BiometricResult.AuthenticationSuccess(decryptedString)
                        } else {
                            BiometricResult.AuthenticationError(activity.getString(R.string.something_went_wrong))
                        }
                    )
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    resultChannel.trySend(BiometricResult.AuthenticationError(errString.toString()))
                }

                override fun onAuthenticationFailed() {
                    resultChannel.trySend(BiometricResult.AuthenticationFailed)
                }
            }
        )

        prompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
    }

    /**
     * [canAuthenticate] Checks if the user can authenticate using biometrics.
     *
     * This function queries the system's [BiometricManager] to determine if biometric
     * authentication is available and configured. It also sends the result of this check
     * to the `resultChannel`.
     *
     * @return `true` if biometric authentication is possible, `false` otherwise.
     *         If `false`, a [BiometricResults] sealed class instance indicating the reason
     *         is sent to the `resultChannel`.
     *         Possible reasons for returning `false`:
     *         - [BiometricResults.HardwareUnavailable]: Biometric hardware is currently unavailable.
     *         - [BiometricResults.FeatureUnavailable]: The device does not support biometric authentication.
     *         - [BiometricResults.AuthenticationNotSet]: No biometrics are enrolled by the user.
     */
    fun canAuthenticate(): Boolean {
        return when (manager.canAuthenticate(BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                resultChannel.trySend(BiometricResult.HardwareUnavailable)
                false
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                resultChannel.trySend(BiometricResult.FeatureUnavailable)
                false
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                resultChannel.trySend(BiometricResult.AuthenticationNotSet)
                false
            }
            else -> true
        }
    }

    private fun buildPromptInfo(
        title: String,
        description: String,
    ): PromptInfo {
        val builder = PromptInfo.Builder()
            .setTitle(title)
            .setDescription(description)
            .setAllowedAuthenticators(BIOMETRIC_STRONG)
            .setNegativeButtonText(activity.getString(R.string.cancel))

        return builder.build()
    }

    private fun getCipher(): Cipher {
        return Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7)
    }

    private fun getKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        // Before the keystore can be accessed, it must be loaded.
        keyStore.load(null)
        val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        return entry?.secretKey ?: generateSecretKey()
    }

    private fun generateSecretKey(keyGenParameterSpec: KeyGenParameterSpec = getKeyGenParameterSpec()): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }

    private fun deleteKey() {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        keyStore.deleteEntry(KEY_ALIAS)
    }

    private fun getKeyGenParameterSpec() = KeyGenParameterSpec.Builder(
        KEY_ALIAS,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
        .setUserAuthenticationRequired(true)
        // Invalidate the keys if the user has registered a new biometric
        // credential, such as a new fingerprint.
        .setInvalidatedByBiometricEnrollment(true)
        .build()

    /**
     * Represents the possible outcomes of a biometric authentication attempt.
     * This sealed interface acts as a wrapper to provide a structured way
     * to handle different scenarios that can occur during biometric authentication.
     *
     * - [HardwareUnavailable]: Indicates that the biometric hardware is currently unavailable,
     *   possibly because it's being used by another application.
     * - [FeatureUnavailable]: Signifies that the biometric authentication feature is not
     *   supported or available on the current device.
     * - [AuthenticationError]: Represents a general error that occurred during the
     *   authentication process. It includes an error message for more details.
     * - [AuthenticationSuccess]: Indicates that the biometric authentication was successful.
     *   It contains the authenticated data (e.g., an encrypted string).
     * - [AuthenticationFailed]: Signifies that the user failed to authenticate using
     *   their biometrics (e.g., incorrect fingerprint).
     * - [AuthenticationNotSet]: Indicates that the user has not set up any biometric
     *   credentials on their device.
     * - [AuthenticationFailedDueToBiometricEnrollment]: Signifies that the authentication
     *   failed because the biometric enrollment has changed (e.g., a new fingerprint was added),
     *   invalidating the previously stored cryptographic key.
     */
    sealed interface BiometricResult {
        data object HardwareUnavailable : BiometricResult
        data object FeatureUnavailable : BiometricResult
        data class AuthenticationError(val error: String): BiometricResult
        data class AuthenticationSuccess(val data: String) : BiometricResult
        data object AuthenticationFailed : BiometricResult
        data object AuthenticationNotSet : BiometricResult
        data object AuthenticationFailedDueToBiometricEnrollment : BiometricResult
    }

    companion object {
        private const val KEY_ALIAS = "all_eyes_on_you"
    }
}