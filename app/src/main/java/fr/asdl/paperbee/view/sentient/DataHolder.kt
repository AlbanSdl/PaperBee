package fr.asdl.paperbee.view.sentient

import fr.asdl.paperbee.PaperBeeApplication
import fr.asdl.paperbee.storage.DatabaseProxy
import fr.asdl.paperbee.storage.v1.NotableContract.NotableContractInfo.COLUMN_NAME_ORDER
import fr.asdl.paperbee.storage.v1.NotableContract.NotableContractInfo.COLUMN_NAME_PARENT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * The base class for any item contained in a [DataHolderList]. This is useful for any element
 * to display in a [SentientRecyclerViewAdapter]
 */
abstract class DataHolder {

    private var _id: Int? = null
    private var _dbProxy: DatabaseProxy<*>? = null
    private var _dataChanged: ArrayList<String> = arrayListOf()
    private var _order: Int = -1
    private var _parent: Int? = null

    internal fun initializeId(id: Int) {
        if (this._id == null) this._id = id
    }

    /**
     * The id of the [DataHolder].
     */
    val id: Int? get() = _id

    /**
     * The order of the [DataHolder] in its [DataHolderList]
     * Default (unset) value must be negative.
     * This change is saved directly in Database
     */
    var order: Int
        get() = _order
        set(value) {
            if (order == value) return
            _order = value
            _dataChanged.add(COLUMN_NAME_ORDER)
            this.save()
        }

    /**
     * The id of the [DataHolderList] which contains this [DataHolder]
     * If this value is null, it means that this [DataHolder] has not been attached to any parent
     * or that it is contained in the note manager (in the root view)
     * This change is saved directly in Database
     */
    var parentId: Int?
        get() = _parent
        set(value) {
            if (parentId == value) return
            _parent = value
            if (value != null) {
                _dataChanged.add(COLUMN_NAME_PARENT)
                this.save()
            }
        }

    /**
     * The access to the Database containing this [DataHolder] is registered in.
     * Can be used to retrieve the parent.
     * Settings this value ensures the [DataHolder] has an id
     */
    var db: DatabaseProxy<*>?
        get() = _dbProxy
        set(value) {
            val shouldUpdateId = _dbProxy == null && value != null
            _dbProxy = value
            if (shouldUpdateId) {
                val insert = this._id == null
                this._id = _dbProxy?.registerHolder(this)
                if (insert) {
                    _dbProxy?.add(this)
                    this._dataChanged.clear()
                }
            }
        }

    /**
     * Retrieves the [DataHolderList] which contains this [DataHolder].
     * This value can be null if the [DataHolder] has not been attached to any parent.
     */
    @Suppress("UNCHECKED_CAST")
    fun getParent(): DataHolderList<DataHolder>? = db?.findElementById(this.parentId) as? DataHolderList<DataHolder>

    /**
     * Notifies the element that one of its property changed. Use [save] to apply changes in
     * database.
     */
    fun notifyDataChanged(dataColumnName: String) {
        this._dataChanged.add(dataColumnName)
    }

    /**
     * Saves the element in the database.
     */
    fun save(visualUpdate: Boolean = true) {
        val updates = this._dataChanged.distinct().toTypedArray()
        if (updates.isEmpty()) return
        val updateVisuals = visualUpdate && updates.any { it != COLUMN_NAME_PARENT && it != COLUMN_NAME_ORDER }
        this._dbProxy?.update(this, updates) {
            if (it) {
                (this.db?.context as PaperBeeApplication).paperScope.launch(Dispatchers.Main) {
                    _dataChanged.removeAll(updates)
                    if (updateVisuals) getParent()?.notifyUpdated(this@DataHolder)
                }
            }
        }
    }

}
