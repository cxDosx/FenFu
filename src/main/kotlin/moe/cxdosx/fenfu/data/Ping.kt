package moe.cxdosx.fenfu.data

import moe.cxdosx.fenfu.utils.HttpUtil
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.recall
import okhttp3.Request
import kotlin.math.abs

class Ping(private val event: GroupMessageEvent, private val preMessage: MessageReceipt<Contact>) {
    private fun logsTest(): Long {
        val startTime = System.currentTimeMillis()
        val req = Request.Builder().url("https://cn.fflogs.com").head().build()
        val execute = HttpUtil.client.newCall(req).execute()
        val endTime = if (!execute.isSuccessful) {
            0
        } else {
            System.currentTimeMillis()
        }
        return abs(endTime - startTime)
    }

    private fun baiduTest(): Long {
        val startTime = System.currentTimeMillis()
        val req = Request.Builder().url("https://www.baidu.com").head().build()
        val execute = HttpUtil.client.newCall(req).execute()
        val endTime = if (!execute.isSuccessful) {
            0
        } else {
            System.currentTimeMillis()
        }
        return abs(endTime - startTime)
    }

    companion object {
        const val errorTime = 1000 * 1000
        const val timeout = 10 * 1000
    }

    suspend fun ping() {
        val baiduTest = baiduTest()
        val logsTest = logsTest()
        preMessage.recall()
        event.reply(
            At(event.sender) + "\n" +
                    "FFlogsCN的连通性：${if (logsTest > timeout) "无效" else "有效"}  Time：${if (logsTest > errorTime) "不可用" else logsTest}\n" +
                    "网络连通性：${if (baiduTest > timeout) "无效" else "有效"}  Time：${if (baiduTest > errorTime) "不可用" else baiduTest}"
        )
    }
}