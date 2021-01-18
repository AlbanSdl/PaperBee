package fr.asdl.paperbee

import android.app.Application
import android.content.Context
import fr.asdl.paperbee.view.DarkThemed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class PaperBeeApplication : Application(), DarkThemed {

    val paperScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun requireContext(): Context {
        return this
    }

    override fun onCreate() {
        super.onCreate()
        this.applyTheme()
    }

}