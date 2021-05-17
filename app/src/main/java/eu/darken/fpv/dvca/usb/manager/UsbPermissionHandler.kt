package eu.darken.fpv.dvca.usb.manager

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.common.BuildConfigWrap
import eu.darken.fpv.dvca.usb.HWDevice
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.random.Random

@Singleton
class UsbPermissionHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val usbManager: UsbManager,
) {
    private fun createPendingIntent(requestCode: Int) = PendingIntent.getBroadcast(
        context,
        requestCode,
        Intent(ACTION_USB_PERMISSION).apply {
            putExtra(KEY_REQUESTCODE, requestCode)
        },
        0
    )

    private val filter = IntentFilter(ACTION_USB_PERMISSION)

    suspend fun requestPermission(device: HWDevice) = withTimeout(30_000) {
        suspendCancellableCoroutine<Boolean> { cont ->
            val requestCode = Random.nextInt()
            val pi = createPendingIntent(requestCode)

            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    Timber.tag(TAG).d("onReceive(context=%s, intent=%s)", context, intent)
                    if (intent.action != ACTION_USB_PERMISSION) {
                        Timber.tag(TAG).w("Unknown intent: %s", intent)
                        return
                    }
                    val receivedRequestCode = intent.getIntExtra(KEY_REQUESTCODE, 0)
                    if (receivedRequestCode != requestCode) {
                        Timber.tag(TAG).d("Not our request code: %d", receivedRequestCode)
                    }

                    val isGranted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false).also {
                        Timber.tag(TAG).i("Permission request result was: isGranted=%b", it)
                    }
                    context.unregisterReceiver(this)
                    cont.resume(isGranted)
                }
            }

            cont.invokeOnCancellation {
                try {
                    context.unregisterReceiver(receiver)
                } catch (e: Exception) {
                    Timber.tag(TAG).e("Failed to unregister: %s", receiver)
                }
            }
            context.registerReceiver(receiver, filter)


            Timber.tag(TAG).i("Requesting permission for %s", device)
            usbManager.requestPermission(device.rawDevice, pi)
        }
    }

    companion object {
        private val ACTION_USB_PERMISSION = "${BuildConfigWrap.APPLICATION_ID}.USB_PERMISSION"
        private val KEY_REQUESTCODE = "${BuildConfigWrap.APPLICATION_ID}.USB_PERMISSION.requestCode"
        private val TAG = App.logTag("Usb", "PermissionHandler")
    }
}