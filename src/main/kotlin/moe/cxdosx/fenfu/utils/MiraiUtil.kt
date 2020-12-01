package moe.cxdosx.fenfu.utils

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group
import okhttp3.Request
import java.io.InputStream

object MiraiUtil {
    suspend fun sendToTargetGroup(targetGroup: Long, content: String) {
        getTargetGroup(targetGroup)?.sendMessage(content)
    }

    suspend fun sendToTargetFriend(targetFriend: Long, content: String) {
        getTargetFriend(targetFriend)?.sendMessage(content)
    }


    fun getTargetGroup(targetGroup: Long): Group? {
        var target: Group? = null
        Bot.forEachInstance {
            it.groups.forEach { group ->
                if (group.id == targetGroup) {
                    target = group
                    return@forEachInstance
                }
            }
        }
        return target
    }

    fun getTargetFriend(targetFriend: Long): Friend? {
        var target: Friend? = null
        Bot.forEachInstance {
            it.friends.forEach { f ->
                if (f.id == targetFriend) {
                    target = f
                    return@forEachInstance
                }
            }
        }
        return target
    }

    fun getImageInputStream(imageUrl: String): InputStream? {
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

    fun logger(str: String) {
        Bot.forEachInstance {
            it.logger.debug(str)
        }
    }

}