package com.example.firebaseecom.model

import java.io.Serializable

data class UserModel(
    val userName : String,
    val userEmail : String,
    val userImg : String,
    val phNo : String,
    val address :String
): Serializable {
    constructor() : this("","", "","","")
}
