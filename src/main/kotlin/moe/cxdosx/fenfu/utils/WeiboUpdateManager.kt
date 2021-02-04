package moe.cxdosx.fenfu.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moe.cxdosx.fenfu.config.BotConfig
import moe.cxdosx.fenfu.function.WeiboAutoUpdate
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

object WeiboUpdateManager {
    var weiboTimer: Timer? = null
    fun initWeiboUpdate() {
        if (weiboTimer != null) {
            stopWeiboAutoUpdate()
        }
        weiboTimer = Timer().apply {
            schedule(0L, TimeUnit.MINUTES.toMillis(5)) {
                DatabaseHelper.instance.getAllWeiboUpdateUid().forEach {
                    WeiboAutoUpdate.getAllWeiboText(it)
                }
            }
        }
        GlobalScope.launch(Dispatchers.IO) {
            MiraiUtil.sendToTargetFriend(BotConfig.ownerQQ, "微博自动更新已启用")
        }
    }

    fun stopWeiboAutoUpdate() {
        if (weiboTimer != null) {
            weiboTimer!!.cancel()
            weiboTimer = null
        }
        GlobalScope.launch(Dispatchers.IO) {
            MiraiUtil.sendToTargetFriend(BotConfig.ownerQQ, "微博自动更新已停止运行，十五分钟后自动重试")
            Thread.sleep(TimeUnit.MINUTES.toMillis(15))
            if (weiboTimer == null) {
                initWeiboUpdate()
            }
        }
    }
}