package com.sigma.vshcheglov.webshop.extensions

import android.util.Patterns

fun String.isEmailValid() =
    isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.isPasswordValid(): Boolean {
    val MIN_PASSWORD_LENGTH = 6
    return length >= MIN_PASSWORD_LENGTH && isNotEmpty()
}

fun String.isCardNumberValid(): Boolean {
    val MIN_CARD_NUMBER_LENGTH = 10
    val MAX_CARD_NUMBER_LENGTH = 20
    return all { char -> char.isDigit() } && length in MIN_CARD_NUMBER_LENGTH..MAX_CARD_NUMBER_LENGTH
}

fun String.isCvvValid(): Boolean {
    val CV_LENGTH = 3
    return all { char -> char.isDigit() } && length == CV_LENGTH
}
