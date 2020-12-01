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
        ownerSend()

        help()
        csm() //今天吃什么
        queryLogs() //logs查询
        queryHpsLogs() //hps logs查询
        userBind() //用户角色绑定
        atBotLogs() //logs多项选择
        market() //市场查价
        title() //称号查询
        ping()
        //weiboAutoUpdate()
        timeManager()
        goldPrice()

        TimedTask.initTimedTask(this)
    }

    bot.join()
}