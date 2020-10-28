package fr.asdl.paperbee.activities.fragments

import android.view.View

class LayoutFragment(override val layoutId: Int) : AppFragment() {

    override fun onLayoutInflated(view: View) {}

    override fun shouldLockDrawer(): Boolean = false

}