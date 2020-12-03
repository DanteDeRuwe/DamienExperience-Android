package com.example.damiantour.chat

import androidx.databinding.BaseObservable

/**
 * @author Dante De Ruwe en Jordy Van Kerkvoorde
 */
class Message : BaseObservable() {
    var sender: Sender? = null
        get() = field
        set(value) {
            field = value
        }
    var time: String? = null
        get() = field
        set(value) {
            field = value
        }
    var text: String? = null
        get() = field
        set(value) {
            field = value
        }
}