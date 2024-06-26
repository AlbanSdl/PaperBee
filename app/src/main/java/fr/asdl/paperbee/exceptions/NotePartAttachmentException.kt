package fr.asdl.paperbee.exceptions

import fr.asdl.paperbee.note.NotePart
import java.lang.Exception

class NotePartAttachmentException(element: NotePart, to: NotePart, reason: Reason) : Exception(
    "NotePart #${element.id} cannot be attached ${if (reason != Reason.NO_NOTE_ABOVE 
        && reason != Reason.NOT_IN_NOTE) "to NotePart ${to.id} " else ""}because ${reason.message}"
) {
    enum class Reason(val message: String) {
        DIFFERENT_NOTE("they are not in the same Note !"),
        FOREIGN_ELEMENTS("there are foreign elements between them"),
        NO_NOTE_ABOVE("there is no note above where it could be attached !"),
        NOT_IN_NOTE("it is not in any note !");
    }
}