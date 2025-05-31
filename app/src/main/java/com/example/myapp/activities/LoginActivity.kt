package com.example.myapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.R

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val buttonLogin = findViewById<Button>(R.id.buttonLogin)
        val textViewRegisterLink = findViewById<TextView>(R.id.textViewRegisterLink)

        buttonLogin.setOnClickListener {
            // Simulate successful login
            Log.d("LoginActivity", "Login successful")
            // Navigate to MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Finish LoginActivity so user can't navigate back to it
        }

        textViewRegisterLink.setOnClickListener {
            // Navigate to RegisterActivity
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
