package fr.asdl.paperbee.view

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import fr.asdl.paperbee.R

class StatefulExtendedFloatingActionButton(context: Context, attributeSet: AttributeSet) : ExtendedFloatingActionButton(context, attributeSet) {

    companion object {
        private val STATE_CHANGED = IntArray(1) { R.attr.state_changed }
    }

    private var stateChanged = false

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        var state = super.onCreateDrawableState(extraSpace)
        if (stateChanged)
            state = mergeDrawableStates(state, STATE_CHANGED)
        return state
    }

    fun setStateChanged(state: Boolean) {
        if (state != this.stateChanged) {
            this.stateChanged = !this.stateChanged
            this.refreshDrawableState()
        }
    }

}