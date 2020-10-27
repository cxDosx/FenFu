package moe.cxdosx.fenfu.utils

import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object HttpUtil {
    val client = OkHttpClient.Builder()
        .callTimeout(10, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS).build()


    /**
     * GET获取网页原文
     * @param u Url
     * @param params 参数
     *
     * @return 返回响应体原文，如果请求失败返回null
     */
    fun getRawText(u: String, params: Map<String, Any>?): String? {
        val url: String = if (params.isNullOrEmpty()) {
            u
        } else {
            var p = "?"
            params.forEach { (k, v) ->
                p += "$k=$v&"
            }
            u + p.take(p.length - 1)
        }
        val request = Request.Builder().url(url).get().build()
        val response = client.newCall(request).execute()
        return if (response.isSuccessful && response.body != null) {
            response.body!!.string()
        } else {
            null
        }
    }
}

