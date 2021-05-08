package moe.cxdosx.fenfu.config

import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.MessageChainBuilder

object FenFuText {

    const val fenfuErrorMsg = "FenFuError"

    const val serverError = "前方区域繁忙，请稍后再试。"
    const val logsUserQueryEmpty = "゛﹎碴璑泚亽つ。※"
    const val randomFoodRegex = ".*(吃饭|吃什么|饿了|饭点).*"
    const val notFoundBindUser = "诶？还没绑呢，快使用!bind告诉分福你的ID和区服"
    const val notFoundTargetBindUser = "诶？对方还没绑呢，快让他使用!bind告诉分福ID和区服"
    val foodHelp = """
        【分福的美食大全】
        告诉分福你饿了，分福会为你精心推荐菜品^^
        
        相关指令：
        ✨!csm add 食物名：添加一个食物名到随机列表
        ✨!csm fadd 食物名：强制添加一个食物(如遇列表内已存在同名食物将会被拒绝)
        ✨!csm list：查看当前所有食物随机列表
    """.trimIndent()

    val itHelp = """
        分福查询指定已绑定角色的logs
        如要查询指定副本，副本名前需要多打一个空格
        
        ✨!it @对方 [副本名可选]
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
            查询自己的hps logs：!hpsme 副本名*
            ※ hps为治疗职业限定
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
        【现已支持批量查询，请用分号分割物品名称】
        使用方法(*为选填，默认鸟区)：
        !market 物品名称 服务器或大区名*
        你可以在物品名称前后增加大小写不定的HQ或NQ来限制搜索结果
        使用示例：
        !market 黑天马 陆行鸟
        !mitem 邪龙怨影大剑
        !查价 卡部斯的肉hq
        批量查询：
        !market 翠辰砂；黑天马；3级；烟熏；鲱鱼派hq；桦木 莫古力
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
        ✨!it
        ✨!market
        ✨!title
        ✨!gold
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

    val blackListPlayer = """
        您的账号已被分福封禁，无法使用分福相关功能
        如有疑问或需要申诉，请发送邮件到以下任一邮箱
        master@cxdosx.moe
        fenfu@cxdosx.moe
    """.trimIndent()

    val goldPriceHelp = """
        【金价查询】
        本功能可能会涉及部分违反游戏协议的行为
        分福只提供解析展示数据，不做其他相关操作
        如因违反规定被官方封禁分福不负任何责任
        分福同时会封禁恶意使用本功能破坏游戏环境的玩家
        你每一次使用本功能查询的行为都将被记录在案，请悉知
        使用方法：
        ✨!gold 18t 陆行鸟
        ✨!gold 300w
        ✨!gold 鸟
        
        ※注：查金转糖时，只允许输入万单位
    """.trimIndent()

    val goldPriceError = """
        无效的金额
    """.trimIndent()

    fun parseDataError(errMsg: String?): String {
        return "连接分福数据发生了错误（2002）${if (errMsg.isNullOrEmpty()) "" else "\n$errMsg"}"
    }

    fun notFoundLogsData(): String {
        return "这个人很懒，什么都没有留下。"
    }

    fun queryDifficultIdError(): String {
        return "分福陷入了“濒死”效果，身体很虚弱，力量、灵巧、智力、精神均降低50%。\nErrMsg:Difficult Code request failed!"
    }

    fun notFoundAreaId(): String {
        return "分福没听说过有这个副本"
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

    fun unKnowLogsQuerySplit(): String {
        return "分福没听说过有这个服务器或副本"
    }

    fun queryLogsMultiPlayer(value: String): String {
//        return "找到多个与 $value 相关的玩家\n请使用[!logs 用户名 服务器名 副本名]精确查询"
        return "呜呜，“${value}”太多了！分福不知道应该是哪个玩家\n请加上区服后重新查询"
    }

    fun bindUserMultiPlayer(value: String): String {
        return "呜呜，“${value}”太多了！分福找不到你\n请加上区服重新绑定"
    }

    fun unKnowServerName(): String {
        return "分福没听说过有这个服务器"
    }
}