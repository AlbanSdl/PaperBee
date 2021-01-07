package fr.asdl.paperbee.activities

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.transition.TransitionInflater
import com.google.android.material.navigation.NavigationView
import fr.asdl.paperbee.R
import fr.asdl.paperbee.activities.fragments.*
import fr.asdl.paperbee.activities.fragments.sharing.SharingMethod
import fr.asdl.paperbee.note.*
import fr.asdl.paperbee.storage.DatabaseProxy
import fr.asdl.paperbee.storage.DatabaseProxy.Companion.ROOT_ID
import fr.asdl.paperbee.storage.v1.DatabaseAccess
import fr.asdl.paperbee.view.DarkThemed


class MainActivity : AppCompatActivity(), DarkThemed {

    lateinit var dbProxy: DatabaseProxy<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        this.findViewById<NavigationView>(R.id.nav).apply {
            this.setNavigationItemSelectedListener { navigate(it, this) }
        }
        this.dbProxy = DatabaseProxy(this, DatabaseAccess::class.java)
        this.dbProxy.attachRoot()
        if (supportFragmentManager.backStackEntryCount == 0) {
            this.openNotable(dbProxy.findElementById(ROOT_ID) as NoteFolder, false)
            // Handling creation shortcut
            if (intent.extras?.containsKey("create") == true) {
                val note = Note()
                (dbProxy.findElementById(ROOT_ID) as NoteFolder).add(note)
                note.add(NoteText(""))
                this.openNotable(note)
            }
        }
    }

    private fun navigate(it: MenuItem, navigationView: NavigationView?): Boolean {
        if (navigationView?.checkedItem != it || navigationView.checkedItem?.isChecked == false) {
            this.findViewById<DrawerLayout>(R.id.main).closeDrawers()
            when(it.itemId) {
                R.id.drawer_settings -> {
                    val preferences = PreferenceFragmentRoot()
                    preferences.allowEnterTransitionOverlap = true
                    this.loadFragment(preferences, "preferences", FragmentTransition.SLIDE_BOTTOM)
                }
                R.id.goto_trash -> {
                    this.openNotable(dbProxy.findElementById(DatabaseProxy.TRASH_ID) as NoteFolder)
                }
                R.id.goto_main -> {
                    this.supportFragmentManager.popBackStack()
                }
            }
        }
        return false
    }

    fun openNotable(notable: Notable<*>, vararg sharedViews: View) {
        this.openNotable(
            notable, true,
            *(if (notable is NoteFolder) arrayOf(*sharedViews)
                .plus(
                    arrayOf(
                        findViewById(R.id.add_note_button),
                        findViewById(R.id.add_note_selector),
                        findViewById(R.id.add_folder_selector),
                        findViewById(R.id.folder_color)
                    )
                ) else sharedViews)
        )
    }

    fun startSharing(notable: Notable<*>) {
        if (notable.id!! < ROOT_ID) return
        val fragment = SharingFragment()
        fragment.from(notable)
        fragment.allowEnterTransitionOverlap = true
        this.loadFragment(fragment, "share${notable.id!!}", FragmentTransition.SLIDE_BOTTOM)
    }

    fun openImport(method: SharingMethod? = null) {
        val fragment = ImportFragment()
        if (method != null) fragment.method = method
        fragment.allowEnterTransitionOverlap = true
        this.loadFragment(fragment, "import", FragmentTransition.SLIDE_BOTTOM)
    }

    private fun openNotable(
        notable: Notable<*>,
        addToBackStack: Boolean,
        vararg sharedViews: View
    ) = this.loadFragment(
        if (notable is NoteFolder) FolderFragment().attach(notable) else NoteFragment().attach(
            notable as Note
        ),
        if (addToBackStack) notable.id.toString() else null,
        if (notable.id == ROOT_ID) FragmentTransition.LOADING_FADE else if (notable is NoteFolder) FragmentTransition.SLIDE else FragmentTransition.EXPLODE,
        *sharedViews
    )

    private fun loadFragment(
        frag: AppFragment, addToBackStackTag: String?,
        transition: FragmentTransition? = null, vararg sharedViews: View
    ) {

        if (transition != null) {
            val transitionInflater = TransitionInflater.from(this@MainActivity)
            val currentFragment = supportFragmentManager.findFragmentById(R.id.folder_contents)
            if (currentFragment != null) {
                if (sharedViews.isNotEmpty() || transition != FragmentTransition.EXPLODE) {
                    currentFragment.sharedElementReturnTransition =
                        transitionInflater.inflateTransition(R.transition.shared_elements_transition)
                    currentFragment.exitTransition =
                        transitionInflater.inflateTransition(transition.animOut)
                } else {
                    currentFragment.exitTransition =
                        transitionInflater.inflateTransition(FragmentTransition.SLIDE.animOut)
                }
            }
            if (sharedViews.isNotEmpty() || transition != FragmentTransition.EXPLODE) {
                frag.sharedElementEnterTransition =
                    transitionInflater.inflateTransition(R.transition.shared_elements_transition)
                frag.enterTransition = transitionInflater.inflateTransition(transition.animIn)
            } else {
                frag.enterTransition =
                    transitionInflater.inflateTransition(FragmentTransition.SLIDE.animIn)
            }
        }

        val transaction = supportFragmentManager.beginTransaction()
        transaction.setPrimaryNavigationFragment(frag)
        arrayOf(*sharedViews).forEach {
            val targetTransitionName =
                (ViewCompat.getTransitionName(it) ?: "").replace(Regex("#\\d+"), "")
            transaction.addSharedElement(it, targetTransitionName)
        }
        transaction.replace(R.id.folder_contents, frag, addToBackStackTag)
        if (addToBackStackTag != null) transaction.addToBackStack(addToBackStackTag)
        transaction.commit()
    }

    private enum class FragmentTransition(val animIn: Int, val animOut: Int) {
        SLIDE(R.transition.slide_right, R.transition.slide_left),
        SLIDE_BOTTOM(R.transition.slide_bottom, R.transition.folder_explode),
        LOADING_FADE(android.R.transition.no_transition, android.R.transition.fade),
        EXPLODE(R.transition.folder_explode, R.transition.folder_explode)
    }

    override fun onBackPressed() {
        val drawer = this.findViewById<DrawerLayout>(R.id.main)
        if (drawer.isDrawerOpen(GravityCompat.START)) drawer.closeDrawer(GravityCompat.START)
        else {
            window.decorView.clearFocus() // we close the keyboard if displayed
            super.onBackPressed()
        }
    }

    override fun requireContext(): Context {
        return this
    }

    override fun onDestroy() {
        this.dbProxy.close()
        super.onDestroy()
    }

}