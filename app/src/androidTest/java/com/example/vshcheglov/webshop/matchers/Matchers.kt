package com.example.vshcheglov.webshop.matchers

import android.view.View
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import com.google.android.material.textfield.TextInputLayout


fun hasTextInputLayoutErrorText(expectedErrorText: String): TypeSafeMatcher<View> {
    return object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description?) {}

        override fun matchesSafely(item: View?): Boolean {
            if (item !is TextInputLayout) {
                return false
            }

            val error = item.error ?: return false

            return expectedErrorText == error
        }
    }
}