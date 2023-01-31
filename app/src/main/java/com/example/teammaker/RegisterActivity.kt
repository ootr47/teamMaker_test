package com.example.teammaker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.teammaker.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var refUsers: DatabaseReference
    private var firebaseUserID: String = ""
    lateinit var binding : ActivityRegisterBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)


//        val toolbar: Toolbar = findViewById(R.id.toolbar_register)
//        setSupportActionBar(toolbar)
//        supportActionBar!!.title = "Register"
//        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
//        toolbar.setNavigationOnClickListener {
//            val intent = Intent(this@RegisterActivity, WelcomeActivity::class.java)
//            startActivity(intent)
//            finish()
//        }


        mAuth = FirebaseAuth.getInstance()

        binding.registerBtn.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser()
    {
        val username: String = binding.usernameRegister.text.toString()
        val email: String = binding.emailRegister.text.toString()
        val password: String = binding.passwordRegister.text.toString()

        if (username == "")
        {
            Toast.makeText(this@RegisterActivity, "please write username.", Toast.LENGTH_LONG).show()
        }
        else if (email == "")
        {
            Toast.makeText(this@RegisterActivity, "please write email.", Toast.LENGTH_LONG).show()
        }
        else if (password == "")
        {
            Toast.makeText(this@RegisterActivity, "please write password.", Toast.LENGTH_LONG).show()
        }
        else
        {
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener{ task ->
                    if (task.isSuccessful)
                    {
                        firebaseUserID = mAuth.currentUser!!.uid
                        refUsers = Firebase.database("https://teammaker-2299d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users").child(firebaseUserID)


                        val userHashMap = HashMap<String, Any>()
                        userHashMap["uid"] = firebaseUserID
                        userHashMap["username"] = username
//                        userHashMap["profile"] = "https://firebasestorage.googleapis.com/v0/b/messengerapp-f0841.appspot.com/o/profile.png?alt=media&token=a21e87f7-38af-4b5d-9315-3a053d6b6798"
//                        userHashMap["cover"] = "https://firebasestorage.googleapis.com/v0/b/messengerapp-f0841.appspot.com/o/cover.jpg?alt=media&token=1333e753-83b0-45fc-a9c8-1f26e69f59a4"
                        userHashMap["status"] = "offline"
                        userHashMap["search"] = username.toLowerCase()

                        refUsers.updateChildren(userHashMap)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful)
                                {
                                    val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                    }
                    else
                    {
                        Toast.makeText(this@RegisterActivity, "Error Message: " + task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}