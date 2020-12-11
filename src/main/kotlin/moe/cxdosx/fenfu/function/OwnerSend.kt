package moe.cxdosx.fenfu.function

import moe.cxdosx.fenfu.config.FenFuText
import moe.cxdosx.fenfu.utils.DatabaseHelper
import moe.cxdosx.fenfu.utils.PermissionUtils.checkAdminPermission
import moe.cxdosx.fenfu.utils.WeiboUpdateManager
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeFriendMessages
import net.mamoe.mirai.event.subscribeGroupMessages

fun Bot.ownerSend() {
    subscribeFriendMessages {
        startsWith("sendall", trim = true) {
            if (sender.id.checkAdminPermission()) {
                val msg = it.trim()
                groups.forEach { group ->
                    group.sendMessage(msg)
                }
            }
        }
        startsWith("send2", trim = true) {
            if (sender.id.checkAdminPermission()) {
                if (it.contains(" ")) {
                    val split = it.split(" ")
                    val targetGroup = split[0].toLong()
                    if (targetGroup == 0L) {
                        return@startsWith
                    }
                    val msg = it.substring(it.indexOf(" ")).trim()
                    groups.forEach { group ->
                        if (group.id == targetGroup) {
                            group.sendMessage(msg)
                        }
                    }
                }
            }
        }

        contains("reloadTimedTask", true) {
            if (sender.id.checkAdminPermission()) {
                TimedTask.initTimedTask(bot)
                reply(
                    "已重载，共${TimedTask.taskSize()}个事件"
                )
            }
        }

        contains("checkweibo", true) {
            if (sender.id.checkAdminPermission()) {
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

    subscribeGroupMessages {
        Regex(FenFuText.regexMatch("weibosub"), RegexOption.IGNORE_CASE) matching regex@{
            if (sender.id.checkAdminPermission()) {
                val msg = it.replace(" +", " ").trim().split(" ")//防止我自己憨批打两个空格
                if (msg.size == 2) {
                    val uid: Long = msg[1].toLong()
                    if (uid != 0L) {
                        DatabaseHelper.instance.markWeiboUpdateGroup(this.group.id, uid)
                        reply(
                            "当前群组订阅微博推送$uid"
                        )
                    }
                }
            }
        }

        Regex(FenFuText.regexMatch("weibounsub"), RegexOption.IGNORE_CASE) matching regex@{
            if (sender.id.checkAdminPermission()) {
                val msg = it.replace(" +", " ").trim().split(" ")//防止我自己憨批打两个空格
                if (msg.size == 2) {
                    val uid: Long = msg[1].toLong()
                    if (uid != 0L) {
                        DatabaseHelper.instance.removeWeiboUpdate(group.id, uid)
                        reply(
                            "当前群组取消推送$uid"
                        )
                    }
                } else {
                    DatabaseHelper.instance.removeWeiboUpdate(group.id, 0L)
                    reply(
                        "当前群组取消所有推送"
                    )
                }
            }
        }
    }
}