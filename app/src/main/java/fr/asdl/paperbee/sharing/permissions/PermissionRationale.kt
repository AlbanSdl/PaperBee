package fr.asdl.paperbee.sharing.permissions

import android.app.AlertDialog
import android.content.Context
import androidx.annotation.StringRes
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PermissionRationale(
    @StringRes private val title: Int,
    @StringRes private val explanation: Int
) {

    suspend fun display(context: Context): Boolean {
        return suspendCoroutine {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(context.getString(this.title))
            builder.setMessage(context.getString(this.explanation))
            builder.setPositiveButton(context.getString(android.R.string.ok)) { dialog, _ ->
                dialog.dismiss()
                it.resume(true)
            }
            builder.setNegativeButton(context.getString(android.R.string.cancel)) {
                dialog, _ -> dialog.cancel()
            }
            builder.setOnDismissListener { _ -> it.resume(false) }
            builder.show()
        }
    }

}