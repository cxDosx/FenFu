package moe.cxdosx.fenfu.function

import com.google.gson.Gson
import moe.cxdosx.fenfu.config.FenFuText
import moe.cxdosx.fenfu.config.checkBlackList
import moe.cxdosx.fenfu.data.beans.KonachanPostBean
import moe.cxdosx.fenfu.utils.DatabaseHelper
import moe.cxdosx.fenfu.utils.HttpUtil
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.recallIn
import okhttp3.Request
import java.io.InputStream
import kotlin.random.Random
import kotlin.random.nextInt

fun Bot.seTu() {
    subscribeGroupMessages {
        Regex(FenFuText.setuRegex, RegexOption.IGNORE_CASE) matching regex@{
            val checkBlackList = checkBlackList(false)
            if (checkBlackList) {
                return@regex
            }
            if (sender.id != bot.id && DatabaseHelper.instance.checkSeTuEnable()) {
                getImageInputStream(getRandomSeTuImage())?.sendAsImage()?.recallIn(30 * 1000)
            }
        }
    }
}


/**
 * 获取随机图片地址
 */
private fun getRandomSeTuImage(): String {
    val req = Request.Builder().url(
        "https://konachan.com/post.json?tag=r18&page=${
            Random(System.currentTimeMillis()).nextInt(
                IntRange(
                    1,
                    100
                )
            )
        }&limit=1"
    ).build()
    val execute = HttpUtil.client.newCall(req).execute()
    return if (execute.isSuccessful && execute.body != null) {
        var body = execute.body!!.string()
        body = body.substring(1, body.length - 1)
        val json = Gson().fromJson(body, KonachanPostBean::class.java)
        json.sample_url
    } else {
        ""
    }
}

private fun getImageInputStream(imageUrl: String): InputStream? {
    if (imageUrl.isEmpty()) {
        return null
    }
    val execute = HttpUtil.client.newCall(Request.Builder().url(imageUrl).build()).execute()
    return if (execute.isSuccessful && execute.body != null) {
        execute.body!!.byteStream()
    } else {
        null
    }
}
