package moe.cxdosx.fenfu.data.beans

/**
 * Logs查询的等待回复队列
 * 用于只查询用户名但是有多个结果等待查询者再次查询的情况
 */
object LogsIdleQueue {
    val idleQueue = ArrayList<LogsIdle>()
}

