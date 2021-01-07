package fr.asdl.paperbee.activities.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.TextView
import androidx.annotation.StringRes
import fr.asdl.paperbee.R

class PreferenceLicenseFragment : AppFragment() {

    class CopyRightInfo(val author: String, val year: String)
    class LicenseInfo(
        val libName: String,
        val libLicense: License,
        val copyRightInfo: CopyRightInfo
    )
    @Suppress("unused")
    enum class License(@StringRes private val fullText: Int, private val licenseIndex: Int) {
        UNKNOWN(R.string.license_unknown_text, -1),
        APACHE_2_0(R.string.license_apache_full_text, 0);
        fun getName(context: Context): String {
            return if (licenseIndex >= 0)
                context.resources.getStringArray(R.array.licenses)[licenseIndex]
            else context.getString(R.string.unknown_license)
        }
        fun getFullText(context: Context): String {
            return context.getString(this.fullText)
        }
        companion object {
            fun fromIndex(@StringRes licenseIndex: Int): License {
                for (i in values())
                    if (i.licenseIndex == licenseIndex) return i
                return UNKNOWN
            }
        }
    }

    override val layoutId: Int = R.layout.licenses
    private lateinit var licenses: List<LicenseInfo>

    override fun onLayoutInflated(view: View) {
        (this.parentFragment as PreferenceFragmentRoot).setToolbarTitle(R.string.pref_app_third_party_license)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val resources = requireContext().resources
        val libNames = resources.getStringArray(R.array.libs_name)
        val libsAuthors = resources.getStringArray(R.array.libs_author)
        val libsCopyYear = resources.getStringArray(R.array.libs_copyYear)
        val libsLicenseIndex = resources.getIntArray(R.array.libs_license)

        fun getStringAt(arr: Array<String>, position: Int): String {
            return if (arr.size > position) arr[position]
            else getString(R.string.no_data)
        }
        fun getLicense(position: Int): License {
            return if (libsLicenseIndex.size > position) License.fromIndex(libsLicenseIndex[position])
            else License.UNKNOWN
        }

        val licenses = arrayListOf<LicenseInfo>()
        for (i in libNames.indices)
            licenses.add(
                LicenseInfo(
                    libNames[i], getLicense(i), CopyRightInfo(
                        getStringAt(
                            libsAuthors,
                            i
                        ), getStringAt(libsCopyYear, i)
                    )
                )
            )
        this.licenses = licenses

        view.findViewById<ExpandableListView>(R.id.licenses_list).setAdapter(LicenseAdapter())
    }

    override fun shouldLockDrawer(): Boolean = true

    inner class LicenseAdapter: BaseExpandableListAdapter() {
        override fun getGroupCount(): Int = licenses.size

        override fun getChildrenCount(groupPosition: Int): Int = 1

        override fun getGroup(groupPosition: Int): LicenseInfo {
            return licenses[groupPosition]
        }

        override fun getChild(groupPosition: Int, childPosition: Int): LicenseInfo {
            return licenses[groupPosition]
        }

        override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()
        override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()
        override fun hasStableIds(): Boolean = true

        override fun getGroupView(
            groupPosition: Int,
            isExpanded: Boolean,
            convertView: View?,
            parent: ViewGroup?
        ): View {
            val license = this@LicenseAdapter.getGroup(groupPosition)
            val view = convertView ?: View.inflate(requireContext(), R.layout.license_summary, null)
            view.apply {
                findViewById<TextView>(R.id.component_name).text = license.libName
                findViewById<TextView>(R.id.component_author).text = getString(R.string.licensed_authored, license.copyRightInfo.author)
                findViewById<TextView>(R.id.component_copyright).text = getString(R.string.licensed_copyright, license.copyRightInfo.year)
                findViewById<TextView>(R.id.component_license).apply {
                    this.visibility = if (!isExpanded) View.VISIBLE else View.GONE
                    this.text = context.getString(R.string.licensed_under, license.libLicense.getName(context))
                }
            }
            return view
        }

        override fun getChildView(
            groupPosition: Int,
            childPosition: Int,
            isLastChild: Boolean,
            convertView: View?,
            parent: ViewGroup?
        ): View {
            val license = this@LicenseAdapter.getGroup(groupPosition)
            val textView = TextView(context)
            textView.apply {
                this.text = license.libLicense.getFullText(context)
                val padding = context.resources.getDimension(R.dimen.padding_medium).toInt()
                this.setPadding(padding, 0, padding, padding)
            }
            return textView
        }

        override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = false

    }

}