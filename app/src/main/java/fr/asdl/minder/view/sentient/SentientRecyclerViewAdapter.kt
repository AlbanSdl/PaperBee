package fr.asdl.minder.view.sentient

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView


abstract class SentientRecyclerViewAdapter<T : DataHolder>(
    private val dataContainer: DataHolderList<T>
) : RecyclerView.Adapter<SentientRecyclerViewAdapter.ViewHolder>() {

    init {
        this.dataContainer.on(ModificationType.ADDITION) { i: Int, _: Int? -> super.notifyItemInserted(i) }
        this.dataContainer.on(ModificationType.REMOVAL) { i: Int, _: Int? -> super.notifyItemRemoved(i) }
        this.dataContainer.on(ModificationType.UPDATE) { i: Int, _: Int? -> super.notifyItemChanged(i) }
        this.dataContainer.on(ModificationType.MOVED) { i: Int, it: Int? -> super.notifyItemMoved(i, it!!) }
        this.dataContainer.on(ModificationType.CLEAR) { _: Int, _: Int? -> super.notifyDataSetChanged() }
    }

    fun getDataHolder(): DataHolderList<T> {
        return this.dataContainer
    }

    fun getData(index: Int): T? {
        return this.dataContainer.getContents()[index]
    }

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(this.getLayoutId(), parent, false))
    }

    final override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (this.getHeldItem(position) != null) this.onBindViewHolder(holder, this.getHeldItem(position)!!)
    }

    abstract fun onBindViewHolder(holder: ViewHolder, content: T)

    final override fun getItemCount(): Int {
        return this.dataContainer.getContents().size
    }

    abstract fun getLayoutId(): Int

    private fun getHeldItem(pos: Int): T? {
        return if (this.dataContainer.getContents().size > pos) this.dataContainer.getContents()[pos] else null
    }

    class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        fun findViewById(id: Int): View? {
            return this.view.findViewById(id)
        }
    }

}