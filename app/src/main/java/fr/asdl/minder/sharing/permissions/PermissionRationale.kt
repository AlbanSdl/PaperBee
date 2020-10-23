package fr.asdl.minder.sharing.permissions

import android.app.AlertDialog
import android.content.Context
import androidx.annotation.StringRes

class PermissionRationale(
    @StringRes private val title: Int,
    @StringRes private val explanation: Int
) {

    private var okCallBack: (() -> Unit)? = null

    fun setCallback(callback: () -> Unit): PermissionRationale {
        this.okCallBack = callback
        return this
    }

    fun display(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(this.title))
        builder.setMessage(context.getString(this.explanation))
        builder.setPositiveButton(context.getString(android.R.string.ok)) { dialog, _ ->
            dialog.dismiss()
            okCallBack?.invoke()
        }
        builder.setNegativeButton(context.getString(android.R.string.cancel)) { dialog, _ -> dialog.cancel() }
        builder.show()
    }

}