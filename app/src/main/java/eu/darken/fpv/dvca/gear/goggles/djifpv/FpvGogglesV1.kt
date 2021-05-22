package eu.darken.fpv.dvca.gear.goggles.djifpv

import android.content.Context
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.gear.Gear
import eu.darken.fpv.dvca.gear.GearManager
import eu.darken.fpv.dvca.gear.goggles.Goggles
import eu.darken.fpv.dvca.usb.HWDevice
import eu.darken.fpv.dvca.usb.connection.HWConnection
import eu.darken.fpv.dvca.videofeed.core.GeneralFeedSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import timber.log.Timber
import java.time.Instant

class FpvGogglesV1 @AssistedInject constructor(
    @Assisted override val device: HWDevice,
    @ApplicationContext private val context: Context,
    private val gearManager: GearManager,
    private val generalFeedSettings: GeneralFeedSettings,
) : Goggles {

    override val firstSeenAt: Instant = Instant.now()

    private val eventsInternal = MutableStateFlow<Gear.Event?>(null)
    override val events: Flow<Gear.Event>
        get() = eventsInternal.filterNotNull()

    private var wasVideoActive: Boolean = false

    private var videoFeedInternal: Goggles.VideoFeed? = null
    override val videoFeed: Goggles.VideoFeed?
        get() = videoFeedInternal

    private var connection: HWConnection? = null

    override suspend fun startVideoFeed(): Goggles.VideoFeed {
        Timber.tag(TAG).i("startVideoFeed()")
        videoFeedInternal?.let {
            Timber.tag(TAG).w("Feed is already active!")
            return it
        }

        connection = device.openConnection()

        return FpvGogglesV1VideoFeed(
            context,
            connection!!,
            usbReadMode = generalFeedSettings.feedModeDefault.value,
        ).also { feed ->
            videoFeedInternal = feed
        }
    }

    override suspend fun stopVideoFeed() {
        Timber.tag(TAG).i("stopVideoFeed()")
        videoFeedInternal?.let {
            it.close()
            wasVideoActive = false
        }
        videoFeedInternal = null

        connection?.close()
        connection = null
    }

    override suspend fun release() {
        Timber.tag(TAG).d("release()")
        stopVideoFeed()
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
