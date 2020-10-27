package moe.cxdosx.fenfu.function

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moe.cxdosx.fenfu.data.beans.Card
import moe.cxdosx.fenfu.data.beans.WeiboDetailBean
import moe.cxdosx.fenfu.data.beans.WeiboExtendBean
import moe.cxdosx.fenfu.data.beans.WeiboFenFuSendBean
import moe.cxdosx.fenfu.utils.DatabaseHelper
import moe.cxdosx.fenfu.utils.HttpUtil
import moe.cxdosx.fenfu.utils.MiraiUtil
import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.uploadImage
import org.jsoup.Jsoup
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.concurrent.schedule

@Suppress("UNUSED")
fun Bot.weiboAutoUpdate() {
    Timer().apply {
        schedule(0L, TimeUnit.MINUTES.toMillis(5)) {
            DatabaseHelper.instance.getAllWeiboUpdateUid().forEach {
                WeiboAutoUpdate.getAllWeiboText(it)
            }
        }
    }
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
        val params = HashMap<String, Any>()
        params["type"] = "uid"
        params["value"] = uid
        params["containerid"] = getContainerId(uid)
        val rawText = HttpUtil.getRawText("https://m.weibo.cn/api/container/getIndex", params)
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
                            return@breaking
                        }
                        setupWeiboData(it.mblog.retweeted_status == null, weiboId, it)
                        return@breaking
                    }
                }

            }
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
                var preSendMessage = "来自@$userName 发布于 $postTime 的${if (isOriginal) "最新微博" else "转发微博"}\n"
                preSendMessage += card.mblog.text.replace(Regex("<[^>]+>"), "")
                preSendMessage += "\n$originUrl"
                sendWeiboUpdate(WeiboFenFuSendBean(userId.toString(), weiboId, preSendMessage, originUrl, ArrayList()))
            } else {
                val extendsBean = Gson().fromJson(rawText, WeiboExtendBean::class.java)
                if (extendsBean.ok == 1 && extendsBean.data != null && extendsBean.data.ok == 1 && extendsBean.data.longTextContent.isNotEmpty()) {
                    var message = "来自@$userName 发布于 $postTime 的${if (isOriginal) "最新微博" else "转发微博"}\n"
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
        GlobalScope.launch(Dispatchers.IO) {
            val allSubscribeWeiboIdGroups = DatabaseHelper.instance.getAllSubscribeWeiboIdGroups(send.weiboUserId)
            if (send.weiboImage.isEmpty()) {
                allSubscribeWeiboIdGroups.forEach {
                    MiraiUtil.sendToTargetGroup(it.toLong(), send.weiboContent.plus("\n${send.weiboOriginUrl}"))
                }
                DatabaseHelper.instance.saveWeiboSentHistoryId(send.weiboId)
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
                DatabaseHelper.instance.saveWeiboSentHistoryId(send.weiboId)
            }
        }

    }
}