package eu.darken.fpv.dvca.usb.connection.io

interface HasUsbStats {
    val usbReadMbs: Double
        get() = -1.0
    val bufferReadMbs: Double
        get() = -1.0

    object DUMMY : HasUsbStats {
        override val usbReadMbs: Double = -1.0
        override val bufferReadMbs: Double = -1.0
    }
}