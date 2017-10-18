package partialview.crashrecoveryprotocol.messages

import partialview.crashrecoveryprotocol.Priority
import java.io.Serializable
import java.util.*

class HelpMeMessage(val requestUUID: UUID, val priority: Priority): Serializable