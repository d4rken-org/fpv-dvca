package eu.darken.fpv.dvca.usb.manager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.fpv.dvca.App
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class UsbDeviceEventReceiver : BroadcastReceiver() {
    @Inject lateinit var usbDeviceManager: HWDeviceManager

    override fun onReceive(context: Context, intent: Intent) {
        Timber.tag(TAG).v("onReceive(context=%s, intent=%s)", context, intent)

        val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
        device?.let { Timber.tag(TAG).v("Event for %s", it) }

        when (intent.action) {
            UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                Timber.tag(TAG).d("%s -> ACTION_USB_DEVICE_ATTACHED", device?.label)
                device?.let { usbDeviceManager.onDeviceAttached(it) }
            }
            UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                Timber.tag(TAG).d("%s -> ACTION_USB_DEVICE_DETACHED", device?.label)
                device?.let { usbDeviceManager.onDeviceDetached(it) }
            }
            ACTION_USB_STATE -> {
                Timber.tag(TAG).d("%s -> ACTION_USB_STATE", device?.label)
            }
            else -> Timber.tag(TAG).w("Unknown ACTION: %s", intent.action)
        }
    }


    companion object {
        private const val ACTION_USB_STATE = "android.hardware.usb.action.USB_STATE"
        private val TAG = App.logTag("Usb", "DeviceEventReceiver")
    }
}