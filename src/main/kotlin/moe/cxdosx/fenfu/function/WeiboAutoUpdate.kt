package moe.cxdosx.fenfu.function

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moe.cxdosx.fenfu.config.BotConfig
import moe.cxdosx.fenfu.data.beans.Card
import moe.cxdosx.fenfu.data.beans.WeiboDetailBean
import moe.cxdosx.fenfu.data.beans.WeiboExtendBean
import moe.cxdosx.fenfu.data.beans.WeiboFenFuSendBean
import moe.cxdosx.fenfu.utils.DatabaseHelper
import moe.cxdosx.fenfu.utils.HttpUtil
import moe.cxdosx.fenfu.utils.MiraiUtil
import moe.cxdosx.fenfu.utils.WeiboUpdateManager
import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.uploadImage
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.abs

@Suppress("UNUSED")
fun Bot.weiboAutoUpdate() {
    WeiboUpdateManager.initWeiboUpdate()
}

object WeiboAutoUpdate {
    /**
     * 获取containerId
     * @param uid 微博用户uid
     */
    private fun getContainerId(uid: String): String {
        return "107603$uid"
    }

    fun getAllWeiboText(uid: String) {
        try {
            getAllWeiboText(uid, false)
        } catch (e: Exception) {
            GlobalScope.launch(Dispatchers.IO) {
                MiraiUtil.sendToTargetFriend(
                    BotConfig.ownerQQ, """
                微博自动更新工具异常暂停：
                ${e.localizedMessage}
            """.trimIndent()
                )
            }
            WeiboUpdateManager.stopWeiboAutoUpdate()
        }
    }

    fun getAllWeiboText(uid: String, checkMode: Boolean) {
        val params = HashMap<String, Any>()
        params["type"] = "uid"
        params["value"] = uid
        params["containerid"] = getContainerId(uid)
        val rawText: String?
        if (checkMode) {
            val response = HttpUtil.getResponse("https://m.weibo.cn/api/container/getIndex", params)
            rawText = if (response.isSuccessful && response.body != null) {
                response.body!!.string()
            } else {
                null
            }
            GlobalScope.launch(Dispatchers.IO) {
                MiraiUtil.sendToTargetFriend(
                    BotConfig.ownerQQ,
                    "CheckUrl=${response.request.url}\nResponse=${response.code}"
                )
            }

        } else {
            rawText = HttpUtil.getRawText("https://m.weibo.cn/api/container/getIndex", params)
        }
        if (!rawText.isNullOrEmpty()) {
            val detailBean = Gson().fromJson(rawText, WeiboDetailBean::class.java)
            if (detailBean.ok == 1 && detailBean.data.cards.isNotEmpty()) { //成功
                run breaking@{
                    detailBean.data.cards.forEach continuing@{
                        if (it.mblog == null) {
                            return@continuing
                        }
                        val weiboId = it.mblog.id
                        if (it.mblog.mblogtype != 0 && DatabaseHelper.instance.checkSentWeibo(weiboId)) {
                            return@continuing
                        }
                        if (DatabaseHelper.instance.checkSentWeibo(weiboId)) {
                            if (checkMode) {
                                GlobalScope.launch(Dispatchers.IO) {
                                    MiraiUtil.sendToTargetFriend(
                                        BotConfig.ownerQQ,
                                        """
                                            已发送过，跳过$weiboId
                                        """.trimIndent()
                                    )
                                }
                            }
                            return@breaking
                        }
                        if (checkMode) {
                            GlobalScope.launch(Dispatchers.IO) {
                                MiraiUtil.sendToTargetFriend(
                                    BotConfig.ownerQQ,
                                    """
                                    正在装填微博数据，准备发送$weiboId
                                """.trimIndent()
                                )
                            }
                        }
                        setupWeiboData(it.mblog.retweeted_status == null, weiboId, it)
                        return@breaking
                    }
                }

            } else {
                //数据有误，停止自动获取
                WeiboUpdateManager.stopWeiboAutoUpdate()
            }
        } else {
            //数据有误，停止自动获取
            WeiboUpdateManager.stopWeiboAutoUpdate()
        }
    }

    /**
     * 拼接并发送微博数据
     * @param isOriginal 原创微博标识
     * @param weiboId 微博id
     * @param card 实体类
     *
     */
    private fun setupWeiboData(isOriginal: Boolean, weiboId: String, card: Card) {
        val userName = card.mblog!!.user.screen_name
        val userId = card.mblog.user.id
        val postTime = card.mblog.created_at
        val originUrl = card.scheme

        val params = HashMap<String, Any>()
        params["id"] = weiboId
        val rawText = HttpUtil.getRawText("https://m.weibo.cn/statuses/extend", params)
        if (!rawText.isNullOrEmpty()) {
            if (rawText.contains("客户端")) {
                var preSendMessage = "来自@$userName 发布于 ${timeFormat(postTime)} 的${if (isOriginal) "最新微博" else "转发微博"}\n"
                preSendMessage += card.mblog.text.replace(Regex("<[^>]+>"), "")
                preSendMessage += "\n$originUrl"
                sendWeiboUpdate(WeiboFenFuSendBean(userId.toString(), weiboId, preSendMessage, originUrl, ArrayList()))
            } else {
                val extendsBean = Gson().fromJson(rawText, WeiboExtendBean::class.java)
                if (extendsBean.ok == 1 && extendsBean.data != null && extendsBean.data.ok == 1 && extendsBean.data.longTextContent.isNotEmpty()) {
                    var message = "来自@$userName 发布于 ${timeFormat(postTime)} 的${if (isOriginal) "最新微博" else "转发微博"}\n"
                    message += extendsBean.data.longTextContent
                    val document = Jsoup.parse(message)
                    val element = document.select("a[data-url]")
                    element.forEach {
                        message = message.replaceFirst("网页链接", it.attr("data-url"))
                    }
                    message = message.replace("<br />", "\n").replace(Regex("<[^>]+>"), "")
                    val imgList = ArrayList<String>()
                    if (!card.mblog.pics.isNullOrEmpty()) {
                        card.mblog.pics.forEach {
                            imgList.add(it.large.url)
                        }
                    }
                    sendWeiboUpdate(WeiboFenFuSendBean(userId.toString(), weiboId, message, originUrl, imgList))
                }
            }
        }
    }


    private fun sendWeiboUpdate(send: WeiboFenFuSendBean) {
        DatabaseHelper.instance.saveWeiboSentHistoryId(send.weiboId)
        if (send.weiboContent.contains("微博官方唯一抽奖工具@微博抽奖平台")) {
            GlobalScope.launch(Dispatchers.IO) {
                MiraiUtil.sendToTargetFriend(
                    BotConfig.ownerQQ,
                    """
                        发现一条微博更新，因为是抽奖公示，所以被跳过了
                    """.trimIndent()
                )
            }
            return // 微博抽奖结果不发送
        }
        GlobalScope.launch(Dispatchers.IO) {
            val allSubscribeWeiboIdGroups = DatabaseHelper.instance.getAllSubscribeWeiboIdGroups(send.weiboUserId)
            if (send.weiboImage.isEmpty()) {
                allSubscribeWeiboIdGroups.forEach {
                    MiraiUtil.sendToTargetGroup(it.toLong(), send.weiboContent.plus("\n${send.weiboOriginUrl}"))
                }
            } else {
                allSubscribeWeiboIdGroups.forEach { group ->
                    val targetGroup = MiraiUtil.getTargetGroup(group.toLong())
                    val images = ArrayList<Image>()
                    send.weiboImage.forEach { img ->
                        val imageInputStream = MiraiUtil.getImageInputStream(img)
                        if (imageInputStream != null) {
                            val uploadImage = targetGroup?.uploadImage(imageInputStream)
                            if (uploadImage != null) {
                                images.add(uploadImage)
                            }
                        }
                    }
                    val msg = MessageChainBuilder()
                    msg.append(send.weiboContent)
                    images.forEach {
                        msg.append(it)
                    }
                    msg.append("\n${send.weiboOriginUrl}")
                    MiraiUtil.getTargetGroup(group.toLong())?.sendMessage(msg.asMessageChain())
                }
            }
        }

    }
}

/**
 * 格式化微博时间显示
 * 返回格式为Tue Apr 06 16:01:08 +0800 2021
 * 与本地时间对比后返回相应的时间显示
 * @param timeStr 欲转换的时间
 * @return 规则：1分钟内返回 刚刚
 *              60分钟内返回 x分钟
 *              24小时内返回 x小时
 *              其余返回转换后的时间格式YYYY HH:mm:ss
 */
@Suppress("DEPRECATION")
fun timeFormat(timeStr: String): String {
    val convertDate = try {
        Date(timeStr)
    } catch (e: Exception) {
        println(e.localizedMessage)
        null
    } ?: return timeStr
    val currentDateTimeUnix = Calendar.getInstance().timeInMillis
    val oneMinute = TimeUnit.MINUTES.toMillis(1)
    val oneHour = TimeUnit.HOURS.toMillis(1)
    val oneDay = TimeUnit.DAYS.toMillis(1)
    val convertDateTimeUnix = convertDate.time
    val timeDiff = abs(currentDateTimeUnix - convertDateTimeUnix)
    return if (timeDiff < oneMinute) {
        "刚刚"
    } else if (timeDiff < oneHour) {
        val v = timeDiff / oneMinute
        "${v}分钟前"
    } else if (timeDiff < oneDay) {
        val v = timeDiff / oneHour
        "${v}小时前"
    } else {
        SimpleDateFormat("YYYY HH:mm:ss").format(convertDate)
    }
}