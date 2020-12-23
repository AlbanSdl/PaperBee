package fr.asdl.paperbee

import android.app.Application
import android.content.Context
import fr.asdl.paperbee.view.DarkThemed

class PaperBeeApplication : Application(), DarkThemed {

    override fun requireContext(): Context {
        return this
    }

    override fun onCreate() {
        super.onCreate()
        this.applyTheme()
    }

}