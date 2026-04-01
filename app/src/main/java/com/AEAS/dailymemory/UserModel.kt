package com.AEAS.dailymemory

// migración 0001_01_01_000000_create_users_table.php
data class User(
    val uid: String = "",
    val name: String = "",
    val appe: String = "",
    val username: String = "",
    val email: String = "",
    val role: String = "user",
    val scoreGeneral: Int = 0,
    val storyLevel: Int = 1
)