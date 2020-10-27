package moe.cxdosx.fenfu.function

import kotlinx.coroutines.launch
import moe.cxdosx.fenfu.data.beans.TimedTaskSendBean
import moe.cxdosx.fenfu.utils.DatabaseHelper
import moe.cxdosx.fenfu.utils.TimeUtil
import net.mamoe.mirai.Bot
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

fun Bot.timedTask(sendBean: TimedTaskSendBean): Timer {

    return Timer().apply {
        schedule(TimeUtil.getNextTaskTime(sendBean.startTime, sendBean.sendTime), TimeUnit.DAYS.toMillis(1)) {
            if (sendBean.endTime.time <= System.currentTimeMillis()) {
                cancel()
                return@schedule
            }
            launch {
                groups.forEach {
                    if (sendBean.sendGroup.isEmpty() || sendBean.sendGroup.contains(it.id)) {
                        it.sendMessage(sendBean.sendContent)
                    }
                }
            }
        }
    }
}

object TimedTask {
    private val taskList = ArrayList<Timer>()

    fun initTimedTask(bot: Bot) {
        if (taskList.isNotEmpty()) {
            taskList.forEach {
                it.cancel()
            }
            taskList.clear()
        }
        val allTimedTask = DatabaseHelper.instance.getAllTimedTask()
        allTimedTask.forEach {
            taskList.add(bot.timedTask(it))
        }
        bot.logger.info("[TimedTask]当前共${allTimedTask.size}个定时时间")
    }

    fun taskSize(): Int {
        return taskList.size
    }
}