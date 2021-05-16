package eu.darken.fpv.dvca.gear

import eu.darken.fpv.dvca.usb.DVCADevice
import kotlinx.coroutines.flow.Flow

interface Gear {
    val device: DVCADevice

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

    suspend fun updateDevice(device: DVCADevice?)

    interface Factory {
        fun canHandle(device: DVCADevice): Boolean

        fun create(
            gearManager: GearManager,
            device: DVCADevice,
        ): Gear
    }
}

