package fr.asdl.minder.activities.fragments.sharing

import fr.asdl.minder.R
import fr.asdl.minder.activities.fragments.AppFragment
import fr.asdl.minder.activities.fragments.SubFragment

abstract class ShareBaseFragment : AppFragment(), SubFragment {

    override fun getToolbarId(): Int = R.id.share_toolbar

}