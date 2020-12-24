package fr.asdl.paperbee.activities.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.core.content.ContextCompat
import fr.asdl.paperbee.activities.MainActivity
import fr.asdl.paperbee.note.Notable
import fr.asdl.paperbee.note.Note
import fr.asdl.paperbee.note.NoteFolder

abstract class NotableFragment<T: Notable<*>> : AppFragment() {

    companion object {
        const val SAVED_INSTANCE_TAG = "paperbee:fragNotableTagId"
    }

    protected abstract var notable: T
    override val shouldRetainInstance: Boolean = true

    @CallSuper
    open fun attach(notable: T): NotableFragment<T> {
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

    override fun restoreState(savedInstanceState: Bundle) {
        val t = (this.activity as? MainActivity)?.noteManager?.findElementById(savedInstanceState.getInt(SAVED_INSTANCE_TAG))
        if (this is FolderFragment && t is NoteFolder) this.attach(t)
        else if (this is NoteFragment && t is Note) this.attach(t)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        this.updateBackgroundTint(view)
        return view
    }

    override fun saveState(savedInstanceState: Bundle) {
        savedInstanceState.putInt(SAVED_INSTANCE_TAG, this.notable.id!!)
    }
}