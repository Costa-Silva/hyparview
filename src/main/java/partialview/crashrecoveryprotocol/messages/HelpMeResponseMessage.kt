package partialview.crashrecoveryprotocol.messages

import partialview.crashrecoveryprotocol.HelpResult
import java.io.Serializable
import java.util.*

class HelpMeResponseMessage(val requestUUID: UUID, val result: HelpResult): Serializable