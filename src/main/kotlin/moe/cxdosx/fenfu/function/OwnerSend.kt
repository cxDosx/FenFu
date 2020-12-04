package moe.cxdosx.fenfu.function

import moe.cxdosx.fenfu.config.BotConfig
import moe.cxdosx.fenfu.utils.DatabaseHelper
import moe.cxdosx.fenfu.utils.WeiboUpdateManager
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeFriendMessages

fun Bot.ownerSend() {
    subscribeFriendMessages {
        startsWith("sendall", trim = true) {
            if (sender.id == BotConfig.ownerQQ) {
                val msg = it.trim()
                groups.forEach { group ->
                    group.sendMessage(msg)
                }
            }
        }

        contains("reloadTimedTask", true) {
            if (sender.id == BotConfig.ownerQQ) {
                TimedTask.initTimedTask(bot)
                reply(
                    "已重载，共${TimedTask.taskSize()}个事件"
                )
            }
        }

        contains("checkweibo", true) {
            if (sender.id == BotConfig.ownerQQ) {
                DatabaseHelper.instance.getAllWeiboUpdateUid().forEach {
                    WeiboAutoUpdate.getAllWeiboText(it, true)
                }
            }
        }

        contains("reloadWeibo", true) {
            if (WeiboUpdateManager.weiboTimer == null) {
                WeiboUpdateManager.initWeiboUpdate()
            } else {
                WeiboUpdateManager.stopWeiboAutoUpdate()
                WeiboUpdateManager.initWeiboUpdate()
            }
        }

        contains("stopweibo", true) {
            WeiboUpdateManager.stopWeiboAutoUpdate()
        }
    }
}