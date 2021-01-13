package fr.asdl.paperbee.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * This Activity is shown when the user opens the Application and will stay displayed
 * until Android fully loads the MainActivity. At that time, this activity is finished
 * and fades away.
 */
class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Handling main activity
        intent.setClass(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}