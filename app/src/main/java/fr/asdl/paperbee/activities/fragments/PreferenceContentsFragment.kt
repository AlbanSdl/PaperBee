package fr.asdl.paperbee.activities.fragments

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import fr.asdl.paperbee.R
import fr.asdl.paperbee.view.DarkThemed

class PreferenceContentsFragment : PreferenceFragmentCompat(), DarkThemed {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val manager = preferenceManager
        manager.sharedPreferencesName = getString(R.string.preferences_file)
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val pref = findPreference<Preference>(getString(R.string.pref_app_theme_name))
        pref?.sharedPreferences?.edit()?.putString(
            getString(R.string.pref_app_theme_name), this.getCurrentSettings().mode.toString()
        )?.apply()
        pref?.setOnPreferenceChangeListener { _, value ->
            this.applyTheme(DarkThemed.Theme.fromSysVal((value as String).toInt()))
            true
        }
    }

}