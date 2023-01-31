package com.example.teammaker.Notifications

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService

class MyFirebaseInstanceId : FirebaseMessagingService()
{
    override fun onNewToken(p0: String)
    {
        super.onNewToken(p0)

        val firebaseUser = FirebaseAuth.getInstance().currentUser

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@addOnCompleteListener
            }
            if (task.result != null) {
                val refreshToken: String = task.result
                if (firebaseUser!= null)
                {
                    updateToken(refreshToken)
                }
            }
        }




    }



    private fun updateToken(refreshToken: String?)
    {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val ref = Firebase.database("https://teammaker-2299d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Tokens")
        val token = Token(refreshToken!!)
        ref.child(firebaseUser!!.uid).setValue(token)
    }
}