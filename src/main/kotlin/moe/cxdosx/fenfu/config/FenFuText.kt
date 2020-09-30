package moe.cxdosx.fenfu.config

import moe.cxdosx.fenfu.utils.DatabaseHelper
import java.lang.StringBuilder

object FenFuText {

    const val serverError = "前方区域繁忙，请稍后再试。"
    const val logsUserQueryEmpty = "没有找到相关光战的信息呢=_="
    const val randomFoodRegex = ".*(吃饭|吃什么|饿了|饭点).*"
    const val notFoundBindUser = "你还没有绑定角色，你可以使用!bind来绑定你的角色"
    val foodHelp = """
        吃什么使用帮助：
        你可以通过吃饭或者饿了之类的语句触发吃什么
        常规指令(*为可选)：
        !csm：吃什么
        !csm add 食物名：添加一个食物名到随机列表
        !csm fadd 食物名：强制添加一个食物(如遇列表内已存在同名食物将会被拒绝)
        !csm list：查看当前所有食物随机列表
    """.trimIndent()

    /**
     * 匹配正整数
     */
    const val matchNumber: String = "^[1-9]\\d*\$"


    fun randomFood(str: String): String {
        return "经过我精密的推算，你适合吃 $str"
    }

    fun logsHelp(): String {
        return """
            分福阶段性更新中，如遇问题请联系开发者->QQ 591701074
            使用查询命令必须使用!或者/开头
            其中*号为可填项
            查询某人的dps logs：!logs 用户名 服务器名* 副本名*
            查询某人的hps logs：!hps 用户名 服务器名* 副本名*
            查询自己的dps logs：!me 副本名*
            查询自己的hps logs：!mehps 副本名*
            =================================================
            logs功能已基本完成所有功能，你可以使用!bind来绑定自己的角色
            然后使用指令查询：!me 副本名*
        """.trimIndent()
    }

    fun bindHelp(): String {
        return """
        使用!bind来绑定你的角色！
        具体使用指令(*为选填)：
        !bind 角色名 区服名*
        当logs中没有重名玩家时无需填写区服名，绑定唯一角色
        换绑请再次使用此命令，会自动换绑
        解绑请使用!unbind
    """.trimIndent()
    }

    fun help(): String {
        return """
分福Ver2.0目前已开源，欢迎来点star！
https://github.com/cxDosx/FenFu
当前可用功能：
✨!logs
✨!hps
✨!bind
✨!unbind
✨!me
✨饿了别叫🐴
        """.trimIndent()
    }

    fun parseDataError(errMsg: String?): String {
        return "数据没办法处理了，请联系开发者T_T${if (errMsg.isNullOrEmpty()) "" else "\n$errMsg"}"
    }

    fun notFoundLogsData(userName: String, serverName: String, zoneId: Int): String {
        return "没有找到[$serverName]${userName}在${DatabaseHelper.instance.zoneIdQueryDungeonName(zoneId)}的相关副本logs记录"
    }

    fun queryDifficultIdError(): String {
        return "分福坏掉了QAQ\nErrMsg:Difficult Code request failed!"
    }

    fun notFoundAreaId(bossOrAreaName: String): String {
        return "没有找到有关于 $bossOrAreaName 的相关副本信息"
    }

    fun regexMatch(vararg command: String): String {
        val sb = StringBuilder()
        for (c in command) {
            sb.append("|(?:^|\n)!$c.*|(?:^|\n)/$c.*|(?:^|\n)！$c.*")
        }
        return sb.toString().substring(1)
    }

    fun unKnowLogsQuerySplit(value: String): String {
        return "没有找到和 $value 相关匹配的服务器或者副本\n请使用[!logs 用户名 服务器名 副本名]精确查询"
    }

    fun queryLogsMultiPlayer(value: String): String {
        return "找到多个与 $value 相关的玩家\n请使用[!logs 用户名 服务器名 副本名]精确查询"
    }

    fun bindUserMultiPlayer(value: String): String {
        return "找到多个与 $value 相关的玩家\n请使用[!bind 用户名 服务器名]精确绑定"
    }

    fun unKnowServerName(value: String): String {
        return "无法识别的服务器名称：$value"
    }
}