package fr.asdl.paperbee.preferences

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import fr.asdl.paperbee.R

class AppPreferences(val context: Context) {

    companion object {
        val APP_THEME = Preference(R.string.pref_app_theme_name, MODE_NIGHT_FOLLOW_SYSTEM)
    }

    inline fun <reified T> get(preference: Preference<T>): T {
        val pref = this.context.getSharedPreferences(this.context.getString(R.string.preferences_file), Context.MODE_PRIVATE)
        val prefName = this.context.getString(preference.resId)
        return when (T::class) {
            String::class -> pref.getString(prefName, preference.default!! as String) as T
            Integer::class -> pref.getInt(prefName, preference.default!! as Int) as T
            Boolean::class -> pref.getBoolean(prefName, preference.default!! as Boolean) as T
            Float::class -> pref.getFloat(prefName, preference.default!! as Float) as T
            Long::class -> pref.getLong(prefName, preference.default!! as Long) as T
            else -> throw UnsupportedOperationException("Not yet implemented")
        }
    }

    inline fun <reified T> set(preference: Preference<T>, value: T) {
        val editor = this.context.getSharedPreferences(this.context.getString(R.string.preferences_file), Context.MODE_PRIVATE).edit()
        val prefName = this.context.getString(preference.resId)
        when (T::class) {
            String::class -> editor.putString(prefName, value as? String ?: preference.default!! as String)
            Integer::class -> editor.putInt(prefName, value as? Int ?: preference.default!! as Int)
            Boolean::class -> editor.putBoolean(prefName, value as? Boolean ?: preference.default!! as Boolean)
            Float::class -> editor.putFloat(prefName, preference.default as? Float ?: preference.default!! as Float)
            Long::class -> editor.putLong(prefName, preference.default as? Long ?: preference.default!! as Long)
            else -> throw UnsupportedOperationException("Not yet implemented")
        }
        editor.apply()
    }

    data class Preference<out T>(val resId: Int, val default: T)

}