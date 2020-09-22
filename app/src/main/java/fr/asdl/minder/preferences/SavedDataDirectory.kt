package fr.asdl.minder.preferences

import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList

class SavedDataDirectory(private var directoryName: String, private var context: Context) {

    inline fun <reified T> getData(): LinkedList<T> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            val returnValue = LinkedList<T>()
            for (str in this.load()!!)
                returnValue.add(Json.decodeFromString(str))
            return returnValue
        }
        return LinkedList(this.load()?.stream()!!.map { Json.decodeFromString<T>(it) }.collect(Collectors.toList()))
    }

    fun load(): List<String>? {
        val returnValue = ArrayList<String>()
        val dir = File(context.filesDir.absolutePath + File.separator + this.directoryName + File.separator)
        if (!dir.exists()) dir.mkdir()
        for (f: File in dir.listFiles()!!) {
            try {
                val inputStream = FileInputStream(f)
                val reader = BufferedReader(InputStreamReader(inputStream))
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

}