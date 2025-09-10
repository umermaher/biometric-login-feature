package com.umermahar.biometriclogin.data

import androidx.datastore.core.Serializer
import com.umermahar.biometriclogin.domain.Credentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import java.util.Base64

@Serializable
data class CredentialsPreferences(
    val email: String? = null,
    val encryptedPassword: String? = null
)

object CredentialsPreferencesSerializer: Serializer<CredentialsPreferences> {
    override val defaultValue: CredentialsPreferences
        get() = CredentialsPreferences()

    override suspend fun readFrom(input: InputStream): CredentialsPreferences {
        val encryptedBytes = withContext(Dispatchers.IO) {
            input.use { it.readBytes() }
        }
        val encryptedBytesDecoded = Base64.getDecoder().decode(encryptedBytes)
        val decryptedBytes = Crypto.decrypt(encryptedBytesDecoded)
        val decodedJsonString = decryptedBytes.decodeToString()
        return Json.decodeFromString(string = decodedJsonString)
    }

    override suspend fun writeTo(t: CredentialsPreferences, output: OutputStream) {
        val json = Json.encodeToString(t)
        val bytes = json.toByteArray()
        val encryptedBytes = Crypto.encrypt(bytes)
        val encryptedBytesBase64 = Base64.getEncoder().encode(encryptedBytes)
        withContext(Dispatchers.IO) {
            output.use {
                it.write(encryptedBytesBase64)
            }
        }
    }

}

internal fun CredentialsPreferences.toCredentialsDomain(): Credentials? {
    return if(email != null && encryptedPassword != null) {
        Credentials(email, encryptedPassword)
    } else {
        null
    }
}