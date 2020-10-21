package fr.asdl.minder.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.transition.TransitionInflater
import fr.asdl.minder.IntAllocator
import fr.asdl.minder.R
import fr.asdl.minder.activities.fragments.AppFragment
import fr.asdl.minder.activities.fragments.NoteFragment
import fr.asdl.minder.activities.fragments.FolderFragment
import fr.asdl.minder.activities.fragments.LayoutFragment
import fr.asdl.minder.note.*


class MainActivity : AppCompatActivity() {

    lateinit var noteManager: NoteManager

    override fun onCreate(savedInstanceState: Bundle?) {
        noteManager = NoteManager(this, IntAllocator())
        noteManager.load()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (supportFragmentManager.backStackEntryCount == 0) {
            this.loadFragment(LayoutFragment(R.layout.loading), null)
            this.openNotable(noteManager, false)
            // Handling creation shortcut
            if (intent.extras?.containsKey("create") == true) {
                val note = Note("", noteManager, idAllocator = noteManager.idAllocator, parentId = noteManager.id)
                noteManager.add(note)
                note.add(NoteText(""))
                this.openNotable(note)
            }
        }
    }

    fun openNotable(notable: Notable<*>, vararg sharedViews: View) {
        this.openNotable(notable, true,
            *(if (notable is NoteFolder) arrayOf(*sharedViews)
                .plus(arrayOf(findViewById(R.id.add_note_button),
                    findViewById(R.id.add_note_selector),
                    findViewById(R.id.add_folder_selector),
                    findViewById(R.id.folder_color))) else sharedViews))
    }

    private fun openNotable(notable: Notable<*>, addToBackStack: Boolean, vararg  sharedViews: View) = this.loadFragment(
        if (notable is NoteFolder) FolderFragment().attach(notable) else NoteFragment().attach(notable as Note),
        if (addToBackStack) notable.id.toString() else null,
        if (notable is NoteManager) FragmentTransition.LOADING_FADE else if (notable is NoteFolder) FragmentTransition.SLIDE else FragmentTransition.EXPLODE,
        *sharedViews
    )

    private fun loadFragment(frag: AppFragment, addToBackStackTag: String?,
                     transition: FragmentTransition? = null, vararg  sharedViews: View) {

        if (transition != null) {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.folder_contents)
            if (currentFragment != null) {
                if (sharedViews.isNotEmpty() || transition != FragmentTransition.EXPLODE) {
                    currentFragment.sharedElementReturnTransition = TransitionInflater.from(this@MainActivity).inflateTransition(R.transition.note_editor_open)
                    currentFragment.exitTransition = TransitionInflater.from(this@MainActivity).inflateTransition(transition.animOut)
                } else {
                    currentFragment.exitTransition = TransitionInflater.from(this@MainActivity).inflateTransition(FragmentTransition.SLIDE.animOut)
                }
            }
            if (sharedViews.isNotEmpty() || transition != FragmentTransition.EXPLODE) {
                frag.sharedElementEnterTransition = TransitionInflater.from(this@MainActivity).inflateTransition(R.transition.note_editor_open)
                frag.enterTransition = TransitionInflater.from(this@MainActivity).inflateTransition(transition.animIn)
            } else {
                frag.enterTransition = TransitionInflater.from(this@MainActivity).inflateTransition(FragmentTransition.SLIDE.animIn)
            }
        }

        val transaction = supportFragmentManager.beginTransaction()
        arrayOf(*sharedViews).forEach {
            val targetTransitionName = (ViewCompat.getTransitionName(it) ?: "").replace(Regex("#\\d+"), "")
            transaction.addSharedElement(it, targetTransitionName)
        }
        transaction.replace(R.id.folder_contents, frag, addToBackStackTag)
        if (addToBackStackTag != null) transaction.addToBackStack(addToBackStackTag)
        transaction.commit()
    }

    private enum class FragmentTransition(val animIn: Int, val animOut: Int) {
        SLIDE(R.transition.slide_right, R.transition.slide_left),
        LOADING_FADE(android.R.transition.no_transition, android.R.transition.fade),
        EXPLODE(R.transition.folder_explode, R.transition.folder_explode)
    }

}