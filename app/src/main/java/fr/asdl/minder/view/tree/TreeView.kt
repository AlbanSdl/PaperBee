package fr.asdl.minder.view.tree

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import fr.asdl.minder.IntAllocator
import fr.asdl.minder.R

class TreeView(context: Context, attr: AttributeSet?, defStyleAttr: Int) : ScrollView (context, attr, defStyleAttr) {
    constructor(context: Context, attr: AttributeSet?) : this(context, attr, 0)
    constructor(context: Context) : this(context, null)

    private var treeRoot: TreeNode<*>? = null
    private val intAllocator: IntAllocator = IntAllocator()

    fun attachData(tree: TreeNode<*>) {
        this.intAllocator.clear()
        this.treeRoot = tree
        this.treeRoot!!.onAttached(intAllocator)
        this.updateDisplay(this.treeRoot!!)
    }

    private fun getViewHolder(node: TreeNode<*>?): ViewGroup? {
        if (node == null) return this
        return this.findViewById(id(node.getId()))
    }

    private fun updateDisplay(node: TreeNode<*>) {
        val current = this.getViewHolder(node)
        if (current == null)
            this.inflateNode(node, parent = this.getViewHolder(node.getParent())!!)
        else
            this.inflateNode(node, recycledView = current)
        if (node.isExpanded())
            for (i in 0 until node.getChildCount())
                updateDisplay(node.getChildAt(i))
    }

    @SuppressLint("InflateParams")
    private fun inflateNode(node: TreeNode<*>, parent: ViewGroup? = null, recycledView: ViewGroup? = null): ViewGroup {
        val root = recycledView ?: LayoutInflater.from(context).inflate(R.layout.tree_part_layout, null) as ViewGroup
        if (recycledView == null) parent?.addView(root)
        val treePart = root.findViewById(R.id.tree_linear_h) as LinearLayout
        val expandButton = treePart.findViewById<View>(R.id.tree_expand_button)
        fun updateGroupIndicator(expand: Boolean) {
            val expandImage = expandButton.findViewById<ImageView>(R.id.tree_expand_indicator)
            if (expand) {
                expandImage.setImageState(EMPTY_STATE_SET, false)
                for (i in 0 until node.getChildCount())
                    root.removeViewInLayout(getViewHolder(node.getChildAt(i)))
            } else {
                expandImage.setImageState(SELECTED_STATE_SET, false)
            }
        }
        if (!node.canExpand()) {
            expandButton.visibility = View.INVISIBLE
            expandButton.isClickable = false
        }
        else expandButton.setOnClickListener {
            updateGroupIndicator(!node.toggleExpansion())
            this.updateDisplay(node)
        }
        updateGroupIndicator(!node.isExpanded())
        root.id = id(node.getId())
        (treePart.layoutParams as MarginLayoutParams).marginStart =
            context.resources.getDimension(R.dimen.padding_small).toInt() * node.getDepth()
        node.onCreateView(LayoutInflater.from(context).inflate(node.getLayoutId(), treePart))
        return treePart
    }

    private fun id(id: Int): Int = (id + 200) shl 5

}