package fr.asdl.minder.activities.fragments.sharing

import android.view.LayoutInflater
import android.view.View
import android.view.View.NO_ID
import android.view.animation.AnimationUtils
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import fr.asdl.minder.R
import fr.asdl.minder.activities.MainActivity
import fr.asdl.minder.note.Notable
import fr.asdl.minder.note.bindings.NotableTree
import fr.asdl.minder.view.tree.TreeView

class ComponentChooserFragment : ShareBaseFragment() {

    override val layoutId: Int = R.layout.share_chooser

    override fun onLayoutInflated(view: View) {
        val orig = this@ComponentChooserFragment.getSharingFragment()
        val layoutInflater = LayoutInflater.from(this.context)
        val next = view.findViewById<View>(R.id.next)

        fun updateNextButton() {
            val size = orig?.selection?.size ?: 0
            if (next.isEnabled != size > 0) {
                next.animation = AnimationUtils.loadAnimation(
                    this.context,
                    if (next.isEnabled) R.anim.scale_out else R.anim.scale_in
                )
                next.isEnabled = size > 0
                next.visibility = if (next.isEnabled) View.VISIBLE else View.GONE
                next.animate()
            }
        }

        fun updateChip(notable: Notable<*>, addition: Boolean) {
            val chipGroup = view.findViewById<ChipGroup>(R.id.share_selection_group)
            updateNextButton()
            if (!addition) {
                val chip = view.findViewWithTag<Chip>(notable)
                if (chip != null) chipGroup.removeView(chip)
            } else {
                val chip = layoutInflater.inflate(R.layout.directory_notable_chip, chipGroup)
                    .findViewById<Chip>(R.id.chip)
                chip.text = notable.title
                chip.id = NO_ID
                chip.setOnCloseIconClickListener {
                    chipGroup.removeView(chip)
                    orig?.selection?.remove(notable)
                    updateNextButton()
                }
                chip.tag = notable
            }
        }

        this.setToolBarIsClose(true)
        if (orig != null) {
            orig.selection.forEach {
                updateChip(it, true)
            }
            view.findViewById<TreeView>(R.id.share_selector_tree).attachData(
                NotableTree(
                    orig.getOpenedFrom() ?: (activity as MainActivity).noteManager
                ) {
                    if (it !in orig.selection) {
                        fun addRec(notable: Notable<*>) {
                            if (notable !in orig.selection) {
                                orig.selection.add(notable)
                                updateChip(notable, true)
                            }
                            notable.getContents().filterIsInstance<Notable<*>>()
                                .forEach { n -> addRec(n) }
                        }
                        addRec(it)
                    } else {
                        orig.selection.remove(it)
                        updateChip(it, false)
                    }
                })
            next.setOnClickListener {
                orig.displayFragment(OptionsFragment(), "optionShare${orig.getNotableId()}")
            }
        }
        updateNextButton()
    }

    override fun getSharedViews(): List<View> {
        return listOf(this.view!!.findViewById(R.id.next))
    }

}