package fr.asdl.paperbee.storage.v1

import android.provider.BaseColumns
import fr.asdl.paperbee.note.Note
import fr.asdl.paperbee.note.NoteCheckBoxable
import fr.asdl.paperbee.note.NoteFolder
import fr.asdl.paperbee.note.NoteText
import fr.asdl.paperbee.view.sentient.DataHolder

object NotableContract {

    enum class DataHolderType(val id: Int) {
        NOTE(0),
        FOLDER(1),
        TEXT(2),
        CHECKBOX(3);

        companion object {
            fun fromInt(i: Int): DataHolderType? {
                for (v in values()) if (v.id == i) return v
                return null
            }
            fun getType(v: DataHolder): DataHolderType? {
                return when (v::class.java) {
                    Note::class.java -> NOTE
                    NoteFolder::class.java -> FOLDER
                    NoteText::class.java -> TEXT
                    NoteCheckBoxable::class.java -> CHECKBOX
                    else -> null
                }
            }
        }
    }

    object NotableContractInfo : BaseColumns {
        const val TABLE_NAME = "notables"

        const val COLUMN_NAME_ID = "id"
        const val COLUMN_NAME_ORDER = "item_order"
        const val COLUMN_NAME_PARENT = "parent"
        const val COLUMN_NAME_TYPE = "type"

        /* Variable data:
         * Contains the title if the entry is a Notable
         * Contains the content if the entry is a NotePart */
        const val COLUMN_NAME_PAYLOAD = "payload"

        /* Variable data:
        * Contains the color if the entry is a Notable
        * Contains the checked property of a CheckBox */
        const val COLUMN_NAME_EXTRA = "extra"

        /* List of queries executed */
        const val SQL_CREATE_ENTRIES = "CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_NAME_ID INTEGER PRIMARY KEY, " +
                "$COLUMN_NAME_ORDER INTEGER NOT NULL, " +
                "$COLUMN_NAME_PARENT INTEGER, " +
                "$COLUMN_NAME_TYPE INTEGER NOT NULL, " +
                "$COLUMN_NAME_PAYLOAD TEXT, " +
                "$COLUMN_NAME_EXTRA TEXT)"

        const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS $TABLE_NAME"

    }
}