package eu.darken.fpv.dvca.common

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.fpv.dvca.App
import timber.log.Timber
import java.io.File
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstallId @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val installIDFile = File(context.filesDir, INSTALL_ID_FILENAME)
    val id: String by lazy {
        val existing = if (installIDFile.exists()) {
            installIDFile.readText().also {
                if (!UUID_PATTERN.matcher(it).matches()) throw IllegalStateException("Invalid InstallID: $it")
            }
        } else {
            null
        }

        return@lazy existing ?: UUID.randomUUID().toString().also {
            Timber.tag(TAG).i("New install ID created: %s", it)
            installIDFile.writeText(it)
        }
    }

    companion object {
        private val TAG: String = App.logTag("InstallID")
        private val UUID_PATTERN = Pattern.compile(
            "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"
        )

        private const val INSTALL_ID_FILENAME = "installid"
    }
}

