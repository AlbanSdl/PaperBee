package fr.asdl.minder.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.transition.TransitionInflater
import fr.asdl.minder.IntAllocator
import fr.asdl.minder.R
import fr.asdl.minder.activities.fragments.EditorFragment
import fr.asdl.minder.activities.fragments.FolderFragment
import fr.asdl.minder.note.Notable
import fr.asdl.minder.note.Note
import fr.asdl.minder.note.NoteFolder
import fr.asdl.minder.note.NoteManager


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        this.loadFragment(Fragment(R.layout.loading), null)
        Thread {
            val noteManager = NoteManager(this, IntAllocator())
            noteManager.load()
            runOnUiThread {
                this.openNotable(noteManager, false)
            }
        }.start()
    }

    fun openNotable(notable: Notable<*>, vararg sharedViews: View) {
        this.openNotable(notable, true, *sharedViews)
    }

    private fun openNotable(notable: Notable<*>, addToBackStack: Boolean, vararg  sharedViews: View) = this.loadFragment(
        if (notable is NoteFolder) FolderFragment(notable) else EditorFragment(notable as Note),
        if (addToBackStack) notable.id.toString() else null,
        if (notable is NoteManager) FragmentTransition.LOADING_FADE else if (notable is NoteFolder) FragmentTransition.SLIDE else FragmentTransition.EXPLODE,
        *sharedViews
    )

    private fun loadFragment(frag: Fragment, addToBackStackTag: String?,
                             transition: FragmentTransition? = null,
                             vararg  sharedViews: View) {

        if (transition != null) {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.folder_contents)
            if (currentFragment != null) {
                currentFragment.sharedElementReturnTransition = TransitionInflater.from(this@MainActivity).inflateTransition(R.transition.note_editor_open)
                currentFragment.exitTransition = TransitionInflater.from(this@MainActivity).inflateTransition(transition.animOut)
            }
            frag.sharedElementEnterTransition = TransitionInflater.from(this@MainActivity).inflateTransition(R.transition.note_editor_open)
            frag.enterTransition = TransitionInflater.from(this@MainActivity).inflateTransition(transition.animIn)
        }

        val transaction = supportFragmentManager.beginTransaction()
        arrayOf(*sharedViews).forEach { transaction.addSharedElement(it, ViewCompat.getTransitionName(it) ?: "") }
        transaction.replace(R.id.folder_contents, frag, addToBackStackTag)
        if (addToBackStackTag != null) transaction.addToBackStack(addToBackStackTag)
        transaction.commit()
    }

    private enum class FragmentTransition(val animIn: Int, val animOut: Int) {
        SLIDE(android.R.transition.slide_left, android.R.transition.slide_right),
        LOADING_FADE(android.R.transition.no_transition, android.R.transition.fade),
        EXPLODE(android.R.transition.explode, android.R.transition.explode)
    }

}