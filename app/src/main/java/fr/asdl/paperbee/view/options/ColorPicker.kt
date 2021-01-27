package fr.asdl.paperbee.view.options

import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import fr.asdl.paperbee.R
import fr.asdl.paperbee.view.rounded.RoundedImageView
import java.lang.ref.WeakReference

class ColorPicker<T : IColor>(
    context: Context, private val colors: Array<T>,
    selected: T?, private val callbackOnClose: Boolean,
    @StringRes title: Int? = null, colorContext: Context? = null,
    private val onSelect: (T?) -> Unit
) {

    private val colorContext: WeakReference<Context?> = WeakReference(colorContext)
    private var selectedIndex: Int = colors.indexOf(selected)

    init {
        val recyclerView = RecyclerView(context)
        recyclerView.layoutManager =
            StaggeredGridLayoutManager(5, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.adapter = ColorAdapter()
        recyclerView.overScrollMode = View.OVER_SCROLL_NEVER
        val padding = context.resources.getDimension(R.dimen.padding_small).toInt()
        recyclerView.setPadding(padding, padding, padding, padding)
        AlertDialog.Builder(context, R.style.ColorPickerTheme)
            .setTitle(title ?: R.string.color_change_title)
            .setNegativeButton(if (callbackOnClose) R.string.confirm else R.string.close) { dial, _ -> dial.dismiss() }
            .setView(recyclerView)
            .setOnDismissListener { if (this.callbackOnClose) onSelect(if (selectedIndex < 0) null else colors[selectedIndex]) }
            .show()
    }

    private fun select(image: ColorPickerView, position: Int) {
        if (selectedIndex >= 0)
            (image.parent!!.parent!!.parent!! as View).findViewById<View>(id(selectedIndex))
                .findViewById<ColorPickerView>(R.id.color_picker_elem).deselect()
        val pos = if (position == selectedIndex) -1 else position
        selectedIndex = pos
        if (pos < 0) {
            if (!this.callbackOnClose) onSelect(null)
            image.deselect()
        } else {
            if (!this.callbackOnClose) onSelect(colors[pos])
            image.select()
        }
    }

    private fun id(position: Int): Int = (position + 200) shl 5

    private inner class ColorAdapter : RecyclerView.Adapter<ColorAdapter.ViewHolder>() {

        private inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.picker_layout, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val image = holder.itemView.findViewById<ColorPickerView>(R.id.color_picker_elem)
            holder.itemView.id = id(position)
            if (position == selectedIndex) image.select()
            else image.deselect()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) image.tooltipText =
                holder.itemView.context.getString(colors[position].colorName)
            image.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    this@ColorPicker.colorContext.get() ?: holder.itemView.context,
                    colors[position].id
                )
            )
            image.setOnClickListener { select(image, position); }
        }

        override fun getItemCount(): Int {
            return this@ColorPicker.colors.size
        }

    }

    class ColorPickerView(context: Context, attributeSet: AttributeSet) :
        RoundedImageView(context, attributeSet) {
        override fun doesOverrideBackground(): Boolean = false
        fun select() = this.setImageState(SELECTED_STATE_SET, true)
        fun deselect() = this.setImageState(EMPTY_STATE_SET, true)
    }

}