package com.tyshko.auth.security.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey

class RsaKeyManager {
    private val keyStore = KeyStore.getInstance(keyStoreType).apply { load(null) }
    private val alias = "WebVetCare_Key"

    init {
        generateKey()
    }

    private fun generateKey() {
        if (!keyStore.containsAlias(alias)){
            val keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA,
                alias
            )
            val parameterSpec: KeyGenParameterSpec = KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            )
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                .setKeySize(2048)
                .build()

            keyPairGenerator.initialize(parameterSpec)
            keyPairGenerator.generateKeyPair()
        }
    }

    fun providePublicKey() : PublicKey {
        return keyStore.getCertificate(alias).publicKey
    }

    fun providePrivateKey() : PrivateKey{
        return  keyStore.getKey(alias, null) as PrivateKey
    }

    companion object{
        const val keyStoreType = "AndroidKeyStore"
    }

}