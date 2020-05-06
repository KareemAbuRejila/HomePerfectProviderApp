package com.codeshot.home_perfect_provider.ui

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import cc.cloudist.acplibrary.ACProgressBaseDialog
import cc.cloudist.acplibrary.ACProgressConstant
import cc.cloudist.acplibrary.ACProgressFlower
import com.codeshot.home_perfect_provider.R
import com.codeshot.home_perfect_provider.common.Common.USERS_REF
import com.codeshot.home_perfect_provider.databinding.ActivityLoginBinding
import com.codeshot.home_perfect_provider.databinding.DialogLoginBinding
import com.codeshot.home_perfect_provider.ui.main.MainActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private val TAG: String = "LOGIN ACTIVITY"
    private lateinit var activityLoginBinding: ActivityLoginBinding
    private lateinit var progressBar: ACProgressBaseDialog
    private var codeSent: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isLogined()
        activityLoginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        activityLoginBinding.ccpLogin.registerPhoneNumberTextView(activityLoginBinding.edtPhoneLogin)
        progressBar = ACProgressFlower.Builder(this)
            .direction(ACProgressConstant.DIRECT_CLOCKWISE)
            .themeColor(Color.WHITE)
            .text("Please Wait ....!")
            .fadeColor(Color.DKGRAY).build()
        activityLoginBinding.edtPhoneLogin
            .setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    return@OnEditorActionListener true
                }
                false
            })
        activityLoginBinding.btnConLogin.setOnClickListener(View.OnClickListener { v: View? ->
            sendVerificationCode()
        })
        activityLoginBinding.codeInput.addOnCompleteListener { code: String? ->
            verifySignInCode()
        }
    }

    fun showLoginDialog(view: View) {
        val alertDialog =
            AlertDialog.Builder(this)
                .setPositiveButton("Login",
                    DialogInterface.OnClickListener { dialog, which ->
                        loginDialog("in")
                    })
                .setNegativeButton("SignUp", DialogInterface.OnClickListener { dialog, which ->
                    signUpDialog("up")
                }).create()
        alertDialog.show()
    }

    private fun signUpDialog(s: String) {
        val dialogbinding = DialogLoginBinding.inflate(
            layoutInflater,
            activityLoginBinding.root as ViewGroup?, false
        )
        dialogbinding.btnLogin.text = "SignUp"
        dialogbinding.btnLogin.setOnClickListener {
            val email = dialogbinding.edtEmailLogDia.text.toString()
            val pass = dialogbinding.edtPassLogDia.text.toString()
            val repass = dialogbinding.edtRePassLogDia.text.toString()
            if (pass == repass) {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                    .addOnSuccessListener {
                        sendToHomeActivity("new")
                    }
            }
        }

        val signupDiaog = AlertDialog.Builder(this)
            .setView(dialogbinding.root).create()
        signupDiaog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        signupDiaog.show()
    }

    private fun loginDialog(s: String) {
        val dialogbinding = DialogLoginBinding.inflate(
            layoutInflater,
            activityLoginBinding.root as ViewGroup?, false
        )
        val loginDialog = AlertDialog.Builder(this)
            .setView(dialogbinding.root).create()

        dialogbinding.edtRePassLogDia.visibility = View.INVISIBLE
        dialogbinding.btnLogin.text = "Login"
        dialogbinding.btnLogin.setOnClickListener {
            val email = dialogbinding.edtEmailLogDia.text.toString()
            val pass = dialogbinding.edtPassLogDia.text.toString()
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener {
                    sendToHomeActivity("new")
                }.addOnFailureListener {
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                    loginDialog.dismiss()
                }
        }
        loginDialog.show()
        loginDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


    }

    private fun isLogined() {
        if (FirebaseAuth.getInstance().currentUser != null) {
            sendToHomeActivity("old")
        } else
            return
    }


    private fun sendVerificationCode() {
        val phoneNamber = activityLoginBinding.ccpLogin.fullNumberWithPlus
        if (phoneNamber.isEmpty()) {
            activityLoginBinding.edtPhoneLogin.error = "Phone Number is required  "
        } else if (phoneNamber.length < 10) {
            activityLoginBinding.edtPhoneLogin.error = "Check your Phone Number "
        } else {
            progressBar.show()
            USERS_REF.whereEqualTo("phone", phoneNamber)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) return@addSnapshotListener
                    if (querySnapshot!!.isEmpty) {
                        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNamber,  // Phone number to verify
                            60,  // Timeout duration
                            TimeUnit.SECONDS,  // Unit of timeout
                            this,  // Activity (for callback binding)
                            mCallbacks
                        ) // OnVerificationStateChangedCallbacks
                    } else {
                        progressBar.dismiss()
                        Toast.makeText(this, "This Number used by User Account", Toast.LENGTH_LONG)
                            .show()
                        return@addSnapshotListener
                    }

                }


        }
    }

    private val mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(this@LoginActivity, e.message, Toast.LENGTH_LONG).show()
                Log.e(TAG, e.message)
                progressBar.hide()
            }

            override fun onCodeSent(
                s: String,
                forceResendingToken: PhoneAuthProvider.ForceResendingToken
            ) {
                super.onCodeSent(s, forceResendingToken)
                progressBar.hide()
                activityLoginBinding.codeInput.visibility = View.VISIBLE
                codeSent = s
            }
        }

    private fun verifySignInCode() {
        val code: String = activityLoginBinding.codeInput.code.toString()
        if (TextUtils.isEmpty(code)) {
            Toast.makeText(
                this@LoginActivity,
                "Code IS Empty", Toast.LENGTH_LONG
            ).show()
        } else {
            progressBar.show()
            val credential = PhoneAuthProvider.getCredential(codeSent, code)
            signInWithPhoneAuthCredential(credential)
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
//                    val userID: String = FirebaseAuth.getInstance().currentUser?.uid.toString()
//                    val drviceToken =FirebaseInstanceId.getInstance().token
                    Toast.makeText(
                        this@LoginActivity,
                        " " + "IS Logined",
                        Toast.LENGTH_LONG
                    )
                    sendToHomeActivity("new")
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        val errorMsg = task.exception.toString()
                        progressBar.hide()
                        Toast.makeText(
                            this@LoginActivity,
                            "Incorrect Verification Code $errorMsg", Toast.LENGTH_LONG
                        ).show()
                        Log.e(TAG, errorMsg)
                    }
                }
            }
    }

    private fun sendToHomeActivity(userType: String) {
        val homeIntent = Intent(this, MainActivity::class.java)
        homeIntent.putExtra("user", userType)
        startActivity(homeIntent)
        finish()
    }
}
