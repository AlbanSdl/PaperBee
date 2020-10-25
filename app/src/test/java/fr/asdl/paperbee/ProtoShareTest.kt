package fr.asdl.paperbee

import fr.asdl.paperbee.note.NoteFolder
import fr.asdl.paperbee.sharing.ShareProcess
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ProtoShareTest {

    @Test
    fun share() {
        val password = "Export password"
        val folder = NoteFolder("Name", null, idAllocator = null, parentId = -1)
        val process = ShareProcess()
        val sharedData = process.encrypt(password, listOf(folder))
        val retrievedData = process.decrypt(password, sharedData)
        assertEquals(folder.title, (retrievedData[0] as NoteFolder).title)
    }

}