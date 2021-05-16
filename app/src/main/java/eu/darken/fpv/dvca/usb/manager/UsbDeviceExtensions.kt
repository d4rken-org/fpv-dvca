package eu.darken.fpv.dvca.usb.manager

import android.hardware.usb.UsbDevice

val UsbDevice.identifier: String
    get() = this.deviceName

val UsbDevice.label: String
    get() = this.productName ?: identifier