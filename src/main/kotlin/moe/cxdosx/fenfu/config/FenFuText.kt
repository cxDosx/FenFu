package moe.cxdosx.fenfu.config

import moe.cxdosx.fenfu.utils.DatabaseHelper
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.MessageChainBuilder

object FenFuText {

    const val fenfuErrorMsg = "FenFuError"

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


    fun randomFood(sender: Member, foodName: String): MessageChainBuilder {
        return MessageChainBuilder()
            .append("分福发动了“抽卡”。")
            .append("\n")
            .append("▷ 对")
            .append(At(sender))
            .append("的胃附加了“$foodName”的效果。")
    }

    val logsHelp = """
            使用查询命令必须使用!或者/开头
            其中*号为可填项
            查询某人的dps logs：!logs 用户名 服务器名* 副本名*
            查询某人的hps logs：!hps 用户名 服务器名* 副本名*
            查询自己的dps logs：!me 副本名*
            查询自己的hps logs：!mehps 副本名*
        """.trimIndent()

    val bindHelp = """
        【绑定角色】
        如：
        ✨!bind 丝瓜卡夫卡 拂晓之间
        如logs中没有重名玩家，区服可为空
        完成绑定后可用!me便捷查询
        换绑：请再次使用此命令，将自动换绑
        解绑：请使用!unbind
    """.trimIndent()

    val marketHelp = """
        查询全服板子价格工具Market
        使用方法(*为选填，默认鸟区)：
        !market [物品名称] [服务器或大区名*]
        你可以在物品名称前后增加大小写不定的HQ或NQ来限制搜索结果
        使用示例：
        !market 黑天马 陆行鸟
        !mitem 邪龙怨影大剑
        !查价 卡部斯的肉hq
        命令头可用：
        !market
        !mitem
        !市场
        !查价
        !价格
        数据资源来自https://universalis.app/
        非官方数据源，价格存在延迟
        如果你也想贡献市场价格数据，请参照以下教程
        https://universalis.app/contribute
        https://ffcafe.org/matcha/universalis/
    """.trimIndent()

    val help = """
        分福Ver2.0目前已开源，欢迎来点star！
        https://github.com/cxDosx/FenFu
        当前可用功能：
        ✨!logs
        ✨!hps
        ✨!bind
        ✨!unbind
        ✨!me
        ✨!market
        ✨!title
        ✨饿了别叫🐴
        """.trimIndent()

    val titleHelp = """
        【称号查询】
        告诉分福你想了解的称号吧（更新至Patch5.25）
        如：
        ✨!title 樱花公主
        ✨!称号 征服者
         ※ 支持模糊查询
    """.trimIndent()

    fun parseDataError(errMsg: String?): String {
        return "数据没办法处理了，请联系开发者T_T${if (errMsg.isNullOrEmpty()) "" else "\n$errMsg"}"
    }

    fun notFoundLogsData(userName: String, serverName: String, zoneId: Int, hps: Boolean): String {
        return if (hps) {
            "没有找到[$serverName]${userName}在${DatabaseHelper.instance.zoneIdQueryDungeonName(zoneId)}的相关副本治疗职业的hps记录"
        } else {
            "没有找到[$serverName]${userName}在${DatabaseHelper.instance.zoneIdQueryDungeonName(zoneId)}的相关副本logs记录"
        }
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

    fun regexAnything(vararg command: String): String {
        val sb = StringBuilder()
        for (c in command) {
            sb.append("|$c")
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