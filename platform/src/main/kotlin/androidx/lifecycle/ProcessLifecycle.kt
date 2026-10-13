package androidx.lifecycle

/**
 * Minimal desktop stand-in for the Android process lifecycle owner.
 */
object ProcessLifecycleOwner {
    val lifecycle: Lifecycle = Lifecycle()

    fun get(): ProcessLifecycleOwner = this
}

class Lifecycle {
    enum class State { DESTROYED, INITIALIZED, CREATED, STARTED, RESUMED }

    enum class Event { ON_CREATE, ON_START, ON_RESUME, ON_PAUSE, ON_STOP, ON_DESTROY, ON_ANY }

    private val observers = mutableListOf<LifecycleEventObserver>()
    private var currentState: State = State.RESUMED

    val currentStateValue: State get() = currentState

    fun addObserver(observer: LifecycleObserver) {
        if (observer is LifecycleEventObserver) {
            observers.add(observer)
            observer.onStateChanged(this, Event.ON_CREATE)
            observer.onStateChanged(this, Event.ON_START)
            observer.onStateChanged(this, Event.ON_RESUME)
        }
    }

    fun removeObserver(observer: LifecycleObserver) {
        observers.remove(observer)
    }

    fun getCurrentState(): State = currentState

    fun handleLifecycleEvent(event: Event) {
        currentState = when (event) {
            Event.ON_CREATE -> State.CREATED
            Event.ON_START -> State.STARTED
            Event.ON_RESUME -> State.RESUMED
            Event.ON_PAUSE -> State.STARTED
            Event.ON_STOP -> State.CREATED
            Event.ON_DESTROY -> State.DESTROYED
            Event.ON_ANY -> currentState
        }
        observers.forEach { it.onStateChanged(this, event) }
    }
}

interface LifecycleObserver

fun interface LifecycleEventObserver : LifecycleObserver {
    fun onStateChanged(source: Lifecycle, event: Lifecycle.Event)
}

abstract class DefaultLifecycleObserver : LifecycleEventObserver {
    override fun onStateChanged(source: Lifecycle, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> onCreate(source)
            Lifecycle.Event.ON_START -> onStart(source)
            Lifecycle.Event.ON_RESUME -> onResume(source)
            Lifecycle.Event.ON_PAUSE -> onPause(source)
            Lifecycle.Event.ON_STOP -> onStop(source)
            Lifecycle.Event.ON_DESTROY -> onDestroy(source)
            else -> {}
        }
    }

    open fun onCreate(owner: Lifecycle) {}
    open fun onStart(owner: Lifecycle) {}
    open fun onResume(owner: Lifecycle) {}
    open fun onPause(owner: Lifecycle) {}
    open fun onStop(owner: Lifecycle) {}
    open fun onDestroy(owner: Lifecycle) {}
}
