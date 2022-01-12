package fr.asdl.paperbee.activities.fragments

import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import fr.asdl.paperbee.R
import fr.asdl.paperbee.activities.fragments.receiving.ReceptionBaseFragment
import fr.asdl.paperbee.activities.fragments.receiving.ReceptionFragment
import fr.asdl.paperbee.activities.fragments.sharing.SharingMethod
import fr.asdl.paperbee.note.NoteFolder
import fr.asdl.paperbee.sharing.ShareProcess
import fr.asdl.paperbee.view.sentient.DataHolder

class ImportFragment : AppFragment(), FragmentContainer<ReceptionBaseFragment> {

    val shareProcess = ShareProcess()
    var shareData: ByteArray? = null
    var method: SharingMethod = SharingMethod.FILE
    var content: List<DataHolder>? = null
    var destination: NoteFolder? = null

    override val transitionIn: Int
        get() = R.transition.slide_bottom

    override val transitionOut: Int
        get() = R.transition.folder_explode

    override val shouldRetainInstance: Boolean = true

    override val layoutId: Int = R.layout.share_layout

    override fun onLayoutInflated(view: View) {
        view.findViewById<Toolbar>(R.id.share_toolbar).setTitle(R.string.share_import)
        this.displayFragment(ReceptionFragment(), null, true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> activity?.onBackPressed()
            else -> return false
        }
        return true
    }

    override fun shouldLockDrawer(): Boolean = true

    override fun getFragmentContainerId(): Int = R.id.share_fragment_container

}