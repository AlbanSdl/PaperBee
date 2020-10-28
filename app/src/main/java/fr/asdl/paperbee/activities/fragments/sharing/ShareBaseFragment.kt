package fr.asdl.paperbee.activities.fragments.sharing

import fr.asdl.paperbee.R
import fr.asdl.paperbee.activities.fragments.AppFragment
import fr.asdl.paperbee.activities.fragments.SubFragment

abstract class ShareBaseFragment : AppFragment(), SubFragment {

    override fun getToolbarId(): Int = R.id.share_toolbar
    override fun shouldLockDrawer(): Boolean = true

}