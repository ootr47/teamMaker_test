package com.example.teammaker

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teammaker.Model.ChatData
import com.example.teammaker.databinding.ActivityChatBinding
import com.example.teammaker.databinding.ActivityMainBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.example.teammaker.Model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.database.R
import com.squareup.picasso.Picasso
import com.example.teammaker.Notifications.*

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatActivity : AppCompatActivity() {
    var userIdVisit: String = ""
    var firebaseUser: FirebaseUser? = null
    var chatsAdapter: ChatAdapter? = null
    var mChatList: List<ChatData>? = null
//    lateinit var recycler_view_chats: RecyclerView
    var reference: DatabaseReference? = null

    var notify = false
    var apiService: APIService? = null

    lateinit var binding: ActivityChatBinding

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)

        setContentView(binding.root)


//        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_message_chat)
//        setSupportActionBar(toolbar)
//        supportActionBar!!.title = ""
//        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
//        toolbar.setNavigationOnClickListener {
//            finish()
//        }


        apiService = Client.Client.getClient("https://fcm.googleapis.com/")!!.create(APIService::class.java)


        intent = intent
        userIdVisit = intent.getStringExtra("visit_id")!!
        firebaseUser = FirebaseAuth.getInstance().currentUser

        binding.recyclerViewChats.setHasFixedSize(true)
        var linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        binding.recyclerViewChats.layoutManager = linearLayoutManager

        reference = Firebase.database("https://teammaker-2299d-default-rtdb.asia-southeast1.firebasedatabase.app/").reference
            .child("Users").child(userIdVisit)
        reference!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot)
            {
                val user: Users? = p0.getValue(Users::class.java)

                binding.usernameMchat.text = user!!.getUserName()
//                Picasso.get().load(user.getProfile()).into(binding.profileImageMchat)

                retrieveMessages(firebaseUser!!.uid, userIdVisit, user.getProfile())
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

        binding.sendMessageBtn.setOnClickListener {
            notify = true
            val message = binding.textMessage.text.toString()
            if (message == "")
            {
                Toast.makeText(this@ChatActivity, "Please write a message, first...", Toast.LENGTH_LONG).show()
            }
            else
            {
                sendMessageToUser(firebaseUser!!.uid, userIdVisit, message)
            }
            binding.textMessage.setText("")
        }

//        attact_image_file_btn.setOnClickListener {
//            notify = true
//            val intent = Intent()
//            intent.action = Intent.ACTION_GET_CONTENT
//            intent.type = "image/*"
//            startActivityForResult(Intent.createChooser(intent,"Pick Image"), 438)
//        }

        seenMessage(userIdVisit)
    }




    private fun sendMessageToUser(senderId: String, receiverId: String?, message: String)
    {
        val reference = Firebase.database("https://teammaker-2299d-default-rtdb.asia-southeast1.firebasedatabase.app/").reference
        val messageKey = reference.push().key

        val messageHashMap = HashMap<String, Any?>()
        messageHashMap["sender"] = senderId
        messageHashMap["message"] = message
        messageHashMap["receiver"] = receiverId
        messageHashMap["isseen"] = false
        messageHashMap["url"] = ""
        messageHashMap["messageId"] = messageKey
        reference.child("Chats")
            .child(messageKey!!)
            .setValue(messageHashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                {
                    val chatsListReference = Firebase.database("https://teammaker-2299d-default-rtdb.asia-southeast1.firebasedatabase.app/")
                        .reference
                        .child("ChatList")
                        .child(firebaseUser!!.uid)
                        .child(userIdVisit)

                    chatsListReference.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(p0: DataSnapshot) {
                            if (!p0.exists())
                            {
                                chatsListReference.child("id").setValue(userIdVisit)
                            }

                            val chatsListReceiverRef = Firebase.database("https://teammaker-2299d-default-rtdb.asia-southeast1.firebasedatabase.app/")
                                .reference
                                .child("ChatList")
                                .child(userIdVisit)
                                .child(firebaseUser!!.uid)

                            chatsListReceiverRef.child("id").setValue(firebaseUser!!.uid)
                        }

                        override fun onCancelled(p0: DatabaseError) {

                        }
                    })
                }
            }

        //implement the push notifications using fcm
        val usersReference = Firebase.database("https://teammaker-2299d-default-rtdb.asia-southeast1.firebasedatabase.app/").reference
            .child("Users").child(firebaseUser!!.uid)
        usersReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot)
            {
                val user = p0.getValue(Users::class.java)
                if (notify)
                {
                    sendNotification(receiverId, user!!.getUserName(), message)
                }
                notify = false
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }


    private fun sendNotification(receiverId: String?, userName: String?, message: String)
    {
        val ref = Firebase.database("https://teammaker-2299d-default-rtdb.asia-southeast1.firebasedatabase.app/").reference.child("Tokens")

        val query = ref.orderByKey().equalTo(receiverId)

        query.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot)
            {
                for (dataSnapshot in p0.children)
                {
                    val token: Token? = dataSnapshot.getValue(Token::class.java)

                    val data = Data(
                        firebaseUser!!.uid,
                        R.drawable.notification_icon_background,
                        "$userName: $message",
                        "New Message",
                        userIdVisit
                    )

                    val sender = Sender(data!!, token!!.getToken().toString())

                    apiService!!.sendNotification(sender)
                        .enqueue(object : Callback<MyResponse>
                        {
                            override fun onResponse(
                                call: Call<MyResponse>,
                                response: Response<MyResponse>
                            )
                            {
                                if (response.code() == 200)
                                {
                                    if (response.body()!!.success !== 1)
                                    {
                                        Toast.makeText(this@ChatActivity, "Failed, Nothing happen.", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }

                            override fun onFailure(call: Call<MyResponse>, t: Throwable) {

                            }
                        })
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }


//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode==438 && resultCode==RESULT_OK && data!=null && data!!.data!=null)
//        {
//            val progressBar = ProgressDialog(this)
//            progressBar.setMessage("image is uploading, please wait....")
//            progressBar.show()
//
//            val fileUri = data.data
//            val storageReference = FirebaseStorage.getInstance().reference.child("Chat Images")
//            val ref = Firebase.database("https://teammaker-2299d-default-rtdb.asia-southeast1.firebasedatabase.app/").reference
//            val messageId = ref.push().key
//            val filePath = storageReference.child("$messageId.jpg")
//
//            var uploadTask: StorageTask<*>
//            uploadTask = filePath.putFile(fileUri!!)
//
//            uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
//                if(!task.isSuccessful)
//                {
//                    task.exception?.let {
//                        throw it
//                    }
//                }
//                return@Continuation filePath.downloadUrl
//            }).addOnCompleteListener { task ->
//                if (task.isSuccessful)
//                {
//                    val downloadUrl = task.result
//                    val url = downloadUrl.toString()
//
//                    val messageHashMap = HashMap<String, Any?>()
//                    messageHashMap["sender"] = firebaseUser!!.uid
//                    messageHashMap["message"] = "sent you an image."
//                    messageHashMap["receiver"] = userIdVisit
//                    messageHashMap["isseen"] = false
//                    messageHashMap["url"] = url
//                    messageHashMap["messageId"] = messageId
//
//                    ref.child("Chats").child(messageId!!).setValue(messageHashMap)
//                        .addOnCompleteListener { task ->
//                            if (task.isSuccessful)
//                            {
//                                progressBar.dismiss()
//
//                                //implement the push notifications using fcm
//                                val reference = Firebase.database("https://teammaker-2299d-default-rtdb.asia-southeast1.firebasedatabase.app/").reference
//                                    .child("Users").child(firebaseUser!!.uid)
//                                reference.addValueEventListener(object : ValueEventListener{
//                                    override fun onDataChange(p0: DataSnapshot)
//                                    {
//                                        val user = p0.getValue(Users::class.java)
//                                        if (notify)
//                                        {
//                                            sendNotification(userIdVisit, user!!.getUserName(), "sent you an image.")
//                                        }
//                                        notify = false
//                                    }
//
//                                    override fun onCancelled(p0: DatabaseError) {
//
//                                    }
//                                })
//                            }
//                        }
//                }
//            }
//        }
//    }


    private fun retrieveMessages(senderId: String, receiverId: String?, receiverImageUrl: String?)
    {
        mChatList = ArrayList()
        val reference = Firebase.database("https://teammaker-2299d-default-rtdb.asia-southeast1.firebasedatabase.app/").reference.child("Chats")

        reference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot)
            {
                (mChatList as ArrayList<ChatData>).clear()
                for (snapshot in p0.children)
                {
                    val chat = snapshot.getValue(ChatData::class.java)

                    if (chat!!.getReceiver().equals(senderId) && chat.getSender().equals(receiverId)
                        || chat.getReceiver().equals(receiverId) && chat.getSender().equals(senderId))
                    {
                        (mChatList as ArrayList<ChatData>).add(chat)
                    }
                    chatsAdapter = ChatAdapter(this@ChatActivity, (mChatList as ArrayList<ChatData>), receiverImageUrl!!)
                    binding.recyclerViewChats.adapter = chatsAdapter
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }


    var seenListner: ValueEventListener? = null

    private fun seenMessage(userId: String)
    {
        val reference = Firebase.database("https://teammaker-2299d-default-rtdb.asia-southeast1.firebasedatabase.app/").reference.child("Chats")

        seenListner = reference!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot)
            {
                for (dataSnapshot in p0.children)
                {
                    val chat = dataSnapshot.getValue(ChatData::class.java)

                    if (chat!!.getReceiver().equals(firebaseUser!!.uid) && chat!!.getSender().equals(userId))
                    {
                        val hashMap = HashMap<String, Any>()
                        hashMap["isseen"] = true
                        dataSnapshot.ref.updateChildren(hashMap)
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }


    override fun onPause() {
        super.onPause()

        reference!!.removeEventListener(seenListner!!)
    }
}
