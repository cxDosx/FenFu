package moe.cxdosx.fenfu.function

import com.google.gson.Gson
import moe.cxdosx.fenfu.config.FenFuText
import moe.cxdosx.fenfu.config.checkBlackList
import moe.cxdosx.fenfu.data.beans.KonachanPostBean
import moe.cxdosx.fenfu.utils.DatabaseHelper
import moe.cxdosx.fenfu.utils.HttpUtil
import moe.cxdosx.fenfu.utils.MiraiUtil
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.recallIn
import okhttp3.Request

fun Bot.seTu() {
    subscribeGroupMessages {
        Regex(FenFuText.setuRegex, RegexOption.IGNORE_CASE) matching regex@{
            val checkBlackList = checkBlackList(false)
            if (checkBlackList) {
                return@regex
            }
            if (sender.id != bot.id && DatabaseHelper.instance.checkSeTuEnable()) {
                MiraiUtil.getImageInputStream(getRandomSeTuImage())?.sendAsImage()?.recallIn(30 * 1000)
            }
        }
    }
}


/**
 * 获取随机图片地址
 */
private fun getRandomSeTuImage(): String {
    val req = Request.Builder().url(
        "https://konachan.com/post.json?limit=1&tags=order:random%20rating:explicit"
    ).build()
    val execute = HttpUtil.client.newCall(req).execute()
    return if (execute.isSuccessful && execute.body != null) {
        var body = execute.body!!.string()
        body = body.substring(1, body.length - 1)
        val json = Gson().fromJson(body, KonachanPostBean::class.java)
        if (json.created_at < 1546272000) { //过滤2019年之前的图片
            getRandomSeTuImage() // 重新获取图片
        }
        json.sample_url
    } else {
        ""
    }
}


