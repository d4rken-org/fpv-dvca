package eu.darken.fpv.dvca.gear.goggles.djifpv

import android.content.Context
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.common.coroutine.DispatcherProvider
import eu.darken.fpv.dvca.common.flow.shareLatest
import eu.darken.fpv.dvca.gear.Gear
import eu.darken.fpv.dvca.gear.goggles.Goggles
import eu.darken.fpv.dvca.gear.goggles.VideoFeedSettings
import eu.darken.fpv.dvca.usb.HWDevice
import eu.darken.fpv.dvca.usb.connection.HWConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import timber.log.Timber
import java.time.Instant

class FpvGogglesV1 @AssistedInject constructor(
    @Assisted override val device: HWDevice,
    @ApplicationContext private val context: Context,
    private val videoFeedSettings: VideoFeedSettings,
    dispatcherProvider: DispatcherProvider,
) : Goggles {

    private val gearScope = CoroutineScope(context = dispatcherProvider.IO)
    override val firstSeenAt: Instant = Instant.now()

    private var wasVideoActive: Boolean = false

    val connection: Flow<HWConnection> = callbackFlow {
        Timber.tag(TAG).i("Opening device connection for %s", device)
        val connection = device.openConnection()
        send(connection)
        awaitClose {
            Timber.tag(TAG).i("Closing device connection for %s", device)
            connection.close()
        }
    }.shareLatest(scope = gearScope, started = SharingStarted.WhileSubscribed(replayExpirationMillis = 3000))

    override val videoFeed: Flow<Goggles.VideoFeed> = connection.flatMapLatest { connection ->
        Timber.tag(TAG).i("Creating videofeed on %s", connection)
        callbackFlow<Goggles.VideoFeed> {
            val feed = FpvGogglesV1VideoFeed(
                context,
                connection,
                usbReadMode = videoFeedSettings.feedModeDefault.value,
            )
            send(feed)

            awaitClose {
                Timber.tag(TAG).i("Closing video feed on %s", connection)
                feed.close()

                wasVideoActive = false
            }
        }
    }.shareLatest(scope = gearScope, started = SharingStarted.WhileSubscribed(replayExpirationMillis = 3000))

    override suspend fun release() {
        Timber.tag(TAG).d("release()")
        gearScope.cancel("release() was called")
    }

    override fun toString(): String = device.logId

    companion object {
        private val TAG = App.logTag("Gear", "FpvGogglesV1")
        private const val VENDOR_ID = 11427
        private const val PRODUCT_ID = 31
    }

    @AssistedFactory
    abstract class Factory : Gear.Factory {

        override fun canHandle(device: HWDevice): Boolean = device.rawDevice.run {
            vendorId == VENDOR_ID && productId == PRODUCT_ID
        }

        abstract override fun create(device: HWDevice): FpvGogglesV1
    }
}
