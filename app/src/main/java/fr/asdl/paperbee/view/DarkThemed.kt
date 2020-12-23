package fr.asdl.paperbee.view

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
     * Retrieves the current theme of the application
     */
    fun getCurrentSettings(): Theme {
        return Theme.fromSysVal(AppPreferences(requireContext()).get(AppPreferences.APP_THEME))
    }

    /**
     * Applies and saves a new theme for the application.
     * If the application is starting and sets the theme at that time, pass null as [theme]
     */
    fun applyTheme(theme: Theme? = null) {
        val savedTheme = getCurrentSettings()
        if (theme != null) AppPreferences(requireContext()).set(AppPreferences.APP_THEME, theme.mode)
        setDefaultNightMode((theme ?: savedTheme).mode)
    }

}