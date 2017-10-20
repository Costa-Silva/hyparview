package partialview.protocols.crashrecovery.messages

import partialview.protocols.crashrecovery.NeighborRequestResult
import java.io.Serializable

class NeighborRequestReplyMessage(val result: NeighborRequestResult): Serializable