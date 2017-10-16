package partialview

import akkaNetwork.NodeID
import java.io.Serializable

class JoinMessage(val newNode: NodeID) : Serializable