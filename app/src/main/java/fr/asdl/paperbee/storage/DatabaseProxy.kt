package fr.asdl.paperbee.storage

import android.content.Context
import fr.asdl.paperbee.IntAllocator
import fr.asdl.paperbee.R
import fr.asdl.paperbee.note.NoteFolder
import fr.asdl.paperbee.note.NotePart
import fr.asdl.paperbee.storage.v1.NotableContract.NotableContractInfo.COLUMN_NAME_ID
import fr.asdl.paperbee.view.sentient.DataHolder
import fr.asdl.paperbee.view.sentient.DataHolderList
import java.util.*

class DatabaseProxy<in T : DatabaseAccess>(context: Context, databaseClass: Class<T>) {

    companion object {
        const val ROOT_ID = -1
        const val TRASH_ID = -2
    }

    val context: Context get() = dbAccess.context
    private val idAllocator: IntAllocator = IntAllocator()
    private val dbAccess: DatabaseAccess =
        databaseClass.declaredConstructors[0].newInstance(context) as DatabaseAccess
    private val holderList: ArrayList<DataHolder> = arrayListOf()

    init {
        dbAccess.select {
            it.forEach { d -> d.db = this; holderList.add(d) }
        }
    }

    fun findElementById(id: Int?): DataHolder? = holderList.find { it.id == id }
    fun findContent(id: Int?): LinkedList<DataHolder> {
        return LinkedList(holderList.filter { it.parentId == id }.sortedBy { it.order })
    }
    fun findNoteContent(id: Int): LinkedList<NotePart> {
        return LinkedList(holderList.filterIsInstance<NotePart>().filter { it.getNote()?.id == id })
    }

    fun add(dataHolder: DataHolder) {
        dataHolder.db = this
        dbAccess.insert(dataHolder) {
            if (it) holderList.add(dataHolder)
        }
    }

    fun delete(dataHolder: DataHolder) {
        dbAccess.delete(
            DatabaseFilter().and(
                null,
                DatabaseFilter.FilterEntry(
                    COLUMN_NAME_ID,
                    dataHolder.id.toString(),
                    DatabaseFilter.Operator.EQUALS
                )
            )
        ) {
            if (it > 0)
                holderList.remove(dataHolder)
        }
    }

    fun update(
        dataHolder: DataHolder, columnsToUpdate: Array<String>,
        callback: (updated: Boolean) -> Unit
    ) {
        dbAccess.update(
            dataHolder, columnsToUpdate, DatabaseFilter().and(
                null,
                DatabaseFilter.FilterEntry(
                    COLUMN_NAME_ID,
                    dataHolder.id.toString(),
                    DatabaseFilter.Operator.EQUALS
                )
            )
        ) {
            callback(it > 0)
        }
    }

    internal fun registerHolder(dataHolder: DataHolder): Int {
        return if (dataHolder.id == null) idAllocator.allocate() else idAllocator.forceAllocate(
            dataHolder.id!!
        )
    }

    internal fun attachRoot() {
        holderList.add(this.acquireRoot())
        holderList.add(this.acquireTrash())
    }

    private fun acquireRoot(): NoteFolder {
        val root = NoteFolder()
        root.initializeId(ROOT_ID)
        root.title = dbAccess.context.getString(R.string.notes_root_name)
        root.db = this
        return root
    }

    private fun acquireTrash(): NoteFolder {
        val trash = NoteFolder()
        trash.initializeId(TRASH_ID)
        trash.title = dbAccess.context.getString(R.string.trash_can)
        trash.db = this
        return trash
    }

    /**
     * Imports elements from an export or a shared file and adds them to the current [DatabaseProxy].
     * The ids of these elements are remapped to match the [idAllocator] state.
     * If a notable already exists in the [DatabaseProxy] it will duplicate with a different id so
     * that the two notables are fully independent
     *
     * Can also be used to duplicate Notables by inserting A DEEP COPY of them in a list and calling
     * this method. You may use the ShareProcess to create an appropriate deep copy.
     */
    fun import(notables: List<DataHolder>, destination: NoteFolder = this.findElementById(ROOT_ID) as NoteFolder) {
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
            mappedIds[it] = this.idAllocator.forceAllocate(it)
        }
        notables.forEach { notable ->
            runRecursively(notable) {
                it.initializeId(mappedIds[it.id!!]!!)
                it.parentId = if (it.parentId in mappedIds) mappedIds[it.parentId!!] else destination.id!!
                // We reset position as it should not take the position of any existing element.
                it.order = -1
            }
        }
        // The ids have been remapped, now we can add the elements
        notables.forEach { this.add(it) }
    }

    fun close() {
        this.dbAccess.close()
    }

}