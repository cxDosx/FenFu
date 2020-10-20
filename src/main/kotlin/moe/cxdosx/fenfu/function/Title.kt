package moe.cxdosx.fenfu.function

import moe.cxdosx.fenfu.config.FenFuText
import moe.cxdosx.fenfu.config.checkBlackList
import moe.cxdosx.fenfu.utils.DatabaseHelper
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Face

/**
 * 称号查询系统
 */
fun Bot.title() {
    subscribeGroupMessages {
        Regex(FenFuText.regexMatch("title", "称号"), RegexOption.IGNORE_CASE) matching regex@{
            val checkBlackList = checkBlackList()
            if (checkBlackList) {
                return@regex
            }
            val msg = it.replace(" +", " ").trim()//防止憨批打两个空格
            if (msg.contains(" ")) {
                val split = msg.split(" ")
                if (split.size >= 2) {
                    val queryTitle = msg.substring(msg.indexOf(" ")).trim()
                    val titleBean = DatabaseHelper.instance.queryTitle(queryTitle)
                    if (titleBean == null) {
                        reply(
                            At(sender) + "\n没有找到有关${queryTitle}的称号"
                        )
                    } else {
                        var messageChain = At(sender) + "\n"
                        if (titleBean.titleName.contains("/")) {
                            val titleNameSplit = titleBean.titleName.split("/")
                            for ((index, element) in titleNameSplit.withIndex()) {
                                messageChain = if (index % 2 == 0) {
                                    messageChain.plus("\uD83D\uDC66\uD83C\uDFFB") //Boy
                                } else {
                                    messageChain.plus("\uD83D\uDC67\uD83C\uDFFB") //Girl
                                }
                                messageChain = messageChain.plus("<‹${element.trim()}›>")
                                if (index != titleNameSplit.size - 1) {
                                    messageChain = messageChain.plus("｜")
                                }
                            }
                            messageChain = messageChain
                                .plus("\n")
                        } else {
                            messageChain = messageChain
                                .plus("<‹${titleBean.titleName}›>")
                                .plus("\n")
                        }
                        if (titleBean.oop) {
                            messageChain = messageChain
                                .plus("※ 该称号已绝版！")
                                .plus(Face(Face.kuaikule))
                                .plus("\n")
                        }
                        messageChain = messageChain
                            .plus("——————————\n")
                            .plus("关联成就：${titleBean.achievement}")
                            .plus("\n")
                            .plus("获取途径：")
                        messageChain = if (titleBean.desc.isEmpty()) {
                            messageChain.plus(titleBean.descAll)
                        } else {
                            messageChain.plus(titleBean.desc)
                        }
                        if (titleBean.aboutLink.isNotEmpty()) {
                            messageChain = messageChain
                                .plus("\n")
                                .plus("相关链接：")
                                .plus(titleBean.aboutLink)
                        }
                        reply(
                            messageChain
                        )
                    }
                } else {
                    reply(
                        At(sender) + "\n${FenFuText.titleHelp}"
                    )
                }
            } else {
                reply(
                    At(sender) + "\n${FenFuText.titleHelp}"
                )
            }
        }
    }
}