package moe.cxdosx.fenfu.function

import moe.cxdosx.fenfu.config.FenFuText
import moe.cxdosx.fenfu.config.checkBlackList
import moe.cxdosx.fenfu.utils.DatabaseHelper
import moe.cxdosx.fenfu.utils.HttpUtil
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import okhttp3.Request
import org.jsoup.Jsoup
import kotlin.math.roundToInt

fun Bot.goldPrice() {

    subscribeGroupMessages {
        Regex(FenFuText.regexMatch("gold", "金价"), RegexOption.IGNORE_CASE) matching regex@{
            val checkBlackList = checkBlackList()
            if (checkBlackList) {
                return@regex
            }
            val defaultServer = DatabaseHelper.LuXingNiao
            val msg = it.replace(" +", " ").trim()//防止憨批打两个空格
            if (msg.contains(" ")) {
                val split = msg.split(" ")
                if (split.size == 2) {
                    val sp1 = split[1].toLowerCase().trim()
                    val serverNameEN = DatabaseHelper.instance.queryENServerName(sp1)
                    if (serverNameEN.isEmpty()) {
                        parseData(split[1].toLowerCase().trim(), defaultServer)
                    } else {
                        parseData("", serverNameEN)
                    }
                } else if (split.size == 3) {
                    val enServerName = DatabaseHelper.instance.queryENServerName(split[2])
                    if (enServerName.isEmpty()) {
                        reply(
                            At(sender) + "\n" + FenFuText.unKnowServerName()
                        )
                    } else {
                        parseData(split[1].toLowerCase().trim(), enServerName)
                    }
                } else {
                    reply(
                        At(sender) + "\n" + FenFuText.goldPriceHelp
                    )
                }
            } else {
                reply(
                    At(sender) + "\n" + FenFuText.goldPriceHelp
                )
            }
        }
    }

}

private suspend fun GroupMessageEvent.parseData(goldText: String, serverEN: String) {
    val region = DatabaseHelper.instance.queryServerRegion(serverEN)
    if (region != "CN") {
        reply(
            At(sender) + "\n" + FenFuText.goldPriceHelp
        )
        return
    }
    if (goldText.contains(FenFuText.regexAnything("t", "糖").toRegex())) {
        //查糖转金
        val gold = convertDouble(goldText)
        if (gold == null) {
            reply(
                At(sender) + "\n" + FenFuText.goldPriceError
            )
        } else {
            cleanDataFrom5173(serverEN, gold, GoldPriceQueryType.MONEY2GLI)
        }
    } else if (goldText.contains(FenFuText.regexAnything("w", "万").toRegex())) {
        //查金转糖
        val gold = convertDouble(goldText)
        if (gold == null) {
            reply(
                At(sender) + "\n" + FenFuText.goldPriceError
            )
        } else {
            cleanDataFrom5173(serverEN, gold, GoldPriceQueryType.GLI2MONEY)
        }
    } else {
        //都查
        if (goldText.isEmpty()) {
            cleanDataFrom5173(serverEN, 0.00, GoldPriceQueryType.NONE_SERVER)
            return
        }
        val gold = goldText.toDoubleOrNull()
        if (gold == null) {
            reply(
                At(sender) + "\n" + FenFuText.goldPriceHelp
            )
        } else {
            cleanDataFrom5173(serverEN, gold, GoldPriceQueryType.ALL)
        }
    }
}

enum class GoldPriceQueryType {
    MONEY2GLI,
    GLI2MONEY,
    ALL,
    NONE_SERVER
}

private fun convertDouble(s: String): Double? {
    var str = s
    str = str.replace("w", "")
    str = str.replace("万", "")
    str = str.replace("t", "")
    str = str.replace("糖", "")
    return str.toDoubleOrNull()
}

suspend fun GroupMessageEvent.cleanDataFrom5173(serverName: String, num: Double, type: GoldPriceQueryType) {
    val server = when (serverName) {
        DatabaseHelper.MoGuLi -> {
            "zqq0tl-srj0gw-0-0"
        }
        DatabaseHelper.MaoXiaoPang -> {
            "zqq0tl-wpcfpw-0-0"
        }
        else -> {
            "0-wcyztw-0-0"
        }
    }
    val serverCN = DatabaseHelper.instance.queryCNServerName(serverName)
    val urlHead = "http://s.5173.com/search/0ab3b27dc612483cb06dc7cb92c390ad-"
    val urlEnd = "-c4524f-0-0-0-a-a-a-a-a-0-0-0-0.shtml"
    val execute =
        HttpUtil.client.newCall(Request.Builder().get().url(urlHead.plus(server).plus(urlEnd)).build()).execute()
    if (execute.isSuccessful && execute.body != null) {
        val content = execute.body!!.string()
        val rootDocument = Jsoup.parse(content)
        val elementsByClass = rootDocument.getElementsByClass("pdlist_unitprice")
        val goldPriceList = ArrayList<Double>()
        elementsByClass.forEach {
            val first = it.getElementsByTag("li").first().text()
            val pricePreOneYuan = first.substring(first.indexOf("=") + 1, first.indexOf("万"))
            goldPriceList.add(pricePreOneYuan.toDouble())
        }
        val goldAvg = (goldPriceList.toList().stream().mapToDouble { it }.average().asDouble * 100).roundToInt() / 100
        DatabaseHelper.instance.markGoldPriceLog(this)
        reply(
            At(sender) + "\n" +
                    """
                分福帮你查询了当前 $serverCN 的金币价格情况
                目前金价比大约在 1:${goldAvg}w 左右
                ${
                        when (type) {
                            GoldPriceQueryType.GLI2MONEY -> {
                                "$num 万Gli = ${(num / goldAvg * 100).roundToInt() / 100}糖"
                            }
                            GoldPriceQueryType.MONEY2GLI -> {
                                "$num 糖 = ${(num * goldAvg * 100).roundToInt() / 100}万Gli"
                            }
                            GoldPriceQueryType.ALL -> {
                                //ALL
                                """
                                    $num 万Gli = ${(num / goldAvg * 100).roundToInt() / 100} 万Gli=糖
                                    $num 糖 = ${(num * goldAvg * 100).roundToInt() / 100} 糖=万Gli
                                """.trimIndent()
                            }
                            else -> {
                                ""
                            }
                        }
                    }
                【关于相关于本功能的注意事项请使用!gold help获取】
           """.trimIndent()
        )
    } else {
        reply(
            At(sender) + "\n" +
                    FenFuText.serverError
        )
    }
}