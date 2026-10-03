package com.tyshko.auth.security.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey

class RsaKeyManager {
    private val keyStore = KeyStore.getInstance(KEY_STORE_TYPE).apply { load(null) }
    private val initialized: Boolean by lazy {
        generateKey()
        true
    }

    init {
        generateKey()
    }

    private fun generateKey() {
        if (!keyStore.containsAlias(KEY_ALIAS)){
            val keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA,
                KEY_STORE_TYPE
            )
            val parameterSpec: KeyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            )
                .setDigests(DIGEST)
                .setSignaturePaddings(SIGNATURE_PADDING)
                .setKeySize(KEY_SIZE)
                .build()

            keyPairGenerator.initialize(parameterSpec)
            keyPairGenerator.generateKeyPair()
        }
    }

    fun providePublicKey() : PublicKey {
        initialized
        return keyStore.getCertificate(KEY_ALIAS).publicKey
    }

    fun providePrivateKey() : PrivateKey{
        initialized
        return  keyStore.getKey(KEY_ALIAS, null) as PrivateKey
    }

    companion object {
        const val KEY_STORE_TYPE = "AndroidKeyStore"
        private const val KEY_ALIAS = "WebVetCare_Key"
        private const val KEY_SIZE = 2048
        private const val DIGEST = KeyProperties.DIGEST_SHA256
        private const val SIGNATURE_PADDING = KeyProperties.SIGNATURE_PADDING_RSA_PKCS1
    }

}