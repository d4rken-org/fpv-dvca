package eu.darken.fpv.dvca.gear

import eu.darken.fpv.dvca.usb.core.DVCADevice

sealed interface Gear {
    val device: DVCADevice

    val identifier: String
        get() = device.identifier

    val label: String
        get() = device.label

    val isGearConnected: Boolean

    fun updateDevice(device: DVCADevice?)

    interface Goggles : Gear

    interface Factory {
        fun canHandle(device: DVCADevice): Boolean

        fun create(
            gearManager: GearManager,
            device: DVCADevice,
        ): Gear
    }
}

