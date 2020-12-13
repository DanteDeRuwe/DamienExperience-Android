package com.example.damiantour.chat

class JoinData {
    var username: String? = null
    var email: String? = null
    var room: String? = null

    constructor(username: String, email: String, room: String){
        this.username = username
        this.email = email
        this.room = room
    }
}