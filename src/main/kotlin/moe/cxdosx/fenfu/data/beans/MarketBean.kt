package moe.cxdosx.fenfu.data.beans


data class MarketBean(
    val dcName: String,
    val itemID: Long,
    val worldID: Long,
    val lastUploadTime: Long,
    val regularSaleVelocity: Double,
    val nqSaleVelocity: Double,
    val hqSaleVelocity: Double,
    val averagePrice: Double,
    val averagePriceNQ: Double,
    val averagePriceHQ: Double,
    val listings: List<ListingsBean>,
    val recentHistory: List<RecentHistoryBean>
)

data class ListingsBean(
    var creatorID: String,
    var lastReviewTime: Int,
    var listingID: String,
    var pricePerUnit: Int,
    var quantity: Int,
    var retainerID: String,
    var sellerID: String,
    var stainID: Int,
    var worldName: String?,
    var creatorName: String,
    var hq: Boolean,
    var isCrafted: Boolean,
    var onMannequin: Boolean,
    var retainerCity: Int,
    var retainerName: String,
    var total: Int
)

data class RecentHistoryBean(
    var isHq: Boolean,
    var pricePerUnit: Int,
    var quantity: Int,
    var timestamp: Int,
    var worldName: String,
    var buyerName: String,
    var total: Int
)