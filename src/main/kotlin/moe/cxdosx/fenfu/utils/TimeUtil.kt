package moe.cxdosx.fenfu.utils

import java.util.*

object TimeUtil {
    /**
     * 获取下一次消息发送时间
     * @param sendTime 发送时间，24小时制，样式类似于12/30意为12点30分
     */
    fun getNextTaskTime(sendTime: String): Date {
        val timeSplit = sendTime.split("/")
        val instance = Calendar.getInstance()
        when {
            timeSplit[0].toInt() < instance.get(Calendar.HOUR_OF_DAY) -> {
                //今天不发
                instance.add(Calendar.DAY_OF_MONTH, 1)
            }
            timeSplit[0].toInt() == instance.get(Calendar.HOUR_OF_DAY) -> {
                //小时相等，判断分钟
                if (timeSplit[1].toInt() <= instance.get(Calendar.MINUTE)) {
                    //今天不发
                    instance.add(Calendar.DAY_OF_MONTH, 1)
                }
            }
        }
        instance.set(Calendar.HOUR_OF_DAY, timeSplit[0].toInt())
        instance.set(Calendar.MINUTE, timeSplit[1].toInt())
        instance.set(Calendar.SECOND, 0)
        return instance.time
    }
}