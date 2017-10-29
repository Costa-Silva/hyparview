package globalview

import java.io.Serializable

class GVMessagesCounter(var messagesToResolveConflict: Int = 0,
                        var messagesToCheckIfAlive: Int = 0): Serializable