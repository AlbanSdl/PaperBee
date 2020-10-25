package fr.asdl.paperbee.note

import android.content.Context
import fr.asdl.paperbee.IntAllocator
import fr.asdl.paperbee.R
import fr.asdl.paperbee.preferences.SavedDataDirectory
import fr.asdl.paperbee.view.sentient.DataHolder
import fr.asdl.paperbee.view.sentient.DataHolderList

class NoteManager(context: Context, idAllocator: IntAllocator) : NoteFolder(context.getString(R.string.notes_root_name), idAllocator = idAllocator, noteManager = null, parentId = null) {

    companion object {
        const val ROOT_ID = -1
        const val TRASH_ID = -2
    }

    private val dataDirectory = SavedDataDirectory(context.getString(R.string.notes_directory_name), context)
    private val serializer = NoteSerializer()
    private val trash = NoteFolder(context.getString(R.string.trash_can), noteManager = this, idAllocator = idAllocator, parentId = null)

    init {
        this.noteManager = this
        this.trash.id = TRASH_ID
        this.trash.notify = true
    }

    fun load() {
        this.load(dataDirectory.loadData(this.serializer), this)
    }

    public override fun save(element: Notable<*>): Boolean {
        element.noteManager = this
        if (element.noteManager == null) element.noteManager = this
        this.dataDirectory.saveDataAsync(element, serializer = this.serializer)
        return true
    }

    public override fun delete(element: Notable<*>, oldId: Int): Boolean {
        this.dataDirectory.saveDataAsync(id = oldId, serializer = this.serializer)
        return true
    }

    override fun findElementById(id: Int?): DataHolder? {
        if (id == TRASH_ID) return trash
        return super.findElementById(id)
    }

    override fun shouldNotify(): Boolean = true
    override var id: Int? = ROOT_ID
    override var order: Int = 0

    private fun load(notables: ArrayList<Notable<*>>, destination: NoteFolder) {
        fun fillFolder(folder: Notable<*>) {
            if (folder !is NoteFolder) return
            val stored = notables.filter { it.parentId == folder.id }
            notables.removeAll(stored)
            stored.sortedBy { it.order }.forEach {
                folder.add(it)
                fillFolder(it)
            }
        }
        fillFolder(destination)
        // Put everything else in the trash
        notables.forEach { trash.add(it) }
    }

    /**
     * Imports elements from an export or a shared file and adds them to the current [NoteManager].
     * The ids of these elements are remapped to match the [idAllocator] state.
     * If a notable already exists in the [NoteManager] it will duplicate with a different id so
     * that the two notables are fully independent
     *
     * Can also be used to duplicate Notables by inserting A DEEP COPY of them in a list and calling
     * this method. You may use the ShareProcess to create an appropriate deep copy.
     */
    fun import(notables: List<Notable<*>>, destination: NoteFolder = this) {
        if (this.idAllocator == null) return
        // We define a recursive function to propagate actions to children at any depth
        // In our case, this will only take action to reach NoteParts as Notes are not
        // stored as content of NoteFolders
        fun runRecursively(holder: DataHolder, execute: (DataHolder) -> Unit) {
            execute(holder)
            if (holder is DataHolderList<*>)
                holder.getContents().forEach { runRecursively(it, execute) }
        }
        // We have to remap the ids of all components: Notables and NoteParts, updating the
        // parentIds too.
        val mappedIds = hashMapOf<Int, Int>()
        notables.forEach { notable ->
            runRecursively(notable) {
                mappedIds[it.id!!] = -1
            }
        }
        mappedIds.keys.forEach {
            mappedIds[it] = this.idAllocator!!.forceAllocate(it)
        }
        notables.forEach { notable ->
            runRecursively(notable) {
                it.id = mappedIds[it.id!!]
                it.parentId = if (it.parentId in mappedIds) mappedIds[it.parentId!!] else destination.id!!
                // We reset position as it should not take the position of any existing element.
                it.order = -1
            }
        }
        // The ids have been remapped, now we can add the elements
        this.load(ArrayList(notables), destination)
    }
}