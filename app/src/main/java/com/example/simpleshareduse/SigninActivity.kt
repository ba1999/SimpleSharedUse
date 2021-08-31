package com.example.simpleshareduse

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import splitties.alertdialog.alertDialog
import splitties.alertdialog.cancelButton
import splitties.alertdialog.positiveButton
import splitties.toast.toast

class SigninActivity : AppCompatActivity(), View.OnClickListener {

    private val btnLogin : Button by lazy{findViewById(R.id.btnLogin)}
    private val btnRegister : Button by lazy{findViewById(R.id.btnRegister)}
    private val etEmail : EditText by lazy{findViewById(R.id.editTextEmail)}
    private val etPassword : EditText by lazy{findViewById(R.id.editTextPassword)}
    private val textViewPWReset : TextView by lazy{findViewById(R.id.textViewPWReset)}

    private val mFirebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private lateinit var mAuthListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        btnLogin.setOnClickListener(this)
        btnRegister.setOnClickListener(this)
        textViewPWReset.setOnClickListener(this)

        mAuthListener = FirebaseAuth.AuthStateListener {
            val user = mFirebaseAuth.currentUser

            if (user != null) {
                user.sendEmailVerification()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            FirebaseAuth.getInstance().signOut()
                            toast(R.string.verify_mail)
                        } else {
                            toast(it.exception!!.message.toString())
                        }
                    }
            }
        }
    }

        override fun onClick(v: View?) {
            var email : String
            var password : String

            when (v?.id) {
                R.id.btnLogin -> {
                    email = etEmail.text.toString()
                    password = etPassword.text.toString()
                    signIn(email, password)
                }
                R.id.btnRegister -> {
                    email = etEmail.text.toString()
                    password = etPassword.text.toString()
                    register(email, password)
                }
                R.id.textViewPWReset -> {
                    sendResetPw()
                }
            }
        }

    private fun validateForm(email: String, password: String): Boolean {

        var valid = true

        if (email.isEmpty()) {
            etEmail.error = getString(R.string.required)
            valid = false
        }

        if (password.isEmpty()) {
            etPassword.error = getString(R.string.required)
            valid = false
        }

        return valid
    }

    private fun register(email: String, password: String) {

        if (!validateForm(email, password)) {
            return
        }

        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        mAuthListener.onAuthStateChanged(mFirebaseAuth)
                    } else {
                        toast(task.exception!!.message.toString())
                    }
                }
    }

    private fun signIn(email: String, password: String) {

        if (!validateForm(email, password)) {
            return
        }

        mFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        if (mFirebaseAuth.currentUser!!.isEmailVerified) {
                            toast(R.string.login_success)
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        } else {
                            toast(R.string.reminder_verify)
                        }
                    } else {
                        toast(it.exception!!.message.toString())
                }
        }
    }

    private fun sendResetPw() {
        val editTextEmail = EditText(applicationContext)
        editTextEmail.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        alertDialog(
            title = getString(R.string.forgotPw_title),
            message = getString(R.string.forgotPw_msg)


        ) {
            setView(editTextEmail)
            positiveButton(R.string.send) {
                val mail = editTextEmail.text.toString().trim()
                if (mail.isEmpty()) {
                    toast(R.string.fill_out)
                    it.dismiss()
                } else {
                    sendMail(mail)
                }
            }
            cancelButton()
        }.show()
    }

    private fun sendMail(mail: String) {
        mFirebaseAuth.sendPasswordResetEmail(mail)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    toast(R.string.reset_pw)
                } else {
                    toast(it.exception!!.message.toString())
                }
            }
    }


}
