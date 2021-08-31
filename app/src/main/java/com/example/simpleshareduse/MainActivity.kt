package com.example.simpleshareduse

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private val tvLogStatus : TextView by lazy { findViewById(R.id.tvLogStatus) }

    private val mFirebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (mFirebaseAuth.currentUser == null) {
            tvLogStatus.text = getString(R.string.logged_out)
        } else {
            tvLogStatus.text = getString(R.string.logged_in)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.itLogin -> {
                onClickMenuItem_1()
                true
            }
            R.id.itLogout -> {
                onClickMenuItem_2()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Einloggen und Registrieren
    fun onClickMenuItem_1() {
        // ist der Nutzer eingelogged?
        if (mFirebaseAuth.currentUser != null) {
            Toast.makeText(this, resources.getString(R.string.logged_in),
                    Toast.LENGTH_LONG).show()
            tvLogStatus.text = getString(R.string.alreadyLoggedIn)
        } else {
            val intent = Intent(this, SigninActivity::class.java)
            startActivity(intent)
        }
    }

    // Ausloggen
    fun onClickMenuItem_2() {
        if (mFirebaseAuth.currentUser == null) {
            Toast.makeText(applicationContext,
                    R.string.alreadyLoggedOut, Toast.LENGTH_LONG).show()
        } else {
            //Befehl zum Ausloggen
            mFirebaseAuth.signOut()
            tvLogStatus.text = getString(R.string.logged_out)
        }
    }

}