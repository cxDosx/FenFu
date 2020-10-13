package moe.cxdosx.fenfu.data.beans

data class UserBanBean(
    val userName: String,
    val server: String,
    val reason: String,
    val source: String,
    val time: String,
    val id: String,
    val count: Int
)