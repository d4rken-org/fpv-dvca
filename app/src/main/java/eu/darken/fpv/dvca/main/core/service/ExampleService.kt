package eu.darken.fpv.dvca.main.core.service

import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.fpv.dvca.common.smart.SmartService
import javax.inject.Inject


@AndroidEntryPoint
class ExampleService : SmartService() {

    @Inject lateinit var binder: ExampleBinder

    override fun onBind(intent: Intent): IBinder = binder

}