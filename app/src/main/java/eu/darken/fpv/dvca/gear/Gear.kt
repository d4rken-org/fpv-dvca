package eu.darken.fpv.dvca.gear

import eu.darken.fpv.dvca.usb.HWDevice
import kotlinx.coroutines.flow.Flow

interface Gear {
    val device: HWDevice

    val identifier: String
        get() = device.identifier

    val label: String
        get() = device.label

    val isGearConnected: Boolean

    val events: Flow<Event>

    sealed interface Event {
        val gear: Gear

        data class GearAttached(override val gear: Gear) : Event

        data class GearDetached(override val gear: Gear) : Event
    }

    suspend fun updateDevice(device: HWDevice?)

    interface Factory {
        fun canHandle(device: HWDevice): Boolean

        fun create(
            gearManager: GearManager,
            device: HWDevice,
        ): Gear
    }
}

