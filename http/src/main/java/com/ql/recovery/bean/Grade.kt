package com.ql.recovery.bean

data class Grade(
    val current_exp: Int,
    val end_exp: Int,
    val grade: Int,
    val source: List<ExpSource>,
    val start_exp: Int,
    val unlock_games: List<UnlockGame>,
    val unlock_gifts: List<UnlockGift>
)

data class ExpSource(
    val exp: Int,
    val type: String
)

data class UnlockGame(
    val icon: String,
    val id: Int
)

data class UnlockGift(
    val icon: String,
    val id: Int
)
