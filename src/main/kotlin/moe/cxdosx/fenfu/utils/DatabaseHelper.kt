package moe.cxdosx.fenfu.utils

import moe.cxdosx.fenfu.config.BotConfig
import moe.cxdosx.fenfu.config.FenFuText
import moe.cxdosx.fenfu.data.beans.LogsUser
import moe.cxdosx.fenfu.data.beans.TimedTaskSendBean
import moe.cxdosx.fenfu.data.beans.TitleBean
import moe.cxdosx.fenfu.data.beans.UserBanBean
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.data.content
import java.sql.Connection
import java.sql.Date
import java.sql.DriverManager
import java.sql.Statement
import java.util.*
import kotlin.collections.ArrayList

class DatabaseHelper {
    companion object {
        const val JDBC_DRIVER = "com.mysql.jdbc.Driver"
        const val MYSQL_USER_NAME = BotConfig.dbUserName
        const val MYSQL_PWD = BotConfig.dbPwd
        const val JDBC_MYSQL_URL = BotConfig.jdbcUrl

        val instance: DatabaseHelper by lazy { DatabaseHelper() }

        const val LuXingNiao = "LuXingNiao"
        const val MoGuLi = "MoGuLi"
        const val MaoXiaoPang = "MaoXiaoPang"
    }

    enum class JobType(var typeId: Int) {
        TANK(0),
        HEALER(1),
        DPS(2)
    }

    private var conn: Connection
    private var stmt: Statement
        get() {
            conn = DriverManager.getConnection(JDBC_MYSQL_URL, MYSQL_USER_NAME, MYSQL_PWD)
            field = conn.createStatement()
            return field
        }

    init {
        Class.forName(JDBC_DRIVER)

        conn = DriverManager.getConnection(JDBC_MYSQL_URL, MYSQL_USER_NAME, MYSQL_PWD)
        stmt = conn.createStatement()
    }


    /**
     * 随机获取吃什么
     */
    fun getRandomFood(): String {
        val sql = "SELECT foodName FROM foods WHERE isEnable = 1 ORDER BY RAND() LIMIT 1"
        val executeQuery = stmt.executeQuery(sql)
        if (executeQuery.next()) {
            return executeQuery.getString("foodName")
        }
        return ""
    }

    fun logsQueryRegion(serverName: String): String {
        val sql = "SELECT serverRegion FROM ffxiv_server_info WHERE serverName = '$serverName'"
        val executeQuery = stmt.executeQuery(sql)
        if (executeQuery.next()) {
            return executeQuery.getString("serverRegion")
        }
        return ""
    }

    /**
     * 模糊获取服务器名
     */
    fun queryIntactServerName(likeName: String): String {
        val sql = "SELECT serverName FROM ffxiv_server_info WHERE serverName LIKE '%$likeName%'"
        val executeQuery = stmt.executeQuery(sql)
        if (executeQuery.next()) {
            return executeQuery.getString("serverName")
        }
        return ""
    }


    /**
     * 获取服务器的英文名称
     * 主要用于中文服务器转英文
     */
    fun queryENServerName(serverName: String): String {
        var serverEN = ""
        if (Regex(FenFuText.regexAnything("鸟", "一区", "陆行鸟"), RegexOption.IGNORE_CASE) matches serverName) {
            serverEN = LuXingNiao
        } else if (Regex(
                FenFuText.regexAnything("猪", "二区", "莫古力", "蘑菇力"),
                RegexOption.IGNORE_CASE
            ) matches serverName
        ) {
            serverEN = MoGuLi
        } else if (Regex(FenFuText.regexAnything("猫", "三区", "猫小胖"), RegexOption.IGNORE_CASE) matches serverName) {
            serverEN = MaoXiaoPang
        } else {
            val sql = "SELECT serverNameEN FROM ffxiv_server_info WHERE serverName LIKE '%$serverName%'"
            val executeQuery = stmt.executeQuery(sql)
            if (executeQuery.next()) {
                return executeQuery.getString("serverNameEN")
            }
        }
        return serverEN
    }

    /**
     * 获取服务器中文名
     */
    fun queryCNServerName(en: String): String {
        return when (en) {
            LuXingNiao -> {
                "陆行鸟"
            }
            MoGuLi -> {
                "莫古力"
            }
            MaoXiaoPang -> {
                "猫小胖"
            }
            else -> {
                val sql = "SELECT serverName FROM ffxiv_server_info WHERE serverNameEN = '$en'"
                val executeQuery = stmt.executeQuery(sql)
                if (executeQuery.next()) {
                    executeQuery.getString("serverName")
                } else {
                    en
                }
            }
        }
    }

    enum class LogsQueryType {
        ADPS,
        RDPS,
        HPS
    }

    fun getZoneQueryType(zoneId: Int): LogsQueryType {
        val sql = "SELECT requestType FROM ffxiv_boss_info WHERE areaId = $zoneId limit 1"
        val executeQuery = stmt.executeQuery(sql)
        if (executeQuery.next()) {
            return if (executeQuery.getInt("requestType") == 1) {
                LogsQueryType.RDPS
            } else {
                LogsQueryType.ADPS
            }
        }
        return LogsQueryType.RDPS
    }

    fun zoneIdQueryDungeonName(zoneId: Int): String {
        val sql = "SELECT dungeonName FROM ffxiv_boss_info WHERE areaId = $zoneId limit 1"
        val executeQuery = stmt.executeQuery(sql)
        if (executeQuery.next()) {
            return executeQuery.getString("dungeonName")
        }
        return "AreaId=$zoneId"
    }

    fun engBossNameConvertChn(engBossName: String): String {
        val sql = "SELECT bossNameCN FROM ffxiv_boss_info WHERE bossName = '${engBossName.replace("'", "\\'")}'"
        val executeQuery = stmt.executeQuery(sql)
        if (executeQuery.next()) {
            return executeQuery.getString("bossNameCN")
        }
        return engBossName
    }

    fun getDungeonDifficult(zoneId: Int): Int {
        val sql = "SELECT extremeDifficult FROM ffxiv_boss_info WHERE areaId = $zoneId limit 1"
        val executeQuery = stmt.executeQuery(sql)
        if (executeQuery.next()) {
            return executeQuery.getInt("extremeDifficult")
        }
        return -1
    }

    fun getDefaultQueryZone(): Int {
        val sql = "SELECT areaId FROM ffxiv_boss_info WHERE defaultQuery = 1"
        val executeQuery = stmt.executeQuery(sql)
        if (executeQuery.next()) {
            return executeQuery.getInt("areaId")
        }
        return -1
    }

    fun bossNameQueryZone(name: String): Int {
        name.trim()
        if (name.isEmpty()) {
            return -1
        }
        val sql = "SELECT areaId FROM ffxiv_boss_info WHERE likeName LIKE '%$name%' OR bossNameCN = '$name' limit 1"
        val executeQuery = stmt.executeQuery(sql)
        if (executeQuery.next()) {
            return executeQuery.getInt("areaId")
        }
        return -1
    }

    /**
     * 查询QQ绑定的角色
     * @return 返回角色实体，如未绑定返回null
     */
    fun queryBindUser(qq: Long): LogsUser? {
        val sql = "SELECT userName,serverName FROM ffxiv_userbind WHERE ownerQQ = $qq limit 1"
        val executeQuery = stmt.executeQuery(sql)
        if (executeQuery.next()) {
            return LogsUser(executeQuery.getString("userName"), executeQuery.getString("serverName"))
        }
        return null
    }

    fun jobIsHealer(jobName: String): Boolean {
        val sql = "SELECT jobType FROM ffxiv_job WHERE jobNameCN = '$jobName' OR jobNameEN = '$jobName'"
        val executeQuery = stmt.executeQuery(sql)
        if (executeQuery.next()) {
            return executeQuery.getInt("jobType") == JobType.HEALER.typeId
        }
        return false
    }

    /**
     * 服务器名必须为标准服务器名
     * @return 是否成功
     */
    fun updateUserBindData(userName: String, server: String, ownerQQ: Long): Boolean {
        val sql = "UPDATE ffxiv_userbind SET userName='$userName', serverName='$server' WHERE ownerQQ=$ownerQQ"
        val executeQuery = stmt.executeUpdate(sql)
        return executeQuery != 0
    }

    fun insertUserBindData(userName: String, server: String, ownerQQ: Long) {
        val sql = "INSERT INTO ffxiv_userbind (userName, serverName, ownerQQ)VALUES ('$userName','$server',$ownerQQ);"
        stmt.execute(sql)
    }

    fun deleteBindUser(ownerQQ: Long) {
        val sql = "DELETE FROM ffxiv_userbind WHERE ownerQQ = $ownerQQ LIMIT 1"
        stmt.execute(sql)
    }

    fun checkFoodLike(checkName: String): String {
        val sql = "SELECT foodName FROM foods WHERE foodName like '%$checkName%'"
        val executeQuery = stmt.executeQuery(sql)
        var likeFood = ""
        while (executeQuery.next()) {
            likeFood += "${executeQuery.getString("foodName")} "
        }
        return likeFood.trim()
    }

    fun checkFoodEqual(checkName: String): Boolean {
        val sql = "SELECT foodName FROM foods WHERE foodName = '$checkName' limit 1"
        val executeQuery = stmt.executeQuery(sql)
        if (executeQuery.next()) {
            return true
        }
        return false
    }

    fun addFood(foodName: String) {
        val sql = "INSERT INTO foods (foodName, isEnable)VALUES ('$foodName','1');"
        stmt.execute(sql)
    }

    fun outputAllFoodName(): String {
        val sql = "SELECT foodName FROM foods WHERE isEnable = 1 order by rand() LIMIT 30"
        val executeQuery = stmt.executeQuery(sql)
        var o = ""
        while (executeQuery.next()) {
            o += "${executeQuery.getString("foodName")} "
        }
        return o.trim().plus(" 等共计${getAllRandomSize()}个食物名")
    }

    fun getAllRandomSize(): Int {
        val sql = "SELECT COUNT(foodName) FROM foods"
        val rs = stmt.executeQuery(sql)
        return rs.getInt(0)
    }

    fun queryTitle(likeName: String): TitleBean? {
        val sql = "SELECT * FROM ffxiv_title WHERE `name` LIKE '%$likeName%' LIMIT 1"
        val executeQuery = stmt.executeQuery(sql)
        while (executeQuery.next()) {
            return TitleBean(
                executeQuery.getString("name"),
                executeQuery.getString("achievement"),
                executeQuery.getString("desc"),
                executeQuery.getString("desc_all"),
                executeQuery.getInt("oop") == 1,
                executeQuery.getNString("aboutLink") ?: "",
            )
        }
        return null
    }


    /**
     * 查询封禁
     */
    fun checkBan(userName: String, serverName: String): UserBanBean? {
        val sql = "SELECT * FROM ffxiv_ban WHERE `name` = '$userName' AND `server` = '$serverName'"
        val executeQuery = stmt.executeQuery(sql)
        var banBean: UserBanBean? = null
        var count = 0
        while (executeQuery.next()) {
            count++
        }
        if (count != 0) {
            executeQuery.first()
            banBean = UserBanBean(
                executeQuery.getString("name"),
                executeQuery.getString("server"),
                executeQuery.getString("reason"),
                executeQuery.getString("source"),
                executeQuery.getString("time"),
                executeQuery.getString("id"),
                count
            )
        }
        return banBean
    }

    fun getAllTimedTask(): List<TimedTaskSendBean> {
        val sql = "SELECT * FROM ffxiv_timed_task WHERE available = 1 AND endTime > NOW()"
        val executeQuery = stmt.executeQuery(sql)
        val taskList = ArrayList<TimedTaskSendBean>()
        while (executeQuery.next()) {
            val sendGroup = executeQuery.getString("sendGroup").trim()
            val sendGroupList = ArrayList<Long>()
            if (sendGroup.isNotEmpty()) {
                if (sendGroup.contains(",")) {
                    val split = sendGroup.split(",")
                    if (split.isNotEmpty()) {
                        split.forEach {
                            sendGroupList.add(it.toLong())
                        }
                    }
                } else {
                    sendGroupList.add(sendGroup.toLong())
                }
            }
            taskList.add(
                TimedTaskSendBean(
                    executeQuery.getString("content"),
                    Date(executeQuery.getTimestamp("startTime").time),
                    Date(executeQuery.getTimestamp("endTime").time),
                    executeQuery.getString("sendTime"),
                    sendGroupList
                )
            )

        }
        return taskList.toList()
    }

    /**
     * 询Bot黑名单
     * @param qq QQ号码
     */
    fun checkBlackList(qq: Long): Boolean {
        if (qq == 0L) {
            return false
        }
        val sql = "SELECT * FROM blacklist WHERE qq = '$qq'"
        val executeQuery = stmt.executeQuery(sql)
        return executeQuery.next()
    }

    /**
     * 询Bot黑名单
     */
    fun checkBlackList(userName: String, serverName: String): Boolean {
        if (userName.isEmpty()) {
            return false
        }
        val sql = "SELECT * FROM blacklist WHERE username = '$userName' AND serverName = '$serverName'"
        val executeQuery = stmt.executeQuery(sql)
        return executeQuery.next()
    }

    fun getAllWeiboUpdateUid(): List<String> {
        val sql = "SELECT distinct weiboUID FROM weibo_auto_update"
        val executeQuery = stmt.executeQuery(sql)
        val result = ArrayList<String>()
        while (executeQuery.next()) {
            result.add(executeQuery.getString("weiboUID"))
        }
        return result
    }

    /**
     * 检查是否发送过微博
     * @param wbId weibo唯一标识ID
     *
     * @return 是否已发送过
     */
    fun checkSentWeibo(wbId: String): Boolean {
        val sql = "SELECT wbId FROM weibo_history WHERE wbId = $wbId LIMIT 1"
        val executeQuery = stmt.executeQuery(sql)
        return executeQuery.next()
    }

    /**
     * 获取所有订阅了这个微博id的群组
     * @param userId 微博userId
     */
    fun getAllSubscribeWeiboIdGroups(userId: String): List<String> {
        val sql = "SELECT `group` FROM weibo_auto_update WHERE weiboUID = $userId"
        val executeQuery = stmt.executeQuery(sql)
        val result = ArrayList<String>()
        while (executeQuery.next()) {
            result.add(executeQuery.getString("group"))
        }
        return result
    }

    fun saveWeiboSentHistoryId(wbId: String) {
        val sql = "INSERT INTO `weibo_history`(`wbId`) VALUES ('${wbId}')"
        stmt.execute(sql)
    }

    fun isTimeManagerGroup(groupId: Long): Boolean {
        val sql = "SELECT * FROM `time_manager_group` WHERE `group` = $groupId LIMIT 1"
        val executeQuery = stmt.executeQuery(sql)
        return executeQuery.next()
    }

    fun getTimeManagerStartTime(groupId: Long): Date? {
        val sql = "SELECT `start_time` FROM `time_manager_group` WHERE `group` = $groupId LIMIT 1"
        val executeQuery = stmt.executeQuery(sql)
        return if (executeQuery.next()) {
            Date(executeQuery.getTimestamp("start_time").time)
        } else {
            null
        }
    }

    fun markTimeManagerLog(groupId: Long, userId: Long, timeChange: Int) {
        val sql =
            "INSERT INTO `time_manager_logs`(`group`, `user`, `time_change`) VALUES ($groupId, $userId, $timeChange)"
        stmt.execute(sql)
    }

    fun getAfterMeasureTimeManagerDate(calendar: Calendar, groupId: Long): Calendar {
        val sql = "SELECT time_change FROM time_manager_logs WHERE `group` = $groupId"
        val executeQuery = stmt.executeQuery(sql)
        val currentCalendar = Calendar.getInstance()
        currentCalendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE))
        currentCalendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY))
        currentCalendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND))
        while (executeQuery.next()) {
            val int = executeQuery.getInt("time_change")
            currentCalendar.add(Calendar.MINUTE, int)
        }
        return currentCalendar
    }

    fun deleteTimeManagerLog(targetGroup: Long) {
        val sql = "DELETE FROM `time_manager_logs` WHERE `group` = $targetGroup"
        stmt.execute(sql)
    }

    fun markGoldPriceLog(groupMessageEvent: GroupMessageEvent) {
        val sql =
            "INSERT INTO gold_price_log(`qq`, `command`, `time`) " +
                    "VALUES ('${groupMessageEvent.sender.id}', '${groupMessageEvent.message.content}', NOW())"
        stmt.execute(sql)
    }

    fun markWeiboUpdateGroup(groupId: Long, weiboUid: Long) {
        if (groupId != 0L && weiboUid != 0L) {
            val sql = "INSERT INTO `weibo_auto_update`(`group`, `weiboUID`) VALUES ('$groupId', '$weiboUid');"
            stmt.execute(sql)
        }
    }

    fun removeWeiboUpdate(groupId: Long, weiboUid: Long) {
        if (groupId != 0L) {
            val sql = "DELETE FROM weibo_auto_update WHERE `group` = $groupId"
            if (weiboUid != 0L) {
                stmt.execute(sql.plus(" AND `weiboUID` = $weiboUid"))
            } else {
                stmt.execute(sql)
            }
        }
    }
}