package globalview

import communicationview.wrappers.CommSharedData
import partialview.wrappers.PVSharedData

class GlobalWriteToFileWrapper(val pvData: PVSharedData,
                               val commWrapper: CommSharedData,
                               val gvData: GVSharedData) {
}