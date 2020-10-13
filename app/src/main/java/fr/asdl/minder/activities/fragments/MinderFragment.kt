package fr.asdl.minder.activities.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import androidx.annotation.CallSuper
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import fr.asdl.minder.activities.MainActivity
import fr.asdl.minder.note.Notable
import fr.asdl.minder.note.Note
import fr.asdl.minder.note.NoteFolder

abstract class MinderFragment<T: Notable<*>> : Fragment() {

    companion object {
        const val SAVED_INSTANCE_TAG = "minder:fragNotableTagId"
    }

    @CallSuper
    open fun attach(notable: T): MinderFragment<T> {
        this.notable = notable
        return this
    }

    abstract fun getTintBackgroundView(fragmentRoot: View): View?

    protected fun updateBackgroundTint(root: View? = null) {
        val view = getTintBackgroundView(root ?: this.view!!) ?: return
        if (notable.color == null)
            view.backgroundTintList = null
        else
            view.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!, notable.color!!.id))
    }

    open fun restore(savedInstanceState: Bundle) {
        val t = (this.activity as? MainActivity)?.noteManager?.findElementById(savedInstanceState.getInt(SAVED_INSTANCE_TAG))
        if (this is FolderFragment && t is NoteFolder) this.attach(t)
        else if (this is EditorFragment && t is Note) this.attach(t)
    }

    protected abstract var notable: T
    abstract val layoutId: Int
    open var menuLayoutId: Int? = null
    open val styleId: Int? = null
    abstract fun onLayoutInflated(view: View)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (savedInstanceState != null) this.restore(savedInstanceState)
        this.setHasOptionsMenu(true)
        val fragmentInflater = if (styleId != null) inflater.cloneInContext(ContextThemeWrapper(activity, styleId!!)) else inflater

        val statusBarColor = TypedValue()
        fragmentInflater.context.theme.resolveAttribute(android.R.attr.statusBarColor, statusBarColor, true)
        activity?.window?.statusBarColor = statusBarColor.data

        val view = fragmentInflater.inflate(this.layoutId, container, false)
        this.updateBackgroundTint(view)
        this.onLayoutInflated(view)
        return view
    }

    final override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (this.menuLayoutId != null) inflater.inflate(this.menuLayoutId!!, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(SAVED_INSTANCE_TAG, this.notable.id!!)
    }
}