package com.example.gameofdraughts

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.FileNotFoundException
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    /*
     * Method that deals with the creation of MainActivity activity
     * Handles when player clicks a button to perform an action
     * @param Bundle savedInstanceState - Saves the instance of the main menu activity
     */
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_screen)

        // Play Game button and listener
        val play = findViewById(R.id.playButton) as Button
        play.setOnClickListener {
            val intent = Intent(this@MainActivity, ButtonBoard::class.java)
            startActivity(intent)
        } // End Play Game button

        // Load Saved Game button and listener
        val load = findViewById(R.id.loadButton) as Button
        load.setOnClickListener {
            try {
                val inputStream: InputStream =
                    getApplicationContext().openFileInput("savedGame.dat")
                if (inputStream != null) {
                    val intent = Intent(this@MainActivity, ButtonBoard::class.java)
                    intent.putExtra("LOAD", true)
                    startActivity(intent)
                }
            } catch (e: FileNotFoundException) {
                Toast.makeText(getApplicationContext(), "No Game Saved!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(
                    getApplicationContext(),
                    "Error loading the game",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } // End Load Saved Game button

        // Exit application button and listener
        val exit = findViewById(R.id.exitButton) as Button
        exit.setOnClickListener {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } // End Exit application button
    }
}
