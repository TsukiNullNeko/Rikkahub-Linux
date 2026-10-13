package android.net

class NetworkInfo {
    enum class State { CONNECTED, CONNECTING, DISCONNECTED, DISCONNECTING, SUSPENDED, UNKNOWN }
    enum class DetailedState { CONNECTED, CONNECTING, DISCONNECTED, IDLE, SCANNING }
    val isConnected: Boolean get() = true
    val state: State get() = State.CONNECTED
    val detailedState: DetailedState get() = DetailedState.CONNECTED
    val typeName: String get() = "ETHERNET"
    val type: Int get() = ConnectivityManager.TYPE_ETHERNET
    val isConnectedOrConnecting: Boolean get() = true
}

class NetworkCapabilities {
    fun hasCapability(capability: Int): Boolean = true
    fun hasTransport(transport: Int): Boolean = true
}

class Network {
    override fun toString(): String = "Network(desktop)"
}

class NetworkRequest {
    class Builder {
        fun addCapability(cap: Int): Builder = this
        fun addTransportType(t: Int): Builder = this
        fun build(): NetworkRequest = NetworkRequest()
    }
}

class LinkProperties

class ConnectivityManager {
    companion object {
        const val TYPE_WIFI = 1
        const val TYPE_MOBILE = 0
        const val TYPE_ETHERNET = 9
        const val DEFAULT_NETWORK_PREFERENCE = 1
        const val EXTRA_REASON = "reason"
        const val EXTRA_NO_CONNECTIVITY = "noConnectivity"
        const val TRANSPORT_WIFI = 1
        const val TRANSPORT_CELLULAR = 0
        const val TRANSPORT_ETHERNET = 3
        const val NET_CAPABILITY_VALIDATED = 16
        const val NET_CAPABILITY_INTERNET = 12
    }

    val activeNetworkInfo: NetworkInfo? get() = NetworkInfo()

    fun getActiveNetwork(): Network? = Network()
    fun getNetworkCapabilities(network: Network?): NetworkCapabilities? = NetworkCapabilities()
    fun isActiveNetworkMetered(): Boolean = false
    fun getAllNetworks(): Array<Network> = arrayOf(Network())
    fun getNetworkInfo(network: Network?): NetworkInfo? = NetworkInfo()
    fun registerNetworkCallback(request: NetworkRequest, callback: NetworkCallback) {}
    fun registerDefaultNetworkCallback(callback: NetworkCallback) {}
    fun unregisterNetworkCallback(callback: NetworkCallback) {}

    open class NetworkCallback {
        open fun onAvailable(network: Network) {}
        open fun onLost(network: Network) {}
        open fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {}
        open fun onUnavailable() {}
    }
}
