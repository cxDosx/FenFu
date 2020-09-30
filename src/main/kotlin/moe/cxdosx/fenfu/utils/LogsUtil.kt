package moe.cxdosx.fenfu.utils

class LogsUtil {
    companion object {
        fun getLogsColor(percent: Double): String {
            return if (percent < 1) {
                "你是怎么做到的？？"
            } else if (percent < 25) {
                "灰"
            } else if (percent >= 25 && percent < 50) {
                "绿"
            } else if (percent >= 50 && percent < 75) {
                "蓝"
            } else if (percent >= 75 && percent < 95) {
                "紫"
            } else if (percent >= 95 && percent < 99) {
                "橙"
            } else if (percent >= 99 && percent < 100) {
                "粉"
            } else {
                "金"
            }
        }
    }
}