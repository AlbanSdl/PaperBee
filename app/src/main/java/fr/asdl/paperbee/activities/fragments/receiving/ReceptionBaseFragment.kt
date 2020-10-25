package fr.asdl.paperbee.activities.fragments.receiving

import fr.asdl.paperbee.R
import fr.asdl.paperbee.activities.fragments.AppFragment
import fr.asdl.paperbee.activities.fragments.SubFragment

abstract class ReceptionBaseFragment : AppFragment(), SubFragment {

    override fun getToolbarId(): Int = R.id.share_toolbar

}