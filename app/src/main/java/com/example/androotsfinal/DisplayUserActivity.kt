package com.example.androotsfinal

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class DisplayUserActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_user)

        // Get the Intent that started this activity and extract the string
        val name = intent.getStringExtra("USER_FIRST_NAME")

        // Capture the layout's TextView and set the string as its text
        val textView = findViewById<TextView>(R.id.name).apply {
            text = name
        }
    }
}
