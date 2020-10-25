package fr.asdl.paperbee.activities.fragments.sharing

import android.view.LayoutInflater
import android.view.View
import android.view.View.NO_ID
import android.view.animation.AnimationUtils
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import fr.asdl.paperbee.R
import fr.asdl.paperbee.activities.MainActivity
import fr.asdl.paperbee.activities.fragments.SharingFragment
import fr.asdl.paperbee.note.Notable
import fr.asdl.paperbee.note.bindings.NotableTree
import fr.asdl.paperbee.view.StatefulExtendedFloatingActionButton
import fr.asdl.paperbee.view.tree.TreeView

class ComponentChooserFragment : ShareBaseFragment() {

    override val layoutId: Int = R.layout.share_chooser

    override fun onLayoutInflated(view: View) {
        val orig = this.parentFragment as SharingFragment
        val layoutInflater = LayoutInflater.from(this.context)
        val next = view.findViewById<StatefulExtendedFloatingActionButton>(R.id.next)

        fun updateNextButton() {
            val size = orig.selection.size
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
                    orig.selection.remove(notable)
                    updateNextButton()
                }
                chip.tag = notable
            }
        }

        this.setToolBarIsClose(true)

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
            next.setStateChanged(true)
            orig.displayFragment(OptionsFragment(), "optionShare${orig.getNotableId()}")
        }
        updateNextButton()
    }

    override fun getSharedViews(): List<View> {
        if (this.view == null) return super.getSharedViews()
        return listOf(this.view!!.findViewById(R.id.next))
    }

}