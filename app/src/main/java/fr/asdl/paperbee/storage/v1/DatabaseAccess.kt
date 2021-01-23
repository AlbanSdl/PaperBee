package fr.asdl.paperbee.storage.v1

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import fr.asdl.paperbee.R
import fr.asdl.paperbee.note.*
import fr.asdl.paperbee.storage.DatabaseAccess
import fr.asdl.paperbee.storage.DatabaseFilter
import fr.asdl.paperbee.storage.SpanProcessor
import fr.asdl.paperbee.storage.v1.NotableContract.NotableContractInfo.COLUMN_NAME_EXTRA
import fr.asdl.paperbee.storage.v1.NotableContract.NotableContractInfo.COLUMN_NAME_ID
import fr.asdl.paperbee.storage.v1.NotableContract.NotableContractInfo.COLUMN_NAME_ORDER
import fr.asdl.paperbee.storage.v1.NotableContract.NotableContractInfo.COLUMN_NAME_PARENT
import fr.asdl.paperbee.storage.v1.NotableContract.NotableContractInfo.COLUMN_NAME_PAYLOAD
import fr.asdl.paperbee.storage.v1.NotableContract.NotableContractInfo.COLUMN_NAME_TYPE
import fr.asdl.paperbee.storage.v1.NotableContract.NotableContractInfo.TABLE_NAME
import fr.asdl.paperbee.view.options.Color
import fr.asdl.paperbee.view.sentient.DataHolder

class DatabaseAccess(context: Context) : DatabaseAccess(context), SpanProcessor {

    override suspend fun querySelect(
        filter: DatabaseFilter,
        sort: String?
    ): List<DataHolder> {
        val cursor = readAccess.query(
            TABLE_NAME,
            null,
            filter.getClause(),
            filter.getArgs(),
            null,
            null,
            sort ?: "$COLUMN_NAME_ID ASC"
        )

        val items = mutableListOf<DataHolder>()
        try {
            with(cursor) {
                while (moveToNext()) {
                    val obj = toObject(
                        getLong(getColumnIndexOrThrow(COLUMN_NAME_ID)),
                        getLong(getColumnIndexOrThrow(COLUMN_NAME_ORDER)),
                        getLongOrNull(getColumnIndexOrThrow(COLUMN_NAME_PARENT)),
                        getInt(getColumnIndexOrThrow(COLUMN_NAME_TYPE)),
                        getStringOrNull(getColumnIndexOrThrow(COLUMN_NAME_PAYLOAD)),
                        getStringOrNull(getColumnIndexOrThrow(COLUMN_NAME_EXTRA))
                    )
                    if (obj != null) items.add(obj)
                }
            }
        } catch (ex: Exception) {
            this.notifyUser(context.getString(R.string.db_error_select))
        } finally {
            cursor.close()
        }

        return items.toList()
    }

    private fun toObject(
        id: Long,
        order: Long,
        parentId: Long?,
        type: Int,
        payload: String?,
        extra: String?
    ): DataHolder? {
        val holder: DataHolder? = when (NotableContract.DataHolderType.fromInt(type)) {
            NotableContract.DataHolderType.NOTE -> { val n = Note(); n.title = payload ?: ""; n.parentId = parentId?.toInt(); n }
            NotableContract.DataHolderType.FOLDER -> { val f = NoteFolder(); f.title = payload ?: ""; f.parentId = parentId?.toInt(); f }
            NotableContract.DataHolderType.TEXT -> { val t = NoteText(deserialize(payload ?: "")); t.parentId = parentId?.toInt(); t}
            NotableContract.DataHolderType.CHECKBOX -> { val c = NoteCheckBoxable(
                deserialize(payload ?: ""),
                extra == "true"
            ); c.parentId = parentId?.toInt(); c }
            else -> null
        }
        holder?.initializeId(id.toInt())
        holder?.order = order.toInt()
        if (holder is Notable<*>) holder.color = Color.getFromTag(extra)
        return holder
    }

    override fun queryInsert(holder: DataHolder): Long {
        val values = ContentValues().apply {
            put(COLUMN_NAME_ID, holder.id)
            put(COLUMN_NAME_ORDER, holder.order)
            put(COLUMN_NAME_PARENT, holder.parentId)
            put(COLUMN_NAME_TYPE, NotableContract.DataHolderType.getType(holder)?.id)
            put(
                COLUMN_NAME_PAYLOAD,
                if (holder is Notable<*>) holder.title else if (holder is TextNotePart) serialize(holder.content) else null
            )
            put(
                COLUMN_NAME_EXTRA,
                if (holder is Notable<*>) holder.color?.tag else if (holder is CheckableNotePart) holder.checked.toString() else null
            )
        }

        return try {
            writeAccess.insert(TABLE_NAME, null, values)
        } catch (ex: Exception) {
            this.notifyUser(context.getString(R.string.db_error_insert))
            -1
        }
    }

    override fun queryDelete(filter: DatabaseFilter): Int {
        return writeAccess.delete(TABLE_NAME, filter.getClause(), filter.getArgs())
    }

    override fun queryUpdate(
        holder: DataHolder, columnsToUpdate: Array<String>,
        filter: DatabaseFilter
    ): Int {
        val values = ContentValues().apply {
            for (i in columnsToUpdate) {
                when (i) {
                    COLUMN_NAME_ID -> put(i, holder.id)
                    COLUMN_NAME_PARENT -> put(i, holder.parentId)
                    COLUMN_NAME_ORDER -> put(i, holder.order)
                    COLUMN_NAME_PAYLOAD -> put(
                        i,
                        if (holder is Notable<*>) holder.title else if (holder is TextNotePart) serialize(holder.content) else null
                    )
                    COLUMN_NAME_EXTRA -> put(
                        i,
                        if (holder is Notable<*>) holder.color?.tag else if (holder is CheckableNotePart) holder.checked.toString() else null
                    )
                }
            }
        }

        return writeAccess.update(
            TABLE_NAME,
            values,
            filter.getClause(),
            filter.getArgs()
        )

    }

    override fun getBridge(): SQLiteOpenHelper = Database(context)

}