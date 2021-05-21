package eu.darken.fpv.dvca.gear

import eu.darken.fpv.dvca.usb.HWDevice
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface Gear {
    val device: HWDevice

    val firstSeenAt: Instant

    val identifier: String
        get() = "${device.identifier}#$gearName"

    val serialNumber: String?
        get() = device.serialNumber

    val label: String
        get() = gearName

    val gearName: String
        get() = device.productName

    val isGearConnected: Boolean

    val logId: String
        get() = "$identifier $gearName"

    val events: Flow<Event>

    sealed interface Event {
        val gear: Gear

        data class GearAttached(override val gear: Gear) : Event

        data class GearDetached(override val gear: Gear) : Event
    }

    suspend fun updateDevice(device: HWDevice?)

    interface Factory {
        fun canHandle(device: HWDevice): Boolean

        fun create(device: HWDevice): Gear
    }
}

