package com.example.simpleshareduse

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import splitties.alertdialog.*
import android.view.*
import android.widget.Button
import androidx.annotation.DrawableRes
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

import splitties.toast.toast

class MainActivity : AppCompatActivity() {

    private val tvLogStatus : TextView by lazy { findViewById(R.id.tvLoginStatus) }
    private val playButton : Button by lazy { findViewById(R.id.playbtn) }
    private val leaderboardButton : Button by lazy { findViewById(R.id.leaderbtn) }

    private val mFirebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db : FirebaseFirestore by lazy { FirebaseFirestore.getInstance()  }

    private var numberOfQuestions = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (mFirebaseAuth.currentUser == null) {
            tvLogStatus.text = getString(R.string.logged_out)
        } else {
            tvLogStatus.text = getString(R.string.logged_in)
        }

        playButton.setOnClickListener{
            if (numberOfQuestions > 4) {
                val intent = Intent(this, SinglePlayerActivity::class.java)
                intent.putExtra(Constants.NUM_OF_QUESTIONS, numberOfQuestions)
                startActivity(intent)
            }
            else{
                toast(getString(R.string.numQuestions))
            }
        }

        leaderboardButton.setOnClickListener {

        }

        updateUI()
        getAmount()
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
            R.id.itName -> {
                onClickMenuItem_3()
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

    // Name ändern
    fun onClickMenuItem_3() {

        if(mFirebaseAuth.currentUser == null){
            toast( getString(R.string.pleaseLogin))
        }
        else{
            val editTextName = EditText(applicationContext)
            alertDialog (title = getString(R.string.setDisplayNameTitle),
                view = editTextName) {
                positiveButton(R.string.posButton){
                    val name = editTextName.text.toString()
                    if(name.isEmpty()){
                        toast(getString(R.string.fill_out))
                    }
                    else{
                        // Code zum Setzen / Aktualisieren des Display Names
                        val user = FirebaseAuth.getInstance().currentUser

                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()

                        user!!.updateProfile(profileUpdates)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {

                                    updateUI()
                                }
                            }
                    }
                }
                negativeButton(R.string.negButton){

                }
            }.show()
        }
    }

    private fun updateUI() {
        val user = mFirebaseAuth.currentUser
        if (user == null) {
            tvLogStatus.text = resources.getString(R.string.statusLogout)
            playButton.isEnabled = false
            leaderboardButton.isEnabled = false
        } else {
            // Displaynamen abfragen
            val name = user.displayName
            val helloMsg = resources.getString(R.string.helloMessage, name)
            tvLogStatus.text = helloMsg
            playButton.isEnabled = true
            leaderboardButton.isEnabled = true
        }
    }

    inline fun Context.alertDialog(title: CharSequence? = null,
                                   message: CharSequence? = null,
                                   @DrawableRes iconResource: Int = 0,
                                   view: View?,
                                   dialogConfig: AlertDialog.Builder.() -> Unit = {}
    ): AlertDialog {
        return AlertDialog.Builder(this).apply {
            this.title = title
            this.message  = message
            setIcon(iconResource)
            setView(view)
            dialogConfig()
        }.create()
    }

    private fun getAmount() {
        numberOfQuestions = -1
        db.collection(Constants.LIBRARY)
            .get()
            .addOnCompleteListener{ task ->
                    if (task.isSuccessful) {
                        numberOfQuestions = task.result!!.size()
                    } else {

                        Toast.makeText(
                            applicationContext,
                            task.exception!!.message,
                            Toast.LENGTH_LONG
                        ).show()

                    }
            }
    }
}