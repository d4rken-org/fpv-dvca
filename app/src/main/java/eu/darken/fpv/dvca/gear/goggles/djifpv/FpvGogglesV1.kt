package eu.darken.fpv.dvca.gear.goggles.djifpv

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
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
    @Assisted private val initialDevice: HWDevice,
    private val gearManager: GearManager,
    private val generalFeedSettings: GeneralFeedSettings,
) : Goggles {
    private var currentDevice: HWDevice? = initialDevice

    override val device: HWDevice
        get() = currentDevice ?: initialDevice

    override val isGearConnected: Boolean
        get() = currentDevice != null

    override val firstSeenAt: Instant = Instant.now()

    private val eventsInternal = MutableStateFlow<Gear.Event?>(null)
    override val events: Flow<Gear.Event>
        get() = eventsInternal.filterNotNull()

    private var wasVideoActive: Boolean = false

    override suspend fun updateDevice(device: HWDevice?) {
        Timber.tag(TAG).d("updateDevice(device=%s)", device)

        val reconnect = device != null && currentDevice == null

        currentDevice = device

        if (device == null) {
            Timber.tag(TAG).w("Device disconnected!")

            val wasActive = videoFeedInternal.value != null || wasVideoActive
            if (wasActive) {
                Timber.tag(TAG).d("Video feed was active on disconnect, will restart after reconnect.")
            }

            stopVideoFeed()

            wasVideoActive = wasActive

            eventsInternal.value = Gear.Event.GearDetached(this)
        } else if (reconnect) {
            Timber.tag(TAG).w("Device reconnected!")

            eventsInternal.value = Gear.Event.GearAttached(this)
            if (wasVideoActive) {
                Timber.tag(TAG).i("Video feed was previously active, starting again.")
                startVideoFeed()
            } else {
                Timber.tag(TAG).v("Video was previously not active.")
            }
        }
    }

    private val videoFeedInternal = MutableStateFlow<Goggles.VideoFeed?>(null)
    override val videoFeed: Flow<Goggles.VideoFeed?> = videoFeedInternal

    private var connection: HWConnection? = null

    override suspend fun startVideoFeed(): Goggles.VideoFeed {
        Timber.tag(TAG).i("startVideoFeed()")
        videoFeedInternal.value?.let {
            Timber.tag(TAG).w("Feed is already active!")
            return it
        }

        connection = device.openConnection()

        return FpvGogglesV1VideoFeed(
            connection!!,
            usbReadMode = generalFeedSettings.feedModeDefault.value,
        ).also { feed ->
            videoFeedInternal.value = feed
        }
    }

    override suspend fun stopVideoFeed() {
        Timber.tag(TAG).i("stopVideoFeed()")
        videoFeedInternal.value?.let {
            it.close()
            wasVideoActive = false
        }
        videoFeedInternal.value = null

        connection?.close()
        connection = null
    }

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
