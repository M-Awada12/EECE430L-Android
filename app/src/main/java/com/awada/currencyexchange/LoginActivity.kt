package com.awada.currencyexchange

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.awada.currencyexchange.api.Authentication
import com.awada.currencyexchange.api.ExchangeService
import com.awada.currencyexchange.api.model.Token
import com.awada.currencyexchange.api.model.User
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var submitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        usernameEditText = findViewById(R.id.txtInptUsername)
        passwordEditText = findViewById(R.id.txtInptPassword)
        submitButton = findViewById(R.id.btnSubmit)

        submitButton.setOnClickListener {
            createUser()
        }
    }

    private fun createUser() {
        val username = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()

        val user = User(username, password)

        ExchangeService.exchangeApi().authenticate(user).enqueue(object : Callback<Token> {
            override fun onResponse(call: Call<Token>, response: Response<Token>) {
                if (response.isSuccessful) {
                    val token = response.body()?.token
                    token?.let {
                        Authentication.saveToken(it)
                        showSnackBar("Successfully Logged In.")
                        submitButton.postDelayed({ onCompleted() }, 1000)
                    }
                } else {
                    showSnackBar("Could not login.")
                }
            }

            override fun onFailure(call: Call<Token>, t: Throwable) {
                showSnackBar("Could not login.")
            }
        })
    }

    private fun onCompleted() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(submitButton, message, Snackbar.LENGTH_LONG).show()
    }
}
