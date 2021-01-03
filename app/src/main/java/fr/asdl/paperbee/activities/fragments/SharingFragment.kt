package fr.asdl.paperbee.activities.fragments

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import fr.asdl.paperbee.R
import fr.asdl.paperbee.activities.MainActivity
import fr.asdl.paperbee.activities.fragments.sharing.ComponentChooserFragment
import fr.asdl.paperbee.activities.fragments.sharing.OptionsFragment
import fr.asdl.paperbee.activities.fragments.sharing.ShareBaseFragment
import fr.asdl.paperbee.activities.fragments.sharing.ShareOptions
import fr.asdl.paperbee.note.Notable
import fr.asdl.paperbee.note.Note

class SharingFragment : AppFragment(), FragmentContainer<ShareBaseFragment> {

    companion object {
        const val SAVED_INSTANCE_TAG = "paperbee:fragOpenedFromTagId"
    }

    override val shouldRetainInstance: Boolean = true

    override val layoutId: Int = R.layout.share_layout
    val selection = arrayListOf<Notable<*>>()
    val shareOptions = ShareOptions()

    private lateinit var openedFrom: Notable<*>

    fun from(from: Notable<*>) {
        this.openedFrom = from
        if (from.id!! >= 0 && selection.size == 0) {
            if (from is Note) selection.add(from)
            else {
                fun rec(nt: Notable<*>) {
                    this.selection.add(nt)
                    nt.filtered.contents.filterIsInstance<Notable<*>>().forEach { rec(it) }
                }
                rec(from)
            }
        }
    }

    fun getOpenedFrom(): Notable<*>? {
        if (openedFrom.id!! < 0) return null
        return this.openedFrom
    }

    override fun onLayoutInflated(view: View) {
        view.findViewById<Toolbar>(R.id.share_toolbar).setTitle(R.string.share)
        this.displayFragment(
            if (this.openedFrom is Note) OptionsFragment() else ComponentChooserFragment(),
            null,
            true
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> activity?.onBackPressed()
            else -> return false
        }
        return true
    }

    override fun shouldLockDrawer(): Boolean = true

    override fun saveState(savedInstanceState: Bundle) {
        savedInstanceState.putInt(SAVED_INSTANCE_TAG, this.openedFrom.id!!)
    }

    override fun restoreState(savedInstanceState: Bundle) {
        val notable = (this.activity as? MainActivity)?.dbProxy?.findElementById(
            savedInstanceState.getInt(SAVED_INSTANCE_TAG)) as Notable<*>
        this.from(notable)
    }

    fun getNotableId(): Int = this.openedFrom.id!!

    override fun getFragmentContainerId(): Int = R.id.share_fragment_container

}