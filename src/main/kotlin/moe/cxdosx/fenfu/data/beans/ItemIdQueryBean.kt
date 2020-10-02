package moe.cxdosx.fenfu.data.beans

data class ItemIdQueryBean(
    val Results: List<ItemIdResult>
)

data class ItemIdResult(
    val ID: Long,
    val Icon: String,
    val Name: String
)