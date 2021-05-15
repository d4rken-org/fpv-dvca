package eu.darken.fpv.dvca.hardware

import eu.darken.fpv.dvca.usb.core.DVCADevice

sealed interface Hardware {
    val identifier: String
    val label: String

    interface Goggles : Hardware

    interface Factory {
        fun canHandle(device: DVCADevice): Boolean

        fun create(device: DVCADevice): Hardware
    }
}

