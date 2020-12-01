package moe.cxdosx.fenfu.utils

import moe.cxdosx.fenfu.function.WeiboAutoUpdate
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

object WeiboUpdateManager {
    var weiboTimer: Timer? = null
    fun initWeiboUpdate() {
        weiboTimer = Timer().apply {
            schedule(0L, TimeUnit.MINUTES.toMillis(5)) {
                DatabaseHelper.instance.getAllWeiboUpdateUid().forEach {
                    WeiboAutoUpdate.getAllWeiboText(it)
                }
            }
        }
    }
}