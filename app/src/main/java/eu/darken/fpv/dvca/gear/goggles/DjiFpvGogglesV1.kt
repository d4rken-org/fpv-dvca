package eu.darken.fpv.dvca.gear.goggles

import eu.darken.fpv.dvca.gear.Gear
import eu.darken.fpv.dvca.gear.GearManager
import eu.darken.fpv.dvca.usb.core.DVCADevice
import javax.inject.Inject

class DjiFpvGogglesV1(
    private val gearManager: GearManager,
    private val initialDevice: DVCADevice,
) : Gear.Goggles {

    private var currentDevice: DVCADevice? = initialDevice

    override val device: DVCADevice
        get() = currentDevice ?: initialDevice

    override val isGearConnected: Boolean
        get() = currentDevice != null

    override fun updateDevice(device: DVCADevice?) {
        currentDevice = device
    }

    class Factory @Inject constructor() : Gear.Factory {

        override fun canHandle(device: DVCADevice): Boolean = device.raw.run {
            vendorId == VENDOR_ID && productId == PRODUCT_ID
        }

        override fun create(
            gearManager: GearManager,
            device: DVCADevice
        ): Gear.Goggles {
            return DjiFpvGogglesV1(
                gearManager = gearManager,
                initialDevice = device,
            )
        }


    }

    companion object {
        private const val VENDOR_ID = 11427
        private const val PRODUCT_ID = 31
    }
}