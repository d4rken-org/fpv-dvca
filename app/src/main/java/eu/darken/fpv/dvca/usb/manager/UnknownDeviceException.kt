package eu.darken.fpv.dvca.usb.manager

import android.hardware.usb.UsbDevice
import eu.darken.fpv.dvca.usb.HWDevice

class UnknownDeviceException(
    deviceIdentifier: String,
    message: String
) : IllegalArgumentException(
    "Unknown device: $deviceIdentifier. $message"
) {

    constructor(
        device: HWDevice,
        message: String = "",
    ) : this(
        deviceIdentifier = device.identifier,
        message = message,
    )

    constructor(
        rawDevice: UsbDevice,
        message: String = "",
    ) : this(
        deviceIdentifier = rawDevice.identifier,
        message = message,
    )
}