package partialview.messages

import java.io.Serializable

class Message : Serializable {

    inner class PartialMessage {}

    inner class GlobalMessage {}

    inner class OtherMessage {}
}