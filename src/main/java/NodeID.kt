data class NodeID(val ip: String, val port: String){
    override fun toString(): String {
        return "$ip:$port"
    }
}