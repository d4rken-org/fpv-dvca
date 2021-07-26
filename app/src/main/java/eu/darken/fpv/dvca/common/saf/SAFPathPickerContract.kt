package eu.darken.fpv.dvca.common.saf

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.RequiresApi
import eu.darken.fpv.dvca.common.BuildVersionWrap
import eu.darken.fpv.dvca.common.hasAPILevel

class SAFPathPickerContract : ActivityResultContract<Uri?, Uri?>() {


    override fun createIntent(context: Context, input: Uri?): Intent =
        Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)

            @RequiresApi(Build.VERSION_CODES.O)
            if (input != null && BuildVersionWrap.hasAPILevel(26)) {
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, input)
            }

            putExtra("android.content.extra.SHOW_ADVANCED", true)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        if (resultCode != Activity.RESULT_OK) return null
        return intent?.data
    }
}