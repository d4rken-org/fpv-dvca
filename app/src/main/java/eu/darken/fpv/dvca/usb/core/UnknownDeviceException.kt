package eu.darken.fpv.dvca.usb.core

import android.hardware.usb.UsbDevice

class UnknownDeviceException(
    deviceIdentifier: String
) : IllegalArgumentException(
    "Unknown device: $deviceIdentifier"
) {

    constructor(device: DVCADevice) : this(deviceIdentifier = device.identifier)

    constructor(rawDevice: UsbDevice) : this(deviceIdentifier = rawDevice.identifier)
}