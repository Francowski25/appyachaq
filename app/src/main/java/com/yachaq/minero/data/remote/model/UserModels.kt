package com.yachaq.minero.data.remote.model

data class RequestUserAdmin(
    val name: String,
    val email: String,
    val password: String? = null,
    val role: String = "minero",
    val status: Boolean = true
)

data class ResponseUser(
    val idUser: String? = null,
    val name: String? = null,
    val email: String? = null,
    val role: String? = null,
    val createdAt: String? = null,
    val status: Boolean? = null,
    val listMessage: List<String>? = null,
    val type: String? = null
)