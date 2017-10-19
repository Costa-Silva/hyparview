package partialview.protocols.crashrecovery.messages

import partialview.protocols.crashrecovery.HelpResult
import java.io.Serializable

class HelpMeReplyMessage(val result: HelpResult): Serializable