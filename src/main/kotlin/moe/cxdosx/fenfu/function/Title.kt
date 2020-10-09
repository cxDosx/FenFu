package moe.cxdosx.fenfu.function

import moe.cxdosx.fenfu.config.FenFuText
import moe.cxdosx.fenfu.utils.DatabaseHelper
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.At

/**
 * 称号查询系统
 */
fun Bot.title() {
    subscribeGroupMessages {
        Regex(FenFuText.regexMatch("title", "称号"), RegexOption.IGNORE_CASE) matching regex@{
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
                        val sb = StringBuffer()
                        if (titleBean.titleName.contains("/")){
                            val titleNameSplit = titleBean.titleName.split("/")
                            for ((index, element) in titleNameSplit.withIndex()){
                                if (index % 2 == 0){
                                    sb.append("\uD83D\uDC66\uD83C\uDFFB") //Boy
                                } else{
                                    sb.append("\uD83D\uDC67\uD83C\uDFFB") //Girl
                                }
                                sb.append("[${element.trim()}]")
                                sb.append("\n")
                            }
                        } else{
                            sb.append("[${titleBean.titleName}]").append("\n")
                        }
                        sb.append("关联成就：${titleBean.achievement}").append("\n")
                        if (titleBean.oop){
                            sb.append("==该称号已绝版==").append("\n")
                        }
                        sb.append("获取途径：")
                        if (titleBean.desc.isEmpty()){
                            sb.append(titleBean.descAll)
                        } else{
                            sb.append(titleBean.desc)
                        }
                        if (titleBean.aboutLink.isNotEmpty()){
                            sb.append("\n")
                            sb.append("相关链接：").append(titleBean.aboutLink)
                        }
                        reply(
                            At(sender) + "\n$sb"
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