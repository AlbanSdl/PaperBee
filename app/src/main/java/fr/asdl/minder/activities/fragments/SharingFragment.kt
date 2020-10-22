package fr.asdl.minder.activities.fragments

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.transition.TransitionInflater
import fr.asdl.minder.R
import fr.asdl.minder.activities.MainActivity
import fr.asdl.minder.activities.fragments.sharing.ComponentChooserFragment
import fr.asdl.minder.activities.fragments.sharing.OptionsFragment
import fr.asdl.minder.activities.fragments.sharing.ShareBaseFragment
import fr.asdl.minder.note.Notable
import fr.asdl.minder.note.Note

class SharingFragment : AppFragment() {

    companion object {
        const val SAVED_INSTANCE_TAG = "minder:fragOpenedFromTagId"
    }

    override val layoutId: Int = R.layout.share_layout
    val selection = arrayListOf<Notable<*>>()

    private lateinit var openedFrom: Notable<*>

    fun from(from: Notable<*>) {
        this.openedFrom = from
        if (from.id!! >= 0) {
            if (from is Note) selection.add(from)
            else {
                fun rec(nt: Notable<*>) {
                    this.selection.add(nt)
                    nt.getRawContents().filterIsInstance<Notable<*>>().forEach { rec(it) }
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
        val toolbar = view.findViewById<Toolbar>(R.id.share_toolbar)
        toolbar.setTitle(R.string.share)
        this.displayFragment(if (this.openedFrom is Note) OptionsFragment() else ComponentChooserFragment(), null)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> activity?.onBackPressed()
            else -> return false
        }
        return true
    }

    override fun saveState(savedInstanceState: Bundle) {
        savedInstanceState.putInt(SAVED_INSTANCE_TAG, this.openedFrom.id!!)
    }

    override fun restoreState(savedInstanceState: Bundle) {
        val notable = (this.activity as? MainActivity)?.noteManager?.findElementById(
            savedInstanceState.getInt(SAVED_INSTANCE_TAG)) as Notable<*>
        this.from(notable)
    }

    fun getNotableId(): Int = this.openedFrom.id!!

    fun displayFragment(frag: ShareBaseFragment, addToBackStackTag: String?) {

        val transitionInflater = TransitionInflater.from(this.context)
        val currentFragment = activity!!.supportFragmentManager.findFragmentById(R.id.share_fragment_container)
        if (currentFragment != null) {
            currentFragment.sharedElementReturnTransition = transitionInflater.inflateTransition(R.transition.note_editor_open)
            currentFragment.exitTransition = transitionInflater.inflateTransition(R.transition.slide_left)
        }
        frag.sharedElementEnterTransition = transitionInflater.inflateTransition(R.transition.note_editor_open)
        frag.enterTransition = transitionInflater.inflateTransition(R.transition.slide_right)

        val transaction = activity!!.supportFragmentManager.beginTransaction()
        (currentFragment as? ShareBaseFragment)?.getSharedViews()?.forEach {
            val targetTransitionName = (ViewCompat.getTransitionName(it) ?: "").replace(Regex("#\\d+"), "")
            transaction.addSharedElement(it, targetTransitionName)
        }
        transaction.replace(R.id.share_fragment_container, frag, addToBackStackTag)
        if (addToBackStackTag != null) transaction.addToBackStack(addToBackStackTag)
        transaction.commit()
    }

}