package moe.cxdosx.fenfu.data.beans

data class WeiboFenFuSendBean(
    val weiboUserId: String,
    val weiboId: String,
    val weiboContent: String,
    val weiboOriginUrl: String,
    val weiboImage: List<String>
)