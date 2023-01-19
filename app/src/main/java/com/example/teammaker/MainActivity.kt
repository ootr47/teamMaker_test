package com.example.teammaker

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teammaker.databinding.ActivityMainBinding
import com.google.firebase.database.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = Firebase.database("https://teammaker-2299d-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val myRef = database.getReference("message")

        if (BuildConfig.DEBUG) {
            Firebase.database.useEmulator("10.0.2.2", 9000)
            Firebase.auth.useEmulator("10.0.2.2", 9099)
            Firebase.storage.useEmulator("10.0.2.2", 9199)
        }

        var data:MutableList<ChatDataFormat> = loadData("")
        var adapter = ChatAdapter()
        adapter.listData = data

        binding.textRecycler.adapter = adapter
        binding.textRecycler.layoutManager = LinearLayoutManager(this)

        binding.btnSend.setOnClickListener {
            val message = binding.editText.text.toString()
            var chatData = ChatData("1", message)
            myRef.push().setValue(chatData)

            val date = System.currentTimeMillis()
            var chat = ChatDataFormat(date, message)

            adapter.listData.add(chat)
            Log.d(TAG, adapter.itemCount.toString())
            adapter.notifyDataSetChanged()

            binding.editText.setText("")
        }
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.child("test2").getValue()
                Log.d(TAG, "Value is: ${value.toString()}")

//                binding.textMessage.text = value.toString()
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })

    }
    fun loadData(message: String): MutableList<ChatDataFormat>{
        val data: MutableList<ChatDataFormat> = mutableListOf()

        val date = System.currentTimeMillis()
        var chat = ChatDataFormat(date, message)

        data.add(chat)

        return data
    }
}