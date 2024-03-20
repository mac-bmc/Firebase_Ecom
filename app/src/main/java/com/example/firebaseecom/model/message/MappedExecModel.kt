package com.example.firebaseecom.model.message

import com.example.firebaseecom.model.UserModel
import java.io.Serializable

data class MappedExecModel(
    val mapID : String,
    val executive: ExecutiveModel? = null,
    var user : UserModel? = null
):Serializable{

    constructor(): this("",null,null)
}
