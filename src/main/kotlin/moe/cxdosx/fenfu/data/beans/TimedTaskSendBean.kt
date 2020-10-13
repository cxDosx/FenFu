package moe.cxdosx.fenfu.data.beans

import java.util.*

data class TimedTaskSendBean(
    val sendContent: String,
    val startTime: Date,
    val endTime: Date,
    val sendTime: String,
    val sendGroup: List<Long>
)