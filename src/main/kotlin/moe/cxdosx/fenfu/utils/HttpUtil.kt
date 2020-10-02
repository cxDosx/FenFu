package moe.cxdosx.fenfu.utils

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class HttpUtil {
    companion object {
        val client = OkHttpClient.Builder()
            .callTimeout(10, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS).build()
    }
}