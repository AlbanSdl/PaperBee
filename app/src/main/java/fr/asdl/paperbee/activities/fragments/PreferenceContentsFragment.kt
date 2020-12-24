package fr.asdl.paperbee.activities.fragments

import android.os.Bundle
import androidx.core.content.pm.PackageInfoCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import fr.asdl.paperbee.R
import fr.asdl.paperbee.view.DarkThemed

class PreferenceContentsFragment : PreferenceFragmentCompat(), DarkThemed {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val manager = preferenceManager
        manager.sharedPreferencesName = getString(R.string.preferences_file)
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val appThemePref = findPreference<Preference>(getString(R.string.pref_app_theme_name))
        appThemePref?.sharedPreferences?.edit()?.putString(
            getString(R.string.pref_app_theme_name), this.getCurrentSettings().mode.toString()
        )?.apply()
        appThemePref?.setOnPreferenceChangeListener { _, value ->
            this.applyTheme(DarkThemed.Theme.fromSysVal((value as String).toInt()))
            true
        }

        val licensePref = findPreference<Preference>(getString(R.string.pref_app_version))
        val pkgInfo = requireActivity().packageManager.getPackageInfo(requireActivity().application.packageName, 0)
        licensePref?.summary = getString(R.string.pref_app_version_summary, pkgInfo.versionName, PackageInfoCompat.getLongVersionCode(pkgInfo))
    }

}