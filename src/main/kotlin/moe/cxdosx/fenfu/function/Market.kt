package moe.cxdosx.fenfu.function

import com.google.gson.Gson
import moe.cxdosx.fenfu.config.FenFuText
import moe.cxdosx.fenfu.config.checkBlackList
import moe.cxdosx.fenfu.data.beans.ItemIdQueryBean
import moe.cxdosx.fenfu.data.beans.ItemIdResult
import moe.cxdosx.fenfu.data.beans.MarketBean
import moe.cxdosx.fenfu.utils.DatabaseHelper
import moe.cxdosx.fenfu.utils.HttpUtil
import moe.cxdosx.fenfu.utils.TextUtil
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.*

const val MARKET_ID_ERROR = -2L
const val MARKET_ID_EMPTY = -1L

const val defaultQueryServer = "LuXingNiao"

fun Bot.market() {
    subscribeGroupMessages {
        Regex(FenFuText.regexMatch("market", "mitem", "查价", "价格", "市场"), RegexOption.IGNORE_CASE) matching regex@{
            val checkBlackList = checkBlackList()
            if (checkBlackList) {
                return@regex
            }
            val msg = it.replace(" +", " ").trim()//防止憨批打两个空格
            if (msg.contains(" ")) {
                val split = msg.split(" ")
                if (split.size == 2) { //仅物品名或help
                    if (split[1].toLowerCase() == "help" || split[1].toLowerCase() == "帮助") {
                        reply(
                            At(sender) + "\n" + FenFuText.marketHelp
                        )
                    } else { //正常询价，默认鸟区
                        val item = queryItemId(split[1])
                        when (item.ID) {
                            MARKET_ID_EMPTY -> {
                                reply(
                                    At(sender) + "\n没有发现包含“${split[1]}”的道具"
                                )
                            }
                            MARKET_ID_ERROR -> {
                                reply(
                                    At(sender) + "\n请求物品ID数据发生了错误（90002）"
                                )
                            }
                            else -> {
                                queryItemPrice(defaultQueryServer, item, checkItemNameQueryType(split[1]), this)
                            }
                        }
                    }
                } else if (split.size == 3) { //包括服务器名
                    val enServerName = DatabaseHelper.instance.queryENServerName(split[2])
                    if (enServerName.isEmpty()) {
                        reply(
                            At(sender) + "\n${FenFuText.unKnowServerName()}"
                        )
                    } else { //询价
                        val item = queryItemId(split[1])
                        when (item.ID) {
                            MARKET_ID_EMPTY -> {
                                reply(
                                    At(sender) + "\n没有发现包含“${split[1]}”的道具"
                                )
                            }
                            MARKET_ID_ERROR -> {
                                reply(
                                    At(sender) + "\n请求物品ID数据发生了错误（90002）"
                                )
                            }
                            else -> {
                                queryItemPrice(enServerName, item, checkItemNameQueryType(split[1]), this)
                            }
                        }
                    }
                } else {
                    reply(
                        At(sender) + "\n" + FenFuText.marketHelp
                    )
                }
            } else {
                reply(
                    At(sender) + "\n" + FenFuText.marketHelp
                )
            }
        }
    }
}

private fun checkItemNameQueryType(name: String): MarketType {
    return when {
        name.toLowerCase().contains("nq") -> {
            MarketType.ONLY_NQ
        }
        name.toLowerCase().contains("hq") -> {
            MarketType.ONLY_HQ
        }
        else -> {
            MarketType.ALL
        }
    }
}


private fun queryItemId(item: String): ItemIdResult {
    var itemName = item.toLowerCase()
    itemName = itemName.replace("hq", "")
    itemName = itemName.replace("nq", "")
    val queryUrl =
        "https://cafemaker.wakingsands.com/search?indexes=item&filters=ItemSearchCategory.ID>=1&columns=ID,Icon,Name&string=$itemName&limit=1&sort_field=LevelItem"
    val request = Request.Builder()
        .url(queryUrl)
        .build()
    val execute = HttpUtil.client.newCall(request).execute()
    if (execute.isSuccessful && execute.body != null) {
        val str = execute.body!!.string()
        val idResult = try {
            Gson().fromJson(str, ItemIdQueryBean::class.java)
        } catch (e: Exception) {
            return ItemIdResult(MARKET_ID_ERROR, "", "")
        }
        return if (idResult.Results.isEmpty()) {
            ItemIdResult(MARKET_ID_EMPTY, "", "")
        } else {
            idResult.Results[0]
        }
    } else {
        return ItemIdResult(MARKET_ID_ERROR, "", "")
    }
}

enum class MarketType {
    ONLY_HQ, //仅HQ
    ONLY_NQ, //仅NQ
    ALL      //全部
}

/**
 * 通过服务器名与Id查询价格
 * 此处已有回复值，所以不返回任何内容
 * @param serverName 格式化后的服务器名 如ShenYiZhiDi
 * @param item 通过[queryItemId]的返回数据来代入
 * @param type 限制输出的内容
 */
suspend private fun queryItemPrice(serverName: String, item: ItemIdResult, type: MarketType, event: GroupMessageEvent) {
    val queryUrl = "https://universalis.app/api/$serverName/${item.ID}"
    val request = Request.Builder().url(queryUrl).build()
    val execute = HttpUtil.client.newCall(request).execute()
    if (execute.isSuccessful && execute.body != null) {
        val str = execute.body!!.string()
        val marketBean = Gson().fromJson(str, MarketBean::class.java)
        val stringBuffer = StringBuffer()
        when (type) {
            MarketType.ONLY_HQ -> {
                stringBuffer.append("[HQ]")
            }
            MarketType.ONLY_NQ -> {
                stringBuffer.append("[NQ]")
            }
            else -> {
            }
        }
        stringBuffer.append("${item.Name}在${DatabaseHelper.instance.queryCNServerName(serverName)}的价格情况如下").append("\n")
        var outputIndex = 0
        val taxRate = 1.05 //税率
        for (element in marketBean.listings) {
            if (type == MarketType.ONLY_NQ && element.hq) {
                continue
            }
            if (type == MarketType.ONLY_HQ && !element.hq) {
                continue
            }
            outputIndex++
            val total = (element.total * taxRate).toInt()
            stringBuffer.append(if (element.hq) "HQ " else "")
                .append("${element.pricePerUnit} x ${element.quantity} = $total")
            if (total >= 10000) {
                stringBuffer.append("(${TextUtil.convertNumber(total)})")
            }
            stringBuffer.append(
                "税込 - ${
                    if (element.worldName == null) DatabaseHelper.instance.queryCNServerName(
                        serverName
                    ) else DatabaseHelper.instance.queryCNServerName(element.worldName!!)
                }\n"
            )
            if (outputIndex >= 10) {
                break
            }
        }
        if (outputIndex == 0) {
            event.reply(
                At(event.sender) + "\n该物品暂无人挂售或无人上传板子数据"
            )
            return
        }
        stringBuffer.append("价格更新时间：[${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(marketBean.lastUploadTime))}]\n")
        val diffTime = System.currentTimeMillis() - marketBean.lastUploadTime
        if (diffTime >= 12 * 60 * 60 * 1000) {
            stringBuffer.append("【该价格更新时间距现在已超过了${diffTime / (60 * 60 * 1000)}个小时】\n")
        }
        stringBuffer.append("https://universalis.app/ 提供数据支持")
        event.reply(
            At(event.sender) + "\n" + stringBuffer.toString()
        )
    } else {
        event.reply(
            At(event.sender) + "\n无法访问服务器，你可以暂时前往https://universalis.app/手动查询"
        )
    }
}