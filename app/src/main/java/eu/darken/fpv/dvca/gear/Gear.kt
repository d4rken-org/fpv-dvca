package eu.darken.fpv.dvca.gear

import eu.darken.fpv.dvca.usb.HWDevice
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

    val logId: String
        get() = "$identifier $gearName"

    suspend fun release()

    interface Factory {
        fun canHandle(device: HWDevice): Boolean

        fun create(device: HWDevice): Gear
    }
}

