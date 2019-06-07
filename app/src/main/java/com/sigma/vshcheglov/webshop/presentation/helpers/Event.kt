package com.sigma.vshcheglov.webshop.presentation.helpres

//Single EventWithContent is designed to be used with the LiveData for performing single event (showing snackbars etc)
class EventWithContent<out T> (private val content: T) {
    var hasBeenHandled = false
    private set

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}

class Event {
    var hasBeenHandled = false
        private set

    fun performEventIfNotHandled(onFirstlyEventHandled :() -> Unit) {
        if (!hasBeenHandled) {
            hasBeenHandled = true
            onFirstlyEventHandled.invoke()
        }
    }
}