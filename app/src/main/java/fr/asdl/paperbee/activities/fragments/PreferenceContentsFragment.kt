package fr.asdl.paperbee.activities.fragments

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.content.pm.PackageInfoCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.transition.TransitionInflater
import fr.asdl.paperbee.R
import fr.asdl.paperbee.view.DarkThemed

class PreferenceContentsFragment : PreferenceFragmentCompat(), DarkThemed {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireView() as ViewGroup).isTransitionGroup = true
        (this.parentFragment as PreferenceFragmentRoot).setToolbarTitle(R.string.settings)
    }

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

        val versionPref = findPreference<Preference>(getString(R.string.pref_app_version))
        val pkgInfo = requireActivity().packageManager.getPackageInfo(requireActivity().application.packageName, 0)
        versionPref?.summary = getString(R.string.pref_app_version_summary, pkgInfo.versionName, PackageInfoCompat.getLongVersionCode(pkgInfo))

        val licensePref = findPreference<Preference>(getString(R.string.pref_app_third_party))
        licensePref?.setOnPreferenceClickListener { _ ->
            val fragment = PreferenceLicenseFragment()
            val transitionInflater = TransitionInflater.from(requireContext())
            val currentFragment =
                requireParentFragment().childFragmentManager.findFragmentById(R.id.settings_container)
            if (currentFragment != null)
                currentFragment.exitTransition =
                    transitionInflater.inflateTransition(R.transition.slide_left)
            fragment.enterTransition = transitionInflater.inflateTransition(R.transition.slide_right)
            requireParentFragment().childFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, fragment)
                .addToBackStack("preferences_licenses")
                .commit()
            true
        }
    }

}
