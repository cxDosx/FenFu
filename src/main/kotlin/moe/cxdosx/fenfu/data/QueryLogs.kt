package moe.cxdosx.fenfu.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import moe.cxdosx.fenfu.config.BotConfig
import moe.cxdosx.fenfu.config.FenFuText
import moe.cxdosx.fenfu.data.beans.LogsIdle
import moe.cxdosx.fenfu.data.beans.LogsIdleQueue
import moe.cxdosx.fenfu.data.beans.LogsUser
import moe.cxdosx.fenfu.utils.DatabaseHelper
import moe.cxdosx.fenfu.utils.HttpUtil
import moe.cxdosx.fenfu.utils.LogsUtil
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import okhttp3.Request
import org.example.myplugin.beans.LogsDataQuery
import org.example.myplugin.beans.LogsUserQuery
import java.net.URLEncoder


class QueryLogs {
    companion object {
        private const val API_KEY = BotConfig.ffLogsApiKey
        private val refererHeaderMap = HashMap<String, String>()

    }

    init {
        refererHeaderMap["Referer"] = "https://cn.fflogs.com/"
    }

    suspend fun queryUser(userName: String, groupMessageEvent: GroupMessageEvent) {
        queryUser(userName, groupMessageEvent, false)
    }

    suspend fun queryUser(userName: String, groupMessageEvent: GroupMessageEvent, isHps: Boolean) {
        val url = "https://cn.fflogs.com/search/autocomplete?term=${URLEncoder.encode(userName, "UTF-8")}"
        val request = Request.Builder()
            .url(url)
        for (key in refererHeaderMap.keys) {
            request.addHeader(key, refererHeaderMap[key].toString())
        }
        val execute = HttpUtil.client.newCall(request.build()).execute()
        if (execute.isSuccessful && execute.body != null) {
            val str = execute.body?.string()
            if (str.equals("[]")) {
                groupMessageEvent.reply(At(groupMessageEvent.sender) + "\n" + FenFuText.logsUserQueryEmpty)
                return
            }
            val users: ArrayList<LogsUserQuery> = try {
                Gson().fromJson(str, object : TypeToken<List<LogsUserQuery>>() {}.type)
            } catch (e: Exception) {
                groupMessageEvent.reply(FenFuText.parseDataError(e.localizedMessage + "\n$str"))
                return
            }
            val removeList = ArrayList<LogsUserQuery>()
            for (element in users) {
                if (element.type != "角色") {
                    removeList.add(element)
                    continue
                }
                if (!element.server.toLowerCase().contains("国服")) {
                    removeList.add(element)
                }
            }
            for (e in removeList) {
                users.remove(e)
            }
            when {
                users.isEmpty() -> {
                    groupMessageEvent.reply(At(groupMessageEvent.sender) + "\n" + FenFuText.logsUserQueryEmpty)
                    return /*FenFuText.logsUserQueryEmpty*/
                }
                users.size == 1 -> {
                    groupMessageEvent.reply(
                        queryLogsData(
                            users[0].label,
                            users[0].server.split("-")[1].trim(),
                            DatabaseHelper.instance.getDefaultQueryZone(),
                            isHps
                        )
                    )
                    return /*queryLogsData(
                            users[0].label,
                            users[0].server.split("-")[1].trim(),
                            DatabaseHelper.instance.getDefaultQueryZone()
                        )*/
                }
                else -> {
                    val stringBuilder = StringBuilder()
                    stringBuilder.append("找到多个角色，请在一分钟内回复序号选择\n[At,引用,只回复序号均可]")
                    val idleUsers = ArrayList<LogsUser>()
                    for ((index, element) in users.withIndex()) {
                        val server = if (element.server.contains("国服")) {
                            element.server.substring(element.server.lastIndexOf("-") + 1).trim()
                        } else {
                            element.server
                        }
                        idleUsers.add(LogsUser(element.label, server))
                        stringBuilder.append("\n${index + 1}.${element.label} - $server")
                    }
                    synchronized(LogsIdleQueue.idleQueue) {
                        LogsIdleQueue.idleQueue.add(LogsIdle(groupMessageEvent, idleUsers))
                    }
                    stringBuilder.append("\n你也可以使用[!logs 用户名 区服名 副本名]的方式来精准查询某用户的详细信息")
                    groupMessageEvent.reply(stringBuilder.toString())
                    return /*stringBuilder.toString()*/
                }
            }
        } else {
            groupMessageEvent.reply(FenFuText.serverError)
            return /*FenFuText.serverError*/
        }
    }

    /**
     * 寻找角色，但此方法返回的是角色对象的列表，如果只有一个，size=1
     * 如果数据异常，也只会返回一个，但是服务器名会为FenFuError
     */
    fun queryUser(userName: String): List<LogsUser> {
        val url = "https://cn.fflogs.com/search/autocomplete?term=${URLEncoder.encode(userName, "UTF-8")}"
        val request = Request.Builder()
            .url(url)
        for (key in refererHeaderMap.keys) {
            request.addHeader(key, refererHeaderMap[key].toString())
        }
        val execute = HttpUtil.client.newCall(request.build()).execute()
        if (execute.isSuccessful && execute.body != null) {
            val str = execute.body?.string()
            if (str.equals("[]")) {
                return ArrayList()
            }
            val users: ArrayList<LogsUserQuery> = try {
                Gson().fromJson(str, object : TypeToken<List<LogsUserQuery>>() {}.type)
            } catch (e: Exception) {
                return ArrayList()
            }
            val removeList = ArrayList<LogsUserQuery>()
            for (element in users) {
                if (element.type != "角色") {
                    removeList.add(element)
                    continue
                }
                if (!element.server.toLowerCase().contains("国服")) {
                    removeList.add(element)
                }
            }
            for (e in removeList) {
                users.remove(e)
            }
            val u = ArrayList<LogsUser>()
            when {
                users.isEmpty() -> {
                    return u
                }
                users.size == 1 -> {
                    u.add(LogsUser(users[0].label, users[0].server.split("-")[1].trim()))
                    return u
                }
                else -> {
                    val idleUsers = ArrayList<LogsUser>()
                    for (element in users) {
                        val server = if (element.server.contains("国服")) {
                            element.server.substring(element.server.lastIndexOf("-") + 1).trim()
                        } else {
                            element.server
                        }
                        idleUsers.add(LogsUser(element.label, server))
                    }
                    return idleUsers
                }
            }
        } else {
            val errorList = ArrayList<LogsUser>()
            val msg = if (execute.body != null) {
                execute.body!!.string()
            } else {
                execute.code.toString()
            }
            errorList.add(
                LogsUser(
                    msg,
                    FenFuText.fenfuErrorMsg
                )
            )
            return errorList
        }
    }

    fun queryLogsData(userName: String, serverName: String, zoneId: Int): String {
        return queryLogsData(userName, serverName, zoneId, false)
    }

    fun queryLogsData(userName: String, serverName: String, zoneId: Int, hps: Boolean): String {
        if (zoneId == -1) {
            return FenFuText.parseDataError("AreaId = -1")
        }
        val queryUrl = StringBuffer(
            "https://cn.fflogs.com/v1/rankings/character/$userName/$serverName/${
                DatabaseHelper.instance.logsQueryRegion(serverName)
            }?api_key=$API_KEY"
        )
        queryUrl.append("&zone=$zoneId")
        if (DatabaseHelper.instance.getZoneQueryType(zoneId) == DatabaseHelper.LogsQueryType.RDPS && !hps) {
            queryUrl.append("&metric=rdps")
        } else if (hps) {
            queryUrl.append("&metric=hps")
        }
        val request = Request.Builder()
            .url(queryUrl.toString())
        for (key in refererHeaderMap.keys) {
            request.addHeader(key, refererHeaderMap[key].toString())
        }
        val response = HttpUtil.client.newCall(request.build()).execute()
        if (response.code == 400) {
            return FenFuText.notFoundLogsData(userName, serverName, zoneId, hps) + "\n${response.body?.string()}"
        }
        if (response.isSuccessful && response.body != null) {
            val str = response.body?.string()
            val logsData: ArrayList<LogsDataQuery> = try {
                Gson().fromJson(str, object : TypeToken<List<LogsDataQuery>>() {}.type)
            } catch (e: Exception) {
                return FenFuText.parseDataError(e.localizedMessage + "\n$str")
            }
            val defaultDifficult = DatabaseHelper.instance.getDungeonDifficult(zoneId)
            if (defaultDifficult == -1) {
                return FenFuText.queryDifficultIdError()
            } else {
                val removeArray = ArrayList<LogsDataQuery>()
                for (element in logsData) {
                    if (element.difficulty != defaultDifficult) {
                        removeArray.add(element)
                        continue
                    }
                    if (hps) {
                        if (!DatabaseHelper.instance.jobIsHealer(element.spec)) {
                            removeArray.add(element)
                            continue
                        }
                    }
                }
                for (e in removeArray) {
                    logsData.remove(e)
                }
                if (logsData.isEmpty()) {
                    return FenFuText.notFoundLogsData(userName, serverName, zoneId, hps)
                } else {
                    val resultStringBuffer = StringBuffer(
                        "$userName - $serverName 的战斗报告\n区域：${
                            DatabaseHelper.instance.zoneIdQueryDungeonName(zoneId)
                        }"
                    )
                    if (queryUrl.contains("&metric=rdps")) {
                        resultStringBuffer.append("\n【请注意，该报告内的DPS数据为R DPS数据】")
                    } else if (queryUrl.contains("&metric=hps")) {
                        resultStringBuffer.append("\n【请注意，该报告为HPS数据】")
                    } else {
                        resultStringBuffer.append("\n【请注意，该报告内的DPS数据为A DPS数据】")
                    }
                    for (element in logsData) {
                        resultStringBuffer.append("\n").append(
                            """
                            =================
                            ${DatabaseHelper.instance.engBossNameConvertChn(element.encounterName)}：
                            职业：${element.spec}
                            ${if (hps) "hps" else "dps"}：${String.format("%.2f", element.total)}
                            排名比：${
                                String.format(
                                    "%.2f",
                                    element.percentile
                                )
                            }%[${LogsUtil.getLogsColor(element.percentile)}]
                            排名：${element.rank}/${element.outOf}
                        """.trimIndent()
                        )
                    }
                    resultStringBuffer.append(
                        "\n【直达logs->${
                            "https://cn.fflogs.com/character/cn/${
                                URLEncoder.encode(serverName, "UTF-8").toLowerCase()
                            }/${
                                URLEncoder.encode(
                                    userName,
                                    "UTF-8"
                                ).toLowerCase()
                            }"
                        }】"
                    )
                    val checkBan = DatabaseHelper.instance.checkBan(userName, serverName)
                    if (checkBan != null) {
                        resultStringBuffer
                            .append(
                                "\n【※※该玩家曾因“${checkBan.reason}”"
                            )
                        if (checkBan.count > 1) {
                            resultStringBuffer.append("及其他违规操作")
                        }
                        resultStringBuffer
                            .append("被官方封禁${checkBan.count}次，")
                            .append(
                                "最近一次被封禁时间：${checkBan.time}，" +
                                        "官方通告：https://ff.web.sdo.com/web8/index.html#/newstab/newscont/${checkBan.id}※※】"
                            )
                    }
                    return resultStringBuffer.toString()
                }
            }
        }
        return FenFuText.serverError
    }
}