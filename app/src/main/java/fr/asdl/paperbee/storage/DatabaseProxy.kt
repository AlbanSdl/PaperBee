package fr.asdl.paperbee.storage

import android.content.Context
import fr.asdl.paperbee.IntAllocator
import fr.asdl.paperbee.R
import fr.asdl.paperbee.note.Notable
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
        return LinkedList(holderList.filterIsInstance<NotePart>().filter { it.getNote()?.id == id }.sortedBy { it.order })
    }

    fun add(dataHolder: DataHolder) {
        dataHolder.db = this
        holderList.add(dataHolder)
        dbAccess.insert(dataHolder) {
            if (!it) holderList.remove(dataHolder) // insertion failed
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
        ) { it ->
            if (it > 0) {
                if (dataHolder.id != null) idAllocator.release(dataHolder.id!!)
                holderList.remove(dataHolder)
                if (dataHolder is DataHolderList<*>) this.findContent(dataHolder.id).forEach { delete(it) }
                dataHolder.db = null // if the item is reinserted it will update its id
            }
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
        this.reMapIds(notables) { if (it is Notable<*>) destination.add(it) else this.delete(it) }
    }

    fun close() {
        this.dbAccess.close()
    }

    /**
     * Allocates new ids to the given elements. The current ids of element are just supposed
     * to indicate relationships between elements, they will NOT be de-allocated.
     */
    internal fun reMapIds(holders: List<DataHolder>, whenOrphan: (d: DataHolder) -> Unit = {}) {
        val parentRemapped = arrayListOf<DataHolder>()
        holders.forEach { it ->
            val previousId = it.id
            this.add(it)
            val allocatedId = it.id!!
            if (previousId != null)
                holders.filter { it.parentId == previousId && !parentRemapped.contains(it) }.forEach {
                    it.parentId = allocatedId
                    parentRemapped.add(it)
                }
        }
        holders.subtract(parentRemapped).forEach(whenOrphan)
    }
}