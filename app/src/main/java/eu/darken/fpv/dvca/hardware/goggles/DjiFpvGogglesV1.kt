package eu.darken.fpv.dvca.hardware.goggles

import eu.darken.fpv.dvca.hardware.Hardware
import eu.darken.fpv.dvca.usb.core.DVCADevice
import javax.inject.Inject

data class DjiFpvGogglesV1(
    val device: DVCADevice
) : Hardware.Goggles {

    override val identifier: String
        get() = device.identifier

    override val label: String
        get() = device.label

    class Factory @Inject constructor() : Hardware.Factory {

        override fun canHandle(device: DVCADevice): Boolean = device.raw.run {
            vendorId == VENDOR_ID && productId == PRODUCT_ID
        }

        override fun create(device: DVCADevice): Hardware.Goggles {
            return DjiFpvGogglesV1(
                device = device
            )
        }
    }

    companion object {
        private const val VENDOR_ID = 11427
        private const val PRODUCT_ID = 31
    }
}