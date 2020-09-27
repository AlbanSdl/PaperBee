package fr.asdl.minder.activities.fragments

import android.os.Bundle
import android.util.TypedValue
import android.view.*
import androidx.fragment.app.Fragment

abstract class MinderFragment : Fragment() {

    abstract val layoutId: Int
    open val menuLayoutId: Int? = null
    open val styleId: Int? = null
    abstract fun onLayoutInflated(view: View)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.setHasOptionsMenu(true)
        val fragmentInflater = if (styleId != null) inflater.cloneInContext(ContextThemeWrapper(activity, styleId!!)) else inflater

        val statusBarColor = TypedValue()
        fragmentInflater.context.theme.resolveAttribute(android.R.attr.statusBarColor, statusBarColor, true)
        activity?.window?.statusBarColor = statusBarColor.data

        val view = fragmentInflater.inflate(this.layoutId, container, false)
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

}