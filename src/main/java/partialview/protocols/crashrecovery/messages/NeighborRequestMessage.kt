package partialview.protocols.crashrecovery.messages

import partialview.protocols.crashrecovery.Priority
import java.io.Serializable

class NeighborRequestMessage(val priority: Priority): Serializable