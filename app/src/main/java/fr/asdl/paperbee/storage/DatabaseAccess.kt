package fr.asdl.paperbee.storage

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import androidx.annotation.CallSuper
import fr.asdl.paperbee.PaperBeeApplication
import fr.asdl.paperbee.exceptions.ContextLostException
import fr.asdl.paperbee.view.sentient.DataHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.Closeable
import java.lang.ref.WeakReference

/**
 * The Access to the Notable database across application and database schema versions.
 * Contains the low level functions handled differently depending on the version of the database
 * schema.
 */
abstract class DatabaseAccess(context: Context) : Closeable {

    internal val context: WeakReference<Context> = WeakReference(context)
    private var _dbStoreW: SQLiteDatabase? = null
    private var _dbStoreR: SQLiteDatabase? = null

    protected val writeAccess: SQLiteDatabase
        get() = _dbStoreW ?: run { _dbStoreW = getBridge().writableDatabase; _dbStoreW!! }
    protected val readAccess: SQLiteDatabase
        get() = _dbStoreR ?: run { _dbStoreR = getBridge().readableDatabase; _dbStoreR!! }

    protected abstract fun getBridge(): SQLiteOpenHelper

    @CallSuper
    override fun close() {
        this._dbStoreW?.close()
        this._dbStoreR?.close()
    }

    protected abstract fun querySelect(
        filter: DatabaseFilter,
        sort: String?
    ): List<DataHolder>

    protected abstract fun queryInsert(holder: DataHolder): Long

    protected abstract fun queryDelete(filter: DatabaseFilter): Int

    protected abstract fun queryUpdate(
        holder: DataHolder,
        columnsToUpdate: Array<String>,
        filter: DatabaseFilter
    ): Int

    protected fun notifyUser(string: String) {
        Toast.makeText(context.get() ?: return, string, Toast.LENGTH_SHORT).show()
    }

    /**
     * Retrieve a list of DataHolders from the database.
     * @param filter the filter of the Database
     * @param sort the sorting part of the SQLite query. Use the name of the column followed by
     * ASC or DESC for an ascending or descending order.
     */
    fun select(
        filter: DatabaseFilter = DatabaseFilter(),
        sort: String? = null,
    ): List<DataHolder> {
        return this@DatabaseAccess.querySelect(filter, sort)
    }

    /**
     * Inserts a DataHolder in the database.
     */
    fun insert(dataHolder: DataHolder, callback: SQLInsertionCallback) {
        getScope().launch {
            callback.invoke(this@DatabaseAccess.queryInsert(dataHolder) >= 0)
        }
    }

    /**
     * Updates DataHolders in the Database.
     */
    fun update(
        holder: DataHolder,
        columnsToUpdate: Array<String>,
        filter: DatabaseFilter,
        callback: SQLCountedCallback
    ) {
        getScope().launch {
            callback.invoke(this@DatabaseAccess.queryUpdate(holder, columnsToUpdate, filter))
        }
    }

    /**
     * Deletes DataHolders from the Database.
     */
    fun delete(filter: DatabaseFilter, callback: SQLCountedCallback) {
        getScope().launch {
            callback.invoke(this@DatabaseAccess.queryDelete(filter))
        }
    }

    private fun getScope(): CoroutineScope {
        return (this.context.get() as? PaperBeeApplication)?.paperScope ?: throw ContextLostException()
    }

}