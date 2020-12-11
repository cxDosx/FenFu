package moe.cxdosx.fenfu.utils

import moe.cxdosx.fenfu.config.BotConfig

object PermissionUtils {
    fun Long.checkAdminPermission(): Boolean {
        return this == BotConfig.ownerQQ
    }
}