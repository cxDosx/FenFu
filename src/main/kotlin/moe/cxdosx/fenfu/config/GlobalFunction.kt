package moe.cxdosx.fenfu.config

import moe.cxdosx.fenfu.utils.DatabaseHelper
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.data.At

suspend fun GroupMessageEvent.checkBlackList(): Boolean {
    return checkBlackList(true)
}

suspend fun GroupMessageEvent.checkBlackList(reply: Boolean): Boolean {
    val block = DatabaseHelper.instance.checkBlackList(this.sender.id)
    if (block && reply) {
        reply(
            At(sender) + "\n${FenFuText.blackListPlayer}"
        )
    }
    return block
}