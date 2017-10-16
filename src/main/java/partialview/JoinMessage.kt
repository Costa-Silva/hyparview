package partialview

import akkanetwork.NodeID
import java.io.Serializable

class JoinMessage(val newNode: NodeID) : Serializable