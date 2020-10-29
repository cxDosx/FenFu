package moe.cxdosx.fenfu.function

import kotlinx.coroutines.launch
import moe.cxdosx.fenfu.config.FenFuText
import moe.cxdosx.fenfu.config.checkBlackList
import moe.cxdosx.fenfu.utils.DatabaseHelper
import moe.cxdosx.fenfu.utils.TimeUtil
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.content
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

fun Bot.timeManager() {
    subscribeGroupMessages {
        startsWith("+", trim = true) {
            parseManager(true, this)
        }
        startsWith("-", trim = true) {
            parseManager(false, this)
        }

        Regex(FenFuText.regexMatch("cleartime", "清空时间"), RegexOption.IGNORE_CASE) matching regex@{
            val checkBlackList = checkBlackList()
            if (checkBlackList) {
                return@regex
            }
            if (!DatabaseHelper.instance.isTimeManagerGroup(group.id)) {
                reply(
                    At(sender) + "\n" + """
                        这里尚未开通这项业务~
                    """.trimIndent()
                )
                return@regex
            }
            DatabaseHelper.instance.deleteTimeManagerLog(group.id)
            refreshGroupName(this.group)
        }


    }


    Timer().apply {
        schedule(TimeUtil.getNextTaskTime(Calendar.getInstance().time, "23/59"), TimeUnit.DAYS.toMillis(1)) {
            launch {
                groups.forEach {
                    if (DatabaseHelper.instance.isTimeManagerGroup(it.id)) {
                        DatabaseHelper.instance.deleteTimeManagerLog(it.id)
                        refreshGroupName(it)
                    }
                }
            }
        }
    }
}


suspend fun parseManager(isLateTime: Boolean, event: GroupMessageEvent) {
    if (!DatabaseHelper.instance.isTimeManagerGroup(event.group.id)) {
        return
    }
    if (event.group.name.contains("[今天\uD83C\uDE1A]")) {
        event.reply(
            """
                今天已经歇了哦
                如果今天又开工了请使用以下任意命令
                ✨!cleartime
                ✨!清空时间
            """.trimIndent()
        )
        return
    }
    val str = event.message.content
    var resultStr = "" //分钟
    var isLow = false
    run {
        str.forEach {
            if (it.isDigit()) {
                resultStr += it
            } else if (it == '.') {
                if (!isLow) {
                    resultStr += "."
                    isLow = true
                } else {
                    return@run
                }
            }
        }
    }
    val toInt = resultStr.toDouble()
    var resultTime = 0
    if (str.contains("小时")) {
        resultTime = (toInt * 60).toInt()
    } else if (str.contains("分钟")) {
        val indexOf = resultStr.indexOf(".")
        resultTime = if (indexOf != -1) {
            resultStr.substring(0, resultStr.indexOf(".")).toInt()
        } else {
            resultStr.toInt()
        }
    }
    DatabaseHelper.instance.markTimeManagerLog(
        event.group.id,
        event.sender.id,
        if (isLateTime) resultTime else resultTime * -1
    )

    refreshGroupName(event.group)
}

fun refreshGroupName(group: Group) {
    val timeManagerStartTime = DatabaseHelper.instance.getTimeManagerStartTime(group.id)
    if (timeManagerStartTime == null) {
        return
    } else {
        val calendar = Calendar.getInstance()
        calendar.time = timeManagerStartTime
        val afterMeasureTimeManagerDate =
            DatabaseHelper.instance.getAfterMeasureTimeManagerDate(calendar, group.id)
        val groupName = group.name
        val d1 = calendar.get(Calendar.DAY_OF_MONTH)
        val d2 = afterMeasureTimeManagerDate.get(Calendar.DAY_OF_MONTH)
        if (d1 == d2) {
            val dateFormat = SimpleDateFormat("HH:mm")
            if (groupName.contains("]")) {
                val indexOf = groupName.indexOf("]")
                if (indexOf == groupName.length - 1) {
                    group.name =
                        "[今天${dateFormat.format(afterMeasureTimeManagerDate.time)}]" + groupName
                } else {
                    group.name =
                        "[今天${dateFormat.format(afterMeasureTimeManagerDate.time)}]" + groupName.substring(
                            groupName.indexOf("]") + 1
                        )
                }
            } else {
                group.name =
                    "[今天${dateFormat.format(afterMeasureTimeManagerDate.time)}]" + groupName
            }
        } else {
            if (groupName.contains("]")) {
                val indexOf = groupName.indexOf("]")
                if (indexOf == groupName.length - 1) {
                    group.name =
                        "[今天\uD83C\uDE1A]$groupName"
                } else {
                    group.name =
                        "[今天\uD83C\uDE1A]" + groupName.substring(
                            groupName.indexOf("]") + 1
                        )
                }
            } else {
                group.name =
                    "[今天\uD83C\uDE1A]$groupName"
            }
        }

    }
}