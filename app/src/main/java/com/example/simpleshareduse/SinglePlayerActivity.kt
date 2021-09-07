package com.example.simpleshareduse

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import splitties.alertdialog.alertDialog
import splitties.alertdialog.negativeButton
import splitties.alertdialog.okButton
import splitties.alertdialog.positiveButton
import splitties.toast.toast
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class SinglePlayerActivity : AppCompatActivity(), View.OnClickListener {

    private val buttonWahr : Button by lazy { findViewById(R.id.buttonWahr) }
    private val buttonFalsch : Button by lazy { findViewById(R.id.buttonFalsch) }
    private val tvPunkte : TextView by lazy { findViewById(R.id.textViewPunkte) }
    private val tvCountdown : TextView by lazy { findViewById(R.id.textViewCountdown) }
    private val tvQuestion : TextView by lazy { findViewById(R.id.textViewQuestions) }

    private val db : FirebaseFirestore by lazy { FirebaseFirestore.getInstance()  }
    private val mFirebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private var punktestand = 0
    private var numOfQuestions = 0
    private lateinit var randomQuestions: ArrayList<Int>
    private var statement: Library? = null
    private var answered: String? = null
    private lateinit var timer: CountDownTimer
    private var timeout = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_player)

        buttonWahr.setOnClickListener(this)
        buttonFalsch.setOnClickListener(this)

        buttonFalsch.isEnabled = false
        buttonWahr.isEnabled = false
        tvQuestion.visibility = View.INVISIBLE


        // Erstelle Liste mit zufällig gewürfelten Zahlen
        numOfQuestions = intent.getIntExtra(Constants.NUM_OF_QUESTIONS, -1)
        randomQuestions = ArrayList()
        for (i in 1..numOfQuestions) {
            randomQuestions.add(i)
        }
        randomQuestions.shuffle()


        timer = object : CountDownTimer(10000, 10000) {
            override fun onTick(millisLeft: Long) {
                // macht nichts, muss aber implementiert werden
            }

            override fun onFinish() {
                // Wenn der Timer abgelaufen ist:
                timer.cancel()
                timeout = true
                playGame()
            }
        }


        //Start Countdown
        val handler = Handler()
        val n = AtomicInteger(3) // initialisiere mit 3.
        val counter: Runnable = object : Runnable {
            override fun run() {
                //Textfeld mit aktuellem n füllen.
                tvCountdown.text = Integer.toString(n.get())
                //wenn n >= 1, sekündlich runterzählen
                if (n.getAndDecrement() >= 1) {
                    handler.postDelayed(this, 1000)
                } else {
                    //Textfeld verschwindet, wenn Countdown zu Ende
                    tvCountdown.visibility = View.INVISIBLE
                    //Spiel wird gestarte
                    startGame()
                }
            }
        }
        handler.postDelayed(counter, 0)
    }

    override fun onClick(view: View) {
        if (view.id == R.id.buttonWahr) answered = Constants.WAHR
        if (view.id == R.id.buttonFalsch) answered = Constants.FALSCH

        playGame()
    }

    private fun startGame() {

        timer.start()

        buttonWahr.isEnabled = true
        buttonFalsch.isEnabled = true
        tvQuestion.visibility = View.VISIBLE

        getNextStatement()
        val punktestr = resources.getString(R.string.textPunkte, punktestand)
        tvPunkte.text = punktestr
    }

    //nächste Frage aus DB lesen und anzeigen
    private fun getNextStatement()
    {
        val documentId= randomQuestions[punktestand].toString()
        db.collection(Constants.LIBRARY).document(documentId)
            .get()
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    // Datenbankantwort in Objektvariable speichern
                    statement = task.result!!.toObject(Library::class.java)
                    // Frage anzeigen
                    tvQuestion.text = statement!!.getQuestion()

                } else {

                    Toast.makeText(
                        applicationContext,
                        task.exception!!.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

    }

    private fun playGame() {
        // Spiele eine Runde
        if (punktestand < randomQuestions.size && statement!!.getAnswer() == answered && timeout == false) {

            punktestand++
            val punktestr = resources.getString(R.string.textPunkte, punktestand)
            tvPunkte.text = punktestr

            if(punktestand == randomQuestions.size){
                // beende Spiel
                buttonWahr.isEnabled = false
                buttonFalsch.isEnabled = false
                saveToDB()
                setEndnote()
            }else {
                getNextStatement()
            }

        } else {
            // beende Spiel

            buttonWahr.isEnabled = false
            buttonFalsch.isEnabled = false
            saveToDB()
            setEndnote()
        }
    }

    private fun saveToDB() {

        // Objekt zum Schreiben in die DB
        val spielergebnis = Scores()
        val user = mFirebaseAuth.currentUser
        spielergebnis.setUserName(user!!.displayName)
        spielergebnis.setUserScore(punktestand)

        // Schreibe in DB
        db.collection(Constants.LEADERBOARD)
            .add(spielergebnis)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    toast(getString(R.string.save))
                } else {

                    Toast.makeText(
                        applicationContext,
                        task.exception!!.message, Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun setEndnote() {
        timer.cancel()
        alertDialog (title = getString(R.string.gameEndedDialog, punktestand)) {
            okButton {
                finish()
            }
        }.show()
    }
}