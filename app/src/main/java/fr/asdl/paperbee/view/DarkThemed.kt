package fr.asdl.paperbee.view

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate.*
import fr.asdl.paperbee.preferences.AppPreferences

/**
 * Use this interface on an Activity or a contextual element to switch dark theme.
 */
interface DarkThemed {

    enum class Theme(val mode: Int) {
        LIGHT(MODE_NIGHT_NO),
        DARK(MODE_NIGHT_YES),
        SYSTEM(MODE_NIGHT_FOLLOW_SYSTEM);

        companion object {
            fun fromSysVal(value: Int): Theme {
                for (i in values())
                    if (i.mode == value) return i
                return SYSTEM
            }
        }
    }

    fun requireContext(): Context

    /**
     * Use this method if you handle activity recreation.
     * In order to handle it, add android:configChanges="uiMode" to the activity in the manifest
     * and override onConfigurationChanged in the activity. In this method, finish the activity
     * and create a copy of it that you can launch. Right after this, call this method which will
     * add a transition for the theme change.
     */
    fun applyThemeChangeTransition() {
        (requireContext() as? Activity)?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    /**
     * Retrieves the current theme of the application
     */
    fun getCurrentSettings(): Theme {
        return Theme.fromSysVal(AppPreferences(requireContext()).get(AppPreferences.APP_THEME).toInt())
    }

    /**
     * Applies and saves a new theme for the application.
     * If the application is starting and sets the theme at that time, pass null as [theme]
     */
    fun applyTheme(theme: Theme? = null, save: Boolean = false) {
        val savedTheme = getCurrentSettings()
        if (theme != savedTheme) {
            if (theme != null && save) AppPreferences(requireContext()).set(AppPreferences.APP_THEME, theme.mode.toString())
            setDefaultNightMode((theme ?: savedTheme).mode)
        }
    }

}