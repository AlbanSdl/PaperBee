package fr.asdl.minder.activities.fragments

import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import fr.asdl.minder.R
import fr.asdl.minder.activities.fragments.receiving.ReceptionBaseFragment
import fr.asdl.minder.activities.fragments.receiving.ReceptionFragment
import fr.asdl.minder.activities.fragments.sharing.SharingMethod
import fr.asdl.minder.note.Notable
import fr.asdl.minder.note.NoteFolder
import fr.asdl.minder.sharing.ShareProcess

class ImportFragment : AppFragment(), FragmentContainer<ReceptionBaseFragment> {

    val shareProcess = ShareProcess()
    var shareData: ByteArray? = null
    var method: SharingMethod = SharingMethod.FILE
    var content: List<Notable<*>>? = null
    var destination: NoteFolder? = null

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

    override fun getFragmentContainerId(): Int = R.id.share_fragment_container

}