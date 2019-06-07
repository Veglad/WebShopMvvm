package com.sigma.vshcheglov.webshop.extensions

import android.app.KeyguardManager
import android.content.Context
import android.content.Context.KEYGUARD_SERVICE
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import com.sigma.vshcheglov.webshop.presentation.helpres.FingerprintState

fun Context.canUseFingerprint(): Boolean {
    return FingerprintManagerCompat.from(this).isHardwareDetected
}

fun Context.getFingerprintSensorState(): FingerprintState {
    if (canUseFingerprint()) {
        val keyguardManager = this.getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        if (!keyguardManager.isKeyguardSecure) {
            return FingerprintState.NOT_BLOCKED
        }
        val fingerprintManager = FingerprintManagerCompat.from(this)
        return if (!fingerprintManager.hasEnrolledFingerprints()) {
            FingerprintState.NO_FINGERPRINTS
        } else FingerprintState.READY
    } else {
        return FingerprintState.NOT_SUPPORTED
    }
}