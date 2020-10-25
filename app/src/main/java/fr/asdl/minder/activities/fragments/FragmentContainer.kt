package fr.asdl.minder.activities.fragments

import android.content.Context
import androidx.annotation.IdRes
import androidx.annotation.TransitionRes
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentManager
import androidx.transition.TransitionInflater
import fr.asdl.minder.R

interface FragmentContainer<in T> where T : AppFragment, T : SubFragment {

    data class TransitionComponents(
        @TransitionRes val transitionIn: Int = R.transition.slide_right,
        @TransitionRes val transitionOut: Int = R.transition.slide_left
    )

    fun getChildFragmentManager(): FragmentManager
    fun getContext(): Context?

    @IdRes
    fun getFragmentContainerId(): Int
    fun getTransition(fragment: T, backStack: String?): TransitionComponents =
        TransitionComponents()

    fun displayFragment(fragment: T, addToBackStackTag: String?, isFromOnCreateView: Boolean = false) {
        if (isFromOnCreateView && this.getChildFragmentManager().fragments.size != 0) return
        val transitionInflater = TransitionInflater.from(getContext() ?: return)
        val currentFragment =
            getChildFragmentManager().findFragmentById(this.getFragmentContainerId())
        val transition = this.getTransition(fragment, addToBackStackTag)
        if (currentFragment != null) {
            currentFragment.sharedElementReturnTransition =
                transitionInflater.inflateTransition(R.transition.shared_elements_transition)
            currentFragment.exitTransition =
                transitionInflater.inflateTransition(transition.transitionOut)
        }
        fragment.sharedElementEnterTransition =
            transitionInflater.inflateTransition(R.transition.shared_elements_transition)
        fragment.enterTransition = transitionInflater.inflateTransition(transition.transitionIn)

        val transaction = getChildFragmentManager().beginTransaction()
        (currentFragment as? SubFragment)?.getSharedViews()?.forEach {
            transaction.addSharedElement(it, ViewCompat.getTransitionName(it) ?: "")
        }
        transaction.replace(this.getFragmentContainerId(), fragment, addToBackStackTag)
        if (addToBackStackTag != null) transaction.addToBackStack(addToBackStackTag)
        transaction.commit()
    }
}