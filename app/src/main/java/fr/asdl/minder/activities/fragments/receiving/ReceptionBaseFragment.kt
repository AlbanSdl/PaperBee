package fr.asdl.minder.activities.fragments.receiving

import fr.asdl.minder.R
import fr.asdl.minder.activities.fragments.AppFragment
import fr.asdl.minder.activities.fragments.SubFragment

abstract class ReceptionBaseFragment : AppFragment(), SubFragment {

    override fun getToolbarId(): Int = R.id.share_toolbar

}