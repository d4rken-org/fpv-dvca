package eu.darken.fpv.dvca.usb.core

import android.hardware.usb.UsbDevice

data class DVCADevice(
    val raw: UsbDevice,
    val hasPermission: Boolean,
    val hasRequestedPermission: Boolean,
) {
    val identifier: String
        get() = raw.deviceName

    val label: String
        get() = "${raw.manufacturerName} \"${raw.productName}\" ${raw.version} (${raw.deviceName})"
}