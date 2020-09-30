package org.example.myplugin.beans

import com.google.gson.annotations.SerializedName


data class LogsDataQuery(

    /**
     * encounterID : 60  //区域ID
     * encounterName : Chaos  //区域Boss名
     * class : Global   // ??
     * spec : Bard   //职业
     * rank : 1620   //击杀当日Rank
     * outOf : 5228  //击杀总人数
     * duration : 477665  //击杀耗时 毫秒
     * startTime : 1554815827558  //开始时间
     * reportID : 1f4PF7qWG69cZCgz  // logs上的reportID
     * fightID : 3   // ??
     * difficulty : 100   //副本困难度
     * characterID : 11968978   //角色ID
     * characterName : 秘制红烧肉  //玩家名
     * server : 神意之地        // 区服
     * percentile : 69   //当时Rank 百分比
     * ilvlKeyOrPatch : 4.4  // 击杀版本
     * total : 6805.36  // 平均DPS
     * estimated : true  // ??
     */
    val encounterID: Int,
    val encounterName: String,
    @SerializedName("class")
    val classX: String,
    val spec: String,
    val rank: Int,
    val outOf: Int,
    val duration: Int,
    val startTime: Long,
    val reportID: String,
    val fightID: Int,
    val difficulty: Int,
    val characterID: Int,
    val characterName: String,
    val server: String,
    val percentile: Double,
    val ilvlKeyOrPatch: Double,
    val total: Double,
    val estimated: Boolean
)