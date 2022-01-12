package fr.asdl.paperbee.activities.fragments

import androidx.annotation.IdRes
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentManager

interface FragmentContainer<in T> where T : AppFragment, T : SubFragment {

    fun getChildFragmentManager(): FragmentManager

    @IdRes
    fun getFragmentContainerId(): Int

    fun displayFragment(fragment: T, addToBackStackTag: String?, isFromOnCreateView: Boolean = false) {
        if (isFromOnCreateView && this.getChildFragmentManager().fragments.size != 0) return
        val currentFragment =
            getChildFragmentManager().findFragmentById(this.getFragmentContainerId())

        val transaction = getChildFragmentManager().beginTransaction()
        (currentFragment as? SubFragment)?.getSharedViews()?.forEach {
            transaction.addSharedElement(it, ViewCompat.getTransitionName(it) ?: "")
        }
        transaction.replace(this.getFragmentContainerId(), fragment, addToBackStackTag)
        if (addToBackStackTag != null) transaction.addToBackStack(addToBackStackTag)
        transaction.commit()
    }
}