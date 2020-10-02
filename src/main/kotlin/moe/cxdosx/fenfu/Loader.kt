package moe.cxdosx.fenfu

import moe.cxdosx.fenfu.config.BotConfig
import moe.cxdosx.fenfu.function.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.join

suspend fun main() {
    val bot = Bot(
        BotConfig.botQQ,
        BotConfig.botPwd
    ) {
        fileBasedDeviceInfo("device.json")
    }.alsoLogin()

    bot.apply {
        help()
        csm()
        queryLogs()
        queryHpsLogs()
        userBind()
        atBotLogs()
        market()
    }

    bot.join()
}