package fr.asdl.minder.preferences

import android.content.Context
import android.os.Build
import android.util.Log
import fr.asdl.minder.view.sentient.DataHolder
import fr.asdl.minder.view.sentient.DataHolderList
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.*
import java.nio.charset.Charset

class SavedDataDirectory(private var directoryName: String, private var context: Context) {

    inline fun <reified T: DataHolder> saveDataAsync(dataHolder: T? = null, id: Int? = null, serializer: KSerializer<T>? = null) {
        if (dataHolder != null) {
            if (serializer == null) save(Json.encodeToString(dataHolder), dataHolder.id!!)
            else save(Json.encodeToString(serializer, dataHolder), dataHolder.id!!)
        }
        else if (id != null) delete(id)
    }

    fun save(content: String, id: Int) {
        Thread {
            val folder = this.getDirectoryFile()
            if (!folder.exists()) folder.mkdir()
            val file = this.getDirectoryFile(id.toString())
            if (!file.exists()) file.createNewFile()
            val outputStream = FileOutputStream(file)
            outputStream.write(content.toByteArray(Charset.forName("UTF-8")))
            outputStream.close()
        }.start()
    }

    fun delete(id: Int) {
        Thread {
            val file = getDirectoryFile(id.toString())
            if (file.exists()) file.delete()
        }.start()
    }

    inline fun <reified T: DataHolder, H: DataHolderList<T>> loadData(context: H, serializer: KSerializer<T>) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            for (str in this.load()!!) context.add(Json.decodeFromString(serializer, str))
        else this.load()!!.stream().forEach { context.add(Json.decodeFromString(serializer, it)) }
    }

    fun load(): List<String>? {
        val returnValue = ArrayList<String>()
        val dir = this.getDirectoryFile()
        if (!dir.exists()) dir.mkdir()
        for (f: File in dir.listFiles()!!) {
            try {
                val inputStream = FileInputStream(f)
                val reader = BufferedReader(InputStreamReader(inputStream, Charset.forName("UTF-8")))
                val data = StringBuilder()
                var line: String? = reader.readLine()
                var nextLine: String?
                while (line != null) {
                    nextLine = reader.readLine()
                    if (nextLine == null) data.append(line) else data.append(line).append('\n')
                    line = nextLine
                }
                inputStream.close()
                reader.close()
                returnValue.add(data.toString())
            } catch (e: Exception) {
                Log.e(this.javaClass.simpleName, "Could not read data file: " + e.message)
            }
        }
        return returnValue
    }

    private fun getDirectoryFile(fileName: String = ""): File {
        return File(context.filesDir.absolutePath + File.separator + this.directoryName + File.separator + fileName)
    }

}