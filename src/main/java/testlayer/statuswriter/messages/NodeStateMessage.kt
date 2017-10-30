package systemsupervisor.graph

import communicationview.wrappers.CommSharedData
import globalview.GVSharedData
import partialview.wrappers.PVSharedData
import java.io.Serializable

class NodeStateMessage(val partialViewData: PVSharedData,
                       val commViewData: CommSharedData,
                       val glovalViewData: GVSharedData
                       ): Serializable