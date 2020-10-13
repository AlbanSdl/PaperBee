package fr.asdl.minder.view.options

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import fr.asdl.minder.R

class ColorPicker(context: Context, private val colors: List<Int>,
                  private var selectedIndex: Int?, private val onSelect: (Int?) -> Unit) {

    private val dialog = AlertDialog.Builder(context)

    init {
        val recyclerView = RecyclerView(context)
        recyclerView.layoutManager = StaggeredGridLayoutManager(5, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.adapter = ColorAdapter()
        val padding = context.resources.getDimension(R.dimen.padding_small).toInt()
        recyclerView.setPadding(padding, padding, padding, padding)
        dialog.setTitle(R.string.color_change_title)
        dialog.setNegativeButton(R.string.confirm) { dial, _ -> dial.dismiss() }
        dialog.setView(recyclerView)
        dialog.show()
    }

    private fun select(image: ColorPickerView, position: Int) {
        if (selectedIndex != null)
            (image.parent!!.parent!! as View).findViewById<View>(id(selectedIndex!!)).findViewById<ColorPickerView>(R.id.color_picker_elem).deselect()
        val pos = if (position == selectedIndex) null else position
        selectedIndex = pos
        if (pos == null) {
            onSelect(null)
            image.deselect()
        } else {
            onSelect(colors[pos])
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
            image.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.context, colors[position]))
            image.setOnClickListener { select(image, position); }
        }

        override fun getItemCount(): Int {
            return this@ColorPicker.colors.size
        }

    }

    class ColorPickerView(context: Context, attributeSet: AttributeSet) : AppCompatImageView(context, attributeSet) {
        fun select() = this.setImageState(SELECTED_STATE_SET, false)
        fun deselect() = this.setImageState(EMPTY_STATE_SET, false)
    }

}