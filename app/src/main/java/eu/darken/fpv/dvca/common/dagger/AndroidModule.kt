package eu.darken.fpv.dvca.common.dagger

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.hardware.usb.UsbManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AndroidModule {

    @Provides
    @Singleton
    fun context(app: Application): Context = app.applicationContext

    @Provides
    @Singleton
    fun notificationManager(context: Context): NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @Provides
    @Singleton
    fun usbManager(context: Context): UsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
}
