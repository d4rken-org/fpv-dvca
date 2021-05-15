package eu.darken.fpv.dvca.usb.core

import android.hardware.usb.UsbDevice

val UsbDevice.identifier: String
    get() = this.deviceName

val UsbDevice.label: String
    get() = this.productName ?: identifier