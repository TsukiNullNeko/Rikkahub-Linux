package androidx.lifecycle

/**
 * Desktop stand-in for androidx ProcessLifecycleOwner, backed by a real LifecycleRegistry
 * (the Android artifact is Android-only).
 */
object ProcessLifecycleOwner {
    private val owner: LifecycleOwner = object : LifecycleOwner {
        private val registry = LifecycleRegistry(this)
        override val lifecycle: Lifecycle get() = registry
    }

    val lifecycle: Lifecycle get() = owner.lifecycle

    fun get(): ProcessLifecycleOwner = this

    /** Marks the process as started/resumed (called once the main window is shown). */
    fun markStarted() {
        (owner.lifecycle as LifecycleRegistry).currentState = Lifecycle.State.STARTED
    }

    fun markResumed() {
        (owner.lifecycle as LifecycleRegistry).currentState = Lifecycle.State.RESUMED
    }
}
