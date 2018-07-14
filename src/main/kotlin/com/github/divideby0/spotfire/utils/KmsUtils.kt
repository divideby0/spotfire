package com.github.divideby0.spotfire.utils

import com.amazonaws.services.kms.AWSKMS
import com.amazonaws.services.kms.AWSKMSClientBuilder
import com.amazonaws.services.kms.model.DecryptRequest
import java.nio.ByteBuffer
import java.util.*

object KmsService {
    val kmsClient: AWSKMS = AWSKMSClientBuilder.defaultClient()

    fun getDecryptedKmsValue(key: String): String {
        val bytes: ByteBuffer = ByteBuffer.wrap(Base64.getDecoder().decode(key))
        val request: DecryptRequest = DecryptRequest().withCiphertextBlob(bytes)

        val response: ByteBuffer = kmsClient.decrypt(request).plaintext
        val byteArray = ByteArray(response.remaining())
        response.get(byteArray)
        return String(byteArray)
    }

    fun getDecryptedKmsEnvvar(envvarName: String): String {
        val ciphertext = System.getenv(envvarName)
        return getDecryptedKmsValue(ciphertext)
    }
}
