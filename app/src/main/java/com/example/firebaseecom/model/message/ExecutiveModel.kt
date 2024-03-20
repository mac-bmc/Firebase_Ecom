package com.example.firebaseecom.model.message


import java.io.Serializable

data class ExecutiveModel(
    val execId :String,
    val execName: String,
    val execEmail: String,
    val userImg: String,
    val isOccupied : Boolean
) : Serializable {
    constructor() : this("","", "", "",false)
}
