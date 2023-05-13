package com.awada.currencyexchange.api.model
import com.google.gson.annotations.SerializedName

class User(
    @SerializedName("user_name")
    var username: String? = null,

    @SerializedName("password")
    var password: String? = null
) {
    @SerializedName("user_id")
    var id: Int? = null
}
