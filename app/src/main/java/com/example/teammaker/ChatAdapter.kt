package com.example.teammaker

import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.teammaker.databinding.ChatRecyclerItemBinding
import java.text.SimpleDateFormat

class ChatAdapter: RecyclerView.Adapter<Holder>() {
    var listData = mutableListOf<ChatDataFormat>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ChatRecyclerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val chat = listData.get(position)
        Log.d(TAG, "position"+position+"onBindViewHolder"+chat.message)
        holder.setChat(chat)
    }

    override fun getItemCount(): Int {
        return listData.size
    }
}
class Holder(val binding: ChatRecyclerItemBinding): RecyclerView.ViewHolder(binding.root){
    fun setChat(chat: ChatDataFormat){

        Log.d(TAG, "setChat : " + chat.message)
        binding.textMessage.text = chat.message

        var sdf = SimpleDateFormat("hh:mm")
        var formattedDate = sdf.format(chat.timestamp)
        binding.timeStamp.text=formattedDate
    }
}