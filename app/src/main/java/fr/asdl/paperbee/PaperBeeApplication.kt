package fr.asdl.paperbee

import android.app.Application
import android.content.Context
import android.nfc.NdefMessage
import fr.asdl.paperbee.storage.DatabaseProxy
import fr.asdl.paperbee.storage.v1.DatabaseAccess
import fr.asdl.paperbee.view.DarkThemed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class PaperBeeApplication : Application(), DarkThemed {

    val paperScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    lateinit var dbProxy: DatabaseProxy<*>

    var nfcServiceListenerFrom: (() -> NdefMessage)? = null
    var nfcServiceListenerTo: ((Int) -> Unit)? = null

    override fun requireContext(): Context {
        return this
    }

    override fun onCreate() {
        super.onCreate()
        this.applyTheme()
        this.dbProxy = DatabaseProxy(requireContext(), DatabaseAccess::class.java)
        this.dbProxy.load()
    }

}