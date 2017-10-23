package partialview

import java.io.Serializable

class PVMessagesCounter(var joinsReceived: Int = 0,
                        var forwardJoinsReceived: Int = 0, var forwardJoinsSent: Int = 0,
                        var neighborRequestsReceived: Int = 0, var neighborRequestsSent: Int = 0,
                        var disconnectsReceived: Int = 0, var disconnectsSent: Int = 0,
                        var shufflesReceived: Int = 0, var shufflesSent: Int = 0): Serializable