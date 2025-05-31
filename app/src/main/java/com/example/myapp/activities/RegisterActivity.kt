package com.example.myapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.R

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val buttonRegister = findViewById<Button>(R.id.buttonRegister)

        buttonRegister.setOnClickListener {
            // Simulate successful registration
            Log.d("RegisterActivity", "Registration successful")
            // Navigate to LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Finish RegisterActivity
        }
    }
}
