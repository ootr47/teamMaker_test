package com.example.teammaker

class ChatData {
    private var userName: String = ""
    private var message: String = ""

    constructor()

    constructor(userName: String, message: String){
        this.userName=userName
        this.message=message
    }
    fun getUserName(): String {
        return this.userName
    }
    fun setUserName(userName: String){
        this.userName=userName
    }
    fun getMessage(): String {
        return this.message
    }
    fun setMessage(message: String){
        this.message=message
    }


}