package moe.cxdosx.fenfu.function

import moe.cxdosx.fenfu.config.FenFuText
import moe.cxdosx.fenfu.utils.DatabaseHelper
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.At

fun Bot.csm() {
    subscribeGroupMessages {
        /**
         * 随机吃
         */
        Regex(FenFuText.randomFoodRegex, RegexOption.IGNORE_CASE) matching regex@{
            if (sender.id != bot.id) {
                reply(FenFuText.randomFood(sender, DatabaseHelper.instance.getRandomFood()).asMessageChain())
            }
        }

        /**
         * csm 系列命令
         */
        Regex(FenFuText.regexMatch("csm"), RegexOption.IGNORE_CASE) matching regex@{
            val msg = it.replace(" +", " ").trim()//防止憨批打两个空格
            if (msg.contains(" ")) {
                val split = msg.split(" ")
                if (split.size == 2 && split[1].toLowerCase() == "list") {
                    reply(At(sender) + "\n当前随机列表含有：\n${DatabaseHelper.instance.outputAllFoodName()}")
                } else if (split.size >= 3 && split[1].toLowerCase() == "add") {
                    val foodName = msg.substring(msg.indexOf(" ")).trim()
                    if (foodName.contains("食物名")) {
                        reply(At(sender) + "\n?¿?¿?¿?¿?¿?¿?¿?¿?¿?¿?¿?¿?¿?¿?¿?¿?¿?¿?¿?¿")
                        return@regex
                    }
                    val checkFood = DatabaseHelper.instance.checkFoodLike(foodName.trim())
                    if (checkFood.isEmpty()) {
                        DatabaseHelper.instance.addFood(foodName)
                        reply(At(sender) + "\n食物名：$foodName 添加成功")
                    } else {
                        reply(At(sender) + "\n已存在有类似的食物名：$checkFood")
                    }
                } else if (split.size >= 3 && split[1].toLowerCase() == "fadd") {
                    val foodName = msg.substring(msg.indexOf(" ")).trim()
                    if (foodName.contains("食物名")) {
                        reply(At(sender) + "\n?¿?¿?¿?¿?¿?¿?¿?¿?¿?¿?¿?¿?¿?¿?¿?¿?¿?¿?¿?¿")
                        return@regex
                    }
                    val checkFoodEq = DatabaseHelper.instance.checkFoodEqual(foodName.trim())
                    if (checkFoodEq) {
                        reply(At(sender) + "\n已存在有一毛一样的食物名，拒绝重复添加")
                    } else {
                        DatabaseHelper.instance.addFood(foodName)
                        reply(At(sender) + "\n食物名：$foodName 添加成功")
                    }
                } else {
                    reply(At(sender) + "\n" + FenFuText.foodHelp)
                }
            } else {
                if (sender.id != bot.id) {
                    reply(FenFuText.randomFood(sender, DatabaseHelper.instance.getRandomFood()).asMessageChain())
                }
            }
        }
    }
}