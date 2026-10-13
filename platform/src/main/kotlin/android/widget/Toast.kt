package android.widget

import android.content.Context

class Toast private constructor(private val text: String, private val duration: Int) {
    companion object {
        const val LENGTH_SHORT = 0
        const val LENGTH_LONG = 1

        fun makeText(context: Context?, text: CharSequence?, duration: Int): Toast =
            Toast(text?.toString() ?: "", duration)

        fun makeText(context: Context?, resId: Int, duration: Int): Toast =
            Toast(context?.getString(resId) ?: "", duration)
    }

    fun setText(text: CharSequence) {}

    fun show() {
        android.content.AppContext.toastHandler?.invoke(
            android.content.AppContext.ToastData(text, duration)
        )
    }
}
