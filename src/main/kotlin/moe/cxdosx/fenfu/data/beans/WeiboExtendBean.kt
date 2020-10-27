package moe.cxdosx.fenfu.data.beans

data class WeiboExtendBean(
    val ok: Int,
    val data: DataBean?
)

data class DataBean(
    val ok: Int,
    val longTextContent: String,
    val reposts_count: Int,
    val comments_count: Int,
    val attitudes_count: Int
)