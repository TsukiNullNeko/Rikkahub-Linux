package android.view.inputmethod

/** Desktop no-op input method manager (the desktop handles text input natively). */
class InputMethodManager {
    companion object {
        const val SHOW_IMPLICIT = 1
        const val SHOW_FORCED = 2
        const val RESULT_UNCHANGED_SHOWN = 0
        const val RESULT_UNCHANGED_HIDDEN = 1
        const val RESULT_SHOWN = 2
        const val RESULT_HIDDEN = 3
    }

    fun showSoftInput(view: Any?, flags: Int): Boolean = false
    fun hideSoftInputFromWindow(windowToken: Any?, flags: Int): Boolean = false
    fun toggleSoftInput(showFlags: Int, hideFlags: Int) {}
    fun isActive(): Boolean = false
    fun isAcceptingText(): Boolean = false
    fun restartInput(view: Any?) {}
}
